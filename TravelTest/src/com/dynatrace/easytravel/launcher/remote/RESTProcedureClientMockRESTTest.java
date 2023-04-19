package com.dynatrace.easytravel.launcher.remote;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.configuration.tree.DefaultConfigurationNode;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.config.ConfigurationReader;
import com.dynatrace.easytravel.launcher.config.NodeFactory;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureSetting;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.*;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPResponseRunnable;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD.Response;

import ch.qos.logback.classic.Level;

public class RESTProcedureClientMockRESTTest {
    private static final Logger LOGGER = LoggerFactory.make();

    private static final int NUMBER_OF_THREADS = 10;
	private static final int NUMBER_OF_TESTS = 100;

	@BeforeClass
	public static void setUpClass() throws IOException {
		LoggerFactory.initLogging();
	}

	@Test
	public void testRemoteHostPropertyIsTransferred() throws IOException {
		final AtomicBoolean called = new AtomicBoolean(false);
		HTTPRunnable runnable = new HTTPRunnable() {
			@Override
			public void run(String uri, String method, Properties header, Properties parms) {
				LOGGER.info("Header: " + header);
				LOGGER.info("Params: " + parms);
				LOGGER.info("URI: " + uri);
				LOGGER.info("Method: " + method);

				// NOTE: NanoHTTP only supports one item per param, not a list which REST/URIs support, so we only get the last query param here in the test!

				String environment = parms.getProperty("environment");
				assertNotNull("Environment should not be null", environment);
				assertEquals(environment, "com.dynatrace.easytravel.host.b2b_frontend" + Constants.REST.PROPERTY_DELIMITER + "myhost123");

				for(String proc : Constants.Procedures.ALL_REMOTE) {
					if(proc.equals(Constants.Procedures.B2B_FRONTEND_ID)) {
						continue;
					}

					assertFalse(environment.contains(proc.replace(" ", "_").toLowerCase()));
				}

				called.set(true);
			}
		};

		MockRESTServer server = new MockRESTServer(runnable, NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "UUID|2343");
		try {

			final EasyTravelConfig config = EasyTravelConfig.read();
			config.launcherHttpPort = server.getPort();

			final DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID);
			final RESTProcedureClient client = new RESTProcedureClient(mapping, "127.0.0.1");

			System.setProperty("com.dynatrace.easytravel.host.b2b_frontend", "myhost123");
			try {
				client.prepare();
			} finally {
				System.clearProperty("com.dynatrace.easytravel.host.b2b_frontend");
			}

			// REST Server should have been called
			assertTrue(called.get());
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRemoteHostPropertyIsTransferredAdditional() throws IOException {
		final AtomicBoolean called = new AtomicBoolean(false);
		HTTPRunnable runnable = new HTTPRunnable() {
			@Override
			public void run(String uri, String method, Properties header, Properties parms) {
				LOGGER.info("Header: " + header);
				LOGGER.info("Params: " + parms);
				LOGGER.info("URI: " + uri);
				LOGGER.info("Method: " + method);

				// NOTE: NanoHTTP only supports one item per param, not a list which REST/URIs support, so we only get the last query param here in the test!

				String environment = parms.getProperty("environment");
				assertNotNull(environment);
				assertEquals(environment, "com.dynatrace.easytravel.host.additional" + Constants.REST.PROPERTY_DELIMITER + "dynasprint,dynalive");

				for(String proc : Constants.Procedures.ALL_REMOTE) {
					assertFalse(environment.contains(proc.replace(" ", "_").toLowerCase()));
				}

				called.set(true);
			}
		};

		MockRESTServer server = new MockRESTServer(runnable, NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "UUID|2343");
		try {

			final EasyTravelConfig config = EasyTravelConfig.read();
			config.launcherHttpPort = server.getPort();

			final DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID);
			final RESTProcedureClient client = new RESTProcedureClient(mapping, "127.0.0.1");

			System.setProperty("com.dynatrace.easytravel.host.additional", "dynasprint,dynalive");
			try {
				client.prepare();
			} finally {
				System.clearProperty("com.dynatrace.easytravel.host.additional");
			}

			// REST Server should have been called
			assertTrue(called.get());
		} finally {
			server.stop();
		}
	}


