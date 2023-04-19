package com.dynatrace.easytravel.launcher.procedures;

import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.CustomerFrontendReservation;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.TomcatResourceReservation;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.DynamicPortDtAgentConfig;
import com.dynatrace.easytravel.launcher.engine.AbstractJavaProcedure;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.StopMode;
import com.dynatrace.easytravel.launcher.engine.WebProcedure;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.BasicLoggerConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.util.LocalUriProvider;
import com.dynatrace.easytravel.util.TextUtils;


public class CustomerFrontendProcedure extends AbstractJavaProcedure implements WebProcedure {

	private static final Logger LOGGER = LoggerFactory.make();

	private TomcatResourceReservation reservation;

	/**
	 *
	 * @param mapping
	 * @throws CorruptInstallationException if the JAR of the customer frontend could not be found
	 * @author martin.wurzinger
	 */
	public CustomerFrontendProcedure(ProcedureMapping mapping) throws CorruptInstallationException {
		super(mapping);
		addPersistenceModeSetting(mapping);

		try {
			reservation = reserveResources();
			addApplicationArguments(reservation);
		} catch (IOException e) {
			final EasyTravelConfig CONFIG = EasyTravelConfig.read();

			throw new IllegalStateException(TextUtils.merge("Unable to start {0} because necessary ports are not available. Configured ranges are: " +
			        "Port: {1,number,#}-{2,number,#}, Shutdown-Port: {3,number,#}-{4,number,#}, AJP-Port: {5,number,#}-{6,number,#}",
			        getName(),
			        CONFIG.frontendPortRangeStart,CONFIG.frontendPortRangeEnd,
			        CONFIG.frontendShutdownPortRangeStart,CONFIG.frontendShutdownPortRangeEnd,
			        CONFIG.frontendAjpPortRangeStart,CONFIG.frontendAjpPortRangeEnd), e);
		}
	}

	@Override
	protected DtAgentConfig getAgentConfig() {
		final EasyTravelConfig CONFIG = EasyTravelConfig.read();
		DtAgentConfig dtAgentConfig = new DynamicPortDtAgentConfig(this, CONFIG.frontendSystemProfile,
				CONFIG.frontendAgent, CONFIG.frontendAgentOptions, CONFIG.frontendEnvArgs);

		// Customize config settings for the procedure.
		adjustAgentConfig("customer frontend", "config.frontendEnvArgs", dtAgentConfig);
		return dtAgentConfig;
	}

	@Override
	protected String getModuleJar() {
		return Constants.Modules.CUSTOMER_FRONTEND;
	}

	@Override
	protected String getWorkingDir() {
		return BaseConstants.SubDirectories.CUSTOMER;
	}

	@Override
	protected String[] getJavaOpts() {
		final EasyTravelConfig CONFIG = EasyTravelConfig.read();
		return (String[]) ArrayUtils.addAll(CONFIG.javaopts, CONFIG.frontendJavaopts);
	}

	@Override
	public Feedback run() {
		if (reservation == null){
			return Feedback.Failure;
		}
		try {
			// check if in the meantime a lower port has been set free
			TomcatResourceReservation newReservation = reserveResources();

			if (this.reservation.compareTo(newReservation) < 0) {
				newReservation.release(); // we do not need the new reservation, so we release the reserved ports
			} else {
				this.reservation.release();
				this.reservation = newReservation;

				// update the commandline arguments for the customer frontend with the updated reservation
				addApplicationArguments(reservation);
			}
		} catch (IOException e) {
			LOGGER.warn("Could not check for a new port number, because an IOException happened", e);
		}

		Feedback processStartFeedback = super.run();
		if (!processStartFeedback.isOk()) {
			// something went wrong before
			return processStartFeedback;
		}

		if (!waitUntilRunning()) {
			LOGGER.warn(TextUtils.merge("Unable to wait until {0} has been started.", getName()));
			return Feedback.Failure;
		}

		LOGGER.info(TextUtils.merge("{0} successfully started.", getName()));
		return Feedback.Success;
	}

	protected TomcatResourceReservation reserveResources() throws IOException {
		return CustomerFrontendReservation.reserveResources();
	}

