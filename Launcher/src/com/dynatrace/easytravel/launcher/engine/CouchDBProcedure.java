package com.dynatrace.easytravel.launcher.engine;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.TeeOutputStream;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants.UrlType;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.misc.LogFileStream;
import com.dynatrace.easytravel.launcher.process.AbstractProcess;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;

import ch.qos.logback.classic.Logger;


/**
 * Implementation of a procedure which starts CouchDB.
 *
 * @author wojtek jarosz
 *
 */
public class CouchDBProcedure extends AbstractNativeProcedure {
	private static final Logger LOGGER = LoggerFactory.make();

	private static final String EASYTRAVEL_CONFIG_PATH = Directories.getConfigDir().getAbsolutePath();
	private static final File LOG_DIRECTORY = Directories.getLogDir();
	
	private static final OperatingSystem USED_OS = OperatingSystem.pickUp();

	/**
	 * The constructor configures the local.ini configuration file and
	 * creates the CouchDB command. The command will then be started in run().
	 *
	 * @param mapping
	 * @throws CorruptInstallationException
	 */
	public CouchDBProcedure(ProcedureMapping mapping) throws CorruptInstallationException {
		super(mapping);

		buildCommand();
	}

	// return value will be taken relative to Directories.getInstallDir()
	@Override
	protected String getExecutable(ProcedureMapping mapping) {
		// Note that the native procedure will be invoked as the path we give here + ".exe",
		// unless the specified path ends with ".cmd", in which case a command shell will automatically
		// be invoked for the batch file.
		return USED_OS == OperatingSystem.WINDOWS ?
		"couchdb/Windows/erts-11.2.2.12/bin/erl"
				: null;
	}

	@Override
	protected DtAgentConfig getAgentConfig() {
		return null;
	}

	// return value will be taken relative to Directories.getInstallDir()
	@Override
	protected String getWorkingDir() {
		return USED_OS == OperatingSystem.WINDOWS ?
				"couchdb/Windows"
				: null;
	}

	protected String getConfigDir() {
		return USED_OS == OperatingSystem.WINDOWS ?
		Directories.getInstallDir().getAbsolutePath() + "/couchdb/Windows/etc"
				: null;
	}

	//========================================================
	//
	// Method to create a custom config file in the user area and to edit it
	// so that:
	//	- The database folder is configured to be in the user area.
	//	- The CouchDB log file configured to be in the user area.
	//
	//========================================================
	
	private void editAndCopyLocalConf() {		
		File templateConfig = new File(getConfigDir() + "/local.ini");
		File destConfig = new File(Directories.getDynaTraceHome().getAbsolutePath(),"local.ini");
		LOGGER.debug("will copy <" + templateConfig.getAbsolutePath() + "> to <" + destConfig.getAbsolutePath() + ">");
				
		try {
			FileUtils.copyFile(templateConfig, destConfig);
		} catch (IOException e) {
			LOGGER.warn("Could not copy file " + templateConfig + " to " + destConfig, e);
		}
		
		// Edit the custom config file so that it points to the database folder in the user area.
		// Note that if the folder does not exist, CouchDB will create it, or at least the last branch on the path.
		// Thus it is not necessary to create the "couchdb" subfolder of the .../easyTravel/database/couchdb path.
		editConfig(destConfig);
	}
	
