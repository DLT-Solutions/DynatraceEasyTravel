package com.dynatrace.easytravel.launcher.nginx;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.engine.ProcedureFactory;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.util.MvelUtils;
import com.dynatrace.easytravel.util.TextUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.util.PropertyPlaceholderHelper;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * This class configures the NGINX HTTP Server.
 * The nginx.conf file can be found in the /.dynaTrace/easyTravel x.x.x/config folder.
 *
 * @author cwpl-rorzecho
 */
public class NginxConfSetup {
    private final Properties nginxConfigProperties;
    private final PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper("${", "}");

    public static final String NGINX_CONFIG = "nginx.conf";
    public static final String NGINX_CONFIG_TEMPLATE = NGINX_CONFIG + ".template";

    public static final File NGINX_CONFIG_FILE = new File(Directories.getConfigDir(), NGINX_CONFIG);

    // template placeholders
    public static final String NGINX_MIME_TYPES_PLACEHOLDER = "nginxMimeTypes";
    public static final String NGINX_CUSTOMER_FRONTEND_HOST_PORT_PLACEHOLDER = "nginxCustomerFrontendHostPort";
    public static final String NGINX_HOST_PLACEHOLDER = "nginxWebServerHost";
    public static final String NGINX_PORT_PLACEHOLDER = "nginxWebServerPort";
    public static final String NGINX_B2B_HOST_PORT_PLACEHOLDER = "nginxB2bHostPort";
    public static final String NGINX_B2B_HOST_PLACEHOLDER = "nginxWebServerB2bHost";
    public static final String NGINX_B2B_PORT_PLACEHOLDER = "nginxWebServerB2bPort";
    public static final String NGINX_ANGULAR_HOST_PORT_PLACEHOLDER = "nginxAngularHostPort";
    public static final String NGINX_ANGULAR_HOST_PLACEHOLDER = "nginxWebServerAngularHost";
    public static final String NGINX_ANGULAR_PORT_PLACEHOLDER = "nginxWebServerAngularPort";       
    public static final String NGINX_ACCESS_LOG_PATH_PLACEHOLDER = "nginxAccessLogPath";
    public static final String NGINX_ERROR_LOG_PATH_PLACEHOLDER = "nginxErrorLogPath";
    public static final String NGINX_PID_FILE_PATH_PLACEHOLDER = "nginxPidFilePath";
    

    public NginxConfSetup() {
        this.nginxConfigProperties = new NginxConfigProperties();
    }

    public NginxConfSetup(Properties nginxConfigProperties) {
        this.nginxConfigProperties = nginxConfigProperties;
    }

    /**
     * Replace placeholders in specified template with set of Properties
     * @param template
     * @return
     */
    public String replacePlaceholders(String template) {
        return replacePlaceholders(template, nginxConfigProperties);
    }

    private String replacePlaceholders(String template, Properties nginxProperties) {
        return placeholderHelper.replacePlaceholders(template, nginxProperties);
    }

    /**
     * Get the nginx properties
     * @return Properties
     */
    public Properties getNginxConfigProperties() {
        return nginxConfigProperties;
    }

    /**
     * Get nginx property
     * @param nginxProperty
     * @return
     */
    public String getNginxPlaceholderProperty(String placeholderProperty) {
        return nginxConfigProperties.getProperty(placeholderProperty);
    }

    /**
     * Create nginx.config file in a user directory folder
     * @throws IOException
     */
    public void createNginxConfig() throws IOException {
        createNginxConfig(new File(Directories.getResourcesDir(), NGINX_CONFIG_TEMPLATE), NGINX_CONFIG_FILE);
    }

    public void createNginxConfig(File nginxTemplate, File nginxConfig) throws IOException {
        final EasyTravelConfig easytravel_config = EasyTravelConfig.read();
       
        if (!nginxConfig.exists() || easytravel_config.nginxWebServerUsesGeneratedHttpdConfig) {

            // load nginx.conf.template file
            String templateString = FileUtils.readFileToString(nginxTemplate);

            // replace placeholders in template file
            String nginxConfigString = replacePlaceholders(templateString);

            // store nginx.conf
            FileUtils.writeStringToFile(nginxConfig, nginxConfigString);
        }

    }

