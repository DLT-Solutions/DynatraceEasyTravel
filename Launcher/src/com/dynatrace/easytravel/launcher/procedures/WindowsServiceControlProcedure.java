package com.dynatrace.easytravel.launcher.procedures;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.exec.*;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.AbstractProcedure;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.StopListener;
import com.dynatrace.easytravel.launcher.engine.StopMode;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.util.DtVersionDetector;

/**
 * Abstract base class for procedures which control a Windows Service, e.g. currently Web Server Agent and Host Agent.
 *
 * @author cwat-dstadler
 */
public abstract class WindowsServiceControlProcedure extends AbstractProcedure {
	private static final Logger LOGGER = Logger.getLogger(WindowsServiceControlProcedure.class.getName());

	protected final AtomicBoolean isRunning = new AtomicBoolean(false);
	protected final AtomicBoolean isDoneRunning = new AtomicBoolean(false);
	protected final AtomicBoolean wasRunningBefore = new AtomicBoolean(false);

	protected final String agentService = getServiceName();
	protected final AtomicReference<Feedback> feedback = new AtomicReference<Feedback>(Feedback.Neutral);

	// make startup asynchronous in order to not hold up startup of other procedures
	protected Thread thread = null;

	public WindowsServiceControlProcedure(ProcedureMapping mapping) throws IllegalArgumentException {
		super(mapping);
	}

	abstract String getServiceNamePattern();

	@Override
	public Feedback run() {
		feedback.set(Feedback.Neutral);

		final String serviceDisplayName = getServiceNamePattern().replace("${version}", "");
		thread = new Thread(serviceDisplayName) {
			@Override
			public void run() {
				isRunning.set(true);

				// retrieve the current state to know if we should stop it during shutdown
				if(getStartedServices().contains(agentService)) {
					wasRunningBefore.set(true);
					LOGGER.info(serviceDisplayName + " was running before, restarting it.");

					// first ensure the service is stopped
					startStopService("stop");
				} else {
					wasRunningBefore.set(false);
					LOGGER.info(serviceDisplayName + " was not running before, starting it now and stopping it later when stopping the procedure.");
				}

				isDoneRunning.set(true);

				// then start it
				feedback.set(startStopService("start"));
			}

		};
		thread.setDaemon(true);
		thread.start();

		LOGGER.info("Had result from " + serviceDisplayName + " startup: " + feedback.get());

		return feedback.get();
	}

	@Override
	public boolean isRunning() {
		return isRunning.get();
	}

	@Override
	public StopMode getStopMode() {
		return StopMode.PARALLEL; // can be stopped in combination with others
	}

	@Override
	public boolean isStoppable() {
		return true;
	}

	@Override
	public Feedback stop() {
		LOGGER.warning("Stopping procedures. Stopping WindowsServiceControl");
		// wait only a certain time for startup to finish
		try {
			if(thread != null) {
				thread.join(10000);
			}
		} catch (InterruptedException e) {
			LOGGER.log(Level.WARNING, "Waiting for thread was interrupted", e);
		}

		// if the service was not running before, stop it here
		if(!wasRunningBefore.get() && isRunning()) {
			startStopService("stop");
		}

		isRunning.set(false);
		isDoneRunning.set(false);

		LOGGER.warning("Stopping procedures. WindowsServiceControl stopped");
		return Feedback.Success;
	}

	@Override
	public boolean isOperatingCheckSupported() {
		return true;
	}

	@Override
	public boolean isOperating() {
		// as commons exec often hangs when querying the state of the service, we report it as running whenever the thread was started
		// to not confuse the user with a procedure which keeps "starting"
		return isRunning.get();
	}

	@Override
	public void addStopListener(StopListener stopListener) {
		// stop notifications not supported
	}

	@Override
	public void removeStopListener(StopListener stopListener) {
		// stop notifications not supported
	}

	@Override
	public void clearStopListeners() {
		// stop notifications not supported
	}

	@Override
	public String getDetails() {
		return "Service: " + getServiceName() +
				"\n" + (wasRunningBefore.get() ? "Service was running before." : "Service was not running before.") +
				"\n" + (isDoneRunning.get() ? "Done starting." : "Still starting...");
	}

	@Override
	public String getLogfile() {
		return null;
	}

	@Override
	public boolean hasLogfile() {
		return false;
	}

	@Override
	public Technology getTechnology() {
		return null;
	}

	@Override
	public boolean agentFound() {
		return false;
	}

	/**
	 * Helper method to run a commandline and collect output.
	 */
	private String execute(CommandLine cmdLine) {
		DefaultExecutor executor = new DefaultExecutor();
		executor.setExitValue(0);

		ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
		executor.setWatchdog(watchdog);

		ByteArrayOutputStream str = new ByteArrayOutputStream();
		executor.setStreamHandler(new PumpStreamHandler(str));

		DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
		try {
			LOGGER.info("Executing: " + cmdLine);
			executor.execute(cmdLine, handler);

			handler.waitFor(60000);

			if(!handler.hasResult()) {
				LOGGER.log(Level.WARNING, "Had timeout when stopping/starting/listing " + getServiceName() + " with command: " + cmdLine);

				// stop the Process forcefully
				watchdog.destroyProcess();

				return "";
			}

			ExecuteException exception = handler.getException();
			if(exception != null) {
				LOGGER.log(Level.WARNING, "Had exception when stopping/starting/listing " + getServiceName() + " with command: " + cmdLine + ": " +
						exception.getClass().getSimpleName() + ": " + exception.getMessage());	// do not log out Exception details with normal log level as they are usually not relevant
				if(LOGGER.isLoggable(Level.FINE)) {
					LOGGER.log(Level.FINE, "Exception details", exception);
				}

				return "";
			}

			int exitValue = handler.getExitValue();
			if (exitValue != 0) {
				LOGGER.warning("Had exit code " + exitValue + " when calling " + cmdLine);
			}

			// if we have some output, return it, otherwise an arbitrary string which is non-empty
			if(str.size() > 0) {
				return new String(str.toByteArray());
			}

			return "done";
		} catch (ExecuteException e) {
			LOGGER.log(Level.WARNING, "Could not stop/start/list " + getServiceName() + " with command: " + cmdLine, e);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Could not stop/start/list " + getServiceName() + " with command: " + cmdLine, e);
		} catch (InterruptedException e) {
			LOGGER.log(Level.WARNING, "Could not stop/start/list " + getServiceName() + " with command: " + cmdLine, e);
		} finally {
			byte[] output = str.toByteArray();
			LOGGER.info("Had output: " + new String(output));
		}
		return "";
	}

