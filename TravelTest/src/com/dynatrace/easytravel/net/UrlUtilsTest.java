package com.dynatrace.easytravel.net;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.ipc.SocketUtils;
import com.dynatrace.easytravel.utils.*;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPResponseRunnable;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD.Response;

import ch.qos.logback.classic.Level;

public class UrlUtilsTest {

	@Test
	public void testRetrieveDataString() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected\n\r\t");
		try {
			assertEquals("expected\n\r\t",
					UrlUtils.retrieveData("http://localhost:" + server.getPort()));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRetrieveDataStringEncodingTimeout() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected\n\r\t");
		try {
			assertEquals("expected\n\r\t",
					UrlUtils.retrieveData("http://localhost:" + server.getPort(), null, 10000));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRetrieveDataStringPost() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected\n\r\t", "POST");
		try {
			assertEquals("expected\n\r\t",
					UrlUtils.retrieveDataPost("http://localhost:" + server.getPort(), BaseConstants.UTF8, "requestbody", "text/plain"));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRetrieveDataStringPostTimeout() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected\n\r\t", "POST");
		try {
			assertEquals("expected\n\r\t",
					UrlUtils.retrieveDataPost("http://localhost:" + server.getPort(), BaseConstants.UTF8, "requestbody", "text/plain", 10000));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRetrieveDataStringPostNullEncoding() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected\n\r\t", "POST");
		try {
			assertEquals("expected\n\r\t",
					UrlUtils.retrieveDataPost("http://localhost:" + server.getPort(), null, "requestbody", "text/plain"));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRetrieveDataStringPostNullBody() throws Exception {
		try {
			UrlUtils.retrieveDataPost("http://localhost", null, null, "text/plain");
			fail("Should catch exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "POST request body must not be null");
		}
	}

	@Test
	public void testRetrieveRawDataPost() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected\n\r\t", "POST");
		try {
			assertEquals("expected\n\r\t",
					new String(UrlUtils.retrieveRawDataPost("http://localhost:" + server.getPort(), "requestbody", "text/plain")));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRetrieveRawDataPostTimeout() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected\n\r\t", "POST");
		try {
			assertEquals("expected\n\r\t",
					new String(UrlUtils.retrieveRawDataPost("http://localhost:" + server.getPort(), "requestbody", "text/plain", 10000)));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRetrieveDataStringPostNullContentType() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected\n\r\t", "POST");
		try {
			assertEquals("expected\n\r\t",
					UrlUtils.retrieveDataPost("http://localhost:" + server.getPort(), BaseConstants.UTF8, "requestbody", null));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRetrieveDataStringPostWrongMethod1() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected\n\r\t", "GET");
		try {
			assertEquals("expected\n\r\t",
					UrlUtils.retrieveDataPost("http://localhost:" + server.getPort(), BaseConstants.UTF8, "requestbody", "text/plain"));
			fail("Should catch IOException with connection error information here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "405", "http://localhost:" + server.getPort(), "Method Not Allowed"); // error code and url are mentioned in the error message
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRetrieveDataStringPostWrongMethod2() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected\n\r\t", "POST");
		try {
			assertEquals("expected\n\r\t",
					UrlUtils.retrieveData("http://localhost:" + server.getPort(), BaseConstants.UTF8));
			fail("Should catch IOException with connection error information here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "405", "http://localhost:" + server.getPort(), "Method Not Allowed"); // error code and url are mentioned in the error message
		} finally {
			server.stop();
		}
	}


	@Test
	public void testRetrieveDataStringCreated() throws Exception {
		MockRESTServer server = new MockRESTServer("201 CREATED", NanoHTTPD.MIME_HTML, "expected");
		try {
			assertEquals("expected",
					UrlUtils.retrieveData("http://localhost:" + server.getPort()));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRetrieveDataStringAccepted() throws Exception {
		MockRESTServer server = new MockRESTServer("202 ACCEPTED", NanoHTTPD.MIME_HTML, "expected");
		try {
			assertEquals("expected",
					UrlUtils.retrieveData("http://localhost:" + server.getPort()));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRetrieveDataStringFailed() throws Exception {
		MockRESTServer server = new MockRESTServer("404 NOT FOUND", NanoHTTPD.MIME_HTML, "expected");
		try {
			UrlUtils.retrieveData("http://localhost:" + server.getPort());
			fail("Should catch IOException with connection error information here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "404", "http://localhost:" + server.getPort()); // error code and url are mentioned in the error message
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRetrieveDataStringTimeout() throws Exception {
		MockRESTServer server = new MockRESTServer("201 CREATED", NanoHTTPD.MIME_HTML, "expected");
		try {
			assertEquals("expected", UrlUtils.retrieveData("http://localhost:" + server.getPort(), 10000));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRetrieveRawDataNoContent() throws Exception {
		MockRESTServer server = new MockRESTServer("204 NO CONTENT", NanoHTTPD.MIME_HTML, "");
		try {
			assertTrue(Arrays.equals("".getBytes(), UrlUtils.retrieveRawData("http://localhost:" + server.getPort())));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRetrieveRawData() throws Exception {
		MockRESTServer server = new MockRESTServer("201 CREATED", NanoHTTPD.MIME_HTML, "expected");
		try {
			assertTrue(Arrays.equals("expected".getBytes(), UrlUtils.retrieveRawData("http://localhost:" + server.getPort())));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRetrieveRawDataTimeout() throws Exception {
		MockRESTServer server = new MockRESTServer("201 CREATED", NanoHTTPD.MIME_HTML, "expected");
		try {
			assertTrue(Arrays.equals("expected".getBytes(), UrlUtils.retrieveRawData("http://localhost:" + server.getPort(), 10000)));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRetrieveDataStringString() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected\u00F6\u00C4\u20AC");
		try {
			assertEquals("expected\u00F6\u00C4\u20AC",
					UrlUtils.retrieveData("http://localhost:" + server.getPort(), "UTF-8"));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRetrieveDataStringStringEncoded() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected\u00F6\u00C4\u20AC");
		try {
			assertEquals("expected\u00C3\u00B6\u00C3\u0084\u00E2\u0082\u00AC",
					UrlUtils.retrieveData("http://localhost:" + server.getPort(), "ISO-8859-15"));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testRetrieveDataStringStringInt() throws Exception {
		HTTPRunnable runnable = new HTTPRunnable() {
			@Override
			public void run(String uri, String method, Properties header, Properties parms) {
				try {
					Thread.sleep(1300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};

		MockRESTServer server = new MockRESTServer(runnable, NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "OK");
		try {
			UrlUtils.retrieveData("http://localhost:" + server.getPort(), null, 1000);
			fail("Should timeout here!");
		} catch (SocketTimeoutException e) {
			// expected
		} finally {
			server.stop();
		}
	}

	@Test
	public void testGenerateHeader() throws Exception {
		Map<String,String> requiredHeaders = new HashMap<String,String>();
		Map<String,String> wrongHeaders = new HashMap<String,String>();
		requiredHeaders.put("Server", "nginx");
		wrongHeaders.put("Server", "apache");
		HTTPResponseRunnable responseRunnable = new HTTPResponseRunnable() {
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				Response response = new Response();
					
					response.addHeader("Server", "nginx/1.8.0");

				return response;
			}
		};

		MockRESTServer server = new MockRESTServer(responseRunnable);
		try {
			assertTrue(UrlUtils.checkHeaders("http://localhost:" + server.getPort(), requiredHeaders).isOK());
			assertFalse(UrlUtils.checkHeaders("http://localhost:" + server.getPort(), wrongHeaders).isOK());
		} finally {
			server.stop();
		}
	}
	@Test
	public void testIsAvailable() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected");
		
		try {
			assertTrue(UrlUtils.checkConnect("http://localhost:" + server.getPort()).isOK());
			assertTrue(UrlUtils.checkConnect("http://localhost:" + server.getPort()).isOK());
			assertFalse(UrlUtils.checkConnect("http://notexistinghost:" + server.getPort()).isOK());
			assertTrue(UrlUtils.checkRead("http://localhost:" + server.getPort()).isOK());
			assertTrue(UrlUtils.checkRead("http://localhost:" + server.getPort()).isOK());
			assertFalse(UrlUtils.checkRead("http://notexistinghost:" + server.getPort()).isOK());
		} finally {
			server.stop();
		}
	}

	@Test
	public void testCheck() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected");
		try {
			assertEquals(UrlUtils.Availability.CONNECT_OK, UrlUtils.checkConnect("http://localhost:" + server.getPort()));
			assertEquals(UrlUtils.Availability.CONNECT_OK, UrlUtils.checkConnect("http://localhost:" + server.getPort() + "/"));
			assertEquals(UrlUtils.Availability.READ_OK, UrlUtils.checkRead("http://localhost:" + server.getPort()));
			assertEquals(UrlUtils.Availability.READ_OK, UrlUtils.checkRead("http://localhost:" + server.getPort() + "/"));
			assertEquals(UrlUtils.Availability.UNKNOWN_HOST, UrlUtils.checkRead("http://notexistinghost:" + server.getPort()));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testCheckNoContent() throws Exception {
		MockRESTServer server = new MockRESTServer("204 NO CONTENT", NanoHTTPD.MIME_HTML, "");
		try {
			assertEquals(UrlUtils.Availability.CONNECT_OK, UrlUtils.checkConnect("http://localhost:" + server.getPort()));
			assertEquals(UrlUtils.Availability.READ_OK, UrlUtils.checkRead("http://localhost:" + server.getPort()));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testCheckError() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_INTERNALERROR, NanoHTTPD.MIME_HTML, "expected");
		try {
			assertEquals(UrlUtils.Availability.SERVER_ERROR, UrlUtils.checkRead("http://localhost:" + server.getPort()));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testCheckBadRequest() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_BADREQUEST, NanoHTTPD.MIME_HTML, "expected");
		try {
			assertEquals(UrlUtils.Availability.SERVER_ERROR, UrlUtils.checkRead("http://localhost:" + server.getPort()));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testCheckForbidden() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_FORBIDDEN, NanoHTTPD.MIME_HTML, "expected");
		try {
			assertEquals(UrlUtils.Availability.SERVER_ERROR, UrlUtils.checkRead("http://localhost:" + server.getPort()));
		} finally {
			server.stop();
		}
	}
	@Test
	public void testCheckNotFound() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_NOTFOUND, NanoHTTPD.MIME_HTML, "expected");
		try {
			assertEquals(UrlUtils.Availability.NOT_FOUND, UrlUtils.checkRead("http://localhost:" + server.getPort()));
		} finally {
			server.stop();
		}
	}
	@Test
	public void testCheckNotImplemented() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_NOTIMPLEMENTED, NanoHTTPD.MIME_HTML, "expected");
		try {
			assertEquals(UrlUtils.Availability.SERVER_ERROR, UrlUtils.checkRead("http://localhost:" + server.getPort()));
		} finally {
			server.stop();
		}
	}
	@Test
	public void testCheckRedirect() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_REDIRECT, NanoHTTPD.MIME_HTML, "expected");
		try {
			assertEquals(UrlUtils.Availability.CONNECT_OK, UrlUtils.checkConnect("http://localhost:" + server.getPort()));
			assertEquals(UrlUtils.Availability.READ_OK, UrlUtils.checkRead("http://localhost:" + server.getPort()));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testIsAvailableInvalidUrl() throws Exception {
		try {
			UrlUtils.checkRead("invalidurl");
			fail("Should catch exception because of invalid url here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "Invalid destination URL");
		}
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(UrlUtils.class);
	}

	@Test
	public void testRunWithDifferentLoglevel() {
		final AtomicReference<Exception> exception = new AtomicReference<Exception>(null);
		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {

			@Override
			public void run() {
				try {
					testRetrieveDataString();
					testRetrieveDataStringCreated();
					testRetrieveDataStringAccepted();
					testRetrieveDataStringFailed();
					testRetrieveDataStringString();
					testRetrieveDataStringTimeout();
					testRetrieveRawDataNoContent();
					testIsAvailable();
					testCheck();
					testCheckBadRequest();
					testCheckError();
					testCheckForbidden();
					testCheckNotFound();
					testCheckNotImplemented();
					testCheckRedirect();
					testCheckNoContent();
				} catch (Exception e) {
					exception.set(e);
				}

			}
		}, UrlUtils.class.getName(), Level.DEBUG);

		assertNull(exception.get());
	}

	@Test
	public void testResolveAddress() throws UnknownHostException {
		assertEquals("192.128.4.92", UrlUtils.resolveAddress("192.128.4.92"));
		// TODO@(stefan.moschinski): add further address resolution test by mocking
	}

	@Test
	public void testIsInternetUrl() {
		assertFalse(UrlUtils.isInternetUrl(""));
		//assertFalse(UrlUtils.isInternetUrl(null));
		assertFalse(UrlUtils.isInternetUrl("a"));
		assertFalse(UrlUtils.isInternetUrl("dynatrace.vmta"));
		assertFalse(UrlUtils.isInternetUrl("www.dynatrace.vmta"));
		assertFalse(UrlUtils.isInternetUrl("www.cpwr.corp"));
		assertFalse(UrlUtils.isInternetUrl("cpwr.corp"));
		assertFalse(UrlUtils.isInternetUrl(".cpwr.corp"));

		// should this be false?!?
		assertTrue(UrlUtils.isInternetUrl("127.0.0.1"));
		assertTrue(UrlUtils.isInternetUrl("10.32.123.43"));

		assertTrue(UrlUtils.isInternetUrl("www.example.com"));
		assertTrue(UrlUtils.isInternetUrl("www7.example.com"));
		assertTrue(UrlUtils.isInternetUrl("house.example.com"));
	}

	@Test
	public void testAvailability() {
		assertFalse(UrlUtils.Availability.UNKNOWN_HOST.isOK());
		assertTrue(UrlUtils.Availability.CONNECT_OK.isOK());
		assertFalse(UrlUtils.Availability.CONNECT_TIMEOUT.isOK());
		assertFalse(UrlUtils.Availability.NOT_FOUND.isOK());
		assertTrue(UrlUtils.Availability.READ_OK.isOK());
		assertFalse(UrlUtils.Availability.READ_TIMEOUT.isOK());
		assertFalse(UrlUtils.Availability.SERVER_ERROR.isOK());
		assertFalse(UrlUtils.Availability.UNKNOWN_HOST.isOK());

		assertFalse(UrlUtils.Availability.UNKNOWN_HOST.timedOut());
		assertFalse(UrlUtils.Availability.CONNECT_OK.timedOut());
		assertTrue(UrlUtils.Availability.CONNECT_TIMEOUT.timedOut());
		assertFalse(UrlUtils.Availability.NOT_FOUND.timedOut());
		assertFalse(UrlUtils.Availability.READ_OK.timedOut());
		assertTrue(UrlUtils.Availability.READ_TIMEOUT.timedOut());
		assertFalse(UrlUtils.Availability.SERVER_ERROR.timedOut());
		assertFalse(UrlUtils.Availability.UNKNOWN_HOST.timedOut());
	}

	@Test
	public void testTimeoutZero() {
		try {
			UrlUtils.checkConnect("http://localhost/", 0);
			fail("Should throw exception with 0 timeout");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "Zero", "timeouts not permitted");
		}
	}

	@Test
	public void testServiceAvailability() throws IOException {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected");
		try {
			assertTrue(UrlUtils.checkServiceAvailability("localhost", server.getPort()));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testServiceAvailabilityUnknownHost() {
		assertFalse(UrlUtils.checkServiceAvailability("notexistinghost", 1));
	}

	@Test
	public void testServiceAvailabilityIOException() throws IOException {
		// ensure we use a port where nothing is listening
		int port = SocketUtils.reserveNextFreePort(10000, 11000, "localhost");
		try {
			assertFalse(UrlUtils.checkServiceAvailability("localhost", port));
		} finally {
			SocketUtils.freePort(port);
		}
	}

	@Test
	public void testWithDifferentLogLevel() {
		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {

			@Override
			public void run() {
				try {
					testServiceAvailability();
					testServiceAvailabilityIOException();
					testServiceAvailabilityUnknownHost();
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
			}
		}, UrlUtils.class.getName(), Level.DEBUG);
	}
}
