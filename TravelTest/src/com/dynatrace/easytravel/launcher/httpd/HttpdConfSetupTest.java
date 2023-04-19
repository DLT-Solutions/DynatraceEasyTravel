package com.dynatrace.easytravel.launcher.httpd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.TestUtil;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.launcher.engine.ProcedureFactory;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;
import com.dynatrace.easytravel.utils.TestEnvironment;
import com.dynatrace.easytravel.utils.TestHelpers;

public class HttpdConfSetupTest {
	static {
		TestUtil.setInstallDirCorrection();
    }
	
	private static final String EASYTRAVEL_CONFIG_PATH = Directories.getConfigDir().getAbsolutePath();

	private File file;

	@Before
	public void setUp() throws Exception {
		file = new File(EASYTRAVEL_CONFIG_PATH, "/httpd.conf");
		assertTrue(!file.exists() || file.delete());

        FileUtils.copyDirectory(new File(TestEnvironment.ABS_TEST_DATA_PATH, "../../ThirdPartyLibraries/Apache/ApacheHTTP/plain_conf"),
                new File(EASYTRAVEL_CONFIG_PATH));

		assertTrue("plain_http.conf not found in directory " + EASYTRAVEL_CONFIG_PATH,
				new File(EASYTRAVEL_CONFIG_PATH + "/plain_httpd.conf").exists());
	}

	@After
	public void tearDown() {
		assertTrue(!file.exists() || file.delete());

		// we modify the config for some tests, ensure that we reset it at the end of the test
		EasyTravelConfig.resetSingleton();
	}

	@Test
	public void testWrite() throws IOException {
		HttpdConfSetup.write(createDtAgentConfig(), false);

		assertTrue(file.exists());

		String result = FileUtils.readFileToString(file, "utf-8");
		EasyTravelConfig config = EasyTravelConfig.read();
		TestHelpers.assertContains(result,
				"Listen " + config.apacheWebServerPort,
				"Listen " + config.apacheWebServerB2bPort,
				(config.apacheWebServerProxyPort > 0 ? "Listen " + config.apacheWebServerProxyPort : ""),
				(config.apacheWebServerStatusPort > 0 ? "Listen " + config.apacheWebServerStatusPort : ""),
				(config.apacheWebServerStatusPort > 0 ? "# enable Apache Status page at http://localhost:" + config.apacheWebServerStatusPort + "/server-status" : ""),
				"DocumentRoot ",
				"ServerName \"EasyTravelWebserver\"",
				"LogFormat ",
				"BalancerMember ajp://",
				"VirtualHost *:" + config.apacheWebServerPort,
				"VirtualHost *:" + config.apacheWebServerB2bPort,
				(config.apacheWebServerProxyPort > 0 ? "VirtualHost *:" + config.apacheWebServerProxyPort : ""),
				"ErrorLog "
				);
		
        TestHelpers.assertNotContains(result, "CustomLog");
	}
	
	@Test
    public void testWriteIncludeServerStatus() throws IOException {
		int STATUS_PORT = 8037;
		EasyTravelConfig config = EasyTravelConfig.read();
		
		try {
			config.apacheWebServerStatusPort = STATUS_PORT;
	
	        HttpdConfSetup.write(createDtAgentConfig(), false);
	
	        assertTrue(file.exists());
	        String result = FileUtils.readFileToString(file, "utf-8");
	
	        TestHelpers.assertContains(result,
	        		"Listen " + STATUS_PORT,
	        		"# enable Apache Status page at http://localhost:" + STATUS_PORT + "/server-status",
	        		"# server info page at http://localhost:" + STATUS_PORT + "/server-info",
	        		"# and the Load Balancer page at http://localhost:" + STATUS_PORT + "/balancer-manager",
	        		"ExtendedStatus On",
	        		"LoadModule status_module modules/mod_status.so",
	        		"LoadModule info_module modules/mod_info.so",
	        		"<Location /server-status>",
	        		"<Location /server-info>",
	        		"<Location /balancer-manager>",
	        		"SetHandler server-status",
	        		"SetHandler server-info",
	        		"SetHandler balancer-manager");
		}
		finally {
			EasyTravelConfig.resetSingleton();
		}
    }
	
