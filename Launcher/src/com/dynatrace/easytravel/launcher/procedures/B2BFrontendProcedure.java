package com.dynatrace.easytravel.launcher.procedures;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.ipc.SocketUtils;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.WebProcedure;
import com.dynatrace.easytravel.launcher.iis.IISExpressConfigs;
import com.dynatrace.easytravel.launcher.iis.IISExpressSetup;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.BasicLoggerConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.LocalUriProvider;

/**
 * @author anita.engleder
 */
public class B2BFrontendProcedure extends AbstractDotNetProcedure implements WebProcedure {

    private static final Logger LOGGER = LoggerFactory.make();
    private final int port;

    public B2BFrontendProcedure(ProcedureMapping mapping) throws IOException, CorruptInstallationException {
    	super(mapping);

        final EasyTravelConfig CONFIG = EasyTravelConfig.read();
    	if(this.isRunningOnLocalIIS()){
    		port = CONFIG.b2bFrontendPortRangeStart;
    		addApplicationArgument("-ap");
    		addApplicationArgument("\"dotNetFrontend_easyTravel\"");  		
    	}else if(CONFIG.b2bFrontendServer.contains("cassini")){	        
	        addApplicationArgument("/run");
	        addApplicationArgument(new File(Directories.getInstallDir(), CONFIG.b2bFrontendDir).getCanonicalPath());
	        addApplicationArgument("Index.aspx");
	        port = SocketUtils.reserveNextFreePort(CONFIG.b2bFrontendPortRangeStart, CONFIG.b2bFrontendPortRangeEnd, null);
	        addApplicationArgument(String.valueOf(port));
	        addApplicationArgument("nobrowser");
	        // tell application about changed location of property file (e.g. in commandline launcher)
	        process.setPropertyFile();
    	}else{
	        port = SocketUtils.reserveNextFreePort(CONFIG.b2bFrontendPortRangeStart, CONFIG.b2bFrontendPortRangeEnd, null);
	        IISExpressSetup setup = new IISExpressSetup(IISExpressConfigs.B2B_FRONTEND_CONFIG, port);
	        setup.generateConfig();
	        addApplicationArgument("/config:" + new File(Directories.getConfigDir(), IISExpressConfigs.B2B_FRONTEND_CONFIG.getName()).getCanonicalPath());
	        addApplicationArgument("/systray:false");
    	}
    }

	@Override
	protected DtAgentConfig getAgentConfig() {
    	final EasyTravelConfig config = EasyTravelConfig.read();
    	return new DtAgentConfig(config.b2bFrontendSystemProfile, null, config.b2bFrontendAgentOptions, config.b2bFrontendEnvArgs);
    }

	@Override
	protected String getExecutable(ProcedureMapping mapping) {
        final EasyTravelConfig CONFIG = EasyTravelConfig.read();
		if(this.isRunningOnLocalIIS()){
			return CONFIG.b2bFrontendServerIIS;
		}
		return CONFIG.b2bFrontendServer;
	}

	@Override
	protected String getWorkingDir() {
        final EasyTravelConfig CONFIG = EasyTravelConfig.read();
		return CONFIG.b2bFrontendDir;
	}

    @Override
	public String getURI() {
        final EasyTravelConfig CONFIG = EasyTravelConfig.read();
		if (StringUtils.isNotEmpty(CONFIG.b2bFrontendPublicUrl)) {
            return CONFIG.b2bFrontendPublicUrl;
        }
        return LocalUriProvider.getLocalUri(port, "/");
	}

    @Override
	public String getURIDNS() {
    	final EasyTravelConfig CONFIG = EasyTravelConfig.read();
		if (StringUtils.isNotEmpty(CONFIG.b2bFrontendPublicUrl)) {
            return CONFIG.b2bFrontendPublicUrl;
        }
        return LocalUriProvider.getLocalUriDNS(port, "/");
	}

	@Override
	public String getLogfile() {
	    return BasicLoggerConfig.getLogFilePath(BaseConstants.LoggerNames.B2B_FRONTEND, String.valueOf(port));
	}

    @Override
    public Feedback stop() {
    	LOGGER.debug("Stopping procedures. Stopping B2BFrontend");
        Feedback feedback = super.stop();
        SocketUtils.freePort(port);
        return feedback;
    }

    // from WebProcedure

    private static final String PROPERTY_B2B_FRONTEND_PORT = "b2bFrontendPort";

	@Override
	public String getPortPropertyName() {
		return PROPERTY_B2B_FRONTEND_PORT;
	}

    @Override
    public int getPort() {
        return port;
    }

	@Override
	public boolean isRunningOnLocalIIS(){
		return checkIfRunningOnLocalIIS();
	}

	/**
	 * Checks if given site is running on Local IIS
	 *
	 * @param site
	 * @param siteTitle
	 * @param port
	 * @return
	 */
	public static boolean checkIfRunningOnLocalIIS(){
        final EasyTravelConfig CONFIG = EasyTravelConfig.read();
		return checkIsRunningOnIIS(CONFIG.b2bFrontendPageToIdentify, CONFIG.b2bFrontendPortRangeStart);
	}

	@Override
	protected void log(String logMessage){
		LOGGER.info(logMessage);
	}
}