	/**
	 * Start or stop the Service depending on the provided start or stop command
	 */
	private Feedback startStopService(String startStop) {
		if (DtVersionDetector.isAPM()) {
			return Feedback.Success;
		}
		CommandLine cmdLine = new CommandLine("net.exe");
		cmdLine.addArgument(startStop);
		cmdLine.addArgument(agentService);

		LOGGER.info(startStop + "ing " + agentService);

		return execute(cmdLine).isEmpty() ? Feedback.Failure : Feedback.Success;
	}

	/**
	 * Return a list of started services on this machine by invoking "net start".
	 */
	private String getStartedServices() {
		CommandLine cmdLine = new CommandLine("net.exe");
		cmdLine.addArgument("start");

		return execute(cmdLine);
	}

	private String getServiceName() {
		String agentPath = getAgentPath(EasyTravelConfig.read());
		String dtVersion = DtVersionDetector.determineDTVersion(agentPath);

		final String serverNamePattern = getServiceNamePattern();
		final String agentService;
		// TODO: make this a generic-regex-base check!
		if ((agentPath != null && agentPath.contains("4.0")) ||
				(dtVersion != null && dtVersion.startsWith("4.0"))) {
			agentService = serverNamePattern.replace("${version}", "4.0.0");
		} else if ((agentPath != null && agentPath.contains("4.1")) ||
				(dtVersion != null && dtVersion.startsWith("4.1"))) {
			agentService = serverNamePattern.replace("${version}", "4.1.0");
		} else if ((agentPath != null && agentPath.contains("4.2")) ||
				(dtVersion != null && dtVersion.startsWith("4.2"))) {
			agentService = serverNamePattern.replace("${version}", "4.2.0");
		} else if ((agentPath != null && agentPath.contains("5.0.0")) ||	// use 5.0.0 to not match on 5.5.0!
				(dtVersion != null && dtVersion.startsWith("5.0.0"))) {
			agentService = serverNamePattern.replace("${version}", "5.0.0");
		} else if ((agentPath != null && agentPath.contains("5.1")) ||
				(dtVersion != null && dtVersion.startsWith("5.1"))) {
			agentService = serverNamePattern.replace("${version}", "5.1.0");
		} else if ((agentPath != null && agentPath.contains("5.5")) ||
				(dtVersion != null && dtVersion.startsWith("5.5"))) {
			agentService = serverNamePattern.replace("${version}", "5.5.0");
		} else if ((agentPath != null && agentPath.contains("5.6")) ||
				(dtVersion != null && dtVersion.startsWith("5.6"))) {
			agentService = serverNamePattern.replace("${version}", "5.6.0");
		} else if ((agentPath != null && agentPath.contains("6.0")) ||
				(dtVersion != null && dtVersion.startsWith("6.0"))) {
			agentService = serverNamePattern.replace("${version}", "6.0.0");
		} else if ((agentPath != null && agentPath.contains("6.1")) ||
				(dtVersion != null && dtVersion.startsWith("6.1"))) {
			agentService = serverNamePattern.replace("${version}", "6.1.0");
		} else if ((agentPath != null && agentPath.contains("6.2")) ||
				(dtVersion != null && dtVersion.startsWith("6.2"))) {
			agentService = serverNamePattern.replace("${version}", "6.2");
		} else if ((agentPath != null && agentPath.contains("6.3")) ||
				(dtVersion != null && dtVersion.startsWith("6.3"))) {
			agentService = serverNamePattern.replace("${version}", "6.3");
		} else if ((agentPath != null && agentPath.contains("6.5")) ||
				(dtVersion != null && dtVersion.startsWith("6.5"))) {
			agentService = serverNamePattern.replace("${version}", "6.5");
		} else if ((agentPath != null && agentPath.contains("7.0")) ||
				(dtVersion != null && dtVersion.startsWith("7.0"))) {
			agentService = serverNamePattern.replace("${version}", "7.0");
		} else {
			// use current version as default
			agentService = serverNamePattern.replace("${version}", BaseConstants.Version.DEFAULT_DYNATRACE_VERSION);
		}
		return agentService;
	}


	private static String getAgentPath(EasyTravelConfig CONFIG) {
		String path = "";

		// get path of agent
		DtAgentConfig config = new DtAgentConfig(CONFIG.backendSystemProfile, CONFIG.backendAgent, CONFIG.backendAgentOptions, CONFIG.backendEnvArgs);
		try {
			path = config.getAgentPath(Technology.JAVA);
		} catch (ConfigurationException e) {
			// no need to warn here, we do this later during starting the procedures anyway...
			// LOGGER.log(Level.WARNING, "Could not determine Java Agent Path", e);
		}

		return path;
	}
}