	@Test
    public void testWriteDontIncludeVirtualHostWhenApacheProxyEmpty() throws IOException {
		EasyTravelConfig config = EasyTravelConfig.read();
		
		try {
			config.apacheWebServerProxyPort = 0;
	
	        HttpdConfSetup.write(createDtAgentConfig(), false);
	
	        assertTrue(file.exists());
	        String result = FileUtils.readFileToString(file, "utf-8");
	
	        TestHelpers.assertNotContains(result, "RequestHeader unset Accept-Encoding", "ProxyRequests On");
		}
		finally {
			EasyTravelConfig.resetSingleton();
		}
    }
	
	@Test
    public void testWriteDontIncludeServerStatus() throws IOException {
		EasyTravelConfig config = EasyTravelConfig.read();
		
		try {
			config.apacheWebServerStatusPort = 0;
	
	        HttpdConfSetup.write(createDtAgentConfig(), false);
	
	        assertTrue(file.exists());
	        String result = FileUtils.readFileToString(file, "utf-8");
	
	        TestHelpers.assertNotContains(result,
	        		"LoadModule status_module modules/mod_status.so",
	        		"LoadModule info_module modules/mod_info.so",
	        		"server-status",
	        		"server-info",
	        		"balancer-manager");
		}
		finally {
			EasyTravelConfig.resetSingleton();
		}
    }

	@Test
	public void testWriteApacheSlowDownModule() throws IOException {
		ApacheConf apacheConf = new ApacheConf();
		apacheConf.setApacheSlowDown(true);

		HttpdConfSetup.write(createDtAgentConfig(), apacheConf);

		assertTrue(file.exists());
		String result = FileUtils.readFileToString(file, "utf-8");

		TestHelpers.assertContains(result,"LoadModule ext_filter_module modules/mod_ext_filter.so");
	}

	@Test
	public void testWriteApacheSlowDownFilterDefine() throws IOException {
		OperatingSystem USED_OS = OperatingSystem.pickUp();

		ApacheConf apacheConf = new ApacheConf();
		apacheConf.setApacheSlowDown(true);

		HttpdConfSetup.write(createDtAgentConfig(), apacheConf);

		assertTrue(file.exists());
		String result = FileUtils.readFileToString(file, "utf-8");

		TestHelpers.assertContains(result, "ExtFilterDefine");
		TestHelpers.assertContains(result, "slowDownInput");
		TestHelpers.assertContains(result, "mode=input");

		if (USED_OS == OperatingSystem.WINDOWS) {
			TestHelpers.assertContains(result, "javaw.exe -jar");
		} else if (USED_OS == OperatingSystem.LINUX) {
			TestHelpers.assertContains(result, "java -jar");
		}

		TestHelpers.assertContains(result, "com.dynatrace.easytravel.plugin.");
		TestHelpers.assertContains(result, ExtFilterDefineConfig.CMD);
	}

    @Test
    public void testModRewriteModule() throws IOException {
        ApacheConf apacheConf = new ApacheConf();
        apacheConf.setApacheSlowDown(true);

        HttpdConfSetup.write(createDtAgentConfig(), apacheConf);

        assertTrue(file.exists());
        String result = FileUtils.readFileToString(file, "utf-8");

        TestHelpers.assertContains(result,"LoadModule rewrite_module modules/mod_rewrite.so");
    }

    @Test
    public void testModRewriteModuleEnableForVirtualHost() throws IOException {
        ApacheConf apacheConf = new ApacheConf();
        apacheConf.setApacheSlowDown(true);

        HttpdConfSetup.write(createDtAgentConfig(), apacheConf);

        assertTrue(file.exists());
        String result = FileUtils.readFileToString(file, "utf-8");

        TestHelpers.assertContains(result,"RewriteEngine On");
        TestHelpers.assertContains(result,"RewriteOptions Inherit");
    }

