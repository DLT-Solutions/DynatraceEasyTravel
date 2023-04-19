package com.dynatrace.easytravel.launcher.engine;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.nginx.NginxConfSetup;
import com.dynatrace.easytravel.launcher.nginx.NginxUtils;
import com.dynatrace.easytravel.launcher.process.Process;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author cwpl-rorzecho
 */
public class NginxWebserverProcedureTest {

    private static final Logger LOGGER = LoggerFactory.make();

    public static final String TEST_DATA_PATH = "../TravelTest/testdata";

    static {
        System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");
        LOGGER.warn("Using files from: " + System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION));
    }

    @Test
    public void testDtAgentConfigAPM() throws CorruptInstallationException, IOException, ConfigurationException {
        final EasyTravelConfig config = EasyTravelConfig.read();

        assertThat(config.nginxWebServerAgent, is("auto") );

        assertThat(config.nginxWebServerEnvArgs.length, is(0) );

        assertThat("Default config for apmServerDefault", config.apmServerDefault, isOneOf(InstallationType.APM, InstallationType.Classic, InstallationType.Both));

        DefaultProcedureMapping nginxMapping = new DefaultProcedureMapping("nginx");

        NginxWebserverProcedure nginxWebserverProcedure = new NginxWebserverProcedure(nginxMapping);

        assertThat("Procedure technology is not correct", nginxWebserverProcedure.getTechnology(), is(Technology.NGINX));

        assertThat("Procddure name is not correct", nginxWebserverProcedure.getName(), is("Nginx Webserver"));

        DtAgentConfig agentConfig = nginxWebserverProcedure.getAgentConfig();

        assertThat("Cannot create dtAgentConfig", agentConfig, notNullValue());

        assertThat("Dt agent path for APM mode should not be available", agentConfig.getAgentPath(Technology.NGINX), nullValue());

    }

    @Test
    public void testDtAgentConfigClassic() throws CorruptInstallationException, IOException, ConfigurationException {
        final EasyTravelConfig config = EasyTravelConfig.read();

        assertThat("Default Dt agent detection", config.nginxWebServerAgent, is("auto") );

        assertThat("Default nginxWebServerEnvArgs", config.nginxWebServerEnvArgs.length, is(0) );

        assertThat("Default config for apmServerDefault", config.apmServerDefault, isOneOf(InstallationType.APM, InstallationType.Classic, InstallationType.Both));

        Properties prop = new Properties();
        prop.setProperty("config.apmServerDefault", "Classic");
        config.enhance(prop);

        assertThat("Config for apmServerDefault", config.apmServerDefault, is(InstallationType.Classic));

        DefaultProcedureMapping nginxMapping = new DefaultProcedureMapping("nginx");

        NginxWebserverProcedure nginxWebserverProcedure = new NginxWebserverProcedure(nginxMapping);

        DtAgentConfig agentConfig = nginxWebserverProcedure.getAgentConfig();

        assertThat("Cannot create dtAgentConfig", agentConfig, notNullValue());

        try {
            agentConfig.getAgentPath(Technology.NGINX);
        } catch (Exception ex) {
            assertThat(ex, instanceOf(ConfigurationException.class));
            assertThat(ex.getMessage(), is("No agent found: null, setting: auto, technology: NGINX, install-dirs: []"));
        }

        assertThat("DT agent name", agentConfig.getAgentName(), isEmptyOrNullString());

        // fake agent path
        String nginxWebServerAgentPath = "/home/demouser";

        prop.setProperty("config.nginxWebServerAgent", nginxWebServerAgentPath);
        config.enhance(prop);

        assertThat(config.nginxWebServerAgent, is(nginxWebServerAgentPath));

        DtAgentConfig dtAgentConfig = new DtAgentConfig(null, config.nginxWebServerAgent, null, config.nginxWebServerEnvArgs);
        nginxWebserverProcedure.setDtAgentConfig(dtAgentConfig);

        try {
            dtAgentConfig.getAgentPath(Technology.NGINX);
        } catch (Exception ex) {
            assertThat(ex, instanceOf(ConfigurationException.class));
            assertThat(ex.getMessage(), is("The configured agent at '/home/demouser' does not exist and no other agent could be found."));
        }

        // change nginxWebserverEnvArgs
        prop.setProperty("config.nginxWebServerEnvArgs", "key=val");
        config.enhance(prop);

        nginxWebserverProcedure.setDtAgentConfig(new DtAgentConfig(null, config.nginxWebServerAgent, null, config.nginxWebServerEnvArgs));

        Process process = nginxWebserverProcedure.getProcess();

        assertThat("Get nginx process", process, notNullValue());

        assertThat("Get process dtAgetnConfig", nginxWebserverProcedure.getAgentConfig(), notNullValue());

        assertThat(nginxWebserverProcedure.getAgentConfig().getEnvironmentArgs().size(), is(not(0)));

        assertThat(nginxWebserverProcedure.getAgentConfig().getEnvironmentArgs().get("key"), is("val"));

        EasyTravelConfig.resetSingleton();
    }

    @Test
    public void testRetrieveNginxContent() throws CorruptInstallationException, IOException, InterruptedException {
        final EasyTravelConfig config = EasyTravelConfig.read();

        String FREE_PORT = String.valueOf(findFreePort());

        Properties prop = new Properties();
        prop.setProperty("config.disableFQDN", "false");
        prop.setProperty("config.apmServerDefault", InstallationType.APM.name());
        config.enhance(prop);

        NginxWebserverProcedure nginxWebserverProcedure = new NginxWebserverProcedure(new DefaultProcedureMapping("nginx"));

        assertThat("Cannot create nginxWebserverProcedure", nginxWebserverProcedure, notNullValue());

        assertThat("No nginx port property defined", nginxWebserverProcedure.getPortPropertyName(), is("nginxWebServerPort"));

        assertThat("Different nginx port number", nginxWebserverProcedure.getPort(), is(8079));

        prop.setProperty("config.nginxWebServerPort", FREE_PORT);
        config.enhance(prop);

        String nginxFrontendPublicUrl = nginxWebserverProcedure.getURIDNS();

        assertThat("No Domain name detected", nginxFrontendPublicUrl, containsString("http://"));

        assertThat("Nginx working dir has changed", nginxWebserverProcedure.getWorkingDir(), is("nginx"));

        assertThat("Parent dir/executeable file has changed", nginxWebserverProcedure.getExecutable(null), is("nginx/nginx64"));

        NginxConfSetup nginxConfSetup = new NginxConfSetup();

        assertThat(nginxConfSetup, notNullValue());

        Properties nginxConfigProperties = nginxConfSetup.getNginxConfigProperties();

        // change default log paths
        nginxConfigProperties.setProperty(NginxConfSetup.NGINX_ACCESS_LOG_PATH_PLACEHOLDER, NginxUtils.NGINX_INSTALL_PATH_64 + "/logs");
        nginxConfigProperties.setProperty(NginxConfSetup.NGINX_ERROR_LOG_PATH_PLACEHOLDER, NginxUtils.NGINX_INSTALL_PATH_64 + "/logs");
        nginxConfigProperties.setProperty(NginxConfSetup.NGINX_PID_FILE_PATH_PLACEHOLDER, NginxUtils.NGINX_INSTALL_PATH_64 + "/logs");

        nginxConfSetup = new NginxConfSetup(nginxConfigProperties);

        // create nginx.conf
        nginxConfSetup.createNginxConfig(new File(TEST_DATA_PATH, NginxConfSetup.NGINX_CONFIG_TEMPLATE), new File(TEST_DATA_PATH, NginxConfSetup.NGINX_CONFIG));

        File file = new File(TEST_DATA_PATH, "/nginx.conf");

        assertThat("No nginx.conf file to run NginxWebserverProcedure", file.exists(), is(true));

        nginxWebserverProcedure.setNginxConfSetup(nginxConfSetup);

        nginxWebserverProcedure.applySettings("-c", new File(TEST_DATA_PATH, "/nginx.conf").getAbsolutePath(), "-p", new File(NginxUtils.NGINX_INSTALL_PATH_64).getAbsolutePath() );

        nginxWebserverProcedure.run();

        Thread.sleep(3000);

        try {
            System.out.println("Retrieve data for nginxFrontendPublicUrl: " + nginxFrontendPublicUrl);
            UrlUtils.retrieveData(nginxFrontendPublicUrl);
        } catch (IOException e) {
            // when there is no Customer Frontend available behind nginx webserver the HTTP 502 status code is returned
            assertThat("No nginx process available", e.getMessage(), containsString("Error 502"));
        }

        try {
            for (int i = 0; i < 3; i++) {
                assertThat("Nginx is not operating", nginxWebserverProcedure.isOperating(), is(true));
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            nginxWebserverProcedure.stop();
            assertThat("Nginx is still operating", nginxWebserverProcedure.isOperating(), is(false));
        }


        FileUtils.deleteQuietly(file);
        EasyTravelConfig.resetSingleton();
    }

    private static int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
