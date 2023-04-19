package com.dynatrace.easytravel.launcher.procedures;

import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.engine.*;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.BasicLoggerConfig;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.util.LocalUriProvider;
import com.dynatrace.easytravel.util.TextUtils;


public class ThirdPartyContentServerProcedure extends AbstractJavaProcedure implements WebProcedure {

    private static final Logger LOGGER = Logger.getLogger(ThirdPartyContentServerProcedure.class.getName());

    /**
     * @param settings the settings for the business backend procedure
     * @author peter.lang
     * @throws CorruptInstallationException if the JAR of the business backend could not be found
     */
    public ThirdPartyContentServerProcedure(ProcedureMapping mapping) throws CorruptInstallationException {
        super(mapping);

        // tell application about changed location of property file (e.g. in commandline launcher)
        process.setPropertyFile();
        
		//add environment variables
		String[] thirdpartyEnvArgs = EasyTravelConfig.read().thirdpartyEnvArgs;
		if (thirdpartyEnvArgs != null) {
			Map<String, String> envArgs = DtAgentConfig.parseEnvironmentArgs(thirdpartyEnvArgs);
			for (Map.Entry<String, String> envArg : envArgs.entrySet()) {
				process.setEnvironmentVariable(envArg.getKey(), envArg.getValue());
			}
		}        
    }

	@Override
	/**
	 * always returns null --> do not connect agents to third party content server
	 */
	protected DtAgentConfig getAgentConfig() {
    	return null;
    }

	@Override
	protected String getModuleJar() {
		return Constants.Modules.THIRDPARTY_CONTENT_SERVER;
	}

	@Override
	protected String getWorkingDir() {
		return BaseConstants.SubDirectories.THIRDPARTY;
	}

	@Override
	protected String[] getJavaOpts() {
	    final EasyTravelConfig CONFIG = EasyTravelConfig.read();
		return (String[]) ArrayUtils.addAll(CONFIG.javaopts, CONFIG.thirdpartyJavaopts);
	}

    @Override
    public Feedback run() {
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

    @Override
    public Feedback stop() {
    	LOGGER.warning("Stopping procedures. Stopping ThirdPartyServer");
        if (!isRunning()) {
            // process not running
        	LOGGER.warning("Stopping procedures. ThirdPartyServer was not running");
            return Feedback.Success;
        }

	    final EasyTravelConfig CONFIG = EasyTravelConfig.read();
        TomcatShutdownCommand shutdown = new TomcatShutdownCommand(LocalUriProvider.getLoopbackAdapter(), CONFIG.thirdpartyShutdownPort);

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
        LOGGER.info("Third party content server could be stopped successfully via remote shutdown command.");
        return Feedback.Success;
    }

    @Override
    public boolean isOperatingCheckSupported() {
        return true;
    }

    @Override
    public boolean isOperating() {
        if (!super.isRunning()) {
            return false;
        }

        // here we expect the Backend to run on the host (or virtual host) as we just started it locally...
        final EasyTravelConfig CONFIG = EasyTravelConfig.read();
        return UrlUtils.checkRead(LocalUriProvider.getUri(CONFIG.thirdpartyHost, CONFIG.thirdpartyPort, CONFIG.thirdpartyContextRoot)).isOK();
    }


	/* 
	 * APM-8129: used to mark host as unavailable when scenario is changed.
	 * (non-Javadoc)
	 * @see com.dynatrace.easytravel.launcher.engine.AbstractProcedure#getURI()
	 */
	@Override
	public String getURI() {
		final EasyTravelConfig CONFIG = EasyTravelConfig.read();
		return LocalUriProvider.getUri(CONFIG.thirdpartyHost, CONFIG.thirdpartyPort, CONFIG.thirdpartyContextRoot);
	}

	@Override
	public String getLogfile() {
		return BasicLoggerConfig.getLogFilePath(BaseConstants.LoggerNames.THIRDPARTY_CONTENT);
	}

	@Override
	public boolean hasLogfile() {
	    return true;
	}

    // from WebProcedure

	private static final String PROPERTY_THIRDPARTY_SERVER_PORT = "thirdpartyPort";

	@Override
	public String getPortPropertyName() {
		return PROPERTY_THIRDPARTY_SERVER_PORT;
	}

	@Override
	public int getPort() {
	    final EasyTravelConfig CONFIG = EasyTravelConfig.read();
		return CONFIG.thirdpartyPort;
	}
	@Override
	public StopMode getStopMode() {
		return StopMode.PARALLEL;
	}

	/* (non-Javadoc)
	 * @see com.dynatrace.easytravel.launcher.engine.AbstractProcedure#isInstrumentationSupported()
	 */
	@Override
	public boolean isInstrumentationSupported() {
		return false;
	}
}