    @Test
    public void testModRewriteRules() throws IOException {
        ApacheConf apacheConf = new ApacheConf();
        apacheConf.setApacheSlowDown(true);

        HttpdConfSetup.write(createDtAgentConfig(), apacheConf);

        assertTrue(file.exists());
        String result = FileUtils.readFileToString(file, "utf-8");

        TestHelpers.assertContains(result,"RewriteRule /about /about-orange.jsf [PT]");
        TestHelpers.assertContains(result,"RewriteRule /contact /contact-orange.jsf [PT]");
        TestHelpers.assertContains(result,"RewriteRule /seo /seo-orange.jsf [PT]");
    }
    
    //========================================
	// Tests of explicit Apache agent configuration - START
	//========================================
	
	@Test
	public void testExplicitAgentAPM() throws IOException {
		
		DummyAgentDLL dummyAgentDLL = new DummyAgentDLL();
		dummyAgentDLL.createDummyAgentDLL();

		//========================================
		// Set installation type and Apache
		// explicit agent configuration.
		//========================================
		
		InstallationType savInstType = DtVersionDetector.getInstallationType();
		DtVersionDetector.enforceInstallationType(InstallationType.APM);
		
		// Set configuration to an explicit Apache agent path
		EasyTravelConfig CONFIG = EasyTravelConfig.read();
		CONFIG.apacheWebServerAgent = dummyAgentDLL.getMyTestPath();

		//========================================
		// Run the test proper: to see if
		// we create the correct httpd.conf,
		// based on the installation type.
		//========================================
		
		try {
			// Write out httpd.conf: it should use the above Apache agent setting,
			// but the module name should be dependent on installationType.
			HttpdConfSetup.write(createDtAgentConfig(), false);

			assertTrue(file.exists()); // httpd.conf
			String result = FileUtils.readFileToString(file, "utf-8");
			TestHelpers.assertContains(result, "LoadModule ruxitagent_module \"" + dummyAgentDLL.getMyTestPath() +"\"");
		} finally {
			// tidy up
			EasyTravelConfig.resetSingleton();
			DtVersionDetector.enforceInstallationType(savInstType);
			dummyAgentDLL.destroyDummyAgentDLL();
		}
	}

	@Test
	public void testExplicitAgentClassic() throws IOException {
		
		DummyAgentDLL dummyAgentDLL = new DummyAgentDLL();
		dummyAgentDLL.createDummyAgentDLL();

		//========================================
		// Set installation type and Apache
		// explicit agent configuration.
		//========================================
		
		InstallationType savInstType = DtVersionDetector.getInstallationType();
		DtVersionDetector.enforceInstallationType(InstallationType.Classic);
		
		// Set configuration to an explicit Apache agent path
		EasyTravelConfig CONFIG = EasyTravelConfig.read();
		CONFIG.apacheWebServerAgent = dummyAgentDLL.getMyTestPath();

		//========================================
		// Run the test proper: to see if
		// we create the correct httpd.conf,
		// based on the installation type.
		//========================================
		
		try {
			// Write out httpd.conf: it should use the above Apache agent setting,
			// but the module name should be dependent on installationType.
			HttpdConfSetup.write(createDtAgentConfig(), false);

			assertTrue(file.exists()); // httpd.conf
			String result = FileUtils.readFileToString(file, "utf-8");
			TestHelpers.assertContains(result, "LoadModule dtagent_module \"" + dummyAgentDLL.getMyTestPath() +"\"");
		} finally {
			// tidy up
			EasyTravelConfig.resetSingleton();
			DtVersionDetector.enforceInstallationType(savInstType);
			dummyAgentDLL.destroyDummyAgentDLL();
		}
	}

	//========================================
	// Tests of explicit Apache agent configuration - END
	//========================================
	
