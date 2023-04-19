package com.dynatrace.easytravel.launcher.procedures;

import java.util.logging.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.BasicLoggerConfig;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.util.LocalUriProvider;
import com.dynatrace.easytravel.util.TextUtils;

/**
 * Launches a simple java web service based plugin service
 *
 * @author cwat-rpilz
 *
 */
public class PluginServiceProcedure extends AbstractPluginServiceProcedure {
	public PluginServiceProcedure(ProcedureMapping mapping) throws CorruptInstallationException {
		super(mapping);
	}
	
    private static final Logger LOGGER = Logger.getLogger(ThirdPartyContentServerProcedure.class.getName());
	
    @Override
    public Feedback stop() {
    	LOGGER.warning("Stopping procedures. Stopping PluginService");
        if (!isRunning()) {
            // process not running
        	LOGGER.warning("Stopping procedures. PluginService was not running");
            return Feedback.Success;
        }

	    final EasyTravelConfig CONFIG = EasyTravelConfig.read();
        TomcatShutdownCommand shutdown = new TomcatShutdownCommand(LocalUriProvider.getLoopbackAdapter(), CONFIG.pluginServiceShutdownPort);

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
        LOGGER.info("Plugin service could be stopped successfully via remote shutdown command.");
        return Feedback.Success;
    }

	@Override
	public boolean isOperatingCheckSupported() {
		return true;
	}

	@Override
	public boolean isOperating() {
		String operatingUri = LocalUriProvider.getLocalUri(EasyTravelConfig.read().pluginServicePort, "/PluginService/ping");
		if (operatingUri == null) {
			return false;
		}

		return UrlUtils.checkConnect(operatingUri).isOK();
	}

	@Override
	public boolean hasLogfile() {
		return true;
	}

	@Override
	public String getLogfile() {
		return BasicLoggerConfig.getLogFilePath(BaseConstants.LoggerNames.PLUGIN_SERVICE);
	}

	@Override
	protected String getModuleJar() {
		return Constants.Modules.PLUGIN_SERVICE;
	}

	@Override
	protected String getWorkingDir() {
		return BaseConstants.SubDirectories.CURRENT;
	}

	@Override
	protected String[] getJavaOpts() {
		return new String[0];
	}

	@Override
	protected DtAgentConfig getAgentConfig() {
		return null;
	}

	@Override
	public boolean isInstrumentationSupported() {
		return false;
	}
}