    /**
     * The default set of properties for nginx configuration template file
     *  @author cwpl-rorzecho
     */
     private class NginxConfigProperties extends Properties {
        /**
		 * 
		 */
		private static final long serialVersionUID = -2306983809821233118L;
		private final String EASYTRAVEL_LOG_PATH = Directories.getLogDir().getAbsolutePath();
        private final String EASYTRAVEL_TEPM_PATH = Directories.getTempDir().getAbsolutePath();

        public NginxConfigProperties() {
            switch (NginxUtils.pickNginxExecutable()) {
                case nginx32:
                    this.put(NGINX_MIME_TYPES_PLACEHOLDER, NginxUtils.NGINX_INSTALL_PATH_32 + "/conf");
                    break;
                case nginx64:
                    this.put(NGINX_MIME_TYPES_PLACEHOLDER, NginxUtils.NGINX_INSTALL_PATH_64 + "/conf");
                    break;
            }
            
            String customerFrontendHostPort = ProcedureFactory.getHostOrLocal(Constants.Procedures.CUSTOMER_FRONTEND_ID)+":"+EasyTravelConfig.read().frontendPortRangeStart;
            this.put(NGINX_CUSTOMER_FRONTEND_HOST_PORT_PLACEHOLDER, customerFrontendHostPort);
            this.put(NGINX_HOST_PLACEHOLDER, getPropertyString(NGINX_HOST_PLACEHOLDER));
            this.put(NGINX_PORT_PLACEHOLDER, getPropertyString(NGINX_PORT_PLACEHOLDER));
            
            String b2bFrontendHostPort = ProcedureFactory.getHostOrLocal(Constants.Procedures.B2B_FRONTEND_ID)+":"+EasyTravelConfig.read().b2bFrontendPortRangeStart;
            this.put(NGINX_B2B_HOST_PORT_PLACEHOLDER, b2bFrontendHostPort);
            this.put(NGINX_B2B_HOST_PLACEHOLDER, getPropertyString(NGINX_B2B_HOST_PLACEHOLDER));
            this.put(NGINX_B2B_PORT_PLACEHOLDER, getPropertyString(NGINX_B2B_PORT_PLACEHOLDER));
            
            String angularFrontendHostPort = ProcedureFactory.getHostOrLocal(Constants.Procedures.ANGULAR_FRONTEND_ID)+":"+EasyTravelConfig.read().angularFrontendPortRangeStart;
            this.put(NGINX_ANGULAR_HOST_PORT_PLACEHOLDER, angularFrontendHostPort);
            this.put(NGINX_ANGULAR_HOST_PLACEHOLDER, getPropertyString(NGINX_ANGULAR_HOST_PLACEHOLDER));
            this.put(NGINX_ANGULAR_PORT_PLACEHOLDER, getPropertyString(NGINX_ANGULAR_PORT_PLACEHOLDER));
            
            this.put(NGINX_ACCESS_LOG_PATH_PLACEHOLDER, EASYTRAVEL_LOG_PATH);
            this.put(NGINX_ERROR_LOG_PATH_PLACEHOLDER, EASYTRAVEL_LOG_PATH);
            this.put(NGINX_PID_FILE_PATH_PLACEHOLDER, EASYTRAVEL_TEPM_PATH);
        }
    }

    public static String getPropertyString(String property) {
        final EasyTravelConfig config = EasyTravelConfig.read();
        String propertyString = MvelUtils.getPropertyString(property, config);
        if (propertyString != null) {
            return propertyString;
        } else {
            throw new IllegalStateException(TextUtils.merge("The property {0} cannot be found in {1} class", property, config.getClass().getName()));
        }
    }
}
