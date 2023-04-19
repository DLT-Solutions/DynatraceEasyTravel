package com.dynatrace.diagnostics.uemload.headless;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.ExtendedDemoUser;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.headless.HeadlessVisit.HeadlessGetAction;
import com.dynatrace.easytravel.misc.CommonUser;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPResponseRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.NanoHTTPD.Response;

public class HeadlessVisitRunnableTest3 {
	private static final Logger LOGGER = Logger.getLogger(HeadlessVisitRunnableTest3.class.getName());
	HeadlessVisitTestUtil visitTestUtil;

	@Before
	public void setUp() {
 		visitTestUtil = new HeadlessVisitTestUtil();
 		HeadlessVisitTestUtil.setup(false);
	}

	@After
	public void tearDown() throws InterruptedException {
		visitTestUtil.tearDown(false);
	}
	
	@Ignore("Integration test")
	@Test
	public void testVisit() throws InterruptedException {
		HeadlessVisitTestUtil.runHeadlessVisit(new PageVisit("https://httpbin.org/headers"), false);
		Thread.sleep(60*1000);
	}
		
	@Test
	public void testAngularVisit() throws InterruptedException {
		HeadlessVisitTestUtil.runHeadlessVisit(new PageVisit("SECRET"), false);
		Thread.sleep(10*1000);
		HeadlessVisitTestUtil.runHeadlessVisit(new PageVisit("SECRET"), false);
		Thread.sleep(30*1000);
	}
	
	@Test
	public void testVisitWithMockRestServer() throws IOException, InterruptedException {
		final Properties lastHeaders = new Properties();
		final MockRESTServer server = new MockRESTServer( new HTTPResponseRunnable() {     	
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				lastHeaders.clear();
				lastHeaders.putAll(header);
				return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "OK");
			}
		});
		try {
			String url = String.format("http://localhost:%d", server.getPort());
			LOGGER.info("url:" + url);			
			HeadlessVisitTestUtil.runHeadlessVisit(new PageVisit(url), ExtendedDemoUser.DEMOUSER, false);
			checkUserHeaders(ExtendedDemoUser.DEMOUSER, lastHeaders);

			HeadlessVisitTestUtil.runHeadlessVisit(new PageVisit(url), ExtendedDemoUser.HAINER_USER, false);
			checkUserHeaders(ExtendedDemoUser.HAINER_USER, lastHeaders);
		} finally {
			server.stop();
		} 
	}
		
	private void checkUserHeaders(ExtendedCommonUser user, Properties headers) {
		LOGGER.info("User: " + user.getFullName());
		LOGGER.info("last headers: " + headers.toString());
		String userAgent = headers.get("user-agent").toString();
		assertEquals("user agent incorrect", user.getRandomDesktopBrowser().getUserAgent(), userAgent);
		String xForwardedFor = headers.get("x-forwarded-for").toString(); 
		assertEquals("x-forwarded-for incorrect", user.getLocation().getIp(), xForwardedFor);		
	}
	
	class PageVisit implements Visit {

		private final String url;
		public PageVisit(String url) {
			this.url = url;
		}
		
		@Override
		public Action[] getActions(CommonUser user, Location location) {
			List<Action> actions = new ArrayList<>();
			actions.add(new HeadlessGetAction(url));
			return actions.toArray(new Action[actions.size()]);
		}

		@Override
		public String getVisitName() {
			return "PageVisit";
		}
		
	}
}
