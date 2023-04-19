package com.dynatrace.easytravel.plugin;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.ipc.SocketUtils;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.*;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPResponseRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD.Response;


public class RemotePluginControllerTest {
	private static final Logger LOGGER = LoggerFactory.make();

	private final AtomicBoolean failed = new AtomicBoolean();

	private RemotePluginController controller = new RemotePluginController();

	@After
	public void tearDown() {
		// the tests check this as well to fail early, but make sure we do it for every method at least once
		assertFalse("Should not have failed in the HTTPResponseRunnable, but did, look at logs for details", failed.get());
	}

	@Test
	public void testSendEnabledNoBackend() throws IOException {
		// fail early if BusinessBackend is running on this host

		int port = EasyTravelConfig.read().backendPort;
		assertTrue("BusinessBackend should not run for this test, but seems to be running on port: " + port,
				SocketUtils.isPortAvailable(port, null) &&
				SocketUtils.isPortAvailable(port, "localhost"));

		// this will cause an error with backend which will be logged
		String enabled = controller.sendEnabled("someplugin", true, null);
		assertNull("Should be null, but had: " + enabled, enabled);
		enabled = controller.sendEnabled("someplugin", false, null);
		assertNull("Should be null, but had: " + enabled, enabled);

		try {
			controller.sendEnabled(null, true, null);
			fail("Should throw exception because of null input");
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "without a plugin name");
		}
	}

	@Test
	public void testSendEnabled() throws IOException {
		MockRESTServer server = new MockRESTServer(new HTTPResponseRunnable() {
			int count = 0;
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				count++;
				if(count == 1) {
					return expectUriContains(uri, parms.toString(), "/ConfigurationService/setPluginEnabled", "someplugin");
				} else {
					fail("With uri: " + uri + " params: " + parms);
					return null;
				}
			}
		});

		try {
			EasyTravelConfig.read().webServiceBaseDir = "http://localhost:" + server.getPort() + "/";

			// this will cause an error with backend which will be logged
			assertNotNull(controller.sendEnabled("someplugin", true, null));

			assertFalse("Should not have failed in the HTTPResponseRunnable, but did, look at logs for details", failed.get());
		} finally {
			server.stop();
		}
	}

    @Test
    public void testSendEnabledSpaceBreak() throws IOException {
        MockRESTServer server = new MockRESTServer(new HTTPResponseRunnable() {
            int count = 0;
            @Override
            public Response run(String uri, String method, Properties header, Properties parms) {
                count++;
                if(count == 1) {
                    return expectUriContains(uri, parms.toString(), "/ConfigurationService/setPluginEnabled", "some plugin");
                } else {
                    fail("With uri: " + uri + " params: " + parms);
                    return null;
                }
            }
        });

        try {
            EasyTravelConfig.read().webServiceBaseDir = "http://localhost:" + server.getPort() + "/";

            // this will cause an error with backend which will be logged
            assertNotNull(controller.sendEnabled("some plugin", true, null));

            assertFalse("Should not have failed in the HTTPResponseRunnable, but did, look at logs for details", failed.get());
        } finally {
            server.stop();
        }
    }

    @Test
    public void testSendEnabledPathEncoded() throws IOException {
        MockRESTServer server = new MockRESTServer(new HTTPResponseRunnable() {
            int count = 0;
            @Override
            public Response run(String uri, String method, Properties header, Properties parms) {
                count++;
                if(count == 1) {
                    return expectUriContains(uri, parms.toString(), "/ConfigurationService/setPluginEnabled", "some%20plugin");
                } else {
                    fail("With uri: " + uri + " params: " + parms);
                    return null;
                }
            }
        });

        try {
            EasyTravelConfig.read().webServiceBaseDir = "http://localhost:" + server.getPort() + "/";

            // this will cause an error with backend which will be logged
            assertNotNull(controller.sendEnabled("some%20plugin", true, null));

            assertFalse("Should not have failed in the HTTPResponseRunnable, but did, look at logs for details", failed.get());
        } finally {
            server.stop();
        }
    }

    @Test
	public void testSendEnabledWithCallback() throws IOException {
		final AtomicBoolean called = new AtomicBoolean(false);
		Runnable callback = new Runnable() {
			@Override
			public void run() {
				called.set(true);
			}
		};

		MockRESTServer server = new MockRESTServer(new HTTPResponseRunnable() {
			int count = 0;
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				count++;
				if(count == 1) {
					return expectUriContains(uri, parms.toString(), "/ConfigurationService/setPluginEnabled", "someplugin");
				} else {
					fail("With uri: " + uri + " params: " + parms);
					return null;
				}
			}
		});

		try {
			EasyTravelConfig.read().webServiceBaseDir = "http://localhost:" + server.getPort() + "/";

			// this will cause an error with backend which will be logged
			assertNotNull(controller.sendEnabled("someplugin", true, callback));

			assertFalse("Should not have failed in the HTTPResponseRunnable, but did, look at logs for details", failed.get());
		} finally {
			server.stop();
		}

		assertTrue("Callback should have been called, but wasn't", called.get());
	}

    @Test
	public void testSendEnabledWithCallbackWithSpecialCharacters() throws IOException {
		final AtomicBoolean called = new AtomicBoolean(false);
		Runnable callback = new Runnable() {
			@Override
			public void run() {
				called.set(true);
			}
		};

		MockRESTServer server = new MockRESTServer(new HTTPResponseRunnable() {
			int count = 0;
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				count++;
				if(count == 1) {
					return expectUriContains(uri, parms.toString(), "/ConfigurationService/setPluginEnabled", "~so ? me. ^plug; i*n! $");
				} else {
					fail("With uri: " + uri + " params: " + parms);
					return null;
				}
			}
		});

		try {
			EasyTravelConfig.read().webServiceBaseDir = "http://localhost:" + server.getPort() + "/";

			// this will cause an error with backend which will be logged
			assertNotNull(controller.sendEnabled("~so ? me. ^plug; i*n! $", true, callback));

			assertFalse("Should not have failed in the HTTPResponseRunnable, but did, look at logs for details", failed.get());
		} finally {
			server.stop();
		}

		assertTrue("Callback should have been called, but wasn't", called.get());
	}

	@Test
	public void testSendEnabledFailed() {
		try {
			controller.sendEnabled(null, true, null);
			fail("Should throw exception because of null input");
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "without a plugin name");
		}
	}


	@Test
	public void testSendPluginStateChanged() throws IOException {
		MockRESTServer server = new MockRESTServer(new HTTPResponseRunnable() {
			int count = 0;
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				count++;
				if(count == 1) {
					return expectUriContains(uri, parms.toString(), "/ConfigurationService/setPluginEnabled", "someplugin");
				} else {
					fail("With uri: " + uri + " params: " + parms);
					return null;
				}
			}
		});

		try {
			EasyTravelConfig.read().webServiceBaseDir = "http://localhost:" + server.getPort() + "/";

			// this will cause an error with backend which will be logged
			controller.sendPluginStateChanged("someplugin", false);

			assertFalse("Should not have failed in the HTTPResponseRunnable, but did, look at logs for details", failed.get());
		} finally {
			server.stop();
		}
	}

    @Test
	public void testSendPluginStateChangedSpaceBreak() throws IOException {
		MockRESTServer server = new MockRESTServer(new HTTPResponseRunnable() {
			int count = 0;
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				count++;
				if(count == 1) {
					return expectUriContains(uri, parms.toString(), "/ConfigurationService/setPluginEnabled", "some plugin");
				} else {
					fail("With uri: " + uri + " params: " + parms);
					return null;
				}
			}
		});

		try {
			EasyTravelConfig.read().webServiceBaseDir = "http://localhost:" + server.getPort() + "/";

			// this will cause an error with backend which will be logged
			controller.sendPluginStateChanged("some plugin", false);

			assertFalse("Should not have failed in the HTTPResponseRunnable, but did, look at logs for details", failed.get());
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRequestAllPlugins() throws IOException {
		MockRESTServer server = new MockRESTServer(new HTTPResponseRunnable() {
			int count = 0;
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				count++;
				if(count == 1) {
					return expectUriContains(uri, parms.toString(), "/ConfigurationService/getAllPlugins", null);
				} else {
					fail("With uri: " + uri + " params: " + parms);
					return null;
				}
			}
		});

		try {
			EasyTravelConfig.read().webServiceBaseDir = "http://localhost:" + server.getPort() + "/";

			controller.requestAllPlugins();

			assertFalse("Should not have failed in the HTTPResponseRunnable, but did, look at logs for details", failed.get());
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRequestAllPluginNames() throws IOException {
		MockRESTServer server = new MockRESTServer(new HTTPResponseRunnable() {
			int count = 0;
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				count++;
				if(count == 1) {
					return expectUriContains(uri, parms.toString(), "/ConfigurationService/getAllPluginNames", null);
				} else {
					fail("With uri: " + uri + " params: " + parms);
					return null;
				}
			}
		});

		try {
			EasyTravelConfig.read().webServiceBaseDir = "http://localhost:" + server.getPort() + "/";

			controller.requestAllPluginNames();

			assertFalse("Should not have failed in the HTTPResponseRunnable, but did, look at logs for details", failed.get());
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRequestEnabledPlugins() throws IOException {
		MockRESTServer server = new MockRESTServer(new HTTPResponseRunnable() {
			int count = 0;
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				count++;
				if(count == 1) {
					return expectUriContains(uri, parms.toString(), "/ConfigurationService/getEnabledPlugins", null);
				} else {
					fail("With uri: " + uri + " params: " + parms);
					return null;
				}
			}
		});

		try {
			EasyTravelConfig.read().webServiceBaseDir = "http://localhost:" + server.getPort() + "/";

			controller.requestEnabledPlugins();

			assertFalse("Should not have failed in the HTTPResponseRunnable, but did, look at logs for details", failed.get());
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRequestEnabledPluginNames() throws IOException {
		MockRESTServer server = new MockRESTServer(new HTTPResponseRunnable() {
			int count = 0;
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				count++;
				if(count == 1) {
					return expectUriContains(uri, parms.toString(), "/ConfigurationService/getEnabledPluginNames", null);
				} else {
					fail("With uri: " + uri + " params: " + parms);
					return null;
				}
			}
		});

		try {
			EasyTravelConfig.read().webServiceBaseDir = "http://localhost:" + server.getPort() + "/";

			controller.requestEnabledPluginNames();

			assertFalse("Should not have failed in the HTTPResponseRunnable, but did, look at logs for details", failed.get());
		} finally {
			server.stop();
		}
	}

	@Test
	public void testParseReturnedXML() throws IOException {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "<xml><ns:return>plugin 1</ns:return><ns:return>plugin2</ns:return></xml>");
		try {
			EasyTravelConfig.read().webServiceBaseDir = "http://localhost:" + server.getPort() + "/";

			String[] names = controller.requestEnabledPluginNames();
			assertEquals("Had: " + names,
					2, names.length);

			assertEquals("plugin 1", names[0]);
			assertEquals("plugin2", names[1]);
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRegisterPlugin() throws IOException {
		MockRESTServer server = new MockRESTServer(new HTTPResponseRunnable() {
			int count = 0;
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				count++;
				if(count == 1) {
					return expectUriContains(uri, parms.toString(), "/ConfigurationService/registerPlugins", "someplugin");
				} else {
					fail("With uri: " + uri + " params: " + parms);
					return null;
				}
			}
		});

		try {
			EasyTravelConfig.read().webServiceBaseDir = "http://localhost:" + server.getPort() + "/";

			controller.registerPlugin("someplugin");

			assertFalse("Should not have failed in the HTTPResponseRunnable, but did, look at logs for details", failed.get());
		} finally {
			server.stop();
		}

		try {
			controller.registerPlugin("someplugin");
			fail("Shoult catch Exception");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "Connection refused");
		}
	}

    @Test
	public void testRegisterPluginSpaceBreak() throws IOException {
		MockRESTServer server = new MockRESTServer(new HTTPResponseRunnable() {
			int count = 0;
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				count++;
				if(count == 1) {
					return expectUriContains(uri, parms.toString(), "/ConfigurationService/registerPlugins", "some plugin");
				} else {
					fail("With uri: " + uri + " params: " + parms);
					return null;
				}
			}
		});

		try {
			EasyTravelConfig.read().webServiceBaseDir = "http://localhost:" + server.getPort() + "/";

			controller.registerPlugin("some plugin");

			assertFalse("Should not have failed in the HTTPResponseRunnable, but did, look at logs for details", failed.get());
		} finally {
			server.stop();
		}

		try {
			controller.registerPlugin("some plugin");
			fail("Shoult catch Exception");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "Connection refused");
		}
	}

	@Test
	public void testSetPluginHosts() throws IOException {
		MockRESTServer server = new MockRESTServer(new HTTPResponseRunnable() {
			int count = 0;
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				count++;
				if(count == 1) {
					return expectUriContains(uri, parms.toString(), "/ConfigurationService/registerPlugins", "someplugin");
				} else if (count == 2) {
					expectUriContains(uri, parms.toString(), "/ConfigurationService/setPluginHosts", "name=someplugin");
					return expectUriContains(uri, parms.toString(), "/ConfigurationService/setPluginHosts", "host2");	// only the last param is returned by NanoHTTPD!
				} else {
					fail("With uri: " + uri + " params: " + parms);
					return null;
				}
			}
		});

		try {
			EasyTravelConfig.read().webServiceBaseDir = "http://localhost:" + server.getPort() + "/";

			controller.registerPlugin("someplugin");
			controller.setPluginHosts("someplugin", new String[] {"host1", "host2"});

			assertFalse("Should not have failed in the HTTPResponseRunnable, but did, look at logs for details", failed.get());
		} finally {
			server.stop();
		}

		try {
			controller.setPluginHosts("someplugin", new String[] {"host1", "host2"});
			fail("Shoult catch Exception");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "Connection refused");
		}
	}

    @Test
	public void testSetPluginHostsSpaceBreak() throws IOException {
		MockRESTServer server = new MockRESTServer(new HTTPResponseRunnable() {
			int count = 0;
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				count++;
				if(count == 1) {
					return expectUriContains(uri, parms.toString(), "/ConfigurationService/registerPlugins", "some plugin");
				} else if (count == 2) {
					expectUriContains(uri, parms.toString(), "/ConfigurationService/setPluginHosts", "name=some plugin");
					return expectUriContains(uri, parms.toString(), "/ConfigurationService/setPluginHosts", "host2");	// only the last param is returned by NanoHTTPD!
				} else {
					fail("With uri: " + uri + " params: " + parms);
					return null;
				}
			}
		});

		try {
			EasyTravelConfig.read().webServiceBaseDir = "http://localhost:" + server.getPort() + "/";

			controller.registerPlugin("some plugin");
			controller.setPluginHosts("some plugin", new String[] {"host1", "host2"});

			assertFalse("Should not have failed in the HTTPResponseRunnable, but did, look at logs for details", failed.get());
		} finally {
			server.stop();
		}

		try {
			controller.setPluginHosts("someplugin", new String[] {"host1", "host2"});
			fail("Shoult catch Exception");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "Connection refused");
		}
	}

	private Response expectUriContains(String uri, String params, String contains, String paramsContain) {
		try {
			LOGGER.info("Had: " + uri + " and params " + params + ", expecting " + contains + " and " + paramsContain);
			assertTrue(uri.contains(contains));
			assertTrue(paramsContain == null || params.contains(paramsContain));

			return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "ok");
		} catch (Throwable e) {
			failed.set(true);
			throw new RuntimeException(e);
		}
	}

}
