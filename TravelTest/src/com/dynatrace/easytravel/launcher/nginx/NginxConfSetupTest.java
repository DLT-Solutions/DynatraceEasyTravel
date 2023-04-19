package com.dynatrace.easytravel.launcher.nginx;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.TestHelpers;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import ch.qos.logback.classic.Logger;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author cwpl-rorzecho
 */
public class NginxConfSetupTest {

    private static final Logger LOGGER = LoggerFactory.make();

    static {
      System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");
      LOGGER.warn("Using files from: " + System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION));
    }

    public static final String TEST_DATA_PATH = "../TravelTest/testdata";
    
    private static File file = new File(TEST_DATA_PATH, "/nginx.conf");
    
    @Before
    public void setUp(){
		// Make sure there is no nginx config file.
		if (file.exists()) {
			FileUtils.deleteQuietly(file);
			assertFalse(file.exists());
		}
    }
    
    @After
    public void tearDown(){
    	FileUtils.deleteQuietly(file);
    }

    @Test
    public void nginxConfSetupTest() {

        String nginxConfTemplate = "worker_processes  1;\n" +
                "events {\n" +
                "    worker_connections  1024;\n" +
                "}\n" +
                "http {\n" +
                "    include       \"${nginxMimeTypes}/mime.types\";\n" +
                "    default_type  application/octet-stream;\n" +
                "    access_log  \"${nginxAccessLogPath}/nginx_access.log\";\n" +
                "    sendfile        on;\n" +
                "    keepalive_timeout  65;\n" +
                "    upstream frontend_balancer {\n" +
                "        server 127.0.0.1:8080;\n" +
                "        server 127.0.0.1:8081;\n" +
                "        server 127.0.0.1:8082;\n" +
                "        server 127.0.0.1:8083;\n" +
                "        server 127.0.0.1:8084;\n" +
                "        server 127.0.0.1:8085;\n" +
                "        server 127.0.0.1:8086;\n" +
                "        server 127.0.0.1:8087;\n" +
                "        server 127.0.0.1:8088;\n" +
                "        server 127.0.0.1:8089;\n" +
                "        server 127.0.0.1:8090;\n" +
                "    }\n" +
                "    server {\n" +
                "        listen       ${nginxWebServerPort};\n" +
                "        server_name  ${nginxWebServerHost};\n" +
                "        location / {\n" +
                "            proxy_pass http://frontend_balancer;\n" +
                "        }\n" +
                "        error_page   500 502 503 504  /50x.html;\n" +
                "        location = /50x.html {\n" +
                "            root   html;\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "error_log   \"${nginxErrorLogPath}/nginx_error.log\" info;\n" +
                "pid        \"${nginxPidFilePath}/nginx.pid\";";

        Properties nginxProperties = new Properties();
        nginxProperties.put(NginxConfSetup.NGINX_PORT_PLACEHOLDER, "xxx");
        nginxProperties.put(NginxConfSetup.NGINX_HOST_PLACEHOLDER, "localhost");
        nginxProperties.put(NginxConfSetup.NGINX_MIME_TYPES_PLACEHOLDER, "/home/demouser/easytravel-2.0.0-x64/nginx/nginx-eT-1.6.2-64/conf");
        nginxProperties.put(NginxConfSetup.NGINX_ACCESS_LOG_PATH_PLACEHOLDER, "/home/demouser/.dynaTrace/easyTravel 2.0.0/easyTravel/log");
        nginxProperties.put(NginxConfSetup.NGINX_ERROR_LOG_PATH_PLACEHOLDER, "/home/demouser/.dynaTrace/easyTravel 2.0.0/easyTravel/log");
        nginxProperties.put(NginxConfSetup.NGINX_PID_FILE_PATH_PLACEHOLDER, "/home/demouser/.dynaTrace/easyTravel 2.0.0/easyTravel/tmp");

        NginxConfSetup nginxConfSetup = new NginxConfSetup(nginxProperties);

        String result = nginxConfSetup.replacePlaceholders(nginxConfTemplate);

        System.out.println(result);

        TestHelpers.assertContains(result, "xxx", "localhost", "nginx-eT-1.6.2-64/conf", "easyTravel/log", "easyTravel/tmp");
    }

	@Test
	public void createNginxConfig() throws IOException {

		NginxConfSetup nginxConfSetup = new NginxConfSetup();

		EasyTravelConfig config = EasyTravelConfig.read();

		// ================================================================
		// Case where the nginx.conf does not already exist
		// and overwrite flag set to false.
		// ================================================================

		config.nginxWebServerUsesGeneratedHttpdConfig = false;

		// Create it.
		nginxConfSetup.createNginxConfig(new File(Directories.getResourcesDir(), NginxConfSetup.NGINX_CONFIG_TEMPLATE),
				new File(TEST_DATA_PATH, NginxConfSetup.NGINX_CONFIG));

		// See if it worked OK.
		file = new File(TEST_DATA_PATH, "/nginx.conf");
		assertTrue(file.exists());
		String result = FileUtils.readFileToString(file);
		TestHelpers.assertContains(result, "conf", "8079", "log", "tmp");

		// ================================================================
		// Case where the nginx.conf does not already exist
		// and overwrite flag set to true.
		// ================================================================

		config.nginxWebServerUsesGeneratedHttpdConfig = true;
		//assertFalse(dummyFile.exists());

		// Create it.
		nginxConfSetup.createNginxConfig(new File(Directories.getResourcesDir(), NginxConfSetup.NGINX_CONFIG_TEMPLATE),
				new File(TEST_DATA_PATH, NginxConfSetup.NGINX_CONFIG));

		// See if it worked OK.
		file = new File(TEST_DATA_PATH, "/nginx.conf");
		assertTrue(file.exists());
		result = FileUtils.readFileToString(file);
		TestHelpers.assertContains(result, "conf", "8079", "log", "tmp");

		// ================================================================
		// Case where the nginx.conf does already exist
		// and overwrite flag set to false.
		// ================================================================

		//assertFalse(dummyFile.exists());
		config.nginxWebServerUsesGeneratedHttpdConfig = false;

		// Pre-create it with dummy contents.
		FileUtils.writeStringToFile(new File(TEST_DATA_PATH, NginxConfSetup.NGINX_CONFIG),
				"dummy nginx config file contents");
		assertTrue(file.exists());

		// Confirm pre-creation worked correctly.
		file = new File(TEST_DATA_PATH, "/nginx.conf");
		assertTrue(file.exists());
		result = FileUtils.readFileToString(file);
		TestHelpers.assertContains(result, "dummy nginx config file contents");

		// Attempt to create it.
		nginxConfSetup.createNginxConfig(new File(Directories.getResourcesDir(), NginxConfSetup.NGINX_CONFIG_TEMPLATE),
				new File(TEST_DATA_PATH, NginxConfSetup.NGINX_CONFIG));

		// See that it failed.
		file = new File(TEST_DATA_PATH, "/nginx.conf");
		assertTrue(file.exists());
		result = FileUtils.readFileToString(file);
		TestHelpers.assertContains(result, "dummy nginx config file contents");

		// ================================================================
		// Case where the nginx.conf does already exist
		// and overwrite flag set to true.
		// ================================================================

		//assertFalse(dummyFile.exists());
		config.nginxWebServerUsesGeneratedHttpdConfig = true;

		// Pre-create it with dummy contents.
		FileUtils.writeStringToFile(new File(TEST_DATA_PATH, NginxConfSetup.NGINX_CONFIG),
				"dummy nginx config file contents");
		assertTrue(file.exists());

		// Confirm pre-creation worked correctly.
		file = new File(TEST_DATA_PATH, "/nginx.conf");
		assertTrue(file.exists());
		result = FileUtils.readFileToString(file);
		TestHelpers.assertContains(result, "dummy nginx config file contents");

		// Attempt to create it.
		nginxConfSetup.createNginxConfig(new File(Directories.getResourcesDir(), NginxConfSetup.NGINX_CONFIG_TEMPLATE),
				new File(TEST_DATA_PATH, NginxConfSetup.NGINX_CONFIG));

		// See if it worked OK.
		file = new File(TEST_DATA_PATH, "/nginx.conf");
		assertTrue(file.exists());
		result = FileUtils.readFileToString(file);
		TestHelpers.assertContains(result, "conf", "8079", "log", "tmp");
		
		EasyTravelConfig.resetSingleton();
	}
	
	@Test
	public void nginxRemoteFrontendTest() throws IOException {		
		try {
			System.setProperty("com.dynatrace.easytravel.host.customer_frontend", "myhostwithport");
			EasyTravelConfig config = EasyTravelConfig.read();
			config.frontendPortRangeStart = 6666;
			config.nginxWebServerPort = 7777;
			config.nginxWebServerHost = "customerservername";
			
			checkConfFileContents("myhostwithport", "6666", "7777", "customerservername");
		} finally {
			System.clearProperty("com.dynatrace.easytravel.host.customer_frontend");
			EasyTravelConfig.resetSingleton();		
		}
	}
	
	@Test
	public void nginxRemoteAngularFrontendTest() throws IOException {		
		try {
			System.setProperty("com.dynatrace.easytravel.host.customer_frontend_rest", "myangularhost");
			EasyTravelConfig config = EasyTravelConfig.read();
			config.angularFrontendPortRangeStart = 3333;
			config.nginxWebServerAngularPort = 2222;
			config.nginxWebServerAngularHost = "customerangularservername";
			
			checkConfFileContents("myangularhost", "3333", "2222", "customerangularservername");
		} finally {
			System.clearProperty("com.dynatrace.easytravel.host.customer_frontend_rest");
			EasyTravelConfig.resetSingleton();		
		}
	}
		
	@Test
	public void nginxB2BTest() throws IOException {		
		try{
			System.setProperty("com.dynatrace.easytravel.host.b2b_frontend", "myb2bhostwithport");
			EasyTravelConfig config = EasyTravelConfig.read();
			config.b2bFrontendPortRangeStart = 8888;
			config.nginxWebServerB2bPort = 9999;
			config.nginxWebServerB2bHost = "b2bservername";
			
			checkConfFileContents("myb2bhostwithport", "8888", "9999", "b2bservername");			
		} finally {
			System.clearProperty("com.dynatrace.easytravel.host.b2b_frontend");
			EasyTravelConfig.resetSingleton();
		}
	}
	
	private void checkConfFileContents(String frontendPublicUrl, String frontenPort, String serverPort, String serverHost) throws IOException {
		NginxConfSetup nginxConfSetup = new NginxConfSetup();
		
		// Create it.
		nginxConfSetup.createNginxConfig(new File(Directories.getResourcesDir(), NginxConfSetup.NGINX_CONFIG_TEMPLATE),
			new File(TEST_DATA_PATH, NginxConfSetup.NGINX_CONFIG));
		
		// See if it worked OK.
		assertTrue(file.exists());
		String result = FileUtils.readFileToString(file);
		TestHelpers.assertContains(result, 
				MessageFormat.format("server {0}:{1};", frontendPublicUrl,frontenPort),
				MessageFormat.format("listen       {0}", serverPort),
				MessageFormat.format("server_name  {0}", serverHost)
				);
	}

}