	@Test
	public void testRemoteHostPropertyIsTransferredAdditionalEmpty() throws IOException {
		final AtomicBoolean called = new AtomicBoolean(false);
		HTTPRunnable runnable = new HTTPRunnable() {
			@Override
			public void run(String uri, String method, Properties header, Properties parms) {
				LOGGER.info("Header: " + header);
				LOGGER.info("Params: " + parms);
				LOGGER.info("URI: " + uri);
				LOGGER.info("Method: " + method);

				// NOTE: NanoHTTP only supports one item per param, not a list which REST/URIs support, so we only get the last query param here in the test!

				String environment = parms.getProperty("environment");
				assertNotNull(environment);
				assertEquals(environment, "com.dynatrace.easytravel.host.additional" + Constants.REST.PROPERTY_DELIMITER + "");

				for(String proc : Constants.Procedures.ALL_REMOTE) {
					assertFalse(environment.contains(proc.replace(" ", "_").toLowerCase()));
				}

				called.set(true);
			}
		};

		MockRESTServer server = new MockRESTServer(runnable, NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "UUID|2343");
		try {

			final EasyTravelConfig config = EasyTravelConfig.read();
			config.launcherHttpPort = server.getPort();

			final DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID);
			final RESTProcedureClient client = new RESTProcedureClient(mapping, "127.0.0.1");

			System.setProperty("com.dynatrace.easytravel.host.additional", "");	// set empty on purpose
			try {
				client.prepare();
			} finally {
				System.clearProperty("com.dynatrace.easytravel.host.additional");
			}

			// REST Server should have been called
			assertTrue(called.get());
		} finally {
			server.stop();
		}
	}



	@Test
	public void testRemoteHostPropertyIsTransferredCustomSettings() throws IOException, Exception {
		final AtomicBoolean called = new AtomicBoolean(false);
		HTTPRunnable runnable = new HTTPRunnable() {
			@Override
			public void run(String uri, String method, Properties header, Properties parms) {
				LOGGER.info("Header: " + header);
				LOGGER.info("Params: " + parms);
				LOGGER.info("URI: " + uri);
				LOGGER.info("Method: " + method);

				// NOTE: NanoHTTP only supports one item per param, not a list which REST/URIs support, so we only get the last query param here in the test!

				String setting = parms.getProperty("setting");
				assertNotNull(setting);
				assertEquals("procedure_config" + DefaultProcedureSetting.DELIMITER + "name1" + DefaultProcedureSetting.DELIMITER + "value3", setting);

				called.set(true);
			}
		};

		MockRESTServer server = new MockRESTServer(runnable, NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "UUID|2343");
		try {
			final EasyTravelConfig config = EasyTravelConfig.read();
			config.launcherHttpPort = server.getPort();

			final DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID);

			{ // add a custom setting
				mapping.addSetting(new DefaultProcedureSetting(Scenario.TYPE_SCENARIO_PROCEDURE_CONFIG, "name1", "value3"));

				NodeFactory factory = new NodeFactory();
				DefaultConfigurationNode node = new DefaultConfigurationNode();
				mapping.write(node, factory);

				mapping.read(node, new ConfigurationReader());
			}

			assertTrue(mapping.hasCustomSettings());
			final RESTProcedureClient client = new RESTProcedureClient(mapping, "127.0.0.1");

			client.prepare();

			// REST Server should have been called
			assertTrue(called.get());
		} finally {
			server.stop();
		}
	}


	@Test
	public void testRemoteHostPropertyIsTransferredStayOnOffSettings() throws IOException, Exception {
		final AtomicBoolean called = new AtomicBoolean(false);
		HTTPRunnable runnable = new HTTPRunnable() {
			@Override
			public void run(String uri, String method, Properties header, Properties parms) {
				LOGGER.info("Header: " + header);
				LOGGER.info("Params: " + parms);
				LOGGER.info("URI: " + uri);
				LOGGER.info("Method: " + method);

				// NOTE: NanoHTTP only supports one item per param, not a list which REST/URIs support, so we only get the last query param here in the test!

				String setting = parms.getProperty("setting");
				assertNotNull(setting);
				assertEquals("procedure_config" + DefaultProcedureSetting.DELIMITER + "name1" + DefaultProcedureSetting.DELIMITER + "value3" + DefaultProcedureSetting.DELIMITER + "10" + DefaultProcedureSetting.DELIMITER + "20", setting);

				called.set(true);
			}
		};

		MockRESTServer server = new MockRESTServer(runnable, NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "UUID|2343");
		try {
			final EasyTravelConfig config = EasyTravelConfig.read();
			config.launcherHttpPort = server.getPort();

			final DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID);

			{ // add a custom setting
				mapping.addSetting(new DefaultProcedureSetting(Scenario.TYPE_SCENARIO_PROCEDURE_CONFIG, "name1", "value3", 10, 20));

				NodeFactory factory = new NodeFactory();
				DefaultConfigurationNode node = new DefaultConfigurationNode();
				mapping.write(node, factory);

				mapping.read(node, new ConfigurationReader());
			}

			assertTrue(mapping.hasCustomSettings());
			final RESTProcedureClient client = new RESTProcedureClient(mapping, "127.0.0.1");

			client.prepare();

			// REST Server should have been called
			assertTrue(called.get());
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRemoteHostPropertyIsTransferredStayOnOffSettingsNoType() throws IOException, Exception {
		final AtomicBoolean called = new AtomicBoolean(false);
		HTTPRunnable runnable = new HTTPRunnable() {
			@Override
			public void run(String uri, String method, Properties header, Properties parms) {
				LOGGER.info("Header: " + header);
				LOGGER.info("Params: " + parms);
				LOGGER.info("URI: " + uri);
				LOGGER.info("Method: " + method);

				// NOTE: NanoHTTP only supports one item per param, not a list which REST/URIs support, so we only get the last query param here in the test!

				String setting = parms.getProperty("setting");
				assertNotNull(setting);
				assertEquals("name1" + DefaultProcedureSetting.DELIMITER + "value3" + DefaultProcedureSetting.DELIMITER + "10" + DefaultProcedureSetting.DELIMITER + "20", setting);

				called.set(true);
			}
		};

		MockRESTServer server = new MockRESTServer(runnable, NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "UUID|2343");
		try {
			final EasyTravelConfig config = EasyTravelConfig.read();
			config.launcherHttpPort = server.getPort();

			final DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID);

			{ // add a custom setting
				mapping.addSetting(new DefaultProcedureSetting(null, "name1", "value3", 10, 20));

				NodeFactory factory = new NodeFactory();
				DefaultConfigurationNode node = new DefaultConfigurationNode();
				mapping.write(node, factory);

				mapping.read(node, new ConfigurationReader());
			}

			assertFalse(mapping.hasCustomSettings());
			final RESTProcedureClient client = new RESTProcedureClient(mapping, "127.0.0.1");

			client.prepare();

			// REST Server should have been called
			assertTrue(called.get());
		} finally {
			server.stop();
		}
	}

	@Test
	public void testWithDifferentLogLevel() {
		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {

			@Override
			public void run() {
				try {
					testRemoteHostPropertyIsTransferred();
					testRemoteHostPropertyIsTransferredAdditional();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}, RESTProcedureClient.class.getName(), Level.DEBUG);
	}

    @Test
    public void testMultipleThreads() throws Throwable {
		final AtomicInteger called = new AtomicInteger(0);
		final Map<String, Integer> map = new ConcurrentHashMap<String, Integer>();
		final Random rnd = new Random(System.currentTimeMillis());

		HTTPResponseRunnable runnable = new HTTPResponseRunnable() {

			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				/*LOGGER.info("Header: " + header);
				LOGGER.info("Params: " + parms);
				LOGGER.info("URI: " + uri);
				LOGGER.info("Method: " + method);*/

				called.incrementAndGet();

				if(uri.startsWith("/prepare")) {
					UUID uuid = UUID.randomUUID();
					map.put(uuid.toString(), rnd.nextInt());

					return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, uuid.toString() + "|" + map.get(uuid.toString()));
				} else if(uri.startsWith("/start/")) {
					assertTrue("Looking for: " + StringUtils.removeStart(uri, "/start/") +
							"\nHad: " + map,
							map.containsKey(StringUtils.removeStart(uri, "/start/")));
					return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, State.STARTING.toString());
				} else if(uri.startsWith("/stop/")) {
					assertTrue("Looking for: " + StringUtils.removeStart(uri, "/stop/") +
							"\nHad: " + map,
							map.containsKey(StringUtils.removeStart(uri, "/stop/")));
					return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "OK");
				} else if(uri.startsWith("/log/")) {
					String uuid = StringUtils.removeStart(uri, "/log/");
					assertTrue("Looking for: " + uuid +
							"\nHad: " + map,
							map.containsKey(uuid));
					return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, Integer.toString(map.get(uuid)));
				} else {
					throw new IllegalStateException("Unknown uri: " + uri);
				}
			}
		};

		final MockRESTServer server = new MockRESTServer(runnable);
		try {
			ThreadTestHelper helper =
	            new ThreadTestHelper(NUMBER_OF_THREADS, NUMBER_OF_TESTS);

	        helper.executeTest(new ThreadTestHelper.TestRunnable() {
	            @Override
	            public void doEnd(int threadnum) throws Exception {
	                // do stuff at the end ...
	            }

	            @Override
	            public void run(int threadnum, int iter) throws Exception {
	    			final EasyTravelConfig config = EasyTravelConfig.read();
	    			config.launcherHttpPort = server.getPort();

	    			final DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID);

	    			final RESTProcedureClient client = new RESTProcedureClient(mapping, "127.0.0.1");
	    			String uuid = client.prepare();
	    			assertNotNull("Should get an UUID after prepare(), but had null", uuid);
					assertTrue("Had: " + map, map.containsKey(uuid));
	    			Integer retPort = Integer.valueOf(client.getPort());
					assertEquals("Expected to get the correct UUID after prepare(), but got: " + retPort,
	    					map.get(uuid), retPort);

	    			assertEquals("Expected state 'STARTING' from start()",
	    					State.STARTING, client.start());

	    			assertEquals("Expected state 'OK' from stop()",
	    					"OK", client.stop());

	    			assertEquals(Integer.toString(client.getPort()), client.getLog());
	            }
	        });
		} finally {
			server.stop();
		}

		assertEquals(4 /* four requests per iteration */ * NUMBER_OF_THREADS * NUMBER_OF_TESTS, called.get());
    }

	@Test
	public void testIsInstrumented() throws IOException {
		final AtomicBoolean called = new AtomicBoolean(false);
		HTTPRunnable runnable = new HTTPRunnable() {
			@Override
			public void run(String uri, String method, Properties header, Properties parms) {
				LOGGER.info("Header: " + header);
				LOGGER.info("Params: " + parms);
				LOGGER.info("URI: " + uri);
				LOGGER.info("Method: " + method);

				called.set(true);
			}
		};

		MockRESTServer server = new MockRESTServer(runnable, NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "UUID|2343");
		try {

			final EasyTravelConfig config = EasyTravelConfig.read();
			config.launcherHttpPort = server.getPort();

			final DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID);
			final RESTProcedureClient client = new RESTProcedureClient(mapping, "127.0.0.1");

			assertFalse(client.isInstrumentationSupported());

			// REST Server should not have been called, because we did not prepare
			assertFalse(called.get());

			// now prepare first
			client.prepare();
			assertTrue(called.get());
			called.set(false);
			assertFalse(client.isInstrumentationSupported());

			// REST Server should have been called now
			assertTrue(called.get());
		} finally {
			server.stop();
		}
	}
}
