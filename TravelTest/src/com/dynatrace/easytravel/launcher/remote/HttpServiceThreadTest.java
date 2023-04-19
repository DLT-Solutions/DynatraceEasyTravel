package com.dynatrace.easytravel.launcher.remote;

import static org.junit.Assert.*;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.integration.IntegrationTestBase;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.remote.HttpServiceThread;
import com.dynatrace.easytravel.remote.ShutdownRequestHandler;
import com.dynatrace.easytravel.utils.TestHelpers;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.net.httpserver.HttpServer;

public class HttpServiceThreadTest {
    private static final EasyTravelConfig CONFIG = EasyTravelConfig.read();
	private static final Logger LOGGER = LoggerFactory.make();

    // plus one to not collide with running launcher
    private static final int port = CONFIG.launcherHttpPort + 1;

	/*@BeforeClass
	public static void setUpClass() throws IOException {
		LoggerFactory.initLogging();
	}*/

    @Before
    public void setUp() {
    	// port should be free again after the test
    	IntegrationTestBase.checkPort(port);

    	// debug failures on Linux
    	//Logger.getGlobal().addHandler(new ConsoleHandler());
    	//Logger.getLogger("com.sun.net.httpserver").setLevel(Level.FINER);
    	//org.apache.log4j.Logger.getLogger("com.sun.net.httpserver").setLevel(org.apache.log4j.Level.DEBUG);
    }

    @After
    public void tearDown() throws InterruptedException {
    	// wait a short while to ensure that the port is freed up
    	TestHelpers.waitForPort(port, 3000);

        // clean out leftover batches to not affect other tests
        RESTProcedureControl.getBatches().clear();
    }

    @Test
	public void testStartStopRESTServer() throws Exception {
        Runnable exitInDisplayThreadRunnable = new Runnable() {
            @Override
            public void run() {
                throw new IllegalStateException("Should not be called in this test!");
            }
        };

        HttpServiceThread remoteController = new HttpServiceThread(port, exitInDisplayThreadRunnable);
        remoteController.start();

        remoteController.stopService();
	}

    @Test
	public void testStartRESTServerAndSomeProcedure() throws Exception {
        Runnable exitInDisplayThreadRunnable = new Runnable() {
            @Override
            public void run() {
                throw new IllegalStateException("Should not be called in this test!");
            }
        };

        HttpServiceThread remoteController = new HttpServiceThread(port, exitInDisplayThreadRunnable);
        remoteController.start();

        try {
			ClientConfig clientConfig = new DefaultClientConfig();
		    Client client = Client.create(clientConfig);

	        // ping should work now
		    WebResource r = client.resource("http://localhost:" + port + "/ping");
		    String response = r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
		    assertEquals("pong", response);

	        // statusAll should work now, but not have any result
		    r = client.resource("http://localhost:" + port + "/statusAll");
		    response = r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
		    assertEquals("", response);

		    // prepare should work now,
            r = client.resource("http://localhost:" + port + "/prepare/" + Constants.Procedures.INPROCESS_DBMS_ID.replace(" ", "%20"));
            response = r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
            // should be a valid UUID
            UUID uuid = UUID.fromString(response.substring(0, response.indexOf('|')));
            assertEquals(response.substring(0, response.indexOf('|')), uuid.toString());

	        // start should work now,
		    r = client.resource("http://localhost:" + port + "/start/" + uuid.toString());
		    response = r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
		    // should be a valid UUID
		    assertEquals(State.OPERATING.toString(), response);

	        // statusAll should work now, but not have any result
		    r = client.resource("http://localhost:" + port + "/statusAll");
		    response = r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
		    assertFalse("We should have some state now...", response.isEmpty());

		    // stop procedure again
		    r = client.resource("http://localhost:" + port + "/stop/" + uuid.toString());
		    response = r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
		    // should be a valid UUID
		    assertEquals("OK", response);

	        // stopAll should work now, but not have any result
		    r = client.resource("http://localhost:" + port + "/stopAll");
		    response = r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
		    assertEquals("OK", response);


		    // stop procedure again, should not work
		    r = client.resource("http://localhost:" + port + "/stop/" + uuid.toString());
		    response = r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
		    // should be a valid UUID
		    assertTrue(response, response.startsWith("NOTOK"));
        } finally {
        	remoteController.stopService();
        }
	}

    @Test
	public void testShutdownRESTServer() throws Exception {
        final AtomicBoolean stop = new AtomicBoolean(false);
    	Runnable exitInDisplayThreadRunnable = new Runnable() {
            @Override
            public void run() {
                stop.set(true);
            }
        };

        HttpServiceThread remoteController = new HttpServiceThread(port, exitInDisplayThreadRunnable);
        remoteController.start();

		ClientConfig clientConfig = new DefaultClientConfig();
	    Client client = Client.create(clientConfig);

	    // shutdown
	    WebResource r = client.resource("http://localhost:" + port + "/shutdown");
	    /*String response =*/ r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);

	    /* This now shuts down immediately and thus does not return anything any more
	    // should be "OK"
	    assertTrue("Had response: " + response, response.startsWith("OK"));*/

	    Thread.sleep(1000);

	    assertTrue("Should have seen shutdown in Runnable", stop.get());

        remoteController.stopService();
	}

    @Test
    public void testShutdownHandler() {
    	// fails on null

    	try {
    		ShutdownRequestHandler.setShutdownExecutor(null);
    		fail("Should catch exception");
    	} catch (IllegalArgumentException e) {
    		TestHelpers.assertContains(e, "not allowed");
    	}
    }

    // run on the remote CI machine with:
    // 	chmod -R a+rw src && /home/demouser/devtools/apache-ant-1.7.1/bin/ant -Dtestpattern=RESTProcedureClientTest runtest
    @Test
    public void testLab13() throws Exception {
		final String baseUri = "http://localhost:" + port + "/";

		HttpServer server;

		LOGGER.info("Starting REST server on URI: '" + baseUri + "'...");
		ResourceConfig config = new PackagesResourceConfig("com.dynatrace.easytravel.launcher.remote");

        server = HttpServerFactory.create(baseUri, config);
        server.start();

        try {
	        // ping should work now
			LOGGER.info("Calling REST...");
			assertEquals("pong", UrlUtils.retrieveData("http://localhost:" + port + "/ping"));
			LOGGER.info("Done with REST call");
        } finally {
    		LOGGER.info("Stopping REST server");

    		server.stop(0);

    		// Workaround for Jersey shortcoming: It adds a ThreadPoolExecutor with 60 seconds Worker-timeout
    		// which causes the application to wait up to 60 seconds before actually shutting down
    		// because there is still a non-daemon thread alive...
    		Executor executor = server.getExecutor();
    		if(executor instanceof ExecutorService) {
    			((ExecutorService)executor).shutdown();
    		}
        }
    }
}
