package com.dynatrace.easytravel.launcher.remote;

import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.*;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.ThirdPartyContentProxySelector;
import com.dynatrace.easytravel.config.Version;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.database.DatabaseBase;
import com.dynatrace.easytravel.integration.IntegrationTestBase;
import com.dynatrace.easytravel.launcher.engine.SingleProcedureBatch;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureSetting;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.net.UrlUtils.Availability;
import com.dynatrace.easytravel.utils.*;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPResponseRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD.Response;

public class RESTProcedureClientTest {
    private static final Logger LOGGER = LoggerFactory.make();

    static {
		// set system properties for installation location
		System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");

		LOGGER.info("Using files from: " + System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION));
    }

    private static final EasyTravelConfig CONFIG = EasyTravelConfig.read();

    private static final int TEST_COUNT = 10;

    private HttpServiceThread remoteController;
    private static final String HOSTNAME;
    static {
		try {
			assertNotNull(java.net.InetAddress.getLocalHost());
			String hostname = java.net.InetAddress.getLocalHost().getCanonicalHostName();
			assertNotNull(hostname);
			assertFalse(hostname.equals("localhost"));
			assertFalse(hostname.startsWith("127.0.0"));
			HOSTNAME = hostname;
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
		LoggerFactory.initLogging();

		TestHelpers.waitForPort(CONFIG.launcherHttpPort, 2000);
    }

	@Before
	public void setUp() throws Exception {
		// make sure all the required ports are available
		IntegrationTestBase.checkPort(CONFIG.launcherHttpPort);

		Runnable exitInDisplayThreadRunnable = new Runnable() {
	        @Override
	        public void run() {
	            throw new IllegalStateException("Should not be called in this test!");
	        }
	    };

	    remoteController = new HttpServiceThread(CONFIG.launcherHttpPort, exitInDisplayThreadRunnable);
        remoteController.start();

        LOGGER.info("Using local hostname: " + HOSTNAME + " and port: " + CONFIG.launcherHttpPort);

        // make sure we can reach the url now before doing the actual testing
        Availability checkRead = UrlUtils.checkRead("http://" + HOSTNAME  + ":" + CONFIG.launcherHttpPort + "/version", 2000);
        assertTrue("Had: " + checkRead, checkRead.isOK());
	}

	@After
	public void tearDown() throws InterruptedException {
		if(remoteController != null) {
			remoteController.stopService();
			remoteController = null;
		}

		TestHelpers.waitForPort(CONFIG.launcherHttpPort, 2000);
	}

	@Test
	public void test() {
		final DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID);

		final RESTProcedureClient client = new RESTProcedureClient(mapping, "127.0.0.1");

		assertEquals("127.0.0.1", client.getHost());
		assertEquals(mapping, client.getMapping());

		// check some things up-front
		assertFalse(client.agentFound());
		assertFalse(client.isInstrumentationSupported());

		// should be executed correctly
		String uuid = client.prepare();
		assertNotNull("Should get an UUID back, but did get null", uuid);
		State state = client.start();
		assertNotNull("Should get a state back, but got null", state);
		assertEquals(State.OPERATING, state);
		assertNotNull("Tried to convert from string to UUID: " + uuid,
				UUID.fromString(uuid));

		assertEquals("Expecting state OPERATING after successfull startup",
				State.OPERATING, client.currentState());

		assertEquals("Database running at: localhost:1527", client.getDetails());
		assertEquals("Procedure does not provide log", client.getLog());
		assertFalse(client.agentFound());
		assertFalse(client.isInstrumentationSupported());

		assertEquals("Should not be able to start a second time",
				State.FAILED, client.start());

		assertEquals("Should still run even if we tried to start a second time",
				State.OPERATING, client.currentState());

		assertEquals("Stopping should work here",
				"OK", client.stop());

		assertEquals("After stoppping we expect the state to be STOPPED",
				State.STOPPED, client.currentState());

		String stop = client.stop();
		assertTrue("Expected stopping a second time to return NOTOK but had: " + stop,
				stop.startsWith("NOTOK"));
	}

	@Test
	public void testVersion() throws IOException {
		EasyTravelConfig config = EasyTravelConfig.read();
		String version = UrlUtils.retrieveData("http://127.0.0.1:" + config.launcherHttpPort + "/version");

		assertEquals(Version.read().toString(), version);
    }

	@Test
	public void testStartRemoteProcedureOnOwnHostname() throws Exception {
		final DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID);

		final RESTProcedureClient client = new RESTProcedureClient(mapping, HOSTNAME);

        assertThat(HOSTNAME, is(equalToIgnoringCase(client.getHost())));
		assertEquals(mapping, client.getMapping());

		// set up BusinessBackend as remote procedure on the same host where we are running, but not "localhost"!
		String property = "com.dynatrace.easytravel.host." + mapping.getId().replace(" ", "_").toLowerCase();
		System.setProperty(property, HOSTNAME);
		try {
			// should be executed correctly
			String uuid = client.prepare();
			assertNotNull("Should get back a valid UUID, but had null, check logs",
					uuid);
			State state = client.start();
			assertNotNull("Should get a state back, but got null", state);
			assertEquals(State.OPERATING, state);
			assertNotNull("Tried to convert from string to UUID: " + uuid,
					UUID.fromString(uuid));

			assertEquals("Expecting state OPERATING after successfull startup",
					State.OPERATING, client.currentState());

			assertEquals("Should not be able to start a second time",
					State.FAILED, client.start());

			assertEquals("Should still run even if we tried to start a second time",
					State.OPERATING, client.currentState());

			assertEquals("Stopping should work here",
					"OK", client.stop());

			assertEquals("After stoppping we expect the state to be STOPPED",
					State.STOPPED, client.currentState());

			String stop = client.stop();
			assertTrue("Expected stopping a second time to return NOTOK but had: " + stop,
					stop.startsWith("NOTOK"));
		} finally {
			System.clearProperty(property);
		}
	}

	@Test
	public void testNoRemoteHostAvailable() {
        // simply shut down the server and see what happens in the calls
		remoteController.stopService();

        final DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID);

		final RESTProcedureClient client = new RESTProcedureClient(mapping, "127.0.0.1");

		assertEquals("127.0.0.1", client.getHost());
		assertEquals(mapping, client.getMapping());

        assertNull(client.prepare());

		assertNull("Expecting null return from start() without remote host",
				client.start());

		assertEquals("Expecting state UNKNOWN without remote host",
				State.UNKNOWN, client.currentState());

		String stop = client.stop();
		assertTrue("Expecting a NOTOK when stopping whithout remote host, but had: " + stop,
				stop.startsWith("NOTOK"));
	}

	@Test
	public void testManyTimes() {
		final DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID);

		final RESTProcedureClient client = new RESTProcedureClient(mapping, "127.0.0.1");

		assertEquals("127.0.0.1", client.getHost());
		assertEquals(mapping, client.getMapping());

		// should be executed correctly many times
		long start = System.currentTimeMillis();
		for(int i = 1;i <= TEST_COUNT;i++) {
			String uuid = client.prepare();
			if (i > 1) {
			    assertNull(uuid);
			}
			client.start();
			uuid = client.getUUID();
			assertNotNull("Should get an UUID back, but did get null", uuid);
			assertNotNull("Tried to convert from string to UUID: " + uuid,
					UUID.fromString(uuid));

			assertEquals("Expecting state OPERATING after successfull startup",
					State.OPERATING, client.currentState());

			assertEquals("Expecting state OPERATING after successfull startup",
					State.OPERATING, client.currentState());

			assertEquals("Stopping should work here",
					"OK", client.stop());

			long elapsed = System.currentTimeMillis() -start;
			LOGGER.info("Iteration " + i + " done, aprox. " + elapsed/i + "ms per iteration, " + elapsed + "ms overall.");
		}
	}

	// execute test in combinations of localhost/hostname and with and without proxy
	@Test
	public void test4kRESTLimitJLT57011HostnameProxy() throws Exception {
		executLimitTest(HOSTNAME, true);
	}

	@Test
	public void test4kRESTLimitJLT57011() throws Exception {
		executLimitTest("127.0.0.1", false);
	}

	@Test
	public void test4kRESTLimitJLT57011Proxy() throws Exception {
		executLimitTest("127.0.0.1", true);
	}

	@Test
	public void test4kRESTLimitJLT57011Localhost() throws Exception {
		executLimitTest("localhost", false);
	}

	@Test
	public void test4kRESTLimitJLT57011LocalhostProxy() throws Exception {
		executLimitTest("localhost", true);
	}

	@Test
	public void test4kRESTLimitJLT57011Hostname() throws Exception {
		executLimitTest(HOSTNAME, false);
	}

	private void executLimitTest(String host, boolean proxy) throws IOException {
		if(proxy) {
			ThirdPartyContentProxySelector.applyProxy();
		}

		LOGGER.info("Starting testing with host " + host + " and proxy " + proxy);
		try {
			final DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID);

			final RESTProcedureClient client = new RESTProcedureClient(mapping, host);
			client.setClientTimeouts(5000);

			// ensure that we do not run into the 10 sec refresh intervall in this test
			EasyTravelConfig.resetSingleton();

			// set some properties to very long strings to reach the 4k limit of the GET call
			EasyTravelConfig.read().wsmqChannel = StringUtils.repeat("x", 3000);
			EasyTravelConfig.read().wsmqGetQueueName = StringUtils.repeat("y", 3000);
			EasyTravelConfig.read().wsmqGetQueueName = StringUtils.repeat("z", 3000);

			assertEquals("Had: " + EasyTravelConfig.read().wsmqChannel,
					3000, EasyTravelConfig.read().wsmqChannel.length());

			// should be executed correctly without error
			String uuid = client.prepare();
			assertNotNull("Expect got get a UUID back, but had empty result which indicates a problem in the REST call, check logs for details", uuid);
		} finally {
			ThirdPartyContentProxySelector.clearProxy();
		}
	}

	@Test
	@Ignore("local test")
	public void test4kRESTLimitRemotehostJLT57011() {
		final DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.CREDIT_CARD_AUTH_UNIT_ID);

		final RESTProcedureClient client = new RESTProcedureClient(mapping, "172.16.108.109");

		// ensure that we do not run into the 10 sec refresh intervall in this test
		EasyTravelConfig.resetSingleton();

		// set some properties to very long strings to reach the 4k limit of the GET call
		/*EasyTravelConfig.read().wsmqChannel = StringUtils.repeat("x", 30000);
		EasyTravelConfig.read().wsmqGetQueueName = StringUtils.repeat("y", 30000);
		EasyTravelConfig.read().wsmqGetQueueName = StringUtils.repeat("z", 30000);

		assertEquals("Had: " + EasyTravelConfig.read().wsmqChannel,
				30000, EasyTravelConfig.read().wsmqChannel.length());*/

		// should be executed correctly without error
		String uuid = client.prepare();
		assertNotNull("Should get an UUID back, but did get null", uuid);
	}

	@Test
	@Ignore("local test")
	public void testGetURL() throws IOException {
		String str = UrlUtils.retrieveData(
				"http://172.16.108.109:1697/prepare/b2b%20frontend?property=config.apacheWebServerSimulatesFirewall:false&property=config.b2bFrontendIncreasePerMinute:5&property=config.dtServerWebPort:18020&property=config.internalDatabaseEnabled:true&property=config.thirdpartyUrl:http://localhost:8092/&property=config.dtServerPassword:admin&property=config.nonProxyHosts:*easytravel.com%7Clocalhost&property=config.dtClientWebURL:http://dstadler.dynatrace.local:8030/&property=config.frontendShutdownPortRangeStart:8180&property=config.enableMainframeDemo:false&property=config.dtServerWebURL:http://dstadler.dynatrace.local:18020/&property=config.internalDatabaseHost:dstadler.dynatrace.local&property=config.agent:C:%5Cdata%5Ctrunk%5Cjloadtrace%5Cagent%5Clib64%5Cdtagent.dll&property=config.cacheJourneyPictures:true&property=config.apacheWebServerEnvArgs:&property=config.weblauncherShutdownPort:8095&property=config.databasePassword:APP&property=remotingHost:dynasprint.dynatrace.local&property=config.b2bFrontendDir:dotNET/dotNetB2BFrontend&property=config.frontendAgentOptions:wait%3D5&property=config.customerFrontendStartLoad:10&property=config.b2bFrontendSystemProfile:dotNetFrontend_easyTravel&property=config.apacheWebServerHost:localhost&property=config.memcachedServerHost:localhost&property=config.weblauncherPort:8094&property=config.baseLoadMobileNativeRatio:0.1&property=config.baseLoadMobileBrowserRatio:0.1&property=config.launcherHttpPort:1697&property=config.wsmqUserId:user&property=config.backendSystemProfile:BusinessBackend_easyTravel&property=config.thirdpartyContextRoot:/&property=config.paymentBackendHost:dynasprint.dynatrace.local&property=config.backendAgentOptions:wait%3D5&property=config.dtServerUsername:admin&property=config.apacheWebServerProxyPort:8070&property=config.thirdpartyHost:localhost&property=config.paymentBackendAgentOptions:&property=config.thirdpartyShutdownPort:8192&property=config.b2bFrontendServerIIS:/system32/inetsrv/w3wp&property=config.wsmqQueueManagerName:MB7QMGR&property=config.proxyPort:8001&property=config.backendHost:dynasprint.dynatrace.local&property=config.mobileIncreasePerMinute:5&property=config.apacheWebServerB2bVirtualIp:127.0.0.3&property=config.wsmqPutQueueName:creditcardauthrequest&property=config.creditCardAuthorizationAgent:auto&property=config.backendJavaopts:-Xmx64m&property=config.baseLoadDefault:20&property=config.paymentBackendEnvArgs:DT_WAIT%3D5,COR_ENABLE_PROFILING%3D0x1,COR_PROFILER%3D%7BDA7CFC47-3E35-4c4e-B495-534F93B28683%7D&property=config.paymentBackendServer:dotNET/cassini20/UltiDevCassinWebServer2&property=config.apacheWebServerB2bHost:localhost&property=config.apacheWebServerUsesGeneratedHttpdConfig:true&property=config.mobileStartLoad:0&property=config.wsmqPort:1414&property=config.dotNetBackendWebServiceBaseDir:SECRET,DummyNativeApplication.NET,DotNetPaymentService,DatabaseCleanup&property=config.thirdpartyPort:8092&property=config.webServiceBaseDir:SECRET,-Xms64m,property=config.clusterNode:&property=config.b2bFrontendStartLoad:0&property=config.frontendAjpPortRangeEnd:8290&property=config.b2bFrontendEnvArgs:DT_WAIT%3D5,COR_ENABLE_PROFILING%3D0x1,COR_PROFILER%3D%7BDA7CFC47-3E35-4c4e-B495-534F93B28683%7D&property=config.customerFrontendIncreasePerMinute:5&property=config.wsmqHostName:localhost&property=config.apacheWebServerAgent:C:%5Cdata%5Ctrunk%5Cjloadtrace%5Cagent%5Clib%5Cmod_dtagent22.dll&property=config.backendAgent:auto&property=config.xDynaTraceHeaders:false&property=config.apacheWebServerB2bPort:8999&property=config.antAgent:auto&property=config.b2bFrontendPortRangeEnd:9009&property=config.memoryCacheSingleton:true&property=config.frontendShutdownPortRangeEnd:8190&property=config.frontendContextRoot:/&property=config.creditCardAuthorizationAgentOptions:&property=config.paymentBackendServerIIS:/system32/inetsrv/w3wp&property=config.autostart:&property=config.frontendPortRangeEnd:8090&property=config.creditCardAuthorizationEnvArgs:DT_WAIT%3D5&property=config.weblauncherContextRoot:/&property=config.b2bFrontendPortRangeStart:9000&property=config.antSystemProfilePrefix:Ant_easyTravel&property=config.thirdpartyWebappBase:webapp&property=config.frontendSystemProfile:CustomerFrontend_easyTravel%23%7B_port%7D&property=config.databaseUrl:jdbc:derby://dstadler.dynatrace.local:1527/easyTravelBusiness;create%3Dtrue&property=config.b2bFrontendAgentOptions:&property=config.proxyHost:cns-lnz.emea.cpwr.corp&property=config.frontendJavaopts:-Xmx160m&property=config.paymentBackendSystemProfile:dotNetBackend_easyTravel&property=config.backendShutdownPort:8191&property=config.b2bFrontendMaximumLoad:20&property=config.wsmqPassword:password&property=config.frontendAgent:auto&property=config.b2bFrontendServer:dotNET/cassini20/UltiDevCassinWebServer2&property=config.b2bFrontendPageToIdentify:Account/LogOn.aspx&property=config.webappBase:webapp&property=config.wsmqGetQueueName:creditcardauthresponse");
		assertNotNull(str);
	}

	@Test
	public void testStartFails() {
		final DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID);

		final RESTProcedureClient client = new RESTProcedureClient(mapping, "127.0.0.1");

		// should be executed correctly
		String uuid = client.prepare();
		assertNotNull("Should get an UUID back, but did get null", uuid);
		State state = client.start();
		assertEquals(State.OPERATING, state);
		assertNotNull("Tried to convert from string to UUID: " + uuid,
				UUID.fromString(uuid));

		assertEquals("Expecting state OPERATING after successfull startup",
				State.OPERATING, client.currentState());

		assertEquals("Should not be able to start a second time",
				State.FAILED, client.start());
	}

	@Test
	public void testStartSynchronousProcedure() throws IOException {
		DatabaseBase.setUpClass();

		try {
			final DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.DATABASE_CONTENT_CREATOR_ID);

			final RESTProcedureClient client = new RESTProcedureClient(mapping, "127.0.0.1");

			assertEquals("127.0.0.1", client.getHost());
			assertEquals(mapping, client.getMapping());

			// should be executed correctly
			String uuid = client.prepare();
			assertNotNull("Should get an UUID back, but did get null", uuid);
			State state = client.start();
			assertNotNull("Should get a state back, but got null", state);
			assertEquals(State.STOPPED, state);
			assertNotNull("Tried to convert from string to UUID: " + uuid,
					UUID.fromString(uuid));

			assertEquals("Expecting state STOPPED after successfull run",
					State.STOPPED, client.currentState());

			assertEquals("A synchronous procedure can be started multiple times",
					State.STOPPED, client.start());

			assertEquals("Still reports STOPPED",
					State.STOPPED, client.currentState());

			String stop = client.stop();
			assertTrue("Stopping does not work here as it is already STOPPED",
					stop.startsWith("NOTOK"));

			assertEquals("After stoppping we expect the state to be STOPPED",
					State.STOPPED, client.currentState());

			stop = client.stop();
			assertTrue("Expected stopping a second time to return NOTOK but had: " + stop,
					stop.startsWith("NOTOK"));
		} finally {
			DatabaseBase.tearDownClass();
		}
	}



	@Test
	public void testWithRemoteHostSetting() {
		final DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID);

		final RESTProcedureClient client = new RESTProcedureClient(mapping, "127.0.0.1");

		assertEquals("127.0.0.1", client.getHost());
		assertEquals(mapping, client.getMapping());

		System.setProperty("com.dynatrace.easytravel.host.additional", "dynasprint,dynalive");
		try {
			// should be executed correctly
			LOGGER.info("Now starting to prepare...");
			String uuid = client.prepare();
			assertNotNull("Should get an UUID back, but did get null", uuid);
			LOGGER.info("Now starting ...");
			State state = client.start();

			assertNotNull("Should get a state back, but got null", state);
			assertEquals(State.OPERATING, state);
			assertNotNull("Tried to convert from string to UUID: " + uuid,
					UUID.fromString(uuid));

			assertEquals("Expecting state OPERATING after successfull startup",
					State.OPERATING, client.currentState());

			LOGGER.info("Now trying to start a 2nd time...");
			assertEquals("Should not be able to start a second time",
					State.FAILED, client.start());
		} finally {
			System.clearProperty("com.dynatrace.easytravel.host.additional");
		}

		LOGGER.info("Now stopping...");
		assertEquals("Stopping should work here",
				"OK", client.stop());

		LOGGER.info("stopping done");
		assertEquals("After stoppping we expect the state to be STOPPED",
				State.STOPPED, client.currentState());
	}

	@Test
	public void testWithCustomSetting() throws IOException {
		final DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);

		mapping.addSetting(new DefaultProcedureSetting(Scenario.TYPE_SCENARIO_PROCEDURE_CONFIG, "config.backendAgent", "testvalue2"));

		final RESTProcedureClient client = new RESTProcedureClient(mapping, "127.0.0.1");

		// should be executed correctly
		String uuid = client.prepare();
		assertNotNull("Could not prepare procedure, check logs for details", uuid);

		Map<UUID, SingleProcedureBatch> batches = RESTProcedureControl.getBatches();
		assertNotNull(batches);
		SingleProcedureBatch procedure = batches.get(UUID.fromString(uuid));
		assertNotNull("Should get procedure with UUID: " + uuid + ", but got null", procedure);

		File propertyFile = procedure.getProcedure().getPropertyFile();
		assertNotNull("Did not get property file", propertyFile);
		assertTrue("Did not find: " + propertyFile, propertyFile.exists());

		String properties = FileUtils.readFileToString(propertyFile);
		assertTrue("File " + propertyFile + " (" + properties.length() + ") did not contain expected value 'testvalue2'", properties.contains("config.backendAgent=testvalue2"));
	}

	@Ignore
	@Test
	/*Test ignored. It seems that for testing .Net procedure integration tests would be better choice */
	public void testDotNetFrontendIISServer() throws IOException {
		EasyTravelConfig config = null;
		final DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.B2B_FRONTEND_ID);
		final RESTProcedureClient client = new RESTProcedureClient(mapping, "127.0.0.1");

		String uuid = client.prepare();
		assertNotNull("Should get an UUID back, but did get null", uuid);

		assertNotNull("Should get a state back, but got null", client.start());

		assertFalse("No IIS server available", client.isRunningOnIIS());

		MockRESTServer server = new MockRESTServer(new HTTPResponseRunnable() {
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				Response resp = new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "ok");
				resp.addHeader("Server","IIS");
				return resp;
			}
		});

		try {
			config = EasyTravelConfig.read();
			config.b2bFrontendPageToIdentify = "";
			config.b2bFrontendPortRangeStart = server.getPort();

			assertTrue("IIS server is available", client.isRunningOnIIS());
			assertEquals("OK", client.stop());
		}  finally {
			server.stop();
			EasyTravelConfig.resetSingleton();
		}
	}

	@Ignore("Test ignored. It seems that for testing .Net procedure integration tests would be better choice")
	@Test
	public void testDotNetPaymentIISServer() throws IOException {
		EasyTravelConfig config = null;
		final DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.PAYMENT_BACKEND_ID);
		final RESTProcedureClient client = new RESTProcedureClient(mapping, "127.0.0.1");

		String uuid = client.prepare();
		assertNotNull("Should get an UUID back, but did get null", uuid);

		assertNotNull("Should get a state back, but got null", client.start());

		assertFalse("No IIS server available", client.isRunningOnIIS());

		MockRESTServer server = new MockRESTServer(new HTTPResponseRunnable() {
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				Response resp = new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "ok");
				resp.addHeader("Server","IIS");
				return resp;
			}
		});

		try {
			config = EasyTravelConfig.read();
			config.paymentBackendPageToIdentify= "";
			config.paymentBackendPort = server.getPort();

			assertTrue("IIS server is available", client.isRunningOnIIS());
			assertEquals("OK", client.stop());
		}  finally {
			server.stop();
			EasyTravelConfig.resetSingleton();
		}
	}
}
