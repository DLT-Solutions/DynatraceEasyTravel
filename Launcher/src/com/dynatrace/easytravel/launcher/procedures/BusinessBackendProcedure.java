package com.dynatrace.easytravel.launcher.procedures;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.config.BusinessBackendReservation;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.DynamicPortDtAgentConfig;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.StopMode;
import com.dynatrace.easytravel.launcher.engine.WebProcedure;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.BasicLoggerConfig;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.util.LocalUriProvider;
import com.dynatrace.easytravel.util.TextUtils;


public class BusinessBackendProcedure extends AbstractPluginServiceProcedure implements WebProcedure {
	private static final Logger LOGGER = Logger.getLogger(BusinessBackendProcedure.class.getName());

	private static final String PROPERTY_BUSINESS_BACKEND_PORT = "businessBackendPort";

    private BusinessBackendReservation reservation;

	/**
     * @author martin.wurzinger
     * @throws CorruptInstallationException if the JAR of the business backend could not be found
     */
    public BusinessBackendProcedure(ProcedureMapping mapping) throws CorruptInstallationException {
		super(mapping);
		addPersistenceModeSetting(mapping);

		final EasyTravelConfig CONFIG = EasyTravelConfig.read();
		try {
			if (CONFIG.backendMultiEnabled) {
				reservation = BusinessBackendReservation.reserveResources();
				addApplicationArguments(reservation);
			}
		} catch (IOException e) {
			throw new IllegalStateException(TextUtils.merge("Unable to start {0} because necessary ports are not available. Configured ranges are: " +
			        "Port: {1,number,#}-{2,number,#}, Shutdown-Port: {3,number,#}-{4,number,#}, AJP-Port: {5,number,#}-{6,number,#}",
			        getName(),
			        CONFIG.backendPortRangeStart,CONFIG.backendPortRangeEnd,
			        CONFIG.backendShutdownPortRangeStart,CONFIG.backendShutdownPortRangeEnd,
			        CONFIG.backendAjpPortRangeStart,CONFIG.backendAjpPortRangeEnd), e);
		}
    }
    
	@Override
	protected DtAgentConfig getAgentConfig() {
		final EasyTravelConfig CONFIG = EasyTravelConfig.read();

		DtAgentConfig dtAgentConfig = new DynamicPortDtAgentConfig(this, CONFIG.backendSystemProfile,
				CONFIG.backendAgent, CONFIG.backendAgentOptions, CONFIG.backendEnvArgs);

		// Customize config settings for the procedure.
		adjustAgentConfig("business backend", "config.backendEnvArgs", dtAgentConfig);
		return dtAgentConfig;
	}

	@Override
	protected String getModuleJar() {
		return Constants.Modules.BUSINESS_BACKEND;
	}

	@Override
	protected String getWorkingDir() {
		return BaseConstants.SubDirectories.BUSINESS;
	}

	@Override
	public String getURIDNS() {
		final EasyTravelConfig CONFIG = EasyTravelConfig.read();
	    if (StringUtils.isNotEmpty(CONFIG.backendHost)) {
			return "http://" + CONFIG.backendHost + ":" + CONFIG.backendPort;
	    }

		return "http://localhost";
	}

	@Override
	protected String[] getJavaOpts() {
		final EasyTravelConfig CONFIG = EasyTravelConfig.read();
		return (String[]) ArrayUtils.addAll(CONFIG.javaopts, CONFIG.backendJavaopts);
	}

	@Override
	public Feedback run() {

		if (EasyTravelConfig.read().backendMultiEnabled && (reservation == null)) {
			return Feedback.Failure;
		}
		if (reservation != null) {
			try {
				// check if in the meantime a lower port has been set free
				BusinessBackendReservation newReservation = BusinessBackendReservation.reserveResources();

				if (this.reservation.compareTo(newReservation) < 0) {
					newReservation.release(); // we do not need the new reservation, so we release the reserved ports
				} else {
					this.reservation.release();
					this.reservation = newReservation;

					// update the commandline arguments for the business backend with the updated reservation
					addApplicationArguments(reservation);
				}
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, "Could not check for a new port number, because an IOException happened", e);
			}
		}

		Feedback processStartFeedback = super.run();
		if (!processStartFeedback.isOk()) {
			// something went wrong before
			return processStartFeedback;
		}

		if (!waitUntilRunning()) {
			LOGGER.warning(TextUtils.merge("Unable to wait until {0} has been started.", getName()));
			return Feedback.Failure;
		}