	@Test
	public void testWriteApacheSlowDownFilterServerWaitTime() throws IOException {
		ApacheConf apacheConf = new ApacheConf();
		apacheConf.setApacheSlowDown(true);

		HttpdConfSetup.write(createDtAgentConfig(), apacheConf);
		assertTrue(file.exists());

		EasyTravelConfig config = EasyTravelConfig.read();
		String result = FileUtils.readFileToString(file, "utf-8");

		TestHelpers.assertContains(result, "com.dynatrace.easytravel.plugin.");

		TestHelpers.assertContains(result, ExtFilterDefineConfig.SLOW_DOWN_PLUGIN + " " + config.apacheWebServerWaitTime);

	}

	@Test
	public void testWriteApacheSlowDownLocation() throws IOException {
		ApacheConf apacheConf = new ApacheConf();
		apacheConf.setApacheSlowDown(true);

		HttpdConfSetup.write(createDtAgentConfig(), apacheConf);
		assertTrue(file.exists());

		String result = FileUtils.readFileToString(file, "utf-8");

		TestHelpers.assertContains(result, "<Location />");
		TestHelpers.assertContains(result, "SetInputFilter");
		TestHelpers.assertContains(result, "slowDownInput");
		TestHelpers.assertContains(result, "</Location>");
	}

	@Test
	public void testWriteApacheSlowDownDisabled() throws IOException {
		ApacheConf apacheConf = new ApacheConf();
		apacheConf.setApacheSlowDown(false);

		HttpdConfSetup.write(createDtAgentConfig(), apacheConf);
		assertTrue(file.exists());

		String result = FileUtils.readFileToString(file, "utf-8");

		TestHelpers.assertNotContains(result,"LoadModule ext_filter_module modules/mod_ext_filter.so");
		TestHelpers.assertNotContains(result,"ExtFilterDefine");
		TestHelpers.assertNotContains(result,"<Location />");
		TestHelpers.assertNotContains(result, "<" + "//"+ "Location>");
	}

	@Test
	public void testWriteEqualPorts() throws IOException {
		EasyTravelConfig.read().apacheWebServerB2bPort = EasyTravelConfig.read().apacheWebServerPort;

		HttpdConfSetup.write(createDtAgentConfig(), false);

		assertTrue(file.exists());

		String result = FileUtils.readFileToString(file, "utf-8");
		EasyTravelConfig config = EasyTravelConfig.read();
		TestHelpers.assertContains(result,
				"Listen " + config.apacheWebServerPort,
				(config.apacheWebServerProxyPort > 0 ? "Listen " + config.apacheWebServerProxyPort : "")
				);
	}

	@Test
	public void testAdjustPortsAndRange() throws IOException {
		EasyTravelConfig.read().frontendAjpPortRangeStart = 9820;
		EasyTravelConfig.read().frontendAjpPortRangeEnd = 9830;

		HttpdConfSetup.write(createDtAgentConfig(), false);

		assertTrue(file.exists());

		String result = FileUtils.readFileToString(file, "utf-8");
		TestHelpers.assertContains(result,
				"BalancerMember ajp://localhost:9820 route=jvmRoute-9820",
				"BalancerMember ajp://localhost:9821 route=jvmRoute-9821",
				"BalancerMember ajp://localhost:9822 route=jvmRoute-9822",
				"BalancerMember ajp://localhost:9823 route=jvmRoute-9823",
				"BalancerMember ajp://localhost:9824 route=jvmRoute-9824",
				"BalancerMember ajp://localhost:9825 route=jvmRoute-9825",
				"BalancerMember ajp://localhost:9826 route=jvmRoute-9826",
				"BalancerMember ajp://localhost:9827 route=jvmRoute-9827",
				"BalancerMember ajp://localhost:9828 route=jvmRoute-9828",
				"BalancerMember ajp://localhost:9829 route=jvmRoute-9829",
				"BalancerMember ajp://localhost:9830 route=jvmRoute-9830"
				);
	}