	public void editConfig(File file) {
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();

		List<String> lines = new ArrayList<String>();
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			in = new BufferedReader(new FileReader(file));

			String line;
			LOGGER.debug("About to edit CouchDB local config.");
			while ((line = in.readLine()) != null) {
				
				// Strangely enough the path given in the config file can contain spaces but it does not require quotes.
				// In fact, if you supply quotes, it will not work, as the quotes will get interpreted as parts of the path.
				// Thus the line in the config file should be e.g.
				//
				// [couchdb]
				// ...
				// database_dir = C:\Users\cwpl-wjarosz\.dynaTrace\easyTravel 2.0.0\easyTravel\database\couchdb

				if (line.contains("@@@database_dir@@@")) {
					String editedLine = line.replace("@@@database_dir@@@", Directories.getCouchDBDataDir().getAbsolutePath());
				
					LOGGER.debug("CouchDB config edit: modified line is: <" + editedLine + ">");
					lines.add(editedLine);
				} else if (line.contains("@@@log_file@@@")) {
					// The log file path has been changed to permanent location until the 'ruxit Log Analytics'
					// supports rotated logs. To enable couchdb log rotation include the time stamp in the logfile name again.
					// (make suffix to be ..._" + String.valueOf(System.currentTimeMillis()) +".log");
					String editedLine = line.replace("@@@log_file@@@", LOG_DIRECTORY.getAbsolutePath() + "/couch.log");
				
					LOGGER.debug("CouchDB config edit: modified line is: <" + editedLine + ">");
					lines.add(editedLine);
				} else if (line.contains("@@@uri_file@@@")) {
					// CouchDB will attempt to write to this file, and if it is located in the installation area,
					// we might get UAC permission problems.
					String editedLine = line.replace("@@@uri_file@@@", EASYTRAVEL_CONFIG_PATH + "/couch.uri");
				
					LOGGER.debug("CouchDB config edit: modified line is: <" + editedLine + ">");
					lines.add(editedLine);
				} else if (line.contains("@@@port@@@")) {
					String editedLine = line.replace("@@@port@@@", Integer.toString(EASYTRAVEL_CONFIG.couchDBPort));
				
					LOGGER.debug("CouchDB config edit: modified line is: <" + editedLine + ">");
					lines.add(editedLine);
				} else {
					lines.add(line);
				}
			}
			in.close(); in = null;

			// now, write the file again with the changes
			out = new PrintWriter(file);
			for (String l : lines) {
				out.println(l);
			}
			out.close(); out = null;

		} catch (Exception e) {
			LOGGER.warn("Error editing customized CouchDB configuration file: " + file + "\n", e);
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}

	}

	private void buildCommand() {
		addEnvVariables();
		addProcessArguments();
	}

	private void addEnvVariables() {
		process.setEnvironmentVariable("EMU", "beam");
		process.setEnvironmentVariable("COUCHDB_QUERY_SERVER_JAVASCRIPT", "./bin/couchjs ./share/server/main.js");
		process.setEnvironmentVariable("COUCHDB_QUERY_SERVER_COFFEESCRIPT", "./bin/couchjs ./share/server/main-coffee.js");
		process.setEnvironmentVariable("COUCHDB_FAUXTON_DOCROOT", "./share/www");		
	}
	
	private void addProcessArguments() {
		Path couchDBHome = Paths.get(Directories.getInstallDir().getAbsolutePath(), "couchdb/Windows");
		Path dyntraceDir = Paths.get(Directories.getDynaTraceHome().getAbsolutePath());				
		process.addApplicationArgument("-boot");
		process.addApplicationArgument(couchDBHome.resolve("releases/3.2.2/couchdb").toString());
		process.addApplicationArgument("-args_file"); 
		process.addApplicationArgument(couchDBHome.resolve("etc/vm.args").toString());
		process.addApplicationArgument("-epmd"); 
		process.addApplicationArgument(couchDBHome.resolve("erts-11.2.2.12/bin/epmd.exe").toString());
		process.addApplicationArgument("-config");
		process.addApplicationArgument(couchDBHome.resolve("releases/3.2.2/sys.config").toString());
		process.addApplicationArgument("-couch_ini");
		process.addApplicationArgument(couchDBHome.resolve("etc/default.ini").toString());
		process.addApplicationArgument(couchDBHome.resolve("etc/local.d/10-admins.ini").toString());
		process.addApplicationArgument(dyntraceDir.resolve("local.ini").toString());
		process.addApplicationArgument("-run");
		process.addApplicationArgument("couchDB_ET");
		process.addApplicationArgument("start");
		process.addApplicationArgument(Integer.toString(EasyTravelConfig.read().couchDBShutdownPort));
		//This is directory used by Inets webserver as document root. 
		//It cannot contain space char, so we are using equivalent of c:/Users/someuser/.dynaTrace    
		process.addApplicationArgument(Directories.getDynaTraceHome().getAbsolutePath().replace("\\", "/"));
	}
	
	@Override
	public Feedback run() {
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();

        // We want to be able to generate new config after procedure starts.
        // New config will contain new couch_[timestamep].log file it will help to demonstrate 'Log analysis' feature for
        // rotated logs analysis.
        editAndCopyLocalConf();

        // There is some advantage in creating our own log streams to try to capture log of the native application.
		// Most of this information will be also logged in CouchDB log file.  However, doing it ourselves
		// we will also capture some vital information at the start and termination of the application, particularly
		// if there are problems.

	    AbstractProcess abstractProcess = (AbstractProcess)process;
	    abstractProcess.setTimeout(ExecuteWatchdog.INFINITE_TIMEOUT);
	    String logfile = getLogfile();
		final OutputStream loggingStream = logfile != null ? new LogFileStream(logfile) : null;
	    if (loggingStream != null) {
	    	// Most of the diagnostics is output on stdout, hardly anything on stderr.
	    	// To adjust amount of information, edit the log level in local.ini of CouchDB
    	    abstractProcess.setOut(new PrintStream(new TeeOutputStream(System.out, loggingStream)));
    	    abstractProcess.setErr(new PrintStream(new TeeOutputStream(System.err, loggingStream)));
	    }
	    
		LOGGER.debug("Will start process.");
		Feedback feedback = process.start(new Runnable() {
			@Override
			public void run() {
				IOUtils.closeQuietly(loggingStream);
			}
		}); 
		
		if (feedback == Feedback.Failure) {
			LOGGER.error("Failed to start CouchDB.");
			return feedback;
		}
		
		// We want to give CouchDB time to start, before anyone sends requests to it, such as
		// the content creator, if it is the next procedure in the scenario.
		// Note that the CouchDB content creator, as a synchronous procedure, will start only after
		// the run() method of the previous procedure has finished.
		
		long myStartTime = System.currentTimeMillis();
		LOGGER.debug("Waiting for CouchDB to become available...");
	
		// We want to wait for CouchDB twice as long as for other processes - it takes time.
		// Hence not using a simple call to waitUntilRunning() with no params, but specifying the period ourselves.
		if (!waitUntilRunning(2 * EASYTRAVEL_CONFIG.syncProcessTimeoutMs, EASYTRAVEL_CONFIG.processRunningCheckInterval)) { // Note: uses isRunning()
			long myEndTime = System.currentTimeMillis();
			LOGGER.error("Failed to access CouchDB after time of <" + Long.toString(myEndTime - myStartTime) + "ms> even though the process appear to have started correctly.");
			
			// We should prevent the process actually starting later and staying orphaned,
			// so we arrange to kill it here.
			// It is not even necessary to read the return value: we just need
			// to access the URL and that will cause the erlang controller to shut down.
			
			// Note that there is no point to check if it is operating, as it would not be (yet)
			// but we want to shut down the outer controller.
			try {
				UrlUtils.checkRead(getShutdownURL()).isOK();
			} catch (Exception e) {
				LOGGER.warn("Failure while trying to stop orphaned CouchDB process: the process may have to be killed manually.");
			}
			
			return Feedback.Failure;
		} else {
			LOGGER.debug("CouchDB is available now...");
			return feedback;
		}
	}

	@Override
	public boolean isOperatingCheckSupported() {
		return true;
	}

	@Override
	public boolean isSynchronous() {
		// Not synchronous because:
		// - we do not need to wait for the previous procedure to finish;
		// - we do not want the procedure to disappear from the list, if it is stopped.
		return false;
	}

	@Override
	public boolean isStoppable() {
		return true;
	}

	@Override
	public boolean isRunning() {
		
		// Note that it is NOT sufficient to just run super.isRunning, because then the CouchDB
		// content creator (which is synchronized) would start creating content too soon and fail quickly.
		if (!super.isRunning()) {
			return false;
		}

		return isAvailable();
	}

	@Override
	public boolean isOperating() {
		return isRunning();
	}

	public boolean isAvailable() {
		return UrlUtils.checkRead(getURL()).isOK();
	}

	public String getURL() {
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		return "http://" + EASYTRAVEL_CONFIG.couchDBHost + ":" + EASYTRAVEL_CONFIG.couchDBPort;
	}

	@Override
	public String getURI() {
		return getURL();
	}
	
	@Override
	public Feedback stop() {
		LOGGER.debug("Stopping procedures. Stopping CouchDB");
		try {
			
			if(!isOperating()) {
				return Feedback.Success;
			}
			
			// It is not even necessary to read the return value: we just need
			// to access the URL and that will cause the erlang controller to shut down.
			UrlUtils.checkRead(getShutdownURL()).isOK();
			waitForTermination();
			
			if(isOperating()) {
				// TBD: Here we could potentially wait for termination and check again if it has been successful.
				// If not successful, then we could kill the process.
				// (See similar code for ApacheHttpdProcedure.)
				// Then we would do
				// return Feedback.getMostSevere(Feedback.Success, super.stop());
				LOGGER.debug("Stopping procedures. Stopping CouchDB failed");
				return Feedback.Failure;
			}
			
			// return Feedback.Success;
			LOGGER.debug("Stopping procedures. CouchDB stopped");
			return super.stop();
		
		} catch (InterruptedException e) {
			LOGGER.debug("Stopping procedures. Stopping CouchDB failed");
			return Feedback.Failure;
		}
	}
	
	public String getShutdownURL() {
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		String myURL;
		myURL = "http://" + EASYTRAVEL_CONFIG.couchDBHost + ":" + Integer.toString(EASYTRAVEL_CONFIG.couchDBShutdownPort) + "/erl/couchDB_ET:stop_now";
		return myURL;
	}

	public static String getCrashURL() {
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		String myURL;
		myURL = "http://" + EASYTRAVEL_CONFIG.couchDBHost + ":" + Integer.toString(EASYTRAVEL_CONFIG.couchDBShutdownPort) + "/erl/couchDB_ET:crash_now";
		return myURL;
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
	public String getURI(UrlType type) {
		return getURL();
	}

	@Override
	public String getURIDNS(UrlType type) {
		return getURL();
	}
	
	@Override
	public String getURIDNS() {
		return getURL();
	}

	@Override
	public String getLogfile() {
		// Note that CouchDB log is in the same folder, but we make erl_couch.log ourselves, as  then it
		// also contains some additional useful information.
		return format("%s%serl_couch.log", Directories.getLogDir().getAbsolutePath(), File.separator);
	}

	@Override
	public boolean hasLogfile() {
		return true;
	}

	@Override
	public Technology getTechnology() {
		return Technology.COUCHDB;
	}

	@Override
	public boolean isInstrumentationSupported() {
		return false;
	}
}