	@Override
	public Feedback stop() {
		LOGGER.debug("Stopping procedures. Stopping CustomerFrontend");
		if (!isRunning()) {
			// process not running
			LOGGER.debug("Stopping procedures. CustomerFrontend was not running");
			return Feedback.Success;
		}

		int shutdownPort;
		try {
			shutdownPort = getShutdownPort(); //NOPMD
		} catch (IllegalStateException ise) {
			LOGGER.warn(TextUtils.merge("Unable to stop {0}.", getName()), ise);
			return Feedback.Failure;
		}

		TomcatShutdownCommand shutdown = new TomcatShutdownCommand(LocalUriProvider.getLoopbackAdapter(), shutdownPort);

		Feedback feedback = shutdown.execute();
		if (!feedback.isOk()) {
			LOGGER.warn(TextUtils.merge("Kill {0} because shutdown via remote command failed.", getName()));
			return forceStop();
		}

		/* if shutdown command was successfully sent then wait until server was actually stopped */
		boolean isStillRunning = waitUntilNotRunning();

		/* if Tomcat is still running then destroy it */
		if (isStillRunning) {
			LOGGER.warn(TextUtils.merge("Kill {0} because shutdown via remote command timed out.", getName()));
			return forceStop();
		}

		releasePorts();

		// Tomcat could be stopped successfully in a clean way
		LOGGER.info("Customer Frontend could be stopped successfully via remote shutdown command.");
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
			LOGGER.warn("Trying to release ports that have not been reserved.");
			return;
		}

		// release reserved ports
		reservation.release();
	}

	@Override
	public boolean isOperatingCheckSupported() {
		return true;
	}

	@Override
	public boolean isOperating() {
		if (!isEnabled()) {
			return true;
		}

		if (!super.isRunning()) {
			return false;
		}
		if (reservation == null) {
			// config is not assigned before startup
			return false;
		}

		// it is important to check localhost here - do NOT access the configured Apache Frontend!
		String operatingUri = LocalUriProvider.getLocalUri(getPort(), getContextRoot());
		if (operatingUri == null) {
			return false;
		}

		return UrlUtils.checkConnect(operatingUri).isOK();
	}

	/**
	 * Add application arguments to customer frontend startup command.
	 *
	 * @author martin.wurzinger
	 */
	private void addApplicationArguments(TomcatResourceReservation reservation) {
		// clear the arguments before setting, otherwise we accumulate different settings if the procedure is restarted on a
		// different port
		clearApplicationArguments();

		addPersistenceModeSetting(getMapping());
        addInstalltionModeSetting(getMapping());

        // tell application about changed location of property file (e.g. in commandline launcher)
		process.setPropertyFile();

		addReservationSettings(reservation);
	}

	/**
	 * Get the context root the customer frontend is assigned to.
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
	 * Get the shutdown port the customer front-end is is listening for shutdown commands.
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
	public String getURI() {
		final EasyTravelConfig CONFIG = EasyTravelConfig.read();
	    if (StringUtils.isNotEmpty(CONFIG.frontendPublicUrl)) {
	        return CONFIG.frontendPublicUrl;
	    }
		return LocalUriProvider.getLocalUri(getPort(), getContextRoot());
	}

	@Override
	public String getURIDNS() {
		final EasyTravelConfig CONFIG = EasyTravelConfig.read();
	    if (StringUtils.isNotEmpty(CONFIG.frontendPublicUrl)) {

			// no need to convert to FQDN
			// return LocalUriProvider.getFQDN(CONFIG.frontendPublicUrl);
			return CONFIG.frontendPublicUrl;
	    }

		return LocalUriProvider.getLocalUriDNS(getPort(), getContextRoot());
	}

	@Override
	public String getLogfile() {
		return BasicLoggerConfig.getLogFilePath(BaseConstants.LoggerNames.CUSTOMER_FRONTEND, String.valueOf(getPort()));
	}

	@Override
	public boolean hasLogfile() {
	    return true;
	}

    // from WebProcedure

    private static final String PROPERTY_CUSTOMER_FRONTEND_PORT = "customerFrontendPort";

	@Override
	public String getPortPropertyName() {
		return PROPERTY_CUSTOMER_FRONTEND_PORT;
	}

	/**
	 * Get the port the customer frontend is assigned to.
	 *
	 * @throws IllegalStateException if the procedure has not been started yet
	 * @author martin.wurzinger
	 */
	@Override
	public int getPort() throws IllegalStateException {
		if (reservation == null) {
			throw new IllegalStateException(TextUtils.merge("No port assigned. {0} has not been started yet.", getName()));
		}
		return reservation.getPort();
	}

	@Override
	public StopMode getStopMode() {
		return StopMode.PARALLEL;
	}
}
