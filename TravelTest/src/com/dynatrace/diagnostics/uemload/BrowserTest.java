package com.dynatrace.diagnostics.uemload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.cookie.Cookie;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.http.base.HttpRequest;
import com.dynatrace.diagnostics.uemload.http.base.HttpResponse;
import com.dynatrace.diagnostics.uemload.http.base.UemLoadHttpClient;
import com.dynatrace.diagnostics.uemload.http.callback.HttpResponseCallback;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.UEMLoadSession;
import com.dynatrace.easytravel.misc.CommonUser;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD;

public class BrowserTest {
	private static final FullyRandomLocation FULLY_RANDOM_LOCATION = new FullyRandomLocation();
	private static final CommonUser DEMO_USER = new CommonUser("user", "user");

	@Ignore("Integration test")
	@Test
	public void testGetSpecialOffersRaw() throws IOException {
		String url = "SECRET";
		UemLoadHttpClient client = new UemLoadHttpClient(Bandwidth.DIALUP, BrowserType.ANDROID_4_1);
		HttpRequest request = new HttpRequest(url);
		UEMLoadSession session = new CustomerSession(url, DEMO_USER, FULLY_RANDOM_LOCATION.get(), false);
		client.executeResourceRequest(request, session, new HttpResponseCallback() {
			
			@Override
			public void readDone(HttpResponse response) throws IOException {
				System.out.println("finished");				
			}
		});
	}
		
	@Test
	public void test() throws IOException {
		Browser browser = new Browser(BrowserType.FF_530, FULLY_RANDOM_LOCATION.get(), 0, Bandwidth.DSL_HIGH, BrowserWindowSize._1280x1024);
		browser.addHeader("testheader", "somevalue");

		final AtomicInteger rest = new AtomicInteger();
		HTTPRunnable runnable = new HTTPRunnable() {
			@Override
			public void run(String uri, String method, Properties header, Properties parms) {
				rest.incrementAndGet();
			}
		};
		MockRESTServer server = new MockRESTServer(runnable, NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "somereturn");

		try {
			String url = "http://localhost:" + server.getPort();
			HttpRequest request = browser.createRequest(url);
			assertNotNull(request);

			CommonUser user = new CommonUser("user", "user");
			CustomerSession session = new CustomerSession(url, user, FULLY_RANDOM_LOCATION.get(), true);

			final AtomicBoolean called = new AtomicBoolean(false);
			browser.startPageLoad(url, "sometitle", session, null, null, new UEMLoadCallback() {

				@Override
				public void run() throws IOException {
					called.set(true);
				}
			});

			assertTrue(called.get());
			assertEquals("Expect one web-request", 1, rest.get());

			assertNotNull(browser.getLocation());

			browser.close();
		} finally {
			server.stop();
		}
	}

