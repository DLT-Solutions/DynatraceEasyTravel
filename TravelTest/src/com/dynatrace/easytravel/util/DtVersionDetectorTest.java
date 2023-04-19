package com.dynatrace.easytravel.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;

import javax.net.ssl.SSLException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.misc.RESTConstants;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.net.UrlUtils.Availability;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;
import com.dynatrace.easytravel.utils.ThreadTestHelper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import ch.qos.logback.classic.Logger;

public class DtVersionDetectorTest {
	private static final int DEFAULT_ASSUME_TIMEOUT = 5000;

	private static final String PATTERN_APM_NG_VERSION = "[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]{8}[\\-][0-9]{6}";

	private static final Logger LOGGER = LoggerFactory.make();

	private static final int NUMBER_OF_THREADS = 10;
	private static final int NUMBER_OF_TESTS = 150;

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.0 Safari/537.36";

    static {
		// set system properties for installation location
		System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");

		LOGGER.warn("Using files from: " + System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION));
    }

	@BeforeClass
	public static void setUpClass() throws IOException {
		LoggerFactory.initLogging();

		System.setProperty ("jsse.enableSNIExtension", "false");

		UrlUtils.trustAllHttpsCertificates();

		// clear cache and reset type of installation
		DtVersionDetector.clearCache();
		DtVersionDetector.enforceInstallationType(null);

	}


	// print out when we start each of the tests, needed for hunting some strange timeouts in CI
	@Rule
	public TestRule watcher = new TestWatcher() {
		@Override
		protected void starting(Description description) {
			LOGGER.warn("Starting test " + description.getMethodName());
		}

		@Override
		protected void finished(Description description) {
			LOGGER.warn("Finished test " + description.getMethodName());
		}

	};

	@Before
	public void setup() {
		// by default set it to something where we do not find a server even if one is running locally
		EasyTravelConfig.read().dtServer = "notexisting";
		EasyTravelConfig.read().dtServerWebURL = "http://localhost:29382/";

		EasyTravelConfig.read().apmServerHost = "notexisting";
		EasyTravelConfig.read().apmServerWebURL = "http://localhost:29382/";
	}


	@After
	public void tearDown() {
		// revert to default config-values to not affect other tests
		EasyTravelConfig.resetSingleton();

		// also reset any caches
		DtVersionDetector.clearCache();
		DtVersionDetector.enforceInstallationType(null);
	}

	@Test
	public void testFallback() {
		String agentPath = "C:\\Program Files (x86)\\dynaTrace\\dynaTrace 3.5.1\\agent\\lib\\dtagent.dll";
		String version = DtVersionDetector.determineDTVersion(agentPath);
		assertNotNull("Expecting some version info", version);
		LOGGER.warn("Version: " + version);
		assertClassic();
	}

	@Test
	public void testFallbackCompuware() {
		String agentPath = "C:\\Program Files (x86)\\Compuware\\dynaTrace 3.5.1\\agent\\lib\\dtagent.dll";
		String version = DtVersionDetector.determineDTVersion(agentPath);
		assertNotNull("Expecting some version info", version);
		LOGGER.warn("Version: " + version);
		assertClassic();
	}

	@Test
	public void testFallbackNotFound() {
		try {
			EasyTravelConfig.read().dtServerWebURL = "http://localhost:2342";
			EasyTravelConfig.read().apmServerDefault = InstallationType.Classic;

			String version = DtVersionDetector.determineDTVersion(null);
			assertNull("Should not have a APM server version before starting this test, but had: " + version, version);

			String agentPath = "C:\\Program Files (x86)\\dynaTrace\\dynaTrace x.x.x\\agent\\lib\\dtagent.dll";
			version = DtVersionDetector.determineDTVersion(agentPath);
			assertNull("Expecting no version info, but had: " + version, version);
			assertClassic();
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testConvertAgentPathToVersionString_3_5_1() {
		try {
			EasyTravelConfig.read().apmServerDefault = InstallationType.Classic;
			String agentPath = "C:\\Program Files (x86)\\dynaTrace\\dynaTrace 3.5.1\\agent\\lib\\dtagent.dll";
			String version = DtVersionDetector.convertAgentPathToVersionString(agentPath);
			assertEquals("Expecting version 3.5.1", "3.5.1", version);
			assertClassic();
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testConvertAgentPathToVersionString_4_0_0() {
		try {
			EasyTravelConfig.read().apmServerDefault = InstallationType.Classic;
			String agentPath = "C:\\Program Files (x86)\\dynaTrace\\dynaTrace 4.0.0\\agent\\lib\\dtagent.dll";
			String version = DtVersionDetector.convertAgentPathToVersionString(agentPath);
			assertEquals("Expecting version 4.0.0", "4.0.0", version);
			assertClassic();
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testConvertAgentPathToVersionString_5_5_0() {
		try {
			EasyTravelConfig.read().apmServerDefault = InstallationType.Classic;
			String agentPath = "C:\\Program Files (x86)\\Compuware\\dynaTrace 5.5.0\\agent\\lib\\dtagent.dll";
			String version = DtVersionDetector.convertAgentPathToVersionString(agentPath);
			assertEquals("Expecting version 5.5.0", "5.5.0", version);
			assertClassic();
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testConvertAgentPathToVersionString_5_6_0() {
		try {
			EasyTravelConfig.read().apmServerDefault = InstallationType.Classic;
			String agentPath = "C:\\Program Files (x86)\\dynaTrace\\dynaTrace 5.6.0\\agent\\lib\\dtagent.dll";
			String version = DtVersionDetector.convertAgentPathToVersionString(agentPath);
			assertEquals("Expecting version 5.6.0", "5.6.0", version);
			assertClassic();
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testConvertAgentPathToVersionString_5_6_0CPWR() {
		try {
			EasyTravelConfig.read().apmServerDefault = InstallationType.Classic;
			String agentPath = "C:\\Program Files (x86)\\Compuware\\dynaTrace 5.6.0\\agent\\lib\\dtagent.dll";
			String version = DtVersionDetector.convertAgentPathToVersionString(agentPath);
			assertEquals("Expecting version 5.6.0", "5.6.0", version);
			assertClassic();
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testConvertAgentPathToVersionString_6_0_0() {
		try {
			EasyTravelConfig.read().apmServerDefault = InstallationType.Classic;
			String agentPath = "C:\\Program Files (x86)\\dynaTrace\\dynaTrace 6.0.0\\agent\\lib\\dtagent.dll";
			String version = DtVersionDetector.convertAgentPathToVersionString(agentPath);
			assertEquals("Expecting version 6.0.0", "6.0.0", version);
			assertClassic();
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testConvertAgentPathToVersionString_6_1_0() {
		try {
			EasyTravelConfig.read().apmServerDefault = InstallationType.Classic;
			String agentPath = "C:\\Program Files (x86)\\dynaTrace\\dynaTrace 6.1.0\\agent\\lib\\dtagent.dll";
			String version = DtVersionDetector.convertAgentPathToVersionString(agentPath);
			assertEquals("Expecting version 6.1.0", "6.1.0", version);
			assertClassic();
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testConvertAgentPathToVersionString_6_2() {
		try {
			EasyTravelConfig.read().apmServerDefault = InstallationType.Classic;
			String agentPath = "C:\\Program Files (x86)\\dynaTrace\\dynaTrace 6.2\\agent\\lib\\dtagent.dll";
			String version = DtVersionDetector.convertAgentPathToVersionString(agentPath);
			assertEquals("Expecting version 6.2", "6.2", version);
			assertClassic();
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testConvertAgentPathToVersionString_6_3() {
		try {
			EasyTravelConfig.read().apmServerDefault = InstallationType.Classic;
			String agentPath = "C:\\Program Files (x86)\\dynaTrace\\dynaTrace 6.3\\agent\\lib\\dtagent.dll";
			String version = DtVersionDetector.convertAgentPathToVersionString(agentPath);
			assertEquals("Expecting version 6.3", "6.3", version);
			assertClassic();
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testConvertAgentPathToVersionString_6_5() {
		try {
			EasyTravelConfig.read().apmServerDefault = InstallationType.Classic;
			String agentPath = "C:\\Program Files (x86)\\dynaTrace\\dynaTrace 6.5\\agent\\lib\\dtagent.dll";
			String version = DtVersionDetector.convertAgentPathToVersionString(agentPath);
			assertEquals("Expecting version 6.5", "6.5", version);
			assertClassic();
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testDetermineDtVersionInvalid() throws Exception {
 		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "invalid");
		try {
			EasyTravelConfig.read().dtServerWebURL = "http://localhost:" + server.getPort();
			assertNull(DtVersionDetector.determineDTVersion(null));
			assertNull(DtVersionDetector.determineDTVersion(null));
			assertAPM();
		} finally {
			server.stop();
		}
	}

	@Test
	public void testDetermineDtVersionDynaTrace() throws Exception {
		// currently this is not detected as the REST version usually is sufficient
 		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "dynaTrace 6.0.0.6674  blabla");
		try {
			EasyTravelConfig.read().dtServerWebURL = "http://localhost:" + server.getPort();
			assertNull(DtVersionDetector.determineDTVersion(null));
			assertNull(DtVersionDetector.determineDTVersion(null));
			assertAPM();
		} finally {
			server.stop();
		}
	}

	@Test
	public void testDetermineDtVersionInvalidURL() throws Exception {
		EasyTravelConfig config = EasyTravelConfig.read();
		try {
			config.apmServerDefault = InstallationType.Classic;

			config.dtServerWebURL = "";
			assertNull(DtVersionDetector.determineDTVersion(null));
			assertNull(DtVersionDetector.determineDTVersion(null));
			assertClassic();
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testDetermineDtVersionInvalidSSL() throws Exception {

		// TODO: APM-129160
		//Logger.getLogger(BaseConstants.EMPTY_STRING).setLevel(Level.FINE);
		//Logger.getLogger(DtVersionDetector.class.getName()).setLevel(Level.FINE);
		//Logger.getLogger(DtSSLHelper.class.getName()).setLevel(Level.FINE);

		EasyTravelConfig CONFIG = EasyTravelConfig.read();

		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "invalid");
		try {
			CONFIG.apmServerDefault = InstallationType.Classic;

			LOGGER.warn("Starting with port " + server.getPort());
			CONFIG.dtServer = "localhost";
			CONFIG.dtServerPort = Integer.toString(server.getPort());
			CONFIG.dtServerWebURL = "https://localhost:" + server.getPort();

			CONFIG.apmServerHost = "localhost";
			CONFIG.apmServerPort = Integer.toString(server.getPort());
			CONFIG.apmServerWebPort = Integer.toString(server.getPort());
			CONFIG.apmServerWebURL = "https://localhost:" + server.getPort();

			// verify REST server
		    String sUrl = TextUtils.appendTrailingSlash(CONFIG.dtServerWebURL) + RESTConstants.MANAGEMENT_VERSION;
		    LOGGER.warn("Retrieving test-data from " + sUrl);
		    try {
		    	new DtSSLHelper(10000, USER_AGENT).getData(sUrl);
		    	fail("Should fail here but did not...");
		    } catch (ConnectTimeoutException e) {
		    	// happens sometimes in CI
		    	LOGGER.warn("Could not connect to local server: " + e);
		    } catch (SSLException e) {
		    	// expected
		    }

			LOGGER.warn("First check on port " + server.getPort());
			assertNull(DtVersionDetector.determineDTVersion(null));

			LOGGER.warn("Second check on port " + server.getPort());
			assertNull(DtVersionDetector.determineDTVersion(null));

			LOGGER.warn("Done with port " + server.getPort());
			assertClassic();
		} finally {
			server.stop();
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testDetermineDtVersionVersion() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "4.0.0.1234");
		try {
			EasyTravelConfig.read().dtServerWebURL = "http://localhost:" + server.getPort();
			assertNull("Still null because we also expect the enclosing XML", DtVersionDetector.determineDTVersion(null));
			assertNull("Still null because we also expect the enclosing XML", DtVersionDetector.determineDTVersion(null));
			assertAPM();
		} finally {
			server.stop();
		}
	}

	@Test
	public void testDetermineDtVersionVersionXml() throws Exception {
 		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "<result value=\"4.0.0.2587\"/>");
		try {
			EasyTravelConfig.read().dtServerWebURL = "http://localhost:" + server.getPort();
			assertEquals("4.0.0.2587", DtVersionDetector.determineDTVersion(null));
			assertEquals("4.0.0.2587", DtVersionDetector.determineDTVersion(null));
			assertClassic();
		} finally {
			server.stop();
		}
	}

	@Test
	public void testDetermineDtVersionVersionXml55() throws Exception {
 		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "<result value=\"5.5.0.3587\"/>");
		try {
			EasyTravelConfig.read().dtServerWebURL = "http://localhost:" + server.getPort();
			assertClassic();
		} finally {
			server.stop();
		}
	}

	@Test
	public void testDetermineDtVersionVersionAPM() throws Exception {
		LOGGER.warn("Starting REST Server which simulates old ruxit-version-response");
 		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "[{\"deploymentMetaInfoDto\":{\"id\":1,\"loadFactor\":1.0,\"osInfo\":\"Platform: Windows Server 2008 R2, Version: 6.1, Architecture: amd64\",\"jvmInfo\":\"VM: Java HotSpot(TM) 64-Bit Server VM, Version: 1.7.0_25, Vendor: Oracle Corporation, Max-memory: 2014M\",\"buildVersion\":\"0.4.0.20130801-063958\",\"uri\":\"SECRET\"},\"runtimeMetaInfoDto\":{\"operationState\":\"RUNNING\",\"startupTimestamp\":1375333070791}}]");
		try {
			EasyTravelConfig.read().apmServerHost = "localhost";
			EasyTravelConfig.read().apmServerWebURL = "http://localhost:" + server.getPort();
			EasyTravelConfig.read().apmServerWebPort = String.valueOf(server.getPort());

			LOGGER.warn("Fetching ruxit version now.");
			assertEquals("0.4.0.20130801-063958", DtVersionDetector.determineDTVersion(null));
			assertAPM();
		} finally {
			LOGGER.warn("Finished testing with mock REST services enabled, now stopping again.");
			server.stop();
		}

		LOGGER.warn("reset to ensure we trigger the cache, i.e. we now do not have the REST-Server any more and thus will fail if we actually try to read the URL again");

		DtVersionDetector.enforceInstallationType(null);
		assertEquals("0.4.0.20130801-063958", DtVersionDetector.determineDTVersion(null));
		assertAPM();

		LOGGER.warn("Done.");
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(DtVersionDetector.class);
	}

	private static void assertAPM() {
		assertTrue("Server must be recognized as APM Server", DtVersionDetector.isAPM());
		assertFalse("Server must not be recognized as dynaTrace Server", DtVersionDetector.isClassic());
	}

	private static void assertClassic() {
		assertFalse("Server must not be recognized as APM Server", DtVersionDetector.isAPM());
		assertTrue("Server must be recognized as dynaTrace Server", DtVersionDetector.isClassic());
	}

	private void checkVersionIsAPMNG(boolean versionIsNull) {
		assertEquals(InstallationType.APM, DtVersionDetector.getInstallationType());
		String version = DtVersionDetector.determineDTVersion(null);
		LOGGER.warn("Found version: " + version + " on " + EasyTravelConfig.read().apmServerHost + ":" + EasyTravelConfig.read().apmServerWebPort );
		assertNotEquals("0.4.0.something", version);
		if(versionIsNull) {
			assertNull("Version should be null, but was: " + version, version);
		} else {
			assertTrue("Version did not match expected pattern: " + version, version.matches(PATTERN_APM_NG_VERSION));
		}
		assertEquals(InstallationType.APM, DtVersionDetector.getInstallationType());

		// reset and check again to trigger cache
		DtVersionDetector.enforceInstallationType(null);
		assertEquals(InstallationType.APM, DtVersionDetector.getInstallationType());
		String version2 = DtVersionDetector.determineDTVersion(null);
		assertNotEquals("0.4.0.something", version2);
		if(versionIsNull) {
			assertNull("Version should be null, but was: " + version, version);
		} else {
			assertTrue("Version did not match expected pattern: " + version2, version2.matches(PATTERN_APM_NG_VERSION));
		}
		assertEquals("Versions need to match", version, version2);
	}

	@Ignore("local testing only")
	@Test
	public void testGetData2() throws IOException {
		//final CredentialsProvider credsProvider = new BasicCredentialsProvider();
		final CloseableHttpClient httpclient;

		/*credsProvider.setCredentials(
                new AuthScope(null, -1),
                new UsernamePasswordCredentials(Constants.QB_USER, Constants.QB_PASSWORD));*/

		RequestConfig reqConfig = RequestConfig.custom()
			    .setSocketTimeout(60*1000)
			    .setConnectTimeout(60*1000)
			    .setConnectionRequestTimeout(60*1000)
			    .build();

		httpclient = HttpClients.custom()
                //.setDefaultCredentialsProvider(credsProvider)
				.setDefaultRequestConfig(reqConfig)
                .build();

		HttpGet httpGet = new HttpGet("SECRET");
		CloseableHttpResponse response = httpclient.execute(httpGet);

		try {
			int statusCode = response.getStatusLine().getStatusCode();
			assertEquals(200, statusCode);

		    HttpEntity entity = response.getEntity();

		    String tmp = IOUtils.toString(entity.getContent());
			System.out.println(tmp);

	    	assertTrue(tmp.contains("v.1."));

		    // ensure all content is taken out to free resources
		    EntityUtils.consume(entity);
		} finally {
		    response.close();
		}

	}

	@Ignore("local testing only")
	@Test
	public void testGetData() throws IOException {
		final ClientConfig clientConfig = new DefaultClientConfig();

	    final Client client = Client.create(clientConfig);
	    client.setFollowRedirects(true);


	    WebResource r = client.resource("https://docbvpycxp.dev.dynatracelabs.com:443/?browser_ok");
    	MultivaluedMap<String, String> map = new MultivaluedMapImpl();
    	map.put("browser_ok", Collections.singletonList(""));

		String tmp = r
		    	.queryParams(map)
		    	.accept(MediaType.TEXT_HTML_TYPE)
		    	.get(String.class);

	    //System.out.println(UrlUtils.retrieveData("https://docbvpycxp.dev.ruxitlabs.com:443/?browser_ok"));
    	System.out.println("Using webresource: \n" + tmp);

    	assertTrue(tmp.contains("v.1."));
	}

	@Test
	public void testUITestAutomation() {
		EasyTravelConfig config = EasyTravelConfig.read();
		config.apmServerHost = "docbvpycxp.dev.ruxitlabs.com";
		config.apmServerWebPort = "443";
		config.apmServerUsername = "admin";
		config.apmServerPassword = "admin";
		config.apmServerWebURL = "https://docbvpycxp.dev.ruxitlabs.com:443/";

		Assume.assumeThat("Test can only run if the ruxit machine is running as well",
				UrlUtils.checkRead("https://" + config.apmServerHost + ":" + config.apmServerWebPort, DEFAULT_ASSUME_TIMEOUT), Is.is(Availability.READ_OK));

		checkVersionIsAPMNG(false);
	}

	@Test
	public void testDynatraceProd() throws  Exception {
		EasyTravelConfig config = EasyTravelConfig.read();
		config.apmServerHost = "cdojfgmpzd.live.dynatrace.com";
		config.apmServerWebPort = "443";
		config.apmServerUsername = "admin";
		config.apmServerPassword = "admin";
		config.apmServerWebURL = "SECRET";

		Assume.assumeThat("Test can only run if the ruxit machine is running as well",
				UrlUtils.checkRead("https://" + config.apmServerHost + ":" + config.apmServerWebPort, DEFAULT_ASSUME_TIMEOUT), Is.is(Availability.READ_OK));

		checkVersionIsAPMNG(false);
	}

	@Test
	public void testDemoDev() {
		EasyTravelConfig config = EasyTravelConfig.read();
		config.apmServerHost = "demo.dev.dynatracelabs.com";
		config.apmServerWebPort = "443";
		config.apmServerUsername = "admin";
		config.apmServerPassword = "admin";
		config.apmServerWebURL = "SECRET";

		Assume.assumeThat("Test can only run if the ruxit machine is running as well, tried https://" + config.apmServerHost + ":" + config.apmServerWebPort,
				UrlUtils.checkRead("https://" + config.apmServerHost + ":" + config.apmServerWebPort, DEFAULT_ASSUME_TIMEOUT), Is.is(Availability.READ_OK));

		checkVersionIsAPMNG(false);
	}

	@Test
	public void testAcceptanceE2E() throws IOException {
		EasyTravelConfig config = EasyTravelConfig.read();
		config.apmServerHost = "wjyhngshiv.sprint.dynatracelabs.com";
		config.apmServerWebPort = "443";
		config.apmServerUsername = "admin";
		config.apmServerPassword = "admin";
		config.apmServerWebURL = "https://" + config.apmServerHost + ":" + config.apmServerWebPort + "/";

		Assume.assumeThat("Test can only run if the Acceptance E2E machine is running as well",
				UrlUtils.checkRead("https://" + config.apmServerHost + ":" + config.apmServerWebPort, DEFAULT_ASSUME_TIMEOUT), Is.is(Availability.READ_OK));

		checkVersionIsAPMNG(false);
	}

	@Test
	public void testAcceptanceE2EHTTPS2() throws IOException {
		EasyTravelConfig config = EasyTravelConfig.read();

		// prevent local server from breaking the test
		config.dtServerWebPort = "23252";
		config.dtServerWebURL = "http://" + config.dtServer + ":" + config.dtServerWebPort + "/";

		config.apmServerHost = "wjyhngshiv.sprint.dynatracelabs.com";
		config.apmServerWebPort = "443";
		config.apmServerUsername = "admin";
		config.apmServerPassword = "admin";
		config.apmServerWebURL = "https://" + config.apmServerHost + ":" + config.apmServerWebPort + "/";

		Assume.assumeThat("Test can only run if the Acceptance E2E machine is running as well",
				UrlUtils.checkRead("https://" + config.apmServerHost + ":" + config.apmServerWebPort, DEFAULT_ASSUME_TIMEOUT), Is.is(Availability.READ_OK));


		// why does this fail?
		Assume.assumeThat("Test can only run if the Acceptance E2E machine is running as well at " + config.apmServerWebURL,
				UrlUtils.checkRead(config.apmServerWebURL, DEFAULT_ASSUME_TIMEOUT), Is.is(Availability.READ_OK));

		checkVersionIsAPMNG(false);
	}

	private void assumeDtServerIsRunning(EasyTravelConfig config) throws IOException {
		try {
			Assume.assumeTrue("Test can only run if the machine is running as well at " + config.dtServerWebURL,
					StringUtils.isNotEmpty(new DtSSLHelper(10000, USER_AGENT).getData(config.dtServerWebURL)));
		} catch (HttpHostConnectException e) {
			Assume.assumeNoException("Test can only run if the dynaSprint machine is running as well at " + config.dtServerWebURL, e);
		} catch (ConnectTimeoutException e) {
			Assume.assumeNoException("Test can only run if the dynaSprint machine is running as well at " + config.dtServerWebURL, e);
		} catch (UnknownHostException e) {
			Assume.assumeNoException("Test can only run if the dynaSprint machine is running as well at " + config.dtServerWebURL, e);
		}
	}

	@Test
	public void testDynaDayHTTP() {
		EasyTravelConfig config = EasyTravelConfig.read();
		config.dtServer = "dynaday.dynatrace.vmta";
		config.dtServerWebPort = "8020";
		config.dtServerUsername = "admin";
		config.dtServerPassword = "admin";
		config.dtServerWebURL = "http://" + config.dtServer + ":" + config.dtServerWebPort + "/";

		config.apmServerHost = "dynaday.dynatrace.vmta";
		config.apmServerWebURL = "http://" + config.dtServer + ":" + config.dtServerWebPort + "/";
		config.apmServerPort = "2020";
		config.apmServerWebPort = "8020";

		Assume.assumeThat("Test can only run if the dynaday machine is running as well at " + config.dtServerWebURL,
				UrlUtils.checkRead("http://" + config.dtServer + ":" + config.dtServerWebPort, DEFAULT_ASSUME_TIMEOUT), Is.is(Availability.READ_OK));

		assertEquals(InstallationType.Classic, DtVersionDetector.getInstallationType());

		LOGGER.warn("Found version: " + DtVersionDetector.determineDTVersion(null) + " on " + EasyTravelConfig.read().dtServer + ":" + EasyTravelConfig.read().dtServerWebPort );
		assertTrue(StringUtils.isNotEmpty(DtVersionDetector.determineDTVersion(null)));
	}

	@Test
	public void testDynaDayHTTPS() throws IOException {
		EasyTravelConfig config = EasyTravelConfig.read();
		config.dtServer = "dynaday.dynatrace.vmta";
		config.dtServerWebPort = "8021";
		config.dtServerUsername = "admin";
		config.dtServerPassword = "admin";
		config.dtServerWebURL = "https://" + config.dtServer + ":" + config.dtServerWebPort + "/";

		config.apmServerHost = "dynaday.dynatrace.vmta";
		config.apmServerWebURL = "https://" + config.dtServer + ":" + config.dtServerWebPort + "/";
		config.apmServerPort = "2021";
		config.apmServerWebPort = "8021";

		assumeDtServerIsRunning(config);

		assertEquals(InstallationType.Classic, DtVersionDetector.getInstallationType());

		LOGGER.warn("Found version: " + DtVersionDetector.determineDTVersion(null) + " on " + EasyTravelConfig.read().dtServer + ":" + EasyTravelConfig.read().dtServerWebPort );
		assertTrue(StringUtils.isNotEmpty(DtVersionDetector.determineDTVersion(null)));
	}

	@Test
	public void testAPMNGEmptyHostAndPort() {
		EasyTravelConfig config = EasyTravelConfig.read();
		config.apmServerWebPort = "8020";
		config.apmServerUsername = "admin";
		config.apmServerPassword = "admin";

		config.apmServerHost = null;
		assertEquals(InstallationType.APM, DtVersionDetector.getInstallationType());

		DtVersionDetector.enforceInstallationType(null);
		config.apmServerHost = "";
		assertEquals(InstallationType.APM, DtVersionDetector.getInstallationType());

		DtVersionDetector.enforceInstallationType(null);
		config.apmServerHost = "localhost";
		config.apmServerWebPort = null;
		assertEquals(InstallationType.APM, DtVersionDetector.getInstallationType());

		DtVersionDetector.enforceInstallationType(null);
		config.apmServerHost = "localhost";
		config.apmServerWebPort = "";
		assertEquals(InstallationType.APM, DtVersionDetector.getInstallationType());
	}

	@Test
	public void testHeaderImageAPM() throws Exception {
		DtVersionDetector.enforceInstallationType(InstallationType.APM);
		assertEquals(BaseConstants.Images.HEADER_APM_EASY_TRAVEL, DtVersionDetector.getHeaderImageName());
	}

	@Test
	public void testHeaderImageClassic() throws Exception {
		DtVersionDetector.enforceInstallationType(InstallationType.Classic);
		assertEquals(BaseConstants.Images.HEADER_EASY_TRAVEL, DtVersionDetector.getHeaderImageName());
	}

	@Test
	public void testServerLabelAPM() throws Exception {
		DtVersionDetector.enforceInstallationType(InstallationType.APM);
		assertEquals(BaseConstants.Labels.APM_SERVER, DtVersionDetector.getServerLabel());
	}

	@Test
	public void testServerLabelClassic() throws Exception {
		DtVersionDetector.enforceInstallationType(InstallationType.Classic);
		assertEquals(BaseConstants.Labels.CLASSIC_SERVER, DtVersionDetector.getServerLabel());
	}

    @Test
    public void testMultipleThreads() throws Throwable {
        ThreadTestHelper helper =
            new ThreadTestHelper(NUMBER_OF_THREADS, NUMBER_OF_TESTS);

        helper.executeTest(new ThreadTestHelper.TestRunnable() {
            @Override
            public void doEnd(int threadnum) throws Exception {
                // do stuff at the end ...
            }

            @Override
            public void run(int threadnum, int iter) throws Exception {
                switch (iter%7) {
					case 0:
						DtVersionDetector.getInstallationType();
						break;
					case 1:
						DtVersionDetector.isClassic();
						break;
					case 2:
						DtVersionDetector.isAPM();
						break;
					case 3:
						DtVersionDetector.enforceInstallationType(null);
						break;
					case 4:
						DtVersionDetector.enforceInstallationType(InstallationType.APM);
						break;
					case 5:
						DtVersionDetector.enforceInstallationType(InstallationType.Classic);
						break;
					case 6:
						DtVersionDetector.determineDTVersion(null);
						break;
					default:
						fail();
				}
            }
        });
    }

    @Test
	public void testVersionGreaterOrEqual() {
		String versionString = "6.2";
		assertFalse(DtVersionDetector.isDetectedVersionGreaterOrEqual(versionString, 63));
		assertTrue(DtVersionDetector.isDetectedVersionGreaterOrEqual(versionString, 62));
		assertTrue(DtVersionDetector.isDetectedVersionGreaterOrEqual(versionString, 61));
		assertTrue(DtVersionDetector.isDetectedVersionGreaterOrEqual(versionString, 60));

		versionString = "6.3";
		assertTrue(DtVersionDetector.isDetectedVersionGreaterOrEqual(versionString, 63));
		assertTrue(DtVersionDetector.isDetectedVersionGreaterOrEqual(versionString, 62));
		assertTrue(DtVersionDetector.isDetectedVersionGreaterOrEqual(versionString, 61));
		assertTrue(DtVersionDetector.isDetectedVersionGreaterOrEqual(versionString, 60));

		versionString = "5.6";
		assertFalse(DtVersionDetector.isDetectedVersionGreaterOrEqual(versionString, 63));
		assertFalse(DtVersionDetector.isDetectedVersionGreaterOrEqual(versionString, 62));
		assertFalse(DtVersionDetector.isDetectedVersionGreaterOrEqual(versionString, 61));
		assertFalse(DtVersionDetector.isDetectedVersionGreaterOrEqual(versionString, 60));
		assertTrue(DtVersionDetector.isDetectedVersionGreaterOrEqual(versionString, 55));

		versionString = "6.2.108";
		assertTrue(DtVersionDetector.isDetectedVersionGreaterOrEqual(versionString, 61));
		assertTrue(DtVersionDetector.isDetectedVersionGreaterOrEqual(versionString, 62));
		assertFalse(DtVersionDetector.isDetectedVersionGreaterOrEqual(versionString, 63));

		// Null checks
		assertFalse(DtVersionDetector.isDetectedVersionGreaterOrEqual("", 55));
		assertFalse(DtVersionDetector.isDetectedVersionGreaterOrEqual("", 56));
		assertFalse(DtVersionDetector.isDetectedVersionGreaterOrEqual("", 60));
		assertFalse(DtVersionDetector.isDetectedVersionGreaterOrEqual("", 61));
		assertFalse(DtVersionDetector.isDetectedVersionGreaterOrEqual("", 62));
		assertFalse(DtVersionDetector.isDetectedVersionGreaterOrEqual("", 63));

		assertFalse(DtVersionDetector.isDetectedVersionGreaterOrEqual(null, 61));
		assertFalse(DtVersionDetector.isDetectedVersionGreaterOrEqual(null, 62));
	}

	@Test
    public void testPattern() {
    	String version = "0.5.45.20140514-173618";
		assertTrue("Version did not match expected pattern: " + version, version.matches(PATTERN_APM_NG_VERSION));
    }
}
