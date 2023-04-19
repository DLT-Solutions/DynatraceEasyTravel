package com.dynatrace.easytravel.launcher.engine;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import ch.qos.logback.classic.Logger;

import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeListener;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants.UrlType;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.httpd.ApacheConf;
import com.dynatrace.easytravel.launcher.httpd.ApacheHttpdUtils;
import com.dynatrace.easytravel.launcher.httpd.HttpdConfSetup;
import com.dynatrace.easytravel.launcher.httpd.LogWriter;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.util.LocalUriProvider;

/**
 * Implementation of a procedure which starts the Apache HTTP Server for load
 * balancing between multiple Customer Frontends.
 *
 * @author stefan.moschinski
 *
 */
public class ApacheHttpdProcedure extends AbstractNativeProcedure implements WebProcedure, PluginChangeListener {
	private static final Logger LOGGER = LoggerFactory.make();

	private static final String EASYTRAVEL_CONFIG_PATH = Directories.getConfigDir().getAbsolutePath();

	private ApacheConf apacheConf = new ApacheConf();

	/**
	 * The constructor creates an Apache HTTP server instance and configures the
	 * HTTPD configuration file.
	 *
	 * @param mapping
	 * @throws CorruptInstallationException
	 */
	public ApacheHttpdProcedure(ProcedureMapping mapping) throws CorruptInstallationException {
		super(mapping);

		modifyApacheConf(apacheConf);

		checkForConfigurationErrors();
		adaptAndCopyHttpdConf();
		LogWriter.deleteOldLog();
		applySettings();
	}

	protected void modifyApacheConf(ApacheConf apacheConf) {
		// overriding classes may use this
	}

	@Override
	protected String getExecutable(ProcedureMapping mapping) {
		return ApacheHttpdUtils.getExecutableDependingOnOs();
	}

	@Override
	protected DtAgentConfig getAgentConfig() {
		return createDtAgentConfig();
	}

	@Override
	protected String getWorkingDir() {
		return null;
	}

	private void checkForConfigurationErrors() {
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		if (LocalUriProvider.getUri(EASYTRAVEL_CONFIG.apacheWebServerHost, EASYTRAVEL_CONFIG.apacheWebServerPort, "/").
				equalsIgnoreCase
				(LocalUriProvider.getUri(EASYTRAVEL_CONFIG.apacheWebServerB2bHost,
						EASYTRAVEL_CONFIG.apacheWebServerB2bPort, "/"))) {
			throw new IllegalStateException(
					"The easyTravelConfig.properties are invalid. " +
							"The apacheWebServerHost and the apacheWebServerPort are " +
							"equal to the apacheWebServerB2bHost and the apacheWebServerB2bPort");
		}
	}


	/*
	 * This initializer copies the required files to the EasyTravel
	 * configuration folder. It also initializes the configuration file
	 * httpd.conf by calling HttpdConfSetup.write() or by overwriting it
	 * with a user-provided external config file.
	 */
	private void adaptAndCopyHttpdConf() {
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		
		if (EASYTRAVEL_CONFIG.apacheWebServerUsesGeneratedHttpdConfig) {
		
			//========================================
			// We want to re-generate httpd.conf
			// and the folder too.
			//========================================
			
			adaptAndCopyDefaultHttpdConf(false);
		
		} else { 
		
			if (StringUtils.isNotEmpty(EASYTRAVEL_CONFIG.apacheWebServerHttpdConfig)) {
			
				//========================================
				// We want to use the specified external config
				// file if it exists.
				// We also need to check if we should copy the folder:
				// we assume that if httpd.conf is found if the
				// user home area, then there is no need to
				// copy the folder.
				//
				// Error condition handling:
				// If the specified external config file does not
				// exist, we log an error and try to use
				// an existing httpd.conf if present in the
				// user home area.  If it is not found, we
				// re-generate it and then also copy the folder.
				//========================================
		
				File externalConfig	= null; // user-supplied
				File dest = new File(EASYTRAVEL_CONFIG_PATH + "/httpd.conf"); // httpd.conf in user home
				
				externalConfig = new File(EASYTRAVEL_CONFIG.apacheWebServerHttpdConfig);
				if (externalConfig.exists()) {
		
					// First need to check if we need to copy
					// the folder: if httpd.conf currently does
					// not exist in user home, then we copy the folder.
					if (isHttpdConfFileMissing()) {
						adaptAndCopyDefaultHttpdConf(true);
					}
				
					// copy user-supplied config file onto httpd.conf in user home area
					try {
						FileUtils.copyFile(externalConfig, dest);
						LOGGER.info("Copied " + externalConfig + " to " + dest + " to use a custom Apach config file.");
					} catch (IOException e) {
						LOGGER.warn("Could not copy file " + externalConfig + " to " + dest, e);
					}
					
				} else {
				
					// The specified custom config file does not exist
					LOGGER.warn("Cannot find external Apache config file <"
						+ externalConfig + "> not found.");
				
					// If there already is httpd.conf in user home, do nothing.
					// Else copy the folder and re-generate httpd.conf	
					if (isHttpdConfFileMissing()) {
						adaptAndCopyDefaultHttpdConf(false);
					}
				}
				
			} else {
			
				//========================================
				// We do not want to overwrite httpd.conf
				// but must create it if it does
				// not exist (and in that case we also copy
				// the folder).
				//========================================
				
				if (isHttpdConfFileMissing()) {
					adaptAndCopyDefaultHttpdConf(false);
				}
			}
		}
	}