	@Test
	public void testRemoteFrontend() throws IOException {
		System.setProperty("com.dynatrace.easytravel.host.b2b_frontend", "myhost123");
		System.setProperty("com.dynatrace.easytravel.host.customer_frontend", "myhost124");
		try {
			// verify that this has the expected result
			String hostOrLocal = ProcedureFactory.getHostOrLocal(Constants.Procedures.B2B_FRONTEND_ID);
			assertEquals("had: " + hostOrLocal, "myhost123", hostOrLocal);
			hostOrLocal = ProcedureFactory.getHostOrLocal(Constants.Procedures.CUSTOMER_FRONTEND_ID);
			assertEquals("had: " + hostOrLocal, "myhost124", hostOrLocal);

			HttpdConfSetup.write(createDtAgentConfig(), false);
		} finally {
			System.clearProperty("com.dynatrace.easytravel.host.b2b_frontend");
			System.clearProperty("com.dynatrace.easytravel.host.customer_frontend");
		}

		assertTrue(file.exists());

		String result = FileUtils.readFileToString(file, "utf-8");
		TestHelpers.assertContains(result,
				// still contains all the localhost-entries
				"BalancerMember ajp://localhost:8280 route=jvmRoute-8280",
				"BalancerMember ajp://localhost:8281 route=jvmRoute-8281",
				"BalancerMember ajp://localhost:8282 route=jvmRoute-8282",
				"BalancerMember ajp://localhost:8283 route=jvmRoute-8283",
				"BalancerMember ajp://localhost:8284 route=jvmRoute-8284",
				"BalancerMember ajp://localhost:8285 route=jvmRoute-8285",
				"BalancerMember ajp://localhost:8286 route=jvmRoute-8286",
				"BalancerMember ajp://localhost:8287 route=jvmRoute-8287",
				"BalancerMember ajp://localhost:8288 route=jvmRoute-8288",
				"BalancerMember ajp://localhost:8289 route=jvmRoute-8289",
				"BalancerMember ajp://localhost:8290 route=jvmRoute-8290",

				// also contains entries for the remote hosts for b2b frontend
				"BalancerMember ajp://myhost123:8280 route=jvmRoute-8280",
				"BalancerMember ajp://myhost123:8281 route=jvmRoute-8281",
				"BalancerMember ajp://myhost123:8282 route=jvmRoute-8282",
				"BalancerMember ajp://myhost123:8283 route=jvmRoute-8283",
				"BalancerMember ajp://myhost123:8284 route=jvmRoute-8284",
				"BalancerMember ajp://myhost123:8285 route=jvmRoute-8285",
				"BalancerMember ajp://myhost123:8286 route=jvmRoute-8286",
				"BalancerMember ajp://myhost123:8287 route=jvmRoute-8287",
				"BalancerMember ajp://myhost123:8288 route=jvmRoute-8288",
				"BalancerMember ajp://myhost123:8289 route=jvmRoute-8289",
				"BalancerMember ajp://myhost123:8290 route=jvmRoute-8290",

				// also contains entries for the remote hosts for customer frontend
				"BalancerMember ajp://myhost124:8280 route=jvmRoute-8280",
				"BalancerMember ajp://myhost124:8281 route=jvmRoute-8281",
				"BalancerMember ajp://myhost124:8282 route=jvmRoute-8282",
				"BalancerMember ajp://myhost124:8283 route=jvmRoute-8283",
				"BalancerMember ajp://myhost124:8284 route=jvmRoute-8284",
				"BalancerMember ajp://myhost124:8285 route=jvmRoute-8285",
				"BalancerMember ajp://myhost124:8286 route=jvmRoute-8286",
				"BalancerMember ajp://myhost124:8287 route=jvmRoute-8287",
				"BalancerMember ajp://myhost124:8288 route=jvmRoute-8288",
				"BalancerMember ajp://myhost124:8289 route=jvmRoute-8289",
				"BalancerMember ajp://myhost124:8290 route=jvmRoute-8290"
				);
	}

	private static DtAgentConfig createDtAgentConfig() {
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		return new DtAgentConfig(null,
				EASYTRAVEL_CONFIG.apacheWebServerAgent,
				null,
				EASYTRAVEL_CONFIG.apacheWebServerEnvArgs);
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(HttpdConfSetup.class);
	}
	
