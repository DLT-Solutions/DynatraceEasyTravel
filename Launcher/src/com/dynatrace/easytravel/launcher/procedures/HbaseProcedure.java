package com.dynatrace.easytravel.launcher.procedures;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.hbase.HbaseConnection;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.AbstractNativeProcedure;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.StopMode;
import com.dynatrace.easytravel.launcher.process.Process;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.persistence.PersistenceStoreNotAvailableException;
import com.dynatrace.easytravel.util.ProcessExecutor;

/**
 * Procedure that starts an HBase server
 *
 * @author cwat-ggsenger
 */
public class HbaseProcedure extends AbstractNativeProcedure {

	private static final String[] HBASE_QUORUM = new String[] { "localhost" };
	private static final String LOGFILENAME = "hbase.log";
	private static final Logger log = Logger.getLogger(HbaseProcedure.class.getName());
	private static final File LOG_DIRECTORY = new File(Directories.getLogDir(), "hbase");
	private static final File CONFIG_DIRECTORY = new File(Directories.getConfigDir(), "hbase");
	private static final File DATA_DIRECTORY = new File(Directories.getDatabaseDir(), "hbase-data");
	private static final File DEFAULT_CONFIG_DIRECTORY = new File(new File(Directories.getInstallDir(), BaseConstants.SubDirectories.HBASE), "conf");
	private static final File LIB_DIRECTORY = new File(Directories.getInstallDir(), "lib");
	private static final String[] REQUIRED_JARS = new String[] { "asm-3.1.jar",
			"commons-cli-1.2.jar", "commons-codec-1.8.jar",
			"commons-collections-3.2.2.jar", "commons-configuration-1.10.jar",
			"commons-httpclient-3.1.jar", "commons-io-2.6.jar",
			"commons-lang-2.6.jar", "commons-logging-1.1.3.jar",
			"guava-13.0.1.jar", "httpclient-4.2.1.jar",
			"httpclient-cache-4.2.1.jar", "jackson-core-asl-1.9.2.jar",
			"jackson-mapper-asl-1.9.2.jar", "junit-4.11.jar",
			"metrics-core-2.2.0.jar", "netty-3.6.6.Final.jar" };

	public HbaseProcedure(ProcedureMapping mapping)
			throws CorruptInstallationException {
		super(mapping);
		createDirectories();
		copyConfigDefaults();
		writeConfig();
	}

	/**
	 * Returns name of local machine (not: localhost; used e.g. by orbd).
	 *
	 * @return name of local machine, null if unknown/unresolvable
	 */
	private static String getLocalMachineName() {
	    String hostname = null;
	    try {
	        InetAddress addr = InetAddress.getLocalHost();
	        hostname = addr.getHostName();
	    } catch (UnknownHostException e) {
			log.log(Level.SEVERE, "Could not retrieve name of localhost", e);
	    }
	    return hostname;
	}

	private void createDirectories() {
		recreateDirectory(LOG_DIRECTORY);
		recreateDirectory(CONFIG_DIRECTORY);
		if (!DATA_DIRECTORY.exists()) {
			recreateDirectory(DATA_DIRECTORY);
		}
	}

	private void recreateDirectory(File directory) {
		try {
			FileUtils.deleteDirectory(directory);
			if (!directory.mkdirs()) {
				throw new IOException("Creating directories failed");
			}
		} catch (IOException e) {
			log.warning("Could not delete/create directory " + directory + ": " + e.getMessage());
		}
	}

	private void copyConfigDefaults() {
		copyConfigFile("hbase-env.cmd");
		copyConfigFile("hbase-env.sh");
		copyConfigFile("hadoop-metrics2-hbase.properties");
		copyConfigFile("hbase-policy.xml");
		copyConfigFile("log4j.properties");
		copyConfigFile("regionservers");
	}

	private void copyConfigFile(String name) {
		File from = new File(DEFAULT_CONFIG_DIRECTORY, name);
		File to = new File(CONFIG_DIRECTORY, name);
		try {
			FileUtils.copyFile(from, to);
		} catch (IOException e) {
			log.warning("Failed to copy configuration file " + from + " to " + to + ": " + e.getMessage());
		}
	}