		LOGGER.info(TextUtils.merge("{0} successfully started.", getName()));
		return Feedback.Success;
    }

	/**
	 * Add application arguments to business backend startup command.
	 *
	 * @author martin.wurzinger
	 */
	private void addApplicationArguments(BusinessBackendReservation reservation) {
		// clear the arguments before setting, otherwise we accumulate different settings if the procedure is restarted on a
		// different port
		clearApplicationArguments();
		addPersistenceModeSetting(getMapping());
        addInstalltionModeSetting(getMapping());

		// tell application about changed location of property file (e.g. in commandline launcher)
		process.setPropertyFile();

		addReservationSettings(reservation);
	}

    @Override
    public Feedback stop() {
    	LOGGER.warning("Stopping procedures. Stopping BusinessBackend");
    	if (EasyTravelConfig.read().backendMultiEnabled) {
    		return stopMultiBackend();
    	}

        if (!isRunning()) {
            // process not running
        	LOGGER.warning("Stopping procedures. BusinessBackend was not running");
            return Feedback.Success;
        }

        final EasyTravelConfig CONFIG = EasyTravelConfig.read();
        TomcatShutdownCommand shutdown = new TomcatShutdownCommand(LocalUriProvider.getLoopbackAdapter(), CONFIG.backendShutdownPort);

        Feedback feedback = shutdown.execute();
        if (!feedback.isOk()) {
            LOGGER.warning(TextUtils.merge("Kill {0} because shutdown via remote command failed.", getName()));
            return super.stop();
        }

        /* if shutdown command was successfully sent then wait until server was actually stopped */
        boolean isStillRunning = waitUntilNotRunning();

        /* if Tomcat is still running then destroy it */
        if (isStillRunning) {
            LOGGER.warning(TextUtils.merge("Kill {0} because shutdown via remote command timed out.", getName()));
            return super.stop();
        }

        // Tomcat could be stopped successfully in a clean way
        LOGGER.info("Business backend could be stopped successfully via remote shutdown command.");
        return Feedback.Success;
    }

	public Feedback stopMultiBackend() {
		if (!isRunning()) {
			// process not running
			LOGGER.warning("Stopping procedures. BusinessBackend stopped");
			return Feedback.Success;
		}

		int shutdownPort;
		try {
			shutdownPort = getShutdownPort(); //NOPMD
		} catch (IllegalStateException ise) {
			LOGGER.log(Level.WARNING, TextUtils.merge("Unable to stop {0}.", getName()), ise);
			return Feedback.Failure;
		}

		TomcatShutdownCommand shutdown = new TomcatShutdownCommand(LocalUriProvider.getLoopbackAdapter(), shutdownPort);

		Feedback feedback = shutdown.execute();
		if (!feedback.isOk()) {
			LOGGER.warning(TextUtils.merge("Kill {0} because shutdown via remote command failed.", getName()));
			return forceStop();
		}

		/* if shutdown command was successfully sent then wait until server was actually stopped */
		boolean isStillRunning = waitUntilNotRunning();

		/* if Tomcat is still running then destroy it */
		if (isStillRunning) {
			LOGGER.warning(TextUtils.merge("Kill {0} because shutdown via remote command timed out.", getName()));
			return forceStop();
		}

		releasePorts();

		// Tomcat could be stopped successfully in a clean way
		LOGGER.info("Business Backend could be stopped successfully via remote shutdown command.");
		return Feedback.Success;
	}

	private Feedback forceStop() {
		Feedback feedback = super.stop();

		if (feedback.isOk()) {
			releasePorts();
		}

		return feedback;
	}

	private void releasePorts() {
		if (reservation == null) {
			LOGGER.warning("Trying to release ports that have not been reserved.");
			return;
		}

		// release reserved ports
		reservation.release();
	}

	/**
	 * Get the context root the business backend is assigned to.
	 *
	 * @throws IllegalStateException if the procedure has not been started yet
	 * @author martin.wurzinger
	 */
	public String getContextRoot() throws IllegalStateException {
		if (reservation == null) {
			throw new IllegalStateException(TextUtils.merge("No contextRoot assigned. {0} has not been started yet.", getName()));
		}
		return reservation.getContextRoot();
	}

	/**
	 * Get the shutdown port the business backend is is listening for shutdown commands.
	 *
	 * @throws IllegalStateException if the procedure has not been started yet
	 * @author martin.wurzinger
	 */
	public int getShutdownPort() throws IllegalStateException {
		if (reservation == null) {
			throw new IllegalStateException(
					TextUtils.merge("No shutdown port assigned. {0} has not been started yet.", getName()));
		}
		return reservation.getShutdownPort();
	}

    @Override
    public boolean isOperatingCheckSupported() {
        return true;
    }

    @Override
    public boolean isOperating() {
    	if (LOGGER.isLoggable(Level.FINE)) LOGGER.log(Level.FINE, "isOperating");
        if (!super.isRunning()) {
            return false;
        }

        // here we expect the Backend to run on the local host as we just started it locally...
        final EasyTravelConfig CONFIG = EasyTravelConfig.read();
        return UrlUtils.checkConnect(LocalUriProvider.getLocalUri(getPort(), CONFIG.backendContextRoot)).isOK();
    }

	@Override
	public String getLogfile() {
		if(reservation == null) {
			return BasicLoggerConfig.getLogFilePath(BaseConstants.LoggerNames.BUSINESS_BACKEND);
		}

		return BasicLoggerConfig.getLogFilePath(BaseConstants.LoggerNames.BUSINESS_BACKEND, String.valueOf(getPort()));
	}

	@Override
	public boolean hasLogfile() {
	    return true;
	}

    // from WebProcedure

	@Override
	public String getPortPropertyName() {
		return PROPERTY_BUSINESS_BACKEND_PORT;
	}

	@Override
	public int getPort() {
		if(reservation != null) {
			return reservation.getPort();
		}

        final EasyTravelConfig CONFIG = EasyTravelConfig.read();
		return CONFIG.backendPort;
	}

	@Override
	public StopMode getStopMode() {
		return StopMode.PARALLEL;
	}
}
