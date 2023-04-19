package com.dynatrace.easytravel.launcher.remote;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.Ignore;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.net.UrlUtils.Availability;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPResponseRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.NanoHTTPD.Response;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Just verify that the host where the tests are run does what we
 * expect it to in respect to hostname resolution/DNS/...
 *
 * @author cwat-dstadler
 */
public class MockRESTServerTest {
    private static final Logger LOGGER = LoggerFactory.make();

	@Test
	public void testLocalhost() throws IOException {
		runWithHostname("localhost");
	}

    @Ignore @Test
	public void testLocalhostIP() throws IOException {
		runWithHostname("127.0.0.1");
	}

	@Test
	public void testIP() throws IOException {
		InetAddress localHost = java.net.InetAddress.getLocalHost();
		assertNotNull("Should get a local address", localHost);
		String ipaddress = localHost.getHostAddress();

		LOGGER.info("Had hostname: " + ipaddress + ", address-info: " + localHost);

		assertNotNull("Should get a local ip-address", ipaddress);
		assertFalse("Local ip-address should not equal localhost", ipaddress.equals("localhost"));
		// cannot assert on startsWith("127.0.0") as e.g. lab13 reports an ip-address of 127.0.0.2
		assertFalse("Local ip-address should not equal 127.0.0.1", ipaddress.equals("127.0.0.1"));

		runWithHostname(ipaddress);
	}

	@Test
	public void testHostname() throws IOException {
		assertNotNull(java.net.InetAddress.getLocalHost());
		String hostname = java.net.InetAddress.getLocalHost().getHostName();
		assertNotNull(hostname);
		assertFalse("Local hostname should not equal localhost", hostname.equals("localhost"));
		assertFalse("Local hostname should not start with 127.0.0", hostname.startsWith("127.0.0"));

		runWithHostname(hostname);
	}

	@Test
	public void testCanonicalHostname() throws IOException {
		assertNotNull(java.net.InetAddress.getLocalHost());
		String hostname = java.net.InetAddress.getLocalHost().getCanonicalHostName();
		assertNotNull(hostname);
		assertFalse(hostname.equals("localhost"));
		assertFalse(hostname.startsWith("127.0.0"));

		runWithHostname(hostname);
	}

	private void runWithHostname(String hostname) throws IOException {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK,  NanoHTTPD.MIME_PLAINTEXT, "OK");
		try {
			Availability check = UrlUtils.checkConnect("http://" + hostname + ":" + server.getPort(), 500);
			assertTrue("Host: " + hostname + ": Had: " + check, check.isOK());

			check = UrlUtils.checkRead("http://" + hostname + ":" + server.getPort(), 500);
			assertTrue("Host: " + hostname + ": Had: " + check, check.isOK());

			String data = UrlUtils.retrieveData("http://" + hostname + ":" + server.getPort(), 500);
			assertEquals("Host: " + hostname + ": Had: " + data, "OK", data);
		} finally {
			server.stop();
		}
	}

	@Ignore("This fails sporadically because NanoHTTPD does not implement HTTP PUT correctly and thus sometimes streams are cut-off in-between")
	@Test
	public void testCouchDBContentCreation() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK,  NanoHTTPD.MIME_PLAINTEXT, "OK");
		try {
			Availability check = UrlUtils.checkConnect("http://localhost:" + server.getPort(), 50000000);
			assertTrue("Host: localhost: Had: " + check, check.isOK());

			check = UrlUtils.checkRead("http://localhost:" + server.getPort(), 50000000);
			assertTrue("Host: localhost: Had: " + check, check.isOK());

			String data = UrlUtils.retrieveData("http://localhost:" + server.getPort(), 50000000);
			assertEquals("Host: localhost: Had: " + data, "OK", data);
		} finally {
			server.stop();
		}

		HTTPResponseRunnable runable = new HTTPResponseRunnable() {
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				Response response = null;
				if (uri.contains("result_pic_1.png")) {
					response = new NanoHTTPD.Response("201 Created", "text/plain; charset=utf-8",
							"{\"ok\":true,\"id\":\"result_pic_1.png\",\"rev\":\"1-60f662112976a8ec6208b322422159bc\"}");
				} else {
					throw new IllegalStateException("Unexpected: " + uri);
				}

				// This seem necessary for all of the above responses.
				response.addHeader("Server", "CouchDB/1.6.1 (Erlang OTP/R16B02)");
				return response;
			}
		};

		// start a dummy REST server which returns an specific UUID
		server = new MockRESTServer(runable);

		try {
			final ClientConfig clientConfig = new DefaultClientConfig();
			final Client client = Client.create(clientConfig);

			String fileName = "result_pic_1.png";

			WebResource r = client.resource("http://localhost:" + server.getPort() + "/easy_travel_images/" + fileName);

			// Note "/" to get from root location in the jar.
			try (InputStream ISFromResource = MockRESTServer.class.getResourceAsStream("/" + fileName)) {
				assertNotNull("FAILED TO GET IMAGE FROM RESOURCE: " + "/" + fileName, ISFromResource);

				MultivaluedMap<String, String> params = new MultivaluedMapImpl();
				String tmp = r
						.queryParams(params)
						.entity(ISFromResource)
						.accept(MediaType.TEXT_PLAIN_TYPE)
						.put(String.class);

				assertNotNull(tmp);
			}
		} finally {
			server.stop();
		}
	}
}