	private void adaptAndCopyDefaultHttpdConf(boolean copyDirOnly) {
		try {
			FileUtils.copyDirectory(new File(ApacheHttpdUtils.APACHE_PLAIN_CONF),
										new File(EASYTRAVEL_CONFIG_PATH));
									
		} catch (IOException e) {
			LOGGER.warn("Could not copy Apache configuration folder", e);
		}
	
		// Putting this in a separate try/catch means that we will generate
		// httpd.conf even if we fail to copy the folder (above).
		// This is probably better and also it has testing implications,
		// as currently copying the folder will always fail in our
		// JUnit test suite (it fails to find <install>/dist).
		if (!copyDirOnly) {	
			try {
				HttpdConfSetup.write(createDtAgentConfig(), apacheConf);
			} catch (IOException e) {
				LOGGER.warn("Could not generate Apache configuration file", e);
			}
		}
			
	}

	/**
	 * This method enforces the use of the httpd.conf file in the default
	 * EasyTravel configuration folder (i.e., /.dynaTrace/easyTravel
	 * X.X.X/config).
	 */
	private void applySettings() {
		File HttpdConf = new File(Directories.getConfigDir().getAbsolutePath()
				+ "/httpd.conf");
		process.addApplicationArgument("-f");
		process.addApplicationArgument(HttpdConf.getAbsolutePath());
		if (!OperatingSystem.isCurrent(OperatingSystem.WINDOWS)) {
			process.addApplicationArgument("-D");
			process.addApplicationArgument("NO_DETACH");
		}
	}

	private static DtAgentConfig createDtAgentConfig() {
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		return new DtAgentConfig(null,
				EASYTRAVEL_CONFIG.apacheWebServerAgent,
				null,
				EASYTRAVEL_CONFIG.apacheWebServerEnvArgs);
	}

	protected boolean isHttpdConfFileMissing() {
		return !new File(EASYTRAVEL_CONFIG_PATH + "/httpd.conf").exists();
	}

	/**
	 * Stopping the Apache HTTP server via taskkill on Windows machines. This
	 * way is necessary, because otherwise we had to change the Windows security
	 * settings.
	 *
	 * @see com.dynatrace.easytravel.launcher.engine.AbstractProcessProcedure#stop()
	 */
	@Override
	public Feedback stop() {
		LOGGER.debug("Stopping procedures. Stopping the Apache - start");
		PluginChangeMonitor.unregisterFromPluginChanges(this);
		String[] killInstruction = new String[0];
		try {
			LOGGER.debug("Stopping the Apache - getting kill instruction");
			killInstruction = ApacheHttpdUtils.getKillInstruction();
			LOGGER.debug("Stopping the Apache - kill instruction: " + Arrays.toString(killInstruction));
			ApacheHttpdUtils.killProcess(killInstruction);
			LOGGER.debug("Stopping the Apache - kill command run, waiting...");
			waitForTermination();
			if(isOperating()) {
				LOGGER.debug("Stopping the Apache - apache did not stop, killing again on Linux");
				ApacheHttpdUtils.killIfNotTerminatedLinux();
			}
			LOGGER.debug("Stopping the Apache - stop command finished");

			return Feedback.getMostSevere(Feedback.Success, super.stop());
		} catch (Exception e) {
			logExceptionWhileStoppingWebserver(ArrayUtils.toString(killInstruction), e);
			return Feedback.Failure;
		}
	}

	@Override
	public Feedback run() {
		PluginChangeMonitor.registerForPluginChanges(this);
		return super.run();
	}

	private void logExceptionWhileStoppingWebserver(String killInstruction, Exception e) {
		LOGGER.warn(String.format("The termination of the Apache web server failed using the command %s.",
						killInstruction), e);
	}

