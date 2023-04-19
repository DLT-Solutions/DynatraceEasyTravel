package com.dynatrace.easytravel.launcher.procedures;

import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.core.MediaType;

import org.junit.*;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.UrlType;
import com.dynatrace.easytravel.integration.IntegrationTestBase;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.*;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.remote.HttpServiceThread;
import com.dynatrace.easytravel.launcher.remote.RESTProcedureClient;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.LocalUriProvider;
import com.dynatrace.easytravel.util.TextUtils;
import com.dynatrace.easytravel.utils.*;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPResponseRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD.Response;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class RemoteProcedureTest {
    private static final Logger LOGGER = LoggerFactory.make();

    static {
		// set system properties for installation location
		System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");

		LOGGER.info("Using files from: " + System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION));
    }

    private static final EasyTravelConfig CONFIG = EasyTravelConfig.read();

    private HttpServiceThread remoteController;

    private Runnable exitInDisplayThreadRunnable = new Runnable() {
        @Override
        public void run() {
            throw new IllegalStateException("Should not be called in this test!");
        }
    };

    @BeforeClass
    public static void setUpClass() throws IOException {
    	LoggerFactory.initLogging();

    	TestHelpers.waitForPort(CONFIG.launcherHttpPort, 2000);
    }

	@Before
	public void setUp() throws IOException, InterruptedException {
		IntegrationTestBase.checkPort(CONFIG.launcherHttpPort);

        remoteController = new HttpServiceThread(CONFIG.launcherHttpPort, exitInDisplayThreadRunnable);
        remoteController.start();

        // try to sleep a bit to let httpd service thread start up fully
        Thread.sleep(200);
	}

	@After
	public void tearDown() {
		if(remoteController != null) {
			remoteController.stopService();
		}

		TestHelpers.waitForPort(CONFIG.launcherHttpPort, 2000);
	}

	@Test
	public void testRemoteProcedure() {
		RemoteProcedure proc = createProc(Constants.Procedures.INPROCESS_DBMS_ID);

		assertEquals(StopMode.PARALLEL, proc.getStopMode());

		assertTrue(proc.isEnabled());
		assertFalse(proc.isOperating());
		assertTrue(proc.isOperatingCheckSupported());
		assertFalse(proc.isRunning());
		assertTrue(proc.isStoppable());
		assertFalse(proc.isSynchronous());
		assertTrue(proc.isTransferableTo(proc.getMapping()));

		assertEquals("Stopping without start fails", Feedback.Failure, proc.stop());

		assertNotNull(proc.getDetails());
		assertTrue(proc.hasLogfile());
		assertNotNull(proc.getLogfile());
		assertNull(proc.getURI());
		assertNull(proc.getURI(UrlType.APACHE_B2B_FRONTEND));
		assertNull(proc.getURI(UrlType.APACHE_JAVA_FRONTEND));
		assertNull(proc.getURI(UrlType.APACHE_PROXY));

		assertEquals("Starting works now", Feedback.Success, proc.run());

		assertNull(proc.getTechnology()); // INPROCESS_DBMS has no technology
		assertFalse(proc.isInstrumentationSupported()); // instrumentation for INPROCESS_DBMS is not supported
		assertFalse(proc.agentFound()); // because there is no instrumentation, there can't be an agent

		// once again to trigger caching
		assertFalse(proc.isInstrumentationSupported()); // instrumentation for INPROCESS_DBMS is not supported
		assertFalse(proc.agentFound()); // because there is no instrumentation, there can't be an agent

		assertEquals("Starting a second time fails", Feedback.Failure, proc.run());

		assertTrue(proc.isRunning());
		assertTrue(proc.isOperating());
		assertTrue(proc.isStoppable());

		assertEquals("Stopping works now", Feedback.Success, proc.stop());
		assertEquals("Stopping a second time fails", Feedback.Failure, proc.stop());

		assertFalse(proc.isRunning());
		assertFalse(proc.isOperating());
		assertTrue(proc.isStoppable());
	}

    @Test
    public void testNginxRemoteProcedure() {
        RemoteProcedure proc = createProc(Constants.Procedures.NGINX_WEBSERVER_ID);
        assertThat("Get stop mode", proc.getStopMode(), is(StopMode.PARALLEL));
        assertThat("Is nginx enabled", proc.isEnabled(), is(true));
        assertThat("Is nginx operating", proc.isOperating(), is(false));
        assertTrue(proc.isOperatingCheckSupported());
        assertFalse(proc.isRunning());
        assertTrue(proc.isStoppable());
        assertFalse(proc.isSynchronous());
        assertTrue(proc.isTransferableTo(proc.getMapping()));
        assertEquals("Stopping without start fails", Feedback.Failure, proc.stop());
        assertNotNull(proc.getDetails());
        assertThat("Nginx does not provide remote logfile", proc.hasLogfile(), is(false));
        assertNotNull(proc.getLogfile());
        assertNull(proc.getURI());
        assertThat(proc.getURI(UrlType.NGINX_JAVA_FRONTEND), is("http://127.0.0.1:8079/"));

        String fqdnHost = LocalUriProvider.getFQDN("127.0.0.1");

        assertThat(proc.getURIDNS(UrlType.NGINX_JAVA_FRONTEND), is(equalToIgnoringCase(TextUtils.merge("http://{0}:8079/", fqdnHost))));

        assertThat(proc.getTechnology(), is(Technology.NGINX));
    }


	@Test
	public void testStopListener() throws Exception {
		RemoteProcedure.setWATCHER_THREAD_INTERVALL_MS(100);
		RemoteProcedure proc = createProc(Constants.Procedures.INPROCESS_DBMS_ID);

		final AtomicBoolean stopped = new AtomicBoolean(false);

		AbstractStopListener stopListener = new AbstractStopListener() {

			@Override
			public void notifyProcessStopped() {
				stopped.set(true);
			}
		};
		proc.addStopListener(stopListener);

		assertEquals("Starting works now", Feedback.Success, proc.run());

		assertFalse("Not notified about stop yet", stopped.get());

		assertTrue(proc.isOperating());

		assertFalse("Not notified about stop yet", stopped.get());

		// wait until the thread has run at least once
		Thread.sleep(200);

		assertFalse("Not notified about stop yet", stopped.get());

		assertEquals("Stopping works now", Feedback.Success, proc.stop());

		assertTrue("We should have been notified about stopping now", stopped.get());

		proc.removeStopListener(stopListener);
		proc.clearStopListeners();
	}

	@Test
	public void testStopListenerProcedureStoppedItself() throws Exception {
		RemoteProcedure.setWATCHER_THREAD_INTERVALL_MS(100);
		RemoteProcedure proc = createProc(Constants.Procedures.INPROCESS_DBMS_ID);

		final AtomicBoolean stopped = new AtomicBoolean(false);

		proc.addStopListener(new AbstractStopListener() {

			@Override
			public void notifyProcessStopped() {
				stopped.set(true);
			}
		});

		assertEquals("Starting works now", Feedback.Success, proc.run());

		assertTrue(proc.isOperating());
		assertFalse("Not notified about stop yet", stopped.get());

		// now simulate a stop of the procedure by stopping all procedures
		stopAllProcedures();

		// wait until the thread has run at least once
		Thread.sleep(200);

		assertTrue("We should now have been notified about a stopping process", stopped.get());

		assertEquals("Stopping fails now because procedure is already stopped", Feedback.Failure, proc.stop());

		assertTrue("We still should be notified about stopping", stopped.get());
	}

	public void stopAllProcedures() {
		ClientConfig clientConfig = new DefaultClientConfig();
		Client client = Client.create(clientConfig);

		// shutdown
		WebResource r = client.resource("http://localhost:" + CONFIG.launcherHttpPort + "/stopAll");
		String response = r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
		assertEquals("Had response: " + response, "OK", response);
	}

	@Test
	public void testInvalidUUIDHandling() throws Exception {
		RemoteProcedure.setWATCHER_THREAD_INTERVALL_MS(1000); // set it higher here as we need it
		RemoteProcedure proc = createProc(Constants.Procedures.INPROCESS_DBMS_ID);

		// at first stop the real REST server
		remoteController.stopService();

		final UUID uuid = UUID.randomUUID();
		HTTPResponseRunnable runnable = new HTTPResponseRunnable() {

			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				if(uri.contains("/prepare/")) {
					return new NanoHTTPD.Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, uuid.toString() + "|1243");
				} else if (uri.contains("/start/")) {
					return new NanoHTTPD.Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "STARTING");
				} else if (uri.contains("/stop/")) {
					return new NanoHTTPD.Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "OK");
				} else {
					throw new IllegalStateException("Unexpected REST-call: " + uri);
				}
			}
		};

		// start a dummy REST server which returns an specific UUID
		MockRESTServer server = new MockRESTServer(runnable);
		try {
			EasyTravelConfig.read().launcherHttpPort = server.getPort();

			assertEquals("Starting seems to work here and we stored the UUID", Feedback.Success, proc.run());

			assertNull("no port because only certain procedures support it", proc.getURI());
		} finally {
			server.stop();
		}

		// now start the remote server again
	    remoteController = new HttpServiceThread(CONFIG.launcherHttpPort, exitInDisplayThreadRunnable);
	    remoteController.start();

		assertEquals("Starting now works as it retries after the initial state is 'UNKNOWN'", Feedback.Success, proc.run());
		assertTrue("Expect procedure to be running now", proc.isRunning());
		assertTrue("Expect procedure to be operating now", proc.isOperating());
		assertTrue("Expect procedure to be stoppable", proc.isStoppable());
		assertNull("DBMS-Procedure does not report an URI/port", proc.getURI());

		assertEquals("Stopping works now", Feedback.Success, proc.stop());
		assertEquals("Stopping a second time fails", Feedback.Failure, proc.stop());

		assertFalse("Expect procedure to be not running now", proc.isRunning());
		assertFalse("Expect procedure to be not operating now", proc.isOperating());
		assertTrue("Expect procedure to be stoppable", proc.isStoppable());
	}

	@Ignore @Test
	public void testRunFailedStates() throws Exception {
		RemoteProcedure proc = createProc(Constants.Procedures.INPROCESS_DBMS_ID);

		// at first stop the real REST server
		remoteController.stopService();

		final UUID uuid = UUID.randomUUID();
		final AtomicReference<String> response = new AtomicReference<String>();
		HTTPResponseRunnable runnable = new HTTPResponseRunnable() {

			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				if(uri.contains("/prepare/")) {
					return new NanoHTTPD.Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, uuid.toString() + "|1243");
				} else if (uri.contains("/start/")) {
					return new NanoHTTPD.Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, response.get());
				} else if (uri.contains("/stop/")) {
					return new NanoHTTPD.Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "OK");
				} else {
					throw new IllegalStateException("Unexpected REST-call: " + uri);
				}
			}
		};

		// start a dummy REST server which returns an specific UUID
		MockRESTServer server = new MockRESTServer(runnable);
		try {
			EasyTravelConfig.read().launcherHttpPort = server.getPort();

			// verify the various different failure states and ensure that all lead to Feedback.Failure
			response.set(null);
			assertEquals("Starting does not work", Feedback.Failure, proc.run());

			response.set(State.FAILED.name());
			assertEquals("Starting does not work", Feedback.Failure, proc.run());

			response.set(State.ACCESS_DENIED.name());
			assertEquals("Starting does not work", Feedback.Failure, proc.run());
		} finally {
			server.stop();
		}
	}

	@Test
	public void testConnectionError() throws Exception {
		RemoteProcedure proc = createProc(Constants.Procedures.INPROCESS_DBMS_ID);
		final AtomicBoolean stopped = new AtomicBoolean(false);
		proc.addStopListener(new StopListener() {

			@Override
			public void notifyProcessStopped() {
				stopped.set(true);
			}

			@Override
			public void notifyProcessFailed() {
				stopped.set(true);
			}
		});
		assertFalse("Not stopped and not failed", stopped.get());

		// at first stop the real REST server
		remoteController.stopService();

		final UUID uuid = UUID.randomUUID();
		final AtomicReference<String> response = new AtomicReference<String>();
		HTTPResponseRunnable runnable = new HTTPResponseRunnable() {

			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				if(uri.contains("/prepare/")) {
					return new NanoHTTPD.Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, uuid.toString() + "|1243");
				} else if (uri.contains("/start/")) {
					return new NanoHTTPD.Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, response.get());
				} else if (uri.contains("/status/")) {
					return new NanoHTTPD.Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, response.get());
				} else if (uri.contains("/stop/")) {
					return new NanoHTTPD.Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "OK");
				} else {
					throw new IllegalStateException("Unexpected REST-call: " + uri);
				}
			}
		};

		// For test purposes, reduce the delay we wait before we time out and kill the client
		RESTProcedureClient.MAX_CONNECTION_ERROR_COUNT = 10;

		// start a dummy REST server which returns an specific UUID
		MockRESTServer server = new MockRESTServer(runnable);
		try {
			EasyTravelConfig.read().launcherHttpPort = server.getPort();

			// Setting value to be returned from client.run()
			response.set(State.OPERATING.name());
			proc.run();

			// Give the client some time to run and see if it is still alive
			Thread.sleep(30000);
			assertFalse("NOT stopped after 30s", stopped.get());

			server.stop();

			// In our test scenario, the client should be killed after approximately 10 re-try periods (or perhaps a bit more),
			// but NOT before the 10 re-try periods are up.
			Thread.sleep(RemoteProcedure.WATCHER_THREAD_INTERVALL_MS);
			assertFalse("NOT stopped after one period", stopped.get());
			Thread.sleep(RemoteProcedure.WATCHER_THREAD_INTERVALL_MS);
			assertFalse("NOT stopped after two periods", stopped.get()); // prior to fix for APM-13802, we would have been stopped around here
			Thread.sleep(RemoteProcedure.WATCHER_THREAD_INTERVALL_MS);
			assertFalse("NOT stopped after three periods", stopped.get());
			Thread.sleep(RemoteProcedure.WATCHER_THREAD_INTERVALL_MS);
			assertFalse("NOT stopped after four periods", stopped.get());
			Thread.sleep(RemoteProcedure.WATCHER_THREAD_INTERVALL_MS);
			assertFalse("NOT stopped after five periods", stopped.get());
			Thread.sleep(RemoteProcedure.WATCHER_THREAD_INTERVALL_MS);
			assertFalse("NOT stopped after six periods", stopped.get());

			for (int i=0; i < 10; i++) {
				Thread.sleep(RemoteProcedure.WATCHER_THREAD_INTERVALL_MS);
			}
			Thread.sleep(10000);

			// Now 16 re-try periods plus 10s have passed, we should surely be stopped by now
			assertTrue("stopped after max re-tries reached", stopped.get());

		} finally {
			server.stop();
		}
	}

	@Test
	public void testGetTechnology() {
		// make sure that all procedure-ids report correct technology, even if they are not started at all remotely
		assertNull(createProc(Constants.Procedures.INPROCESS_DBMS_ID).getTechnology());
		assertEquals(Technology.DOTNET_20, createProc(Constants.Procedures.PAYMENT_BACKEND_ID).getTechnology());
		assertEquals(Technology.DOTNET_20, createProc(Constants.Procedures.B2B_FRONTEND_ID).getTechnology());
		assertEquals(Technology.JAVA, createProc(Constants.Procedures.BUSINESS_BACKEND_ID).getTechnology());
		assertEquals(Technology.JAVA, createProc(Constants.Procedures.CUSTOMER_FRONTEND_ID).getTechnology());
		assertEquals(Technology.JAVA, createProc(Constants.Procedures.ANT_ID).getTechnology());
		assertEquals(Technology.JAVA, createProc(Constants.Procedures.CASSANDRA_ID).getTechnology());
		assertEquals(Technology.ADK, createProc(Constants.Procedures.CREDIT_CARD_AUTH_UNIT_ID).getTechnology());
		assertEquals(Technology.MONGODB, createProc(Constants.Procedures.MONGO_DB_ID).getTechnology());
		assertEquals(Technology.MYSQL, createProc(Constants.Procedures.INPROCESS_MYSQL_ID).getTechnology());
		assertEquals(Technology.WEBSERVER, createProc(Constants.Procedures.APACHE_HTTPD_ID).getTechnology());
		assertEquals(Technology.WEBPHPSERVER, createProc(Constants.Procedures.APACHE_HTTPD_PHP_ID).getTechnology());
        assertEquals(Technology.NGINX, createProc(Constants.Procedures.NGINX_WEBSERVER_ID).getTechnology());
        assertEquals(Technology.VAGRANT, createProc(Constants.Procedures.VAGRANT_ID).getTechnology());

		// try with a proc that is actually started which should return a valid Technology by itself
		RemoteProcedure javaProc = createProc(Constants.Procedures.PLUGIN_SERVICE);
		final AtomicBoolean called = new AtomicBoolean(false);
		javaProc.addStopListener(new StopListener() {

			@Override
			public void notifyProcessStopped() {
				called.set(true);
			}

			@Override
			public void notifyProcessFailed() {
				called.set(true);
			}
		});

		assertEquals(Technology.JAVA, javaProc.getTechnology());
		javaProc.run();
		assertFalse("Not stopped or failed now", called.get());
		assertEquals(Technology.JAVA, javaProc.getTechnology());
		javaProc.stop();
	}

	protected RemoteProcedure createProc(String id) {
		final DefaultProcedureMapping mapping = new DefaultProcedureMapping(id);
		RemoteProcedure proc = new RemoteProcedure(mapping, "127.0.0.1");
		return proc;
	}

	@Test
	public void testName() {
		final DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID);

		RemoteProcedure proc = new RemoteProcedure(mapping, "127.0.0.1");
		assertEquals("Derby Database (127.0.0.1)", proc.getName());

		proc = new RemoteProcedure(mapping, null);
		assertEquals("Derby Database (null)", proc.getName());

		EasyTravelConfig.read().shortHostDisplay=true;
		try {
			proc = new RemoteProcedure(mapping, "127.0.0.1");
			assertEquals("Derby Database (127)", proc.getName());

			proc = new RemoteProcedure(mapping, "somehost");
			assertEquals("Derby Database (somehost)", proc.getName());
		} finally {
			EasyTravelConfig.read().shortHostDisplay = false;
		}
	}

	@Test
	public void testGetURI() throws IOException {
		// use a mock rest-interface here to avoid starting all those procedures...
		final EasyTravelConfig config = EasyTravelConfig.read();
		final int launcherPort = config.launcherHttpPort;
		String urlCust = config.frontendPublicUrl;
		String urlAngular = config.angularFrontendPublicUrl;
		String urlB2B = config.b2bFrontendPublicUrl;
		String urlApache = config.apacheFrontendPublicUrl;

		HTTPResponseRunnable runnable = new HTTPResponseRunnable() {
			private int port = 7654;
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				if(uri.startsWith("/prepare")) {
					UUID uuid = UUID.randomUUID();
					port++;
					return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, uuid.toString() + "|" + port);
				} else if(uri.startsWith("/start/")) {
					return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, State.STARTING.toString());
				} else if(uri.startsWith("/stop/")) {
					return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "OK");
				} else {
					throw new IllegalStateException("Unknown uri: " + uri);
				}
			}
		};

		final MockRESTServer server = new MockRESTServer(runnable);
		try {
			config.launcherHttpPort = server.getPort();

			DefaultProcedureMapping mappingCust = new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID);
			RemoteProcedure procCust = new RemoteProcedure(mappingCust, "127.0.0.1");
			assertEquals(Feedback.Success, procCust.run());
			
			DefaultProcedureMapping mappingAngular = new DefaultProcedureMapping(Constants.Procedures.ANGULAR_FRONTEND_ID);
			RemoteProcedure procAngular = new RemoteProcedure(mappingAngular, "127.0.0.1");
			assertEquals(Feedback.Success, procAngular.run());

			DefaultProcedureMapping mappingB2B = new DefaultProcedureMapping(Constants.Procedures.B2B_FRONTEND_ID);
			RemoteProcedure procB2B = new RemoteProcedure(mappingB2B, "127.0.0.1");
			assertEquals(Feedback.Success, procB2B.run());

			DefaultProcedureMapping mappingApache = new DefaultProcedureMapping(Constants.Procedures.APACHE_HTTPD_ID);
			RemoteProcedure procApache = new RemoteProcedure(mappingApache, "127.0.0.1");
			assertEquals(Feedback.Success, procApache.run());

			DefaultProcedureMapping mappingPHP = new DefaultProcedureMapping(Constants.Procedures.APACHE_HTTPD_PHP_ID);
			RemoteProcedure procPHP = new RemoteProcedure(mappingPHP, "127.0.0.1");
			assertEquals(Feedback.Success, procPHP.run());

            DefaultProcedureMapping nginx = new DefaultProcedureMapping(Constants.Procedures.NGINX_WEBSERVER_ID);
			RemoteProcedure procNginx = new RemoteProcedure(nginx, "127.0.0.1");
			assertEquals(Feedback.Success, procNginx.run());

			assertEquals("http://127.0.0.1:7655/", procCust.getURI());
			assertEquals("http://127.0.0.1:7656/", procAngular.getURI());
			assertEquals("http://127.0.0.1:7657/", procB2B.getURI());
			assertEquals("http://127.0.0.1:7658/", procApache.getURI());
			assertEquals("http://127.0.0.1:7659/", procPHP.getURI());
			assertEquals("http://127.0.0.1:7660/", procNginx.getURI());

            String fqdnHost = LocalUriProvider.getFQDN("127.0.0.1");
            assertThat(TextUtils.merge("http://{0}:7655/", fqdnHost), is(equalToIgnoringCase(procCust.getURIDNS())));
            assertThat(TextUtils.merge("http://{0}:7656/", fqdnHost), is(equalToIgnoringCase(procAngular.getURIDNS())));
            assertThat(TextUtils.merge("http://{0}:7657/", fqdnHost), is(equalToIgnoringCase(procB2B.getURIDNS())));
            assertThat(TextUtils.merge("http://{0}:7658/", fqdnHost), is(equalToIgnoringCase(procApache.getURIDNS())));
            assertThat(TextUtils.merge("http://{0}:7659/", fqdnHost), is(equalToIgnoringCase(procPHP.getURIDNS())));
            assertThat(TextUtils.merge("http://{0}:7660/", fqdnHost), is(equalToIgnoringCase(procNginx.getURIDNS())));

			// now with non-null, but empty URLs
			config.frontendPublicUrl = "";
			config.angularFrontendPublicUrl = "";
			config.b2bFrontendPublicUrl = "";
			config.apacheFrontendPublicUrl = "";

			assertEquals("http://127.0.0.1:7655/", procCust.getURI());
			assertEquals("http://127.0.0.1:7656/", procAngular.getURI());
			assertEquals("http://127.0.0.1:7657/", procB2B.getURI());
			assertEquals("http://127.0.0.1:7658/", procApache.getURI());
			assertEquals("http://127.0.0.1:7659/", procPHP.getURI());

			// now with URLs
			config.frontendPublicUrl = "urlcust";
			config.angularFrontendPublicUrl = "urlangular";
			config.b2bFrontendPublicUrl = "urlb2b";
			config.apacheFrontendPublicUrl = "urlapache";

			assertEquals("urlcust", procCust.getURI());
			assertEquals("urlangular", procAngular.getURI());
			assertEquals("urlb2b", procB2B.getURI());
			assertEquals("urlapache", procApache.getURI());
			assertEquals("urlapache", procPHP.getURI());

			// done
			procCust.stop();
			procAngular.stop();
			procB2B.stop();
			procApache.stop();
			procPHP.stop();
            procNginx.stop();
        } finally {
			server.stop();
		}

		// restore config-values
		config.frontendPublicUrl = urlCust;
		config.angularFrontendPublicUrl = urlAngular;
		config.b2bFrontendPublicUrl = urlB2B;
		config.apacheFrontendPublicUrl = urlApache;
		config.launcherHttpPort = launcherPort;
	}

	@Test
	public void testGetURIUrlType() {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.APACHE_HTTPD_ID);

		EasyTravelConfig config = EasyTravelConfig.read();
		String url1 = config.apacheFrontendPublicUrl;
		String url2 = config.apacheB2BFrontendPublicUrl;

        config.apacheFrontendPublicUrl = "url1234";
        config.apacheB2BFrontendPublicUrl = "url8193";

		RemoteProcedure proc = new RemoteProcedure(mapping, "127.0.0.1");
		assertEquals("url1234", proc.getURI(UrlType.APACHE_JAVA_FRONTEND));
		assertEquals("url1234", proc.getURIDNS(UrlType.APACHE_JAVA_FRONTEND));
		assertNull(proc.getURI(UrlType.APACHE_BUSINESS_BACKEND));
		assertEquals("url8193", proc.getURI(UrlType.APACHE_B2B_FRONTEND));
		assertEquals("url8193", proc.getURIDNS(UrlType.APACHE_B2B_FRONTEND));
		assertNull(proc.getURI(UrlType.APACHE_PROXY));

		// same for php
		mapping = new DefaultProcedureMapping(Constants.Procedures.APACHE_HTTPD_PHP_ID);
		proc = new RemoteProcedure(mapping, "127.0.0.1");

		assertEquals("url1234", proc.getURI(UrlType.APACHE_JAVA_FRONTEND));
		assertEquals("url1234", proc.getURIDNS(UrlType.APACHE_JAVA_FRONTEND));
		assertNull(proc.getURI(UrlType.APACHE_BUSINESS_BACKEND));
		assertEquals("url8193", proc.getURI(UrlType.APACHE_B2B_FRONTEND));
		assertEquals("url8193", proc.getURIDNS(UrlType.APACHE_B2B_FRONTEND));
		assertNull(proc.getURI(UrlType.APACHE_PROXY));

		// set null public url to get some null values
        config.apacheFrontendPublicUrl = null;
        config.apacheB2BFrontendPublicUrl = null;

		assertEquals(LocalUriProvider.getUri("127.0.0.1", config.apacheWebServerPort, "/"), proc.getURI(UrlType.APACHE_JAVA_FRONTEND));
        assertThat(LocalUriProvider.getUriDNS("127.0.0.1", config.apacheWebServerPort, "/"), is(equalToIgnoringCase(proc.getURIDNS(UrlType.APACHE_JAVA_FRONTEND))));
		assertEquals(LocalUriProvider.getUri("127.0.0.1", config.apacheWebServerB2bPort, "/"), proc.getURI(UrlType.APACHE_B2B_FRONTEND));
        assertThat(LocalUriProvider.getUriDNS("127.0.0.1", config.apacheWebServerB2bPort, "/"), is(equalToIgnoringCase(proc.getURIDNS(UrlType.APACHE_B2B_FRONTEND))));
		assertNull(proc.getURI(UrlType.APACHE_BUSINESS_BACKEND));
		assertNull(proc.getURI(UrlType.APACHE_PROXY));

		// restore config-values
        config.apacheFrontendPublicUrl = url1;
        config.apacheB2BFrontendPublicUrl = url2;
	}

	@Test
	public void testIsInstrumented() throws IOException {
		final AtomicBoolean called = new AtomicBoolean(false);

		HTTPResponseRunnable runnable = new HTTPResponseRunnable() {

			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				LOGGER.info("Header: " + header);
				LOGGER.info("Params: " + parms);
				LOGGER.info("URI: " + uri);
				LOGGER.info("Method: " + method);

				called.set(true);

				if (uri.startsWith("/prepare")) {
					UUID uuid = UUID.randomUUID();
					return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, uuid.toString() + "|1234");
				} else if (uri.startsWith("/start/")) {
					return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, State.STARTING.toString());
				} else if (uri.startsWith("/isInstrumentationSupported/")) {
					return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, Boolean.TRUE.toString());
				} else if (uri.startsWith("/stop/")) {
					return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "OK");
				} else {
					throw new IllegalStateException("Unknown uri: " + uri);
				}
			}
		};

		MockRESTServer server = new MockRESTServer(runnable);
		try {

			final EasyTravelConfig config = EasyTravelConfig.read();
			config.launcherHttpPort = server.getPort();

			final DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID);
			final RemoteProcedure proc = new RemoteProcedure(mapping, "127.0.0.1");

			assertFalse(proc.isInstrumentationSupported());

			// REST Server should not have been called, because we did not prepare
			assertFalse(called.get());


			// now prepare first
			proc.run();

			assertTrue(called.get());
			called.set(false);

			assertTrue(proc.isInstrumentationSupported());

			// REST Server should have been called now
			assertTrue(called.get());

			proc.stop();
		} finally {
			server.stop();
		}
	}

	@Ignore("Test ignored. It seems that for testing .Net procedure integration tests would be better choice")
	@Test
	public void testDotNetFrontendIISServer() throws IOException {
		final EasyTravelConfig config = EasyTravelConfig.read();

		MockRESTServer server = new MockRESTServer(new MockRESTServer.HTTPResponseRunnable() {
			@Override
			public NanoHTTPD.Response run(String uri, String method, Properties header, Properties parms) {
				NanoHTTPD.Response resp = new NanoHTTPD.Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "ok");
				resp.addHeader("Server","IIS");
				return resp;
			}
		});

		try {
			DefaultProcedureMapping mappingB2B = new DefaultProcedureMapping(Constants.Procedures.B2B_FRONTEND_ID);
			RemoteProcedure procB2B = new RemoteProcedure(mappingB2B, "127.0.0.1");
			assertEquals(Feedback.Success, procB2B.run());

			config.b2bFrontendPageToIdentify = "";
			config.b2bFrontendPortRangeStart = server.getPort();

			procB2B.stop();
		} finally {
			server.stop();
			EasyTravelConfig.resetSingleton();
		}
	}

	@Ignore("Test ignored. It seems that for testing .Net procedure integration tests would be better choice")
	@Test
	public void testDotNetPaymentIISServer() throws IOException {
		final EasyTravelConfig config = EasyTravelConfig.read();

		MockRESTServer server = new MockRESTServer(new MockRESTServer.HTTPResponseRunnable() {
			@Override
			public NanoHTTPD.Response run(String uri, String method, Properties header, Properties parms) {
				NanoHTTPD.Response resp = new NanoHTTPD.Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "ok");
				resp.addHeader("Server","IIS");
				return resp;
			}
		});

		try {
			DefaultProcedureMapping mappingPayment = new DefaultProcedureMapping(Constants.Procedures.PAYMENT_BACKEND_ID);
			RemoteProcedure procPayment = new RemoteProcedure(mappingPayment, "127.0.0.1");
			assertEquals(Feedback.Success, procPayment.run());

			config.paymentBackendPageToIdentify= "";
			config.paymentBackendPort = server.getPort();

			boolean procPaymentOnIIS = procPayment.isRunningOnIIS();
			assertTrue(procPaymentOnIIS);

			procPayment.stop();

		} finally {
			server.stop();
			EasyTravelConfig.resetSingleton();
		}
	}
}