	private void writeConfig() {
		File hbaseSite = new File(CONFIG_DIRECTORY, "hbase-site.xml");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(hbaseSite));
			writer.println("<?xml version=\"1.0\"?>");
			writer.println("<?xml-stylesheet type=\"text/xsl\" href=\"configuration.xsl\"?>");
			writer.println("<configuration>");
			printProperty(writer, "hbase.rootdir", toLocalHBasePath(new File(DATA_DIRECTORY, "data").getAbsolutePath()));
			printProperty(writer, "hbase.zookeeper.property.dataDir", new File(DATA_DIRECTORY, "zk").getAbsolutePath());
			printProperty(writer, "hbase.zookeeper.quorum", getLocalMachineName());
			printProperty(writer, "hbase.zookeeper.property.clientPort", "2181");
			printProperty(writer, "hbase.master", getLocalMachineName() + ":60000");
			writer.println("</configuration>");
			writer.flush();
		} catch (IOException e) {
			log.warning("Failed to write hbase-site.xml: " + e.getMessage());
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	private String toLocalHBasePath(String absolutePath) {
		if (OperatingSystem.pickUp() == OperatingSystem.WINDOWS) {
			// HBase is so dumb it needs the DOS-style path since it does not accept white spaces in the configuration, URLs or anything like that
			String[] args = { "cmd", "/c", "for %I in (\"" + absolutePath + "\") do @echo %~sI" };
			ProcessExecutor p = new ProcessExecutor(Runtime.getRuntime(), args);
			String path = absolutePath;
			try {
				path = p.getInputAsString(1000, TimeUnit.MILLISECONDS).trim();
			} catch (InterruptedException e) {
				log.warning("Interrupted while getting DOS path for HBase: " + e.getMessage());
			} catch (ExecutionException e) {
				log.warning("Failed to get DOS path for HBase: " + e.getMessage());
			} catch (TimeoutException e) {
				log.warning("Timeout when trying to get DOS path for HBase: " + e.getMessage());
			}
			return path;
		}
		return absolutePath;
	}

	private void printProperty(PrintWriter writer, String propertyName, String value) {
		writer.println("  <property>");
		writer.println("    <name>" + propertyName + "</name>");
		writer.println("    <value>" + value + "</value>");
		writer.println("  </property>");
	}

	@Override
	public boolean isInstrumentationSupported() {
		return false;
	}

	@Override
	public boolean isOperatingCheckSupported() {
		return true;
	}

	@Override
	public boolean isOperating() {
		HbaseConnection hbaseConnection = null;
		try {
			hbaseConnection = HbaseConnection.openConnection(HBASE_QUORUM);
			return true;
		} catch (PersistenceStoreNotAvailableException e) {
			log.log(Level.WARNING, "Could not connect to HBase", e);
			return false;
		} finally {
			if (hbaseConnection != null)
				hbaseConnection.closeConnection();
		}
	}

	@Override
	public boolean hasLogfile() {
		return new File(LOG_DIRECTORY, LOGFILENAME).exists();
	}

	@Override
	public String getLogfile() {
		return new File(LOG_DIRECTORY, LOGFILENAME).getAbsolutePath();
	}

	@Override
	public Technology getTechnology() {
		return Technology.HBASE;
	}

	@Override
	protected String getExecutable(ProcedureMapping mapping) {
		if (OperatingSystem.pickUp() == OperatingSystem.WINDOWS) {
			return BaseConstants.SubDirectories.HBASE + "/bin/start-hbase.cmd";
		}
		return BaseConstants.SubDirectories.HBASE + "/bin/start-hbase.sh";
	}

	protected String getStopExecutable() {
		if (OperatingSystem.pickUp() == OperatingSystem.WINDOWS) {
			return BaseConstants.SubDirectories.HBASE + "/bin/stop-hbase.cmd";
		}
		return BaseConstants.SubDirectories.HBASE + "/bin/stop-hbase.sh";
	}

	@Override
	public Feedback stop() {
		log.warning("Stopping procedures. Stopping HBase");
		try {
			Process stopProcess = createProcess(getStopExecutable(), getAgentConfig(), null, Technology.HBASE);
			Feedback feedback = stopProcess.start();
			if (feedback != Feedback.Neutral && feedback != Feedback.Success) {
				log.log(Level.SEVERE, "Could not create stop command for HBase: " + feedback);
			} else {
				int i;
				for (i = 0; (process.isRunning() || stopProcess.isRunning()) && i < 30; i++) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						break;
					}
				}
				if (i < 20) {
					log.warning("Stopping procedures. HBase stopped");
					return super.stop();
				}
				log.log(Level.SEVERE, "HBase did not come down after 20 seconds. Stop process: " +
						(stopProcess.isRunning() ? "running" : "stopped") + ", Start process: " +
						(process.isRunning() ? "running": "stopped") + ". Forcing shutdown.");
			}
		} catch (CorruptInstallationException e) {
			log.log(Level.SEVERE, "Could not create stop command for HBase: ", e);
		}
		
		if(!isRunning()) {
			log.warning("Stopping procedures. HBase stopped");
		}
		
		return super.stop();
	}

	@Override
	public StopMode getStopMode() {
		return StopMode.SEQUENTIAL;
	}

	@Override
	protected String getWorkingDir() {
		return null;
	}

	@Override
	protected DtAgentConfig getAgentConfig() {
		String classpath = buildClasspath(REQUIRED_JARS);

		return new DtAgentConfig(null, null, null,
				new String[] {
						"HBASE_OPTS=-Djava.net.preferIPv4Stack=true",
						"HBASE_CONF_DIR=\"" + CONFIG_DIRECTORY.getAbsolutePath() + "\"",
						"HBASE_LOG_DIR=" + LOG_DIRECTORY.getAbsolutePath(),
						"HBASE_LOGFILE=" + LOGFILENAME,
						"HBASE_CLASSPATH=" + classpath
		});
	}

	private String buildClasspath(String[] requiredJars) {
		StringBuilder sb = new StringBuilder();
		String delim = OperatingSystem.pickUp() == OperatingSystem.WINDOWS ? ";" : ":";
		for (String requiredJar : requiredJars) {
			File jarFile = new File(LIB_DIRECTORY, requiredJar);
			if (!jarFile.exists()) {
				log.warning("Missing JAR file for HBase: " + requiredJar + " was not found in " + LIB_DIRECTORY.getAbsolutePath());
			} else {
				sb.append(jarFile.getAbsolutePath());
				sb.append(delim);
			}
		}
		return sb.toString();
	}

}