	private void waitForTermination() throws InterruptedException {
		for (int i = 0; i < 10; i++) {
			if (!isOperating()) {
				break;
			}
			Thread.sleep(100);
		}
	}

	@Override
	public boolean isOperatingCheckSupported() {
		return true;
	}

	@Override
	public boolean isOperating() {
		return isAvailable();
	}

	public static boolean isAvailable() {
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();

		return UrlUtils.checkRead(LocalUriProvider.getApacheFrontendPublicUrl()).isOK() ||
				// if launching apache for proxy injection, easyTravel is not launched, therefore availability of proxy port is verified here
				UrlUtils.checkConnect("http://localhost:" + EASYTRAVEL_CONFIG.apacheWebServerProxyPort, 30000).isOK();
	}

	@Override
	public String getLogfile() {
		// look for all files "access*.log" and return the most current one
		return getLatestLogfile("error*.log");
	}

	public String getErrorLogfile() {
		// look for all files "error*.log" and return the most current one
		return getLatestLogfile("error*.log");
	}

	private String getLatestLogfile(String wildcard) {
		// look in the log-directory
		File dir = Directories.getLogDir();

		return LogWriter.getLastModifiedFile(dir, wildcard);
	}

	@Override
	public boolean hasLogfile() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.dynatrace.easytravel.launcher.engine.AbstractProcedure#getURI()
	 */
	@Override
	public String getURI() {
        return LocalUriProvider.getURL(UrlType.APACHE_JAVA_FRONTEND, /*useDNS*/ false);
    }

	@Override
	public String getURIDNS() {
		return LocalUriProvider.getURL(UrlType.APACHE_JAVA_FRONTEND, /*useDNS*/ true);
	}

	@Override
	public StopMode getStopMode() {
		return StopMode.PARALLEL;
	}

	@Override
	public Technology getTechnology() {
		return Technology.WEBSERVER;
	}

	@Override
	public String getURI(UrlType type) {
        return LocalUriProvider.getURL(type, /*useDNS*/ false);
    }

	@Override
	public String getURIDNS(UrlType type) {
        return LocalUriProvider.getURL(type, true);
    }
	
    // from WebProcedure
    private static final String PROPERTY_APACHE_HTTP_PORT = "apacheHttpPort";

	@Override
	public String getPortPropertyName() {
		return PROPERTY_APACHE_HTTP_PORT;
	}

	@Override
	public int getPort() {
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		return EASYTRAVEL_CONFIG.apacheWebServerPort;
	}

	/**
	 * Get ApacheProcedure from running batch
	 *
	 * @return Apache StatefulProcedure
	 */
	private StatefulProcedure getApacheProcedure() {
		StatefulProcedure apacheProcedure = null;

		for (StatefulProcedure proc : LaunchEngine.getRunningBatch().getProcedures()) {
			String procedureId = proc.getMapping().getId();
			if (Constants.Procedures.APACHE_HTTPD_ID.equals(procedureId)) {
				apacheProcedure = proc;
			}
		}

		return apacheProcedure;
	}

	/**
	 * Wait for OPERATING state for ApacheHttpdProcedure
	 *
	 * @param apacheProcedure
	 * @return 1 when
	 *
	 */
	private void waitForOperating(StatefulProcedure apacheProcedure) {
		long timeout = System.currentTimeMillis() + 5000;

		do {

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LOGGER.error(e.getMessage());
			}

			if (System.currentTimeMillis() >= timeout) {
				LOGGER.error("Waiting for Apache Webserver operating TIMEOUT");
				break;
			}

		} while (!apacheProcedure.isStartingFinished());

	}

	/**
	 * Stop ApacheHttpdProcedure
	 */
	private void stopApacheProcedure() {
		getApacheProcedure().stop();
	}

	/**
	 * Start ApacheHttpdProcedure
	 */
	private void startApacheProcedure() {
		StatefulProcedure apacheProcedure = getApacheProcedure();
		apacheProcedure.run();
		waitForOperating(apacheProcedure);
	}

	/**
	 * Change httpd.conf file. Inject apache slow down configuration
	 */
	private void changeApacheHttpdConfig() {
		if (!apacheConf.isApacheSlowDown()) {
			apacheConf.setApacheSlowDown(true);
		} else {
			apacheConf.setApacheSlowDown(false);
		}

		adaptAndCopyHttpdConf();
	}

	/**
	 * Method is executed when SlowApacheWebserver plugin has beed enabled/disabled
	 */
	@Override
	public void pluginsChanged() {
		if (apacheConf.isApacheSlowDown() != PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.SLOW_APACHE_WEBSERVER)) {
			stopApacheProcedure();
			changeApacheHttpdConfig();
			startApacheProcedure();
		}

	}

}
