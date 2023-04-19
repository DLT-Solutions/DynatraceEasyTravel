package com.dynatrace.easytravel.util;

import static org.junit.Assert.*;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.net.ssl.SSLException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.junit.Assume;
import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.net.UrlUtils.Availability;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.NanoHTTPD.Response;
import com.dynatrace.easytravel.utils.TestHelpers;


public class DtSSLHelperTest {
	private static final Logger LOGGER = Logger.getLogger(DtSSLHelper.class.getName());
	
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005 Safari/537.36";

	@Test
	public void testGetDataInvalidHost() throws Exception {
		try {
			new DtSSLHelper(10000, USER_AGENT).getData("http://invalidurl/");
			fail("Should throw exception");
		} catch (UnknownHostException e) {
			TestHelpers.assertContains(e, "invalidurl");
		}
	}

	@Test
	public void testGetData() throws Exception {
		MockRESTServer server = new MockRESTServer("200", null, "ok");
		try {
			assertEquals("ok", new DtSSLHelper(10000, USER_AGENT).getData("http://localhost:" + server.getPort()));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testGetDataHttps() throws Exception {
		Availability checkConnect = UrlUtils.checkConnect("https://www.dynatrace.com/", 10000);
		Assume.assumeTrue("Should be able to read from https://www.google.com, but had: " + checkConnect, checkConnect == Availability.CONNECT_OK);

		assertTrue(StringUtils.isNotEmpty(new DtSSLHelper(10000, USER_AGENT).getData("https://www.dynatrace.com/")));
	}

	@Test
	public void testGetDataDemoDev() throws Exception {
		Availability checkConnect = UrlUtils.checkRead("SECRET", 10000);
		Assume.assumeTrue("Should be able to read from SECRET, but had: " + checkConnect, checkConnect == Availability.READ_OK);
		checkConnect = UrlUtils.checkRead("SECRET", 10000);
		Assume.assumeTrue("Should be able to read from SECRET, but had: " + checkConnect, checkConnect == Availability.READ_OK);

		// this usually has no effect any more here untill we convert DtSSLHelper to a normal instantiated class...
		EasyTravelConfig CONFIG = EasyTravelConfig.read();
		CONFIG.apmServerUsername = CONFIG.dtServerUsername;
		CONFIG.apmServerPassword = CONFIG.dtServerPassword;

		String data = new DtSSLHelper(10000, USER_AGENT).getData("SECRET");
		LOGGER.info("Had: " + data);
		assertTrue("Should get some data from ruxitdev, but had empty: " + data, StringUtils.isNotEmpty(data));

		assertTrue("Expected to find the start of version 1 in the returned HTML of SECRET, see the log for the data", data.contains("(v.1."));
	}

	// test timeout
	@Test
	public void testTimeout() throws Exception {
		final AtomicBoolean doStop = new AtomicBoolean(false);

		// set different timeout to make the test run faster
		MockRESTServer server = new MockRESTServer(new MockRESTServer.HTTPResponseRunnable() {

			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				// sleeep a long time so we run into a timeout
				try {
					while(!doStop.get()) {
						Thread.sleep(100);
					}
				} catch (InterruptedException e) {
					throw new IllegalStateException(e);
				}

				return null;
			}
		});

		try {
	    	new DtSSLHelper(500, USER_AGENT).getData("http://localhost:" + server.getPort());
	    	fail("Should fail here but did not...");
	    } catch (SocketTimeoutException e) {
	    	// expected
		} finally {
			doStop.set(true);
			server.stop();
		}
	}

	@Test
	public void testTimeoutSSL() throws Exception {
		final AtomicBoolean doStop = new AtomicBoolean(false);

		// set different timeout to make the test run faster
		MockRESTServer server = new MockRESTServer(new MockRESTServer.HTTPResponseRunnable() {

			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				// sleeep a long time so we run into a timeout
				try {
					while(!doStop.get()) {
						Thread.sleep(100);
					}
				} catch (InterruptedException e) {
					throw new IllegalStateException(e);
				}

				return null;
			}
		});

		try {
	    	new DtSSLHelper(500, USER_AGENT).getData("https://localhost:" + server.getPort());
	    	fail("Should fail here but did not...");
	    } catch (SSLException e) {
	    	// expected
	    } catch (ConnectTimeoutException e) {
	    	// expected
		} finally {
			doStop.set(true);
			server.stop();
		}
	}
}