    /** 
     * Check generation of RuxitAgentConfig entry
     * @throws IOException
     */
    @Test
    public void  testRuxitAgentProerties() throws IOException {
    	InstallationType savInstType = DtVersionDetector.getInstallationType();
		EasyTravelConfig config = EasyTravelConfig.read();
		DummyAgentDLL dummyAgentDLL = new DummyAgentDLL();
		
    	try {
    		//first test some situation where we don't expect RuxitAgentConfig
    		DtVersionDetector.enforceInstallationType(InstallationType.Classic);
    		config.apacheWebServerAgent = "auto";
    		assertNotContainsRuxitAgentConfig();

    		DtVersionDetector.enforceInstallationType(InstallationType.APM);
    		config.apacheWebServerAgent = "auto";
    		assertNotContainsRuxitAgentConfig();
    		
    		dummyAgentDLL.createDummyAgentDLL();
    		
    		DtVersionDetector.enforceInstallationType(InstallationType.Classic);
    		config.apacheWebServerAgent=dummyAgentDLL.getMyTestPath();
    		assertNotContainsRuxitAgentConfig();
    		    		
    		//now RuxitAgentConfig should be added
    		DtVersionDetector.enforceInstallationType(InstallationType.APM);
    		config.apacheWebServerAgent=dummyAgentDLL.getMyTestPath();
    		config.apmServerWebURL="http://somehost.clients.dynatrace.org:8020";
    		config.apmTenant="ruxitTenant";
    		config.apmTenantToken="ruxitToken";
    		
        	HttpdConfSetup.write(createDtAgentConfig(), false);        	
    		assertTrue(file.exists());    		
    		
    		String result = FileUtils.readFileToString(file, "utf-8");
    		TestHelpers.assertContains(result, "RuxitAgentConfig server=http://somehost.clients.dynatrace.org:8020,tenant=ruxitTenant,tenanttoken=ruxitToken");    		
    	} finally {
    		EasyTravelConfig.resetSingleton();
			DtVersionDetector.enforceInstallationType(savInstType);
			dummyAgentDLL.destroyDummyAgentDLL();
    	}
    }

	
	/**
	 * Generate a config file and check that it doesn't contain RuxitAgentConfig line 
     *
	 * @throws IOException 
	 */
	private void assertNotContainsRuxitAgentConfig() throws IOException {
    	HttpdConfSetup.write(createDtAgentConfig(), false);
		assertTrue(file.exists());
				
		String result = FileUtils.readFileToString(file, "utf-8");
		TestHelpers.assertNotContains(result, "RuxitAgentConfig");		
	}
	
	@Test
	public void testWirteMutexDiective() throws IOException {
		ApacheConf apacheConf = new ApacheConf();
		apacheConf.setApacheSlowDown(true);

		HttpdConfSetup.write(createDtAgentConfig(), apacheConf);

		assertTrue(file.exists());
		String result = FileUtils.readFileToString(file, "utf-8");

		if (OperatingSystem.pickUp().equals(OperatingSystem.WINDOWS)) {
			TestHelpers.assertNotContains(result,"Mutex posixsem");
		} else {
			TestHelpers.assertContains(result,"Mutex posixsem");
		}

	}
	
	@Test
	public void testDocumentRootDirectoryDirective() throws IOException {
		ApacheConf apacheConf = new ApacheConf();
		HttpdConfSetup.write(createDtAgentConfig(), apacheConf);
		
		assertTrue(file.exists());
		String result = FileUtils.readFileToString(file, "utf-8").replace("\n", "").replace("\r", "");
		TestHelpers.assertContains(result, getDocumentRootDirectoryDirective());
	}
	
	private String getDocumentRootDirectoryDirective() {
	
		String template = 	"<Directory \"%s\">Require all granted</Directory>";
		return String.format(template, ApacheHttpdUtils.INSTALL_APACHE_PATH + "/htdocs");
	}

}
