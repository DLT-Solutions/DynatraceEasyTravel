package com.dynatrace.easytravel.launcher.procedures;

import java.io.File;
import java.io.IOException;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.engine.WebProcedure;
import com.dynatrace.easytravel.launcher.iis.IISExpressConfigs;
import com.dynatrace.easytravel.launcher.iis.IISExpressSetup;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.BasicLoggerConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

/**
 * @author anita.engleder
 */
public class PaymentBackendProcedure extends AbstractDotNetProcedure implements WebProcedure {

    private static final Logger LOGGER = LoggerFactory.make();

    // TODO philipp.grasboeck: should move to LocalUriProvider
	private static final String PAYMENT_BACKEND_URI_TEMPLATE = "http://{0}:{1,number,#}/";

    public PaymentBackendProcedure(ProcedureMapping mapping) throws IOException, CorruptInstallationException {
        super(mapping);
        final EasyTravelConfig CONFIG = EasyTravelConfig.read();
        if(this.isRunningOnLocalIIS()){
        	addApplicationArgument("-ap");
    		addApplicationArgument("\"dotNetBackend_easyTravel\"");  		
        }else if(CONFIG.paymentBackendServer.contains("cassini")){
	        addApplicationArgument("/run");
	        addApplicationArgument(new File(Directories.getInstallDir(), CONFIG.paymentBackendDir).getCanonicalPath());
	        addApplicationArgument("WebService\\PaymentService.asmx");
	        addApplicationArgument(Integer.toString(CONFIG.paymentBackendPort));
	        addApplicationArgument("nobrowser");
	        // tell application about changed location of property file (e.g. in commandline launcher)
	        process.setPropertyFile();       	
    	}else{
    		IISExpressSetup setup = new IISExpressSetup(IISExpressConfigs.PAYMENT_BACKEND_CONFIG, CONFIG.paymentBackendPort);
	        setup.generateConfig();
	        String confPath = new File(Directories.getConfigDir(), IISExpressConfigs.PAYMENT_BACKEND_CONFIG.getName()).getCanonicalPath();
	        addApplicationArgument("/config:" + confPath);
	        addApplicationArgument("/systray:false");
    	}
    }

    @Override
	protected DtAgentConfig getAgentConfig() {
        final EasyTravelConfig CONFIG = EasyTravelConfig.read();
        return new DtAgentConfig(CONFIG.paymentBackendSystemProfile, null, CONFIG.paymentBackendAgentOptions, CONFIG.paymentBackendEnvArgs);
    }

	@Override
	protected String getExecutable(ProcedureMapping mapping) {
        final EasyTravelConfig CONFIG = EasyTravelConfig.read();
		if(this.isRunningOnLocalIIS()){
			return CONFIG.paymentBackendServerIIS;
		}
		return CONFIG.paymentBackendServer;
	}

	@Override
	protected String getWorkingDir() {
        final EasyTravelConfig CONFIG = EasyTravelConfig.read();
		return CONFIG.paymentBackendDir;
	}

	@Override
	public String getURI() {
        final EasyTravelConfig CONFIG = EasyTravelConfig.read();
        return TextUtils.merge(PAYMENT_BACKEND_URI_TEMPLATE, CONFIG.paymentBackendHost, CONFIG.paymentBackendPort);
    }

	@Override
	public String getLogfile() {
        return BasicLoggerConfig.getLogFilePath(BaseConstants.LoggerNames.PAYMENT_BACKEND);
	}

    // from WebProcedure

    private static final String PROPERTY_PAYMENT_BACKEND_PORT = "paymentBackendPort";

	@Override
	public String getPortPropertyName() {
		return PROPERTY_PAYMENT_BACKEND_PORT;
	}

	@Override
	public int getPort() {
        final EasyTravelConfig CONFIG = EasyTravelConfig.read();
		return CONFIG.paymentBackendPort;
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
		return checkIsRunningOnIIS(CONFIG.paymentBackendPageToIdentify, CONFIG.paymentBackendPort);
	}

	@Override
	protected void log(String logMessage){
		LOGGER.info(logMessage);
	}
}