	@Test
	@Ignore("Test fails, we should either remove or fix the AJAXWorld and SinglePage scenarios and the related methods Browser.loadPage()/Pageview.runInBrowser")
	public void testNPE() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "somereturn");

		try {
			String url = "http://localhost:" + server.getPort();
			Pageview view = new Pageview(url, "title", null, 100);

			Browser browser = new Browser(BrowserType.FF_530, new Location("some", "where", null), 0, Bandwidth.DSL_HIGH, BrowserWindowSize._1280x1024);
			view.run(browser, null);

			assertNotNull(browser.getLocation());
		} finally {
			server.stop();
		}
	}

	@Test
	public void testOthers() {
		Browser browser = new Browser(BrowserType.FF_530, new Location("some", "where", null), 0, Bandwidth.DSL_HIGH, BrowserWindowSize._1280x1024);
		assertNotNull(browser.getLocation());
		assertEquals("some", browser.getLocation().getContinent());
		assertEquals("where", browser.getLocation().getCountry());

		BrowserType.ANDROID_22.getSpeed();

		browser.close();

	}

	@Test
	public void testLoadResource() throws IOException {
		Browser browser = new Browser(BrowserType.FF_530, FULLY_RANDOM_LOCATION.get(), 0, Bandwidth.DSL_HIGH, BrowserWindowSize._1280x1024);
		browser.addHeader("testheader", "somevalue");

		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "somereturn");

		try {
			final AtomicInteger called = new AtomicInteger();
			String url = "http://localhost:" + server.getPort();
			CommonUser user = new CommonUser("user", "user");
			CustomerSession session = new CustomerSession(url, user, FULLY_RANDOM_LOCATION.get(), true);
			browser.loadResources(Collections.singleton("http://localhost:" + server.getPort() + "/img.png"),
					session, url, false, new UEMLoadCallback() {

						@Override
						public void run() throws IOException {
							called.incrementAndGet();
						}
					});

			assertEquals(1, called.get());

			browser.close();
		} finally {
			server.stop();
		}
	}

	@Test
	public void testTwoLoadResource() throws IOException {
		Browser browser = new Browser(BrowserType.FF_530, FULLY_RANDOM_LOCATION.get(), 0, Bandwidth.DSL_HIGH, BrowserWindowSize._1280x1024);
		browser.addHeader("testheader", "somevalue");

		final AtomicInteger rest = new AtomicInteger();
		HTTPRunnable runnable = new HTTPRunnable() {
			@Override
			public void run(String uri, String method, Properties header, Properties parms) {
				rest.incrementAndGet();
			}
		};
		MockRESTServer server = new MockRESTServer(runnable, NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "somereturn");

		try {
			final AtomicInteger called = new AtomicInteger();
			String url = "http://localhost:" + server.getPort();
			CommonUser user = new CommonUser("user", "user");
			CustomerSession session = new CustomerSession(url, user, FULLY_RANDOM_LOCATION.get(), true);
			browser.loadResources(Arrays.asList(new String[] {
					"http://localhost:" + server.getPort() + "/img.png",
					"http://localhost:" + server.getPort() + "/img.png" }),
					session, url, false, new UEMLoadCallback() {

						@Override
						public void run() throws IOException {
							called.incrementAndGet();
						}
					});

			assertEquals("Two web-requests are needed here", 2, rest.get());
			assertEquals("Callback is still only called once", 1, called.get());

			browser.close();
		} finally {
			server.stop();
		}
	}
	
	@Test
	public void testRxVisitorCookieWithSubPage() throws IOException {
		final String visitorID = "15072836049681YDKF9BM7WX4ESUGMIRZDBJEO4UK38YF";
		final Browser browser = new Browser(BrowserType.FF_530, FULLY_RANDOM_LOCATION.get(), 0, Bandwidth.DSL_HIGH, BrowserWindowSize._1280x1024, new VisitorId(visitorID, false));

		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "somereturn");

		try {
			final String url = "http://localhost:" + server.getPort();
			CommonUser user = new CommonUser("user", "user");
			final CustomerSession session = new CustomerSession(url, user, FULLY_RANDOM_LOCATION.get(), true);
			browser.startPageLoad(url, "sometitle", session, null, null, new UEMLoadCallback() {

				@Override
				public void run() throws IOException {
					Cookie rxVisitorCookie = null;
					Collection<Cookie> cookies = browser.getUemLoadHttpClient().getCookies();
					for(Cookie c : cookies) {
						if("rxVisitor".equals(c.getName())) 
							rxVisitorCookie = c;
					}
					
					assertNotNull("No rxVisitor Cookie after startPageLoad method.", rxVisitorCookie);
					assertEquals("Cookie value was different than expected.", visitorID, rxVisitorCookie.getValue());
					
					int numberOfCookies = cookies.size();
					browser.getUemLoadHttpClient().removeCookie("rxVisitor");
					assertTrue("Cookie wasn't removed.", browser.getUemLoadHttpClient().getCookies().size() == numberOfCookies-1);
					
					browser.loadSubPage(url+"/sub", "subtitle", session, new UEMLoadCallback() {
						
						@Override
						public void run() throws IOException {
							
						}
					});
				}
			});
			
			Cookie rxVisitorCookie = null;
			
			for(Cookie c : browser.getUemLoadHttpClient().getCookies()) {
				if("rxVisitor".equals(c.getName())) 
					rxVisitorCookie = c;
			}
			
			assertNotNull("No rxVisitor Cookie after loadSubPage method.", rxVisitorCookie);
			assertEquals("Cookie value was different than expected.", visitorID, rxVisitorCookie.getValue());

			browser.close();
		} finally {
			server.stop();
		}
	}
}
