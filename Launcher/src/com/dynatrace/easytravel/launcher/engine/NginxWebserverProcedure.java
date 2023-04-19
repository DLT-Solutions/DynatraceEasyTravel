package com.dynatrace.easytravel.launcher.engine;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeListener;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.httpd.LogWriter;
import com.dynatrace.easytravel.launcher.nginx.NginxConfSetup;
import com.dynatrace.easytravel.launcher.nginx.NginxUtils;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.util.LocalUriProvider;
import com.dynatrace.easytravel.util.TextUtils;

/**
 * @author cwpl-rorzecho
 */
public class NginxWebserverProcedure extends AbstractNativeProcedure implements WebProcedure, PluginChangeListener {
    private static final Logger LOGGER = LoggerFactory.make();

    private NginxConfSetup nginxConfSetup;

    private DtAgentConfig dtAgentConfig;
    
    private boolean isNginxAlreadyStartedLocally;
    
    private static final Map<String, String> requiredHeaders;
    static{
    	requiredHeaders = new HashMap<String, String>();
    	requiredHeaders.put("Server","nginx");

    }
    public NginxWebserverProcedure(ProcedureMapping mapping) throws CorruptInstallationException, IOException {
        super(mapping);

        nginxConfSetup = new NginxConfSetup();

        createNginxConfig();

        applySettings();
        
		
		
    }

    protected void createNginxConfig() throws IOException {
        nginxConfSetup.createNginxConfig();
    }

    protected void applySettings() {
        process.addApplicationArgument("-c");
        process.addApplicationArgument(NginxConfSetup.NGINX_CONFIG_FILE.getAbsolutePath());
    }

    @Override
    protected String getExecutable(ProcedureMapping mapping) {
        return NginxUtils.getExecutable();
    }

    @Override
    protected String getWorkingDir() {
        return NginxUtils.NGINX_PARENT_DIR;
    }

    @Override
    protected DtAgentConfig getAgentConfig() {
        if (dtAgentConfig != null) {
            return dtAgentConfig;
        }
        return createDtAgentConfig();
    }

    @Override
    public boolean agentFound() {
        return agentFound(getAgentConfig());
    }

    @Override
    public boolean hasLogfile() {
        return false;
    }

    @Override
    public String getLogfile() {
        return null;
    }

    @Override
    public Technology getTechnology() {
        return Technology.NGINX;
    }

    @Override
    public Feedback run() {
        PluginChangeMonitor.registerForPluginChanges(this);
        Feedback feedback = Feedback.Success;
        isNginxAlreadyStartedLocally = false;
        
        if (!isRunning()) {
        	LOGGER.info(TextUtils.merge("Trying to run Nginx Webserver"));
            feedback = super.run();
        } else {
            LOGGER.info(TextUtils.merge("Nginx Webserver is already running on port: {0}", nginxConfSetup.getNginxPlaceholderProperty(NginxConfSetup.NGINX_PORT_PLACEHOLDER)));
            isNginxAlreadyStartedLocally=true;
        }

        return feedback;
    }

    @Override
    public Feedback stop() {
    	LOGGER.debug("Stopping procedures. Stopping NginxWebserver");
        PluginChangeMonitor.unregisterFromPluginChanges(this);
        
        if(isNginxAlreadyStartedLocally){
        	LOGGER.debug("Stopping procedures. NginxWebserver stopped");
        	return Feedback.Success;
        }
        
        String stopOutput = "";
        try {
            stopOutput = NginxUtils.stopNginx();
            LOGGER.info("Trying to stop nginx process...");
            if (isOperating()) {
                LOGGER.info("Nginx process is still running, try stop process again");
                Feedback stop = super.stop();
                if (stop == Feedback.Success) {
                    LOGGER.info("Nginx process stopped successfully");
                    return stop;
                }
                LOGGER.error("Cannot stop nginx process");
                return stop;
            }
        } catch (IOException e) {
            LOGGER.error(TextUtils.merge("Cannot stop Nginx Webserver: {0}", stopOutput));
            return Feedback.Failure;
        }
        LOGGER.info("Nginx process stopped successfully");
        return Feedback.Success;
    }

    @Override
    public StopMode getStopMode() {
    	if(isNginxAlreadyStartedLocally){
    		return StopMode.NONE;
    	}
        return StopMode.PARALLEL;
    }

    @Override
    public String getURI(BaseConstants.UrlType type) {
        return LocalUriProvider.getURL(type, /*useDNS*/ false);
    }

    @Override
    public String getURIDNS(BaseConstants.UrlType type) {
        return LocalUriProvider.getURL(type, /*useDNS*/ true);
    }

    @Override
    public String getURI() {
        return LocalUriProvider.getURL(BaseConstants.UrlType.NGINX_JAVA_FRONTEND, /*useDNS*/ false);
    }

    @Override
    public String getURIDNS() {
        return LocalUriProvider.getURL(BaseConstants.UrlType.NGINX_JAVA_FRONTEND, /*useDNS*/ true);
    }

    @Override
    public int getPort() {
        String nginxWebServerPortProperty = getPortPropertyName();
        return Integer.valueOf(NginxConfSetup.getPropertyString(nginxWebServerPortProperty));
    }

    @Override
    public String getPortPropertyName() {
        Field field = null;
        try {
            field = EasyTravelConfig.class.getField("nginxWebServerPort");
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("The nginx port property is not defined in " + EasyTravelConfig.class.getName() + " class");
        }
        return field.getName();
    }

    @Override
    public boolean isOperatingCheckSupported() {
        return true;
    }

    @Override
    public boolean isOperating() {
        return isRunning();
    }

    @Override
    public boolean isRunning() {
    	return UrlUtils.checkHeaders(this.getURI(), requiredHeaders).isOK();
    }

    @Override
    public boolean isStoppable() {
        return true;
    }

    @Override
    public void pluginsChanged() {
        // do nothing for now
    }

    public String getErrorLogfile() {
        // look for all files "error*.log" and return the most current one
        return getLatestLogfile("nginx_error*.log");
    }

    private String getLatestLogfile(String wildcard) {
        // look in the log-directory
        File dir = Directories.getLogDir();

        return LogWriter.getLastModifiedFile(dir, wildcard);
    }

    public static DtAgentConfig createDtAgentConfig() {
        final EasyTravelConfig config = EasyTravelConfig.read();
        return new DtAgentConfig(null,
                config.nginxWebServerAgent,
                null,
                config.nginxWebServerEnvArgs);
    }

    /**
     * Only for testing
     * @param dtAgentConfig
     */
    public void setDtAgentConfig(DtAgentConfig dtAgentConfig) {
        this.dtAgentConfig = dtAgentConfig;
    }

    /**
     * Only for testing
     * @param processSettings
     */
    public void applySettings(String... appArguments) {
        process.clearApplicationArguments();
        for (String processSetting : appArguments) {
            process.addApplicationArgument(processSetting);
        }
    }

    /**
     * Only for testing
     * @param nginxConfSetup
     */
    public void setNginxConfSetup(NginxConfSetup nginxConfSetup) {
        this.nginxConfSetup = nginxConfSetup;
    }

}
