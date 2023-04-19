package com.dynatrace.diagnostics.uemload.utils;


import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.Header;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.diagnostics.uemload.Bandwidth;
import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.diagnostics.uemload.NavigationTiming;
import com.dynatrace.diagnostics.uemload.http.base.HttpRequest.Type;
import com.dynatrace.diagnostics.uemload.http.base.UemLoadConnection;
import com.dynatrace.diagnostics.uemload.http.base.UemLoadCookieStore;
import com.dynatrace.diagnostics.uemload.http.callback.HttpReaderCallback;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.ThreadTestHelper;


public class UemLoadHttpUtilTest {
	private final static Logger log = LoggerFactory.make();

	public static final int TEST_THREAD_COUNT = 10;
	private static final int RUNS = 500;

	@Test
	public void readResponse() throws IOException {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
		try {
			UemLoadConnection connection = new UemLoadConnection(HttpClientBuilder.create().build(), "http://localhost:" + server.getPort(), Type.GET, new UemLoadCookieStore(), null);
			Collection<Header> headers = Collections.emptyList();
			connection.connect(headers);
			assertTrue("Need a valid response", connection.isResponseValid());

			final AtomicReference<String> ref = new AtomicReference<String>();
			UemLoadHttpUtils.readResponse(connection, Bandwidth.DSL_HIGH, BrowserType.NONE, NavigationTiming.NONE, new HttpReaderCallback() {

				@Override
				public void readDone(byte[] bytes) throws IOException {
					ref.set(new String(bytes));
				}
			});

			assertEquals("Need response to match what we provide in NanoHTTPD", "response", ref.get());

			connection.close();
		} finally {
			server.stop();
		}
	}

	@Test
	public void readResponseUnlimited() throws IOException {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
		try {
			UemLoadConnection connection = new UemLoadConnection(HttpClientBuilder.create().build(), "http://localhost:" + server.getPort(), Type.GET, new UemLoadCookieStore(), null);
			Collection<Header> headers = Collections.emptyList();
			connection.connect(headers);
			assertTrue("Need a valid response", connection.isResponseValid());

			final AtomicReference<String> ref = new AtomicReference<String>();
			UemLoadHttpUtils.readResponse(connection, Bandwidth.UNLIMITED, BrowserType.NONE, NavigationTiming.NONE, new HttpReaderCallback() {

				@Override
				public void readDone(byte[] bytes) throws IOException {
					ref.set(new String(bytes));
				}
			});

			assertEquals("Need response to match what we provide in NanoHTTPD", "response", ref.get());

			connection.close();
		} finally {
			server.stop();
		}
	}

	@Test
	public void readResponseUnavailable() throws IOException {
		UemLoadConnection connection = new UemLoadConnection(HttpClientBuilder.create().build(), "ftp://invalidhost", Type.GET, new UemLoadCookieStore(), null);
		Collection<Header> headers = Collections.emptyList();
		connection.connect(headers);
		assertFalse("Need an invalid response", connection.isResponseValid());
		connection.close();
	}


	@Test
	public void isConnectable() throws Throwable {
		final MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
		try {
			assertTrue("Should be connectable", UemLoadHttpUtils.isConnectable("http://localhost:" + server.getPort()));
		} finally {
			server.stop();
		}
	}

	@Test
	public void isNotConnectable() throws Throwable {
		final MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
		try {
			assertFalse("Should not be connectable", UemLoadHttpUtils.isConnectable("http://notexisting" + server.getPort()));
		} finally {
			server.stop();
		}
	}

	@Test
	public void isConnectableThreaded() throws Throwable {
		final MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
		try {
	        ThreadTestHelper helper =
	                new ThreadTestHelper(TEST_THREAD_COUNT, RUNS);

	            helper.executeTest(new ThreadTestHelper.TestRunnable() {
	                @Override
	                public void doEnd(int threadnum) throws Exception {
	                    // do stuff at the end ...
	                }

	                @Override
	                public void run(int threadnum, int iter) throws Exception {
	                	if(log.isDebugEnabled()) {
	                		log.debug("Starting work in thread " + threadnum + " iter: " + iter);
	                	}

						assertTrue("Should be connectable in thread " + threadnum + " iter: " + iter, UemLoadHttpUtils.isConnectable("http://localhost:" + server.getPort()));
	                }
	            });
		} finally {
			server.stop();
		}
	}
}
