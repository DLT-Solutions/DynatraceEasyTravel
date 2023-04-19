/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: JavaScriptAgentTest.java
 * @date: 01.03.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.JavaScriptAgent.JsAgentOptionsSet;
import com.dynatrace.diagnostics.uemload.http.base.UemLoadHttpClient;
import com.dynatrace.diagnostics.uemload.http.callback.HttpResponseCallback;
import com.dynatrace.diagnostics.uemload.thirdpartycontent.ResourceRequestSummary;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Url;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.TestEnvironment;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;


/**
 *
 * @author peter.lang
 * @author stefan.moschinski
 */
public class JavaScriptAgentTest {

	private File orange_jsf_50_agent = new File(TestEnvironment.TEST_DATA_PATH + File.separator + "uem", "orange_agent_50.jsf");
	private File orange_jsf_50_ruxitagent = new File(TestEnvironment.TEST_DATA_PATH + File.separator + "uem", "orange_ruxitagent_50.jsf");
	
	private JavaScriptAgent.JavaScriptAgentCallback createAgentCallback(){
		return new JavaScriptAgent.JavaScriptAgentCallback() {
			@Override
			public void run(JavaScriptAgent agent) throws IOException {
				List<ResourceRequestSummary> list = Collections.emptyList();
				agent.pageLoadFinished(list, NavigationTiming.NONE, BrowserWindowSize._1366x768, 0);
			}
		};
	}
	
	private UemLoadHttpClient createUemLoadHttpClient(final AtomicReference<String> errorMessage){
		return new UemLoadHttpClient(Bandwidth.BROADBAND, BrowserType.NONE) {
			@Override
			public void post(String url, String refererUrl, HttpResponseCallback callback, byte[] payload,
					List<NameValuePair> formParams) throws IOException {
				Iterator<NameValuePair> it = formParams.iterator();
				while(it.hasNext()) {
					NameValuePair current = it.next();
					if("a".equals(current.getName())) {
						try {
							String actionsString = current.getValue();
							int index1 = actionsString.indexOf(',') + 1;
							int index2 = actionsString.indexOf(',', index1);
							String actionString = actionsString.substring(index1, index2);
							String[] actionStringParts = actionString.split("\\|");
							String actionStringRefererPart = actionStringParts[13];
							if("pageLoadReferer".equals(actionStringRefererPart)) {
								errorMessage.set(null);
							}
							else {
								errorMessage.set("formParam 'a' found, invalid page load referer: " + actionStringRefererPart);
							}
						}
						catch(Exception e) {
							errorMessage.set("formParam 'a' found, error while parsing action String: " + e.getMessage());
						}
						return;//there should be only one NameValuePair with name 'a' anyway...
					}
				}
			}
		};
	}
	
	@Test
	public void testPageLoadReferer() throws IOException {		
		EasyTravelConfig config = EasyTravelConfig.read();
		config.apmServerDefault=InstallationType.Classic;
		config.disableJavaScriptAgent = false;
		InstallationType saveState = DtVersionDetector.getInstallationType();
		DtVersionDetector.enforceInstallationType(InstallationType.Classic);

		try{
			final AtomicReference<String> errorMessage = new AtomicReference<>("unknown error");
	
			JavaScriptAgent.JavaScriptAgentCallback pageLoadRefererTestCallback = createAgentCallback();
	
			UemLoadHttpClient client = createUemLoadHttpClient(errorMessage);
	
			MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
			try {	
				JavaScriptAgent.getJavaScriptAgent(Files.asCharSource(orange_jsf_50_agent, Charsets.UTF_8).read(), client,
						"http://localhost:" + server.getPort(), "title1", Bandwidth.UNLIMITED, "action", "pageLoadReferer", pageLoadRefererTestCallback);
			} finally {
				server.stop();
			}
	
			String error = errorMessage.get();
			assertNull(error, error);
		} finally {
			EasyTravelConfig.resetSingleton();
			DtVersionDetector.enforceInstallationType(saveState);
		}
	}

	@Test
	public void testModeAndAgentMatch() throws Exception {
		InstallationType[] installationTypes = { InstallationType.Classic, InstallationType.APM,
				InstallationType.Classic, InstallationType.APM };
		File[] files = { orange_jsf_50_agent, orange_jsf_50_ruxitagent, orange_jsf_50_ruxitagent, orange_jsf_50_agent };

		EasyTravelConfig config = EasyTravelConfig.read();
		config.disableJavaScriptAgent = false;
		InstallationType saveState = DtVersionDetector.getInstallationType();

		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
		try {
			for (int i = 0; i < 4; i++) {
				config.apmServerDefault = installationTypes[i];
				DtVersionDetector.enforceInstallationType(installationTypes[i]);

				final AtomicReference<String> errorMessage = new AtomicReference<>("unknown error");

				JavaScriptAgent.JavaScriptAgentCallback pageLoadRefererTestCallback = createAgentCallback();

				UemLoadHttpClient client = createUemLoadHttpClient(errorMessage);

					JavaScriptAgent.getJavaScriptAgent(Files.asCharSource(files[i], Charsets.UTF_8).read(), client,
							"http://localhost:" + server.getPort(), "title1", Bandwidth.UNLIMITED, "action",
							"pageLoadReferer", pageLoadRefererTestCallback);

				String error = errorMessage.get();
				if (i < 2)
					assertNull("Error wasn't null with matching mode and agent.", error);
				else
					assertNotNull("Error was null without matching mode and agent.", error);
			}
		} finally {
			server.stop();
			EasyTravelConfig.resetSingleton();
			DtVersionDetector.enforceInstallationType(saveState);
		}
	}

	@Test
	public void testParseAgentUrl() throws MalformedURLException {
		testSingleAgentString("dtagent42_3d_1234.js", "http://localhost:8080", "tp=100,50,3", "_3d", "1234");
		testSingleAgentString("dtagent50_bijnp3_3714.js", "http://localhost:8080", "tp=100,50,3", "_bijnp3", "3714");
		testSingleAgentString("dtagent_3d_1234.js", "http://localhost:8080", "tp=100,50,3", "_3d", "1234");
		testSingleAgentString("dtagent_3dn_1234.js", "http://localhost:8080", "tp=100,50,3", "_3dn", "1234");
		testSingleAgentString("dtagent__1234.js", "http://localhost:8080", "tp=100,50,3", "_", "1234");
		testSingleAgentString("dtagent_1234.js", "http://localhost:8080", "tp=100,50,3", null, "1234");
		testSingleAgentString("dtagent__.js", "http://localhost:8080", "tp=100,50,3", "_", "");
		testSingleAgentString("dtagent_.js", "http://localhost:8080", "tp=100,50,3", null, "");

		// new version format
		testSingleAgentStringNew("dtagent_3dx_6000500011234.js", "http://localhost:8080", "tp=100,50,3", "_3dx", "1234");
		testSingleAgentStringNew("dtagent_3dx_6000500015678.js", "http://localhost:8080", "tp=100,50,3", "_3dx", "5678");



		assertNull(JavaScriptAgent.getJavaScriptAgentUrl("asdf kjasfjk ajsfja /dtagent.js\"\\> asdfaf", "http://localhost:8080"));
	}

	@Test
	public void testFeatureHashParsing() {
		String[] agentUrl = {null, "adz", null, "ntd=1"};
		int flags = JsAgentOptionsSet.parseFeatureHash(agentUrl);
		assertEquals(0, flags);
		assertFalse(JsAgentOptionsSet.isThirdPartyContentDetectionEnabled(flags));
		assertFalse(JsAgentOptionsSet.isNavigationTimingEnabled(flags));
		assertFalse(JsAgentOptionsSet.isPerceivedRenderTimeEnabled(flags));
		assertFalse(JsAgentOptionsSet.isStreamingMediaEnabled(flags));
		assertFalse(JsAgentOptionsSet.isBandwidthEnabled(flags));

		agentUrl[1] = "n"; // set new featurehash
		agentUrl[3] = "";
		flags = JsAgentOptionsSet.parseFeatureHash(agentUrl);
		assertEquals(2, flags);
		assertFalse(JsAgentOptionsSet.isThirdPartyContentDetectionEnabled(flags));
		assertTrue(JsAgentOptionsSet.isNavigationTimingEnabled(flags));
		assertFalse(JsAgentOptionsSet.isPerceivedRenderTimeEnabled(flags));
		assertFalse(JsAgentOptionsSet.isStreamingMediaEnabled(flags));
		assertFalse(JsAgentOptionsSet.isBandwidthEnabled(flags));

		agentUrl[1] = "p"; // set new featurehash
		agentUrl[3] = "ntd=1";
		flags = JsAgentOptionsSet.parseFeatureHash(agentUrl);
		assertEquals(4, flags);
		assertFalse(JsAgentOptionsSet.isThirdPartyContentDetectionEnabled(flags));
		assertFalse(JsAgentOptionsSet.isNavigationTimingEnabled(flags));
		assertTrue(JsAgentOptionsSet.isPerceivedRenderTimeEnabled(flags));
		assertFalse(JsAgentOptionsSet.isStreamingMediaEnabled(flags));
		assertFalse(JsAgentOptionsSet.isBandwidthEnabled(flags));

		agentUrl[1] = "s"; // set new featurehash
		agentUrl[3] = "";
		flags = JsAgentOptionsSet.parseFeatureHash(agentUrl);
		assertEquals(10, flags);
		assertFalse(JsAgentOptionsSet.isThirdPartyContentDetectionEnabled(flags));
		assertTrue(JsAgentOptionsSet.isNavigationTimingEnabled(flags));
		assertFalse(JsAgentOptionsSet.isPerceivedRenderTimeEnabled(flags));
		assertTrue(JsAgentOptionsSet.isStreamingMediaEnabled(flags));
		assertFalse(JsAgentOptionsSet.isBandwidthEnabled(flags));

		agentUrl[1] = "np"; // set new featurehash
		flags = JsAgentOptionsSet.parseFeatureHash(agentUrl);
		assertEquals(6, flags);
		assertFalse(JsAgentOptionsSet.isThirdPartyContentDetectionEnabled(flags));
		assertTrue(JsAgentOptionsSet.isNavigationTimingEnabled(flags));
		assertTrue(JsAgentOptionsSet.isPerceivedRenderTimeEnabled(flags));
		assertFalse(JsAgentOptionsSet.isStreamingMediaEnabled(flags));
		assertFalse(JsAgentOptionsSet.isBandwidthEnabled(flags));

		agentUrl[1] = "nps"; // set new featurehash
		flags = JsAgentOptionsSet.parseFeatureHash(agentUrl);
		assertEquals(14, flags);
		assertFalse(JsAgentOptionsSet.isThirdPartyContentDetectionEnabled(flags));
		assertTrue(JsAgentOptionsSet.isNavigationTimingEnabled(flags));
		assertTrue(JsAgentOptionsSet.isPerceivedRenderTimeEnabled(flags));
		assertTrue(JsAgentOptionsSet.isStreamingMediaEnabled(flags));
		assertFalse(JsAgentOptionsSet.isBandwidthEnabled(flags));

		String[] agent2Url = {null, "n", null, "tp=100,50|"};
		flags = JsAgentOptionsSet.parseFeatureHash(agent2Url);
		assertEquals(3, flags);
		assertTrue(JsAgentOptionsSet.isThirdPartyContentDetectionEnabled(flags));
		assertTrue(JsAgentOptionsSet.isNavigationTimingEnabled(flags));
		assertFalse(JsAgentOptionsSet.isPerceivedRenderTimeEnabled(flags));
		assertFalse(JsAgentOptionsSet.isStreamingMediaEnabled(flags));
		assertFalse(JsAgentOptionsSet.isBandwidthEnabled(flags));

		agent2Url[1] = "np";
		flags = JsAgentOptionsSet.parseFeatureHash(agent2Url);
		assertEquals(7, flags);
		assertTrue(JsAgentOptionsSet.isThirdPartyContentDetectionEnabled(flags));
		assertTrue(JsAgentOptionsSet.isNavigationTimingEnabled(flags));
		assertTrue(JsAgentOptionsSet.isPerceivedRenderTimeEnabled(flags));
		assertFalse(JsAgentOptionsSet.isStreamingMediaEnabled(flags));
		assertFalse(JsAgentOptionsSet.isBandwidthEnabled(flags));

		agent2Url[1] = "nps";
		flags = JsAgentOptionsSet.parseFeatureHash(agent2Url);
		assertEquals(15, flags);
		assertTrue(JsAgentOptionsSet.isThirdPartyContentDetectionEnabled(flags));
		assertTrue(JsAgentOptionsSet.isNavigationTimingEnabled(flags));
		assertTrue(JsAgentOptionsSet.isPerceivedRenderTimeEnabled(flags));
		assertTrue(JsAgentOptionsSet.isStreamingMediaEnabled(flags));
		assertFalse(JsAgentOptionsSet.isBandwidthEnabled(flags));


		agent2Url[1] = "n";
		agent2Url[3] = "rid=RID_-216835711|rpid=1667787993|bandwidth=180";
		flags = JsAgentOptionsSet.parseFeatureHash(agent2Url);
		assertEquals(2, flags);
		assertFalse(JsAgentOptionsSet.isThirdPartyContentDetectionEnabled(flags));
		assertTrue(JsAgentOptionsSet.isNavigationTimingEnabled(flags));
		assertFalse(JsAgentOptionsSet.isStreamingMediaEnabled(flags));
		assertFalse(JsAgentOptionsSet.isBandwidthEnabled(flags));

		agent2Url[1] = "b";
		agent2Url[3] = "rid=RID_-216835711|rpid=1667787993|bandwidth=180|ntd=1";
		flags = JsAgentOptionsSet.parseFeatureHash(agent2Url);
		assertEquals(64, flags);
		assertFalse(JsAgentOptionsSet.isThirdPartyContentDetectionEnabled(flags));
		assertFalse(JsAgentOptionsSet.isNavigationTimingEnabled(flags));
		assertFalse(JsAgentOptionsSet.isStreamingMediaEnabled(flags));
		assertTrue(JsAgentOptionsSet.isBandwidthEnabled(flags));

		agent2Url[1] = "b";
		agent2Url[3] = "rid=RID_-216835711|rpid=1667787993|bandwidth=180";
		flags = JsAgentOptionsSet.parseFeatureHash(agent2Url);
		assertEquals(66, flags);
		assertFalse(JsAgentOptionsSet.isThirdPartyContentDetectionEnabled(flags));
		assertTrue(JsAgentOptionsSet.isNavigationTimingEnabled(flags));
		assertFalse(JsAgentOptionsSet.isStreamingMediaEnabled(flags));
		assertTrue(JsAgentOptionsSet.isBandwidthEnabled(flags));

		agent2Url[1] = "nb";
		agent2Url[3] = "rid=RID_-216835711|rpid=1667787993|bandwidth=180";
		flags = JsAgentOptionsSet.parseFeatureHash(agent2Url);
		assertEquals(66, flags);
		assertFalse(JsAgentOptionsSet.isThirdPartyContentDetectionEnabled(flags));
		assertTrue(JsAgentOptionsSet.isNavigationTimingEnabled(flags));
		assertFalse(JsAgentOptionsSet.isStreamingMediaEnabled(flags));
		assertTrue(JsAgentOptionsSet.isBandwidthEnabled(flags));
		
		agent2Url[1] = "nV";
		agent2Url[3] = "rid=RID_-216835711|rpid=1667787993|bandwidth=180";
		flags = JsAgentOptionsSet.parseFeatureHash(agent2Url);
		assertEquals(130, flags);
		assertFalse(JsAgentOptionsSet.isThirdPartyContentDetectionEnabled(flags));
		assertTrue(JsAgentOptionsSet.isNavigationTimingEnabled(flags));
		assertFalse(JsAgentOptionsSet.isStreamingMediaEnabled(flags));
		assertFalse(JsAgentOptionsSet.isBandwidthEnabled(flags));
		assertTrue(JsAgentOptionsSet.isVisualCompleteTimeEnabled(flags));
	}


	private void testSingleAgentString(String agentUrl, String baseUrl, String agentParameters, String expectedFeatureHash, String expectedVersion) throws MalformedURLException {
		String[] parsedAgentUrl = JavaScriptAgent.getJavaScriptAgentUrl("asdf kjasfjk ajsfja /" + agentUrl + "\" data-dtconfig=\"" + agentParameters + "\"\\> asdfaf", "http://localhost:8080");
		assertNotNull(parsedAgentUrl);
		assertEquals(baseUrl + "/" + agentUrl, parsedAgentUrl[0]);
		if (expectedFeatureHash == null) {
			assertNull(parsedAgentUrl[1]);
		} else {
			assertEquals(expectedFeatureHash, parsedAgentUrl[1]);
		}
		assertEquals(expectedVersion, parsedAgentUrl[2]);
	}

	private void testSingleAgentStringNew(String agentUrl, String baseUrl, String agentParameters, String expectedFeatureHash, String expectedVersion) throws MalformedURLException {
		String[] parsedAgentUrl = JavaScriptAgent.getJavaScriptAgentUrl("window.dT_={cfg:\"" + agentParameters + "\"   <><> asdf kjasfjk ajsfja /" + agentUrl + "\"\"\\> asdfaf", "http://localhost:8080");
		assertNotNull(parsedAgentUrl);
		assertEquals(baseUrl + "/" + agentUrl, parsedAgentUrl[0]);

		if (expectedFeatureHash == null) {
			assertNull(parsedAgentUrl[1]);
		} else {
			assertEquals(expectedFeatureHash, parsedAgentUrl[1]);
		}
		assertEquals(expectedVersion, parsedAgentUrl[2]);
	}

	@Test
	public void testFindAgent() throws MalformedURLException, IOException {
		assertThat(
				JavaScriptAgent.getJavaScriptAgentUrl(Files.asCharSource(orange_jsf_50_agent, Charsets.UTF_8).read(), "http://localhost:8080"),
				arrayContaining("http://localhost:8080/dtagent50_bijnp3_3714.js", "_bijnp3", "3714",
						"rid=RID_2418|rpid=870331088|tp=100,50,3|bandwidth=180"));

	}


	final AtomicBoolean called = new AtomicBoolean(false);
	JavaScriptAgent.JavaScriptAgentCallback callback = new JavaScriptAgent.JavaScriptAgentCallback() {

		@Override
		public void run(JavaScriptAgent agent) throws IOException {
			called.set(true);

			List<ResourceRequestSummary> list = Collections.emptyList();

			agent.pageLoadStarted("some more html");
			agent.pageLoadFinished(list, NavigationTiming.NONE, BrowserWindowSize._1366x768, 0);

			agent.pageLoadStarted("some more html " +
					"data-dtconfig=\"rid=blabla\" rpid=\"awefaorhaaaae|234awre\"");
			agent.pageLoadFinished(list, null, BrowserWindowSize._1366x768, 0);

			agent.startCustomAction("myaction", "type", "info");
			agent.stopCustomAction(true, list, null, BrowserWindowSize._1024x768, 0);

			agent.startCustomAction(null, null, null);
			agent.stopCustomAction(false, list, null, BrowserWindowSize._1024x768, 0);

			// null or non-null depends on the type of test...
			agent.getSourceAction();

			assertNull(agent.getCustomActionName());
			agent.getCustomActionStart();
			assertNull(agent.getCustomActionType());
			assertNotNull(agent.getUrl());
			agent.getPageId();
			agent.getLoadStart();
		}
	};

	@Test
	public void testMethodsNullUrl() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
		try {
			UemLoadHttpClient client = new UemLoadHttpClient(Bandwidth.BROADBAND, BrowserType.NONE);

			JavaScriptAgent.getJavaScriptAgent("some html some more html", client,
					"http://localhost:" + server.getPort(), "title1", Bandwidth.UNLIMITED, "action", null, callback);
		} finally {
			server.stop();
		}

		assertTrue(called.get());
	}

	@Test
	public void testMethods() throws Exception {

		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
		try {
			UemLoadHttpClient client = new UemLoadHttpClient(Bandwidth.BROADBAND, BrowserType.NONE);

			JavaScriptAgent.getJavaScriptAgent("some html src=\"/dtagent_1244.js\" some more html", client,
					"http://localhost:" + server.getPort(), "title1", Bandwidth.UNLIMITED, "action", null, callback);
		} finally {
			server.stop();
		}

		assertTrue(called.get());
	}

	@Test
	public void testMethodsBigVersion() throws Exception {

		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
		try {
			UemLoadHttpClient client = new UemLoadHttpClient(Bandwidth.BROADBAND, BrowserType.NONE);

			JavaScriptAgent.getJavaScriptAgent("some html src=\"/dtagent_500500007757.js\" some more html", client,
					"http://localhost:" + server.getPort(), "title1", Bandwidth.UNLIMITED, "action", null, callback);
		} finally {
			server.stop();
		}

		assertTrue(called.get());
	}

	@Test
	public void testMethodsNullAction() throws Exception {

		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
		try {
			UemLoadHttpClient client = new UemLoadHttpClient(Bandwidth.BROADBAND, BrowserType.NONE);

			JavaScriptAgent.getJavaScriptAgent("some html src=\"/dtagent_1244.js\" some more html", client,
					"http://localhost:" + server.getPort(), "title1", Bandwidth.UNLIMITED, null, null, callback);
		} finally {
			server.stop();
		}

		assertTrue(called.get());
	}

	@Test
	public void testMethodError() throws Exception {
		EasyTravelConfig.read().apmServerDefault = InstallationType.Classic;
		EasyTravelConfig.read().disableJavaScriptAgent = false;
		DtVersionDetector.enforceInstallationType(null);
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_NOTFOUND, NanoHTTPD.MIME_PLAINTEXT, "response");
		try {
			UemLoadHttpClient client = new UemLoadHttpClient(Bandwidth.BROADBAND, BrowserType.NONE);

			JavaScriptAgent.getJavaScriptAgent("some html src=\"/dtagent_1244.js\" some more html", client,
					"http://localhost:" + server.getPort(), "title1", Bandwidth.UNLIMITED, "action", null, callback);
		} finally {
			server.stop();
			EasyTravelConfig.resetSingleton();
			DtVersionDetector.enforceInstallationType(null);
		}

		assertFalse(called.get());
	}

	@Test
	public void testMethodsWithIDs() throws Exception {

		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
		try {
			UemLoadHttpClient client = new UemLoadHttpClient(Bandwidth.BROADBAND, BrowserType.NONE);

			JavaScriptAgent.getJavaScriptAgent("some html src=\"/dtagent_1244.js\" some more html" +
					" data-dtconfig=\"rid=blabla\" rpid=\"awefaorhaaaae|234awre\"", client,
					"http://localhost:" + server.getPort(), "title1", Bandwidth.UNLIMITED, "action", null, callback);
		} finally {
			server.stop();
		}

		assertTrue(called.get());
	}

	@Test
	public void testActionEscape() {
		assertEquals("somestring", JavaScriptAgent.actionEscape("somestring"));
		assertEquals("str^^", JavaScriptAgent.actionEscape("str^"));
		assertEquals("str^p", JavaScriptAgent.actionEscape("str|"));
		assertEquals("str^c", JavaScriptAgent.actionEscape("str,"));
		assertEquals("str^p^^^c^^^^^p^c", JavaScriptAgent.actionEscape("str|^,^^|,"));
	}

	@Test
	public void testUrlChecks(){
		List<String> frontendURLs = Lists.newArrayList(
				Url.START,
				Url.REVIEW,
				Url.PAYMENT,
				Url.PURCHASE,
				Url.TERMS,
				Url.PRIVACY,
				Url.CONTACT,
				Url.ABOUT,
				Url.SEO,
				Url.SEO_ABOUT,
				Url.SEO_CONTACT,
				Url.LOGOUT,
				Url.TRIPDETAILS,
				Url.SPECIAL,
				Url.MOBILE_CONTACT,
				Url.MOBILE_TERMS,
				Url.MOBILE_PRIVACY);

		List<String> magentoURLs = Lists.newArrayList(
				Url.WORDPRESS_SHOP_BLOG,
				Url.WORDPRESS_SHOP_ACCESSORIES,
				Url.WORDPRESS_SHOP_CLOTHING,
				Url.WORDPRESS_SHOP_DECOR,
				Url.WORDPRESS_SHOP_HOODIES,
				Url.WORDPRESS_SHOP_MUSIC,
				Url.WORDPRESS_SHOP_PRODUCT,
				Url.WORDPRESS_SHOP_TSHIRTS);

		for (String url : frontendURLs) {
			assertTrue(JavaScriptAgent.isFrontendUrl("http://127.0.0.1/" + url + "?someparams"));
			assertFalse(JavaScriptAgent.isMagentoShopUrl("http://127.0.0.1/" + url + "?someparams"));
		}

		for (String url : magentoURLs) {
			assertFalse(JavaScriptAgent.isFrontendUrl("http://127.0.0.1/" + url + "?someparams"));
			assertTrue(JavaScriptAgent.isMagentoShopUrl("http://127.0.0.1/" + url + "?someparams"));
		}
	}

	private static final String OLD_HTML = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head>		<title>easyTravel - One step to happiness</title>		<meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\" />		<meta content=\"INDEX, FOLLOW\" name=\"robots\" />		<meta content=\"dynaTrace Software GmbH\" name=\"author\" />		<script type=\"text/javascript\" src=\"/dtagent650_23bijpr_1166.js\" data-dtconfig=\"rid=RID_-1981805739|rpid=-434015995|tp=500,50,0|bandwidth=300|reportUrl=dynaTraceMonitor|agentUri=/dtagent650_23bijpr_1166.js\"></script><link href=\"img/favicon_orange_plane.ico\" rel=\"shortcut icon\" />		<link href=\"img/favicon_orange_plane.png\" rel=\"apple-touch-icon\" />		<link href=\"css/BaseProd.css\" rel=\"stylesheet\" type=\"text/css\" />		<link href=\"css/footer.css\" rel=\"stylesheet\" type=\"text/css\" />        <link href=\"css/rime.css\" rel=\"stylesheet\" type=\"text/css\" />        <link href=\"css/rating.css\" rel=\"stylesheet\" type=\"text/css\" />				<link href=\"css/orange.css\" rel=\"stylesheet\" type=\"text/css\" /><script src=\"js/jquery-1.8.1.js\" type=\"text/javascript\"></script><script type=\"text/javascript\" src=\"js/jquery-ui-1.8.2.min.js\"></script>		<script src=\"js/version.js\" type=\"text/javascript\"></script>		<script src=\"js/dtagentApi.js\" type=\"text/javascript\"></script>		<script src=\"js/FrameworkProd.js\" type=\"text/javascript\"></script>		<script src=\"js/jquery.formLabels1.0.js\" type=\"text/javascript\"></script>		<script src=\"js/headerRotation.js\" type=\"text/javascript\"></script>		<script src=\"js/rating.js\" type=\"text/javascript\"></script>		<script src=\"js/recommendation.js\" type=\"text/javascript\"></script>				<script type=\"text/javascript\">			try {				if (eTVersion) {					dynaTrace.setAppVersion(eTVersion);				}			} catch (e) {}			jQuery(document).ready(function(event) {							});		</script>				<script src=\"problempatterns/changedetectionlib.js\" type=\"text/javascript\"></script><script src=\"/javax.faces.resource/jsf.js.jsf;jsessionid=F0993288590F142C04E7F420D7108636.jvmRoute-8280?ln=javax.faces\" type=\"text/javascript\"></script><script src=\"/javax.faces.resource/bridge.js.jsf;jsessionid=F0993288590F142C04E7F420D7108636.jvmRoute-8280?rand=19429454\" type=\"text/javascript\"></script><script src=\"/javax.faces.resource/compat.js.jsf;jsessionid=F0993288590F142C04E7F420D7108636.jvmRoute-8280\" type=\"text/javascript\"></script><script src=\"/javax.faces.resource/icefaces-compat.js.jsf;jsessionid=F0993288590F142C04E7F420D7108636.jvmRoute-8280\" type=\"text/javascript\"></script></head><body>				<div id=\"margins\"><div class=\"header_container\" xmlns=\"http://www.w3.org/1999/xhtml\">    <div class=\"header_content\">	<div class=\"orangeHeader\">		 <div id=\"homelink\"><a href=\"/orange.jsf;jsessionid=F0993288590F142C04E7F420D7108636.jvmRoute-8280\" id=\"_t13\" name=\"_t13\">Homelink</a>		</div>		<div class=\"orangeHeaderLinks\"><a href=\"/orange.jsf;jsessionid=F0993288590F142C04E7F420D7108636.jvmRoute-8280\" id=\"_t16\" name=\"_t16\">Home</a><span class=\"orangeHeaderSeparator\"></span>                        <a href=\"special-offers.jsp\"> Special Offers</a><span class=\"orangeHeaderSeparator\"></span><a class=\"active_false\" href=\"/about-orange.jsf;jsessionid=F0993288590F142C04E7F420D7108636.jvmRoute-8280\" id=\"_t19\" name=\"_t19\">About</a><span class=\"orangeHeaderSeparator\"></span><a class=\"active_false\" href=\"/contact-orange.jsf;jsessionid=F0993288590F142C04E7F420D7108636.jvmRoute-8280\" id=\"_t22\" name=\"_t22\">Contact</a><span class=\"orangeHeaderSeparator\"></span><a class=\"active_false\" href=\"/legal-orange.jsf;jsessionid=F0993288590F142C04E7F420D7108636.jvmRoute-8280\" id=\"_t25\" name=\"_t25\">Terms of Use</a><span class=\"orangeHeaderSeparator\"></span><a class=\"active_false\" href=\"/privacy-orange.jsf;jsessionid=F0993288590F142C04E7F420D7108636.jvmRoute-8280\" id=\"_t28\" name=\"_t28\">Privacy Policy</a><span class=\"orangeHeaderSeparator\"></span>			<a id=\"destinations\" style=\"float: left;\"></a>		</div>		<div class=\"orangeSocial\"><a class=\"iceOutLnk\" href=\"itms-services://?action=download-manifest&amp;url=https://192.168.56.1:9443/apps/easyTravel.plist\" id=\"j_idt34\" title=\"download iOS app\">	      		<img alt=\"download iOS app\" src=\"img/apple/apple.png\" /></a>			<a href=\"/apps/AndroidEasyTravel-release.apk\" target=\"_blank\"><img alt=\"download android app\" src=\"img/androidbutton.png\" /></a>			<a href=\"https://www.facebook.com/dynatrace\" target=\"_blank\"><img src=\"img/facebookbutton.png\" /></a>			<a href=\"https://twitter.com/dynatrace\" target=\"_blank\"><img src=\"img/twitterbutton.png\" /></a>			<a href=\"http://www.dynatrace.com\" target=\"_blank\"><img src=\"http://www.dynatrace.com/favicon.ico\" /></a>			<img src=\"img/rssbutton.png\" />		</div><form action=\"/orange.jsf;jsessionid=F0993288590F142C04E7F420D7108636.jvmRoute-8280\" class=\"iceFrm\" enctype=\"application/x-www-form-urlencoded\" id=\"loginForm\" method=\"post\" onsubmit=\"return false;\"><input name=\"loginForm\" type=\"hidden\" value=\"loginForm\" /><input type=\"hidden\" name=\"javax.faces.ViewState\" id=\"javax.faces.ViewState\" value=\"8902734520380338197:3091032498414742562\" autocomplete=\"off\" /><input name=\"ice.window\" type=\"hidden\" value=\"ayiou0o09e\" /><input name=\"ice.view\" type=\"hidden\" value=\"vbkfv27\" /><script id=\"loginForm:loginForm_captureSubmit\" type=\"text/javascript\">ice.captureSubmit('loginForm',false);ice.captureEnterKey('loginForm');</script>	<div class=\"orangeHeaderLogin\"><a class=\"iceCmdLnk button\" href=\"javascript:;\" id=\"loginForm:loginLink\" onblur=\"setFocus('');\" onclick=\"var form=formOf(this);form['loginForm:j_idcl'].value='loginForm:loginLink';iceSubmit(form,this,event);form['loginForm:j_idcl'].value='';return false;\" onfocus=\"setFocus(this.id);\">Login</a>	</div><div class=\"icePnlTlTip tripDetailsTip\" id=\"loginForm:userList\" name=\"loginForm:userList\" style=\"display:none;visibility:hidden;\"><table cellpadding=\"0\" cellspacing=\"0\"><tr id=\"loginForm:userList-tr\"><td class=\"icePnlTlTipBody tripDetailsTipBody\" colspan=\"2\"><div class=\"icePnlSrs tripDetailsPanel\" id=\"loginForm:j_idt47\"></div></td></tr></table><span id=\"loginForm:userListscript\"><script type=\"text/javascript\">Ice.modal.stop('loginForm:userList');; Ice.autoPosition.stop('loginForm:userList');; Ice.autoCentre.stop('loginForm:userList');; Ice.iFrameFix.start('loginForm:userList','/xmlhttp/blank');</script></span></div>	<div class=\"orangeHeaderLoginForm\"><div class=\"icePnlPop\" id=\"loginForm:j_idt50\" name=\"loginForm:j_idt50\" style=\"display:none;\"><table cellpadding=\"0\" cellspacing=\"0\"><tr id=\"loginForm:j_idt50-tr\"><td class=\"icePnlPopBody\" colspan=\"2\"><div class=\"icePnlGrp\" id=\"loginForm:j_idt51\"><span class=\"iceOutTxt orangeLoginMessage\" id=\"loginForm:j_idt52\">           				Please <strong>Log In</strong> or create a <strong>New Account</strong></span><label class=\"iceOutLbl orangeLoginUsername\" for=\"loginForm:username\" id=\"loginForm:j_idt56\">Username</label><input class=\"iceInpTxt orangeLoginTextbox orangeLoginUsername\" id=\"loginForm:username\" name=\"loginForm:username\" onblur=\"setFocus('');\" onfocus=\"setFocus(this.id);\" onkeypress=\"iceSubmit(form,this,event);\" onmousedown=\"this.focus();\" type=\"text\" value=\"\" /><label class=\"iceOutLbl orangeLoginPassword\" for=\"loginForm:password\" id=\"loginForm:j_idt57\">Password</label><input class=\"iceInpSecrt orangeLoginTextbox orangeLoginPassword\" id=\"loginForm:password\" name=\"loginForm:password\" onblur=\"setFocus('');\" onfocus=\"setFocus(this.id);\" onkeypress=\"iceSubmit(form,this,event);\" onmousedown=\"this.focus();\" type=\"password\" value=\"\" /><a class=\"commonButton grayButton3 orangeLoginButton orangeLoginNewAccount\" href=\"/orange-newaccount.jsf;jsessionid=F0993288590F142C04E7F420D7108636.jvmRoute-8280\" id=\"loginForm:newAccount\" name=\"loginForm:newAccount\">New Account</a><input class=\"iceCmdBtn loginPrivacy\" id=\"loginForm:j_idt58\" name=\"loginForm:j_idt58\" onblur=\"setFocus('');\" onclick=\"iceSubmit(form,this,event);return false;\" onfocus=\"setFocus(this.id);\" src=\"img/privacypolicy_lock.png\" type=\"image\" /><input class=\"iceCmdBtn commonButton grayButton4 orangeLoginButton orangeLoginCancel\" id=\"loginForm:loginCancel\" name=\"loginForm:loginCancel\" onblur=\"setFocus('');\" onclick=\"iceSubmit(form,this,event);return false;\" onfocus=\"setFocus(this.id);\" type=\"submit\" value=\"Cancel\" /><input class=\"iceCmdBtn commonButton orangeButton4 orangeLoginButton orangeLoginSubmit\" id=\"loginForm:loginSubmit\" name=\"loginForm:loginSubmit\" onblur=\"setFocus('');\" onclick=\"iceSubmit(form,this,event);return false;\" onfocus=\"setFocus(this.id);\" type=\"submit\" value=\"Login\" /></div></td></tr></table><span id=\"loginForm:j_idt50script\"><script type=\"text/javascript\">Ice.modal.stop('loginForm:j_idt50');; Ice.autoPosition.stop('loginForm:j_idt50');; Ice.autoCentre.stop('loginForm:j_idt50');; Ice.iFrameFix.start('loginForm:j_idt50','/xmlhttp/blank');</script></span></div>	</div><div class=\"icePnlPop userList\" id=\"loginForm:j_idt62\" name=\"loginForm:j_idt62\" style=\"display:none;\"><table cellpadding=\"0\" cellspacing=\"0\"><tr id=\"loginForm:j_idt62-tr\"><td class=\"icePnlPopBody userListBody\" colspan=\"2\"><div class=\"icePnlSrs tripDetailsPanel\" id=\"loginForm:j_idt63\"></div></td></tr></table><span id=\"loginForm:j_idt62script\"><script type=\"text/javascript\">Ice.modal.stop('loginForm:j_idt62');; Ice.autoPosition.stop('loginForm:j_idt62');; Ice.autoCentre.stop('loginForm:j_idt62');; Ice.iFrameFix.start('loginForm:j_idt62','/xmlhttp/blank');</script></span></div><span id=\"loginFormhdnFldsDiv\"><input name=\"icefacesCssUpdates\" type=\"hidden\" value=\"\" /><input name=\"loginForm:j_idcl\" type=\"hidden\" /></span></form>	</div>	    </div>    </div>	        <div class=\"body_container\">	            <div class=\"body_content\">	                <div class=\"contentContainer\"><form action=\"/orange.jsf;jsessionid=F0993288590F142C04E7F420D7108636.jvmRoute-8280\" class=\"iceFrm\" enctype=\"application/x-www-form-urlencoded\" id=\"iceform\" method=\"post\" onsubmit=\"return false;\">...";
	private static final String NEW_HTML = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"> <html xmlns=\"http://www.w3.org/1999/xhtml\"><head> \t\t<title>easyTravel - One step to happiness</title>  \t\t<meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\" />  \t\t<meta content=\"INDEX, FOLLOW\" name=\"robots\" />  \t\t<meta content=\"dynaTrace Software GmbH\" name=\"author\" />  \t\t<script type=\"text/javascript\">(function(){var a=window;a.dT_?a.console&&a.console.log(\"Duplicate agent injection detected, turning off redundant initConfig.\"):window.dT_||(window.dT_={cfg:\"tp=500,50,0|bandwidth=300|reportUrl=dynaTraceMonitor|agentUri=/dtagent_23bijpr_6000500001230.js|auto=1|domain=dynatrace.vmta|rid=RID_2418|rpid=-2124250923|app=#APP#\"})})();(function(){function r(){var a=0;try{a=window.performance.timing.navigationStart+Math.floor(window.performance.now())}catch(b){}return 0>=a?(new Date).getTime():a}function X(a,b){return Y(a,b)}function l(a,b){for(var c=1;c<arguments.length;c++)a.push(arguments[c])}function u(a,b){return parseInt(a,b||10)}function C(a){try{if(v)return v[a]}catch(b){}return null}function D(a,b){try{window.sessionStorage.setItem(a,b)}catch(c){}}function m(a,b){var c=-1;b&&(a&&a.indexOf)&&(c=a.indexOf(b));return c}function E(a){document.cookie= a+\'=\"\";path=/\'+(d.domain?\";domain=\"+d.domain:\"\")+\"; expires=Thu, 01-Jan-70 00:00:01 GMT;\"}function F(a){a=encodeURIComponent(a);var b=[];if(a)for(var c=0;c<a.length;c++){var k=a.charAt(c),d=Z[k];d?l(b,d):l(b,k)}return b.join(\"\")}function w(a,b,c){b||0==b?(b=(\"\"+b).replace(/[;\\n\\r]/g,\"_\"),b=\"DTSA\"===a.toUpperCase()?F(b):b,a=a+\"=\"+b+\";path=/\"+(d.domain?\";domain=\"+d.domain:\"\"),c&&(a+=\";expires=\"+c.toUTCString()),document.cookie=a):E(a)}function q(a){var b,c,k,d=document.cookie.split(\";\");for(b=0;b<d.length;b++)if(c= m(d[b],\"=\"),k=d[b].substring(0,c),c=d[b].substring(c+1),k=k.replace(/^\\s+|\\s+$/g,\"\"),k===a)return\"DTSA\"===a.toUpperCase()?decodeURIComponent(c):c;return\"\"}function G(a){return/^[0-9A-Za-z_\\$\\+\\/\\.\\-\\*%\\|]*$/.test(a)}function H(){var a=q(I);return a&&G(a)?a:\"\"}function J(a){a=a||H();var b={sessionId:null,serverId:null};if(a){var c=m(a,\"|\"),d=a;-1!==c&&(d=a.substring(0,c));c=m(d,\"$\");-1!==c?(b.sessionId=d.substring(c+1),b.serverId=d.substring(0,c)):b.sessionId=d}return b}function $(a){return J(a).serverId} function aa(a){if(a)return J(a).sessionId;if(a=e.gSC()){var b=a.indexOf(\"|\");-1!==b&&(a=a.substring(0,b))}return a}function K(a,b){return Math.floor(Math.random()*(b-a+1))+a}function L(a){var b=window.crypto||window.msCrypto,c;if(b)c=new Int8Array(a),b.getRandomValues(c);else{c=[];for(b=0;b<a;b++)c.push(K(0,M))}a=[];for(b=0;b<c.length;b++){var d=Math.abs(c[b]%M),d=9>=d?String.fromCharCode(d+48):String.fromCharCode(d+55);a.push(d)}return a.join(\"\")}function N(a){return document.getElementsByTagName(a)} function O(a){var b=a.length;if(\"number\"===typeof b)a=b;else{for(var b=0,c=2048;a[c-1];)b=c,c+=c;for(var d=7;1<c-b;)d=(c+b)/2,a[d-1]?b=d:c=d;a=a[d]?c:b}return a}function ba(){var a=d.csu,a=(a.indexOf(\"dbg\")==a.length-3?a.substr(0,a.length-3):a)+\"_\"+d.app+\"_Store\";try{if(x){var b=x.getItem(a);if(b){for(var a={},c=b.split(\"|\"),b=0;b<c.length;b++){var e=c[b].split(\"=\");2==e.length&&(a[e[0]]=decodeURIComponent(e[1]))}if(!d.lastModification||parseInt(a.lastModification.substr(0,13),10)>=parseInt(d.lastModification.substr(0, 13),10))for(var f in a)a.hasOwnProperty(f)&&(\"config\"==f?y(a[f]):d[f]=a[f])}}}catch(g){}}function y(a){a=a.split(\"|\");for(var b=0;b<a.length;b++){var c=m(a[b],\"=\");-1===c?d[a[b]]=\"1\":d[a[b].substring(0,c)]=a[b].substring(c+1,a[b].length)}}function ca(a,b){if(a){var c=/([a-zA-Z]*)[0-9]{0,3}_[a-zA-Z_0-9]*_[0-9]+/g.exec(a);if(c&&c.length){var k=c[0];d.csu=c[1];c=k.split(\"_\");d.legacy=\"1\";d.featureHash=c[1];d.dtVersion=e.version[0]+\"\"+e.version[1]}}b&&y(b);c=location.hostname;k=d.domain;c=c&&k?c==k|| -1!==c.indexOf(\".\"+k,c.length-(\".\"+k).length)?!0:!1:!0;c||(delete d.domain,d.domainOverride=\"true\")}function da(){return I}function ea(){return fa}function ga(){return ha}function ia(){return s}function P(a){for(var b=Q(),c=!1,d=0;d<b.length;d++)b[d].frameId===s&&(b[d].actionId=a,c=!0);c||l(b,{frameId:s,actionId:a});R(b)}function R(a,b){var c=\"\";if(a){for(var c=[],d=0;d<a.length;d++)0<d&&0<c.length&&l(c,\"p\"),l(c,a[d].frameId),l(c,\"h\"),l(c,a[d].actionId);c=c.join(\"\")}c||(c=\"-\");w(z,c)}function Q(a){var b= q(z),c=[];if(b&&\"-\"!==b)for(var b=b.split(\"p\"),d=0;d<b.length;d++){var e=b[d].split(\"h\");if(2===e.length&&e[0]&&e[1]){var f=e[0],g;if(!(g=a)){g=f.split(\"_\");g=u(g[0]);var h=r()%A;h<g&&(h+=A);g=g+9E5>h}g&&l(c,{frameId:f,actionId:e[1]})}}return c}function S(){var a=q(t);if(!a||\"\"==a||a.length&&a.length!=T)a=C(t),a&&a.length==T||(U=!0,a=r()+L(ja));var b=a,c=new Date;c.setFullYear(c.getFullYear()+2);w(t,b,c);D(t,b);return a}function ka(){return U}var f=window;if(!f.dT_||!f.dT_.cfg||\"string\"!=typeof f.dT_.cfg|| f.dT_.initialized)f.console&&f.console.log(\"Initconfig not found or agent already initialized! This is an injection issue.\");else{var Y=window.setTimeout,v=window.sessionStorage,e={version:[6,5,0,\"1230\"],cfg:window.dT_&&window.dT_.cfg,ica:1};e.version[3]=parseInt(e.version[3],10);window.dT_=e;e.agentStartTime=r();e.nw=r;e.apush=l;e.st=X;var M=32,Z={\"!\":\"%21\",\"~\":\"%7E\",\"*\":\"%2A\",\"(\":\"%28\",\")\":\"%29\",\"\'\":\"%27\",$:\"%24\",\";\":\"%3B\",\",\":\"%2C\"};e.gSSV=C;e.sSSV=D;e.pn=u;e.iVSC=G;e.io=m;e.dC=E;e.sC=w;e.esc= F;e.gSId=$;e.gDtc=aa;e.gSC=H;e.gC=q;e.cRN=K;e.cRS=L;e.gEL=O;e.gEBTN=N;var d={reportUrl:\"dynaTraceMonitor\",initializedModules:\"\",csu:\"dtagent\",domainOverride:\"false\",dataDtConfig:e.cfg},x;try{x=window.localStorage}catch(la){}if(-1==m(d.dataDtConfig,\"#CONFIGSTRING\")&&(y(d.dataDtConfig),f=function(a){d[a]=0>m(d[a],\"#\"+a.toUpperCase())?d[a]:\"\"},f(\"domain\"),f(\"auto\"),f(\"app\"),(f=d.agentUri)&&-1<m(f,\"_\")))f=/([a-zA-Z0-9]*)[0-9]*_([a-zA-Z0-9]*)_[0-9]*/.exec(f),d.csu=f[1],d.featureHash=f[2],d.dtVersion=e.version[0]+ \"\"+e.version[1];var f=N(\"script\"),V=O(f);if(0<V)for(var h,W=d.csu+\"_bootstrap.js\",B=0;B<V;B++)if(h=f[B],h.attributes){var n=h.attributes.getNamedItem(\"data-dtconfig\");h=h.src;if(n){ca(h,n.value);break}if((n=h&&h.indexOf(W))&&0<=n)n=n+W.length+5,d.app=h.length>n?h.substr(n):\"Default%20Application\"}ba();try{var g=d.ign;if(g&&RegExp(g).test(window.location.href)){document.dT_=window.dT_=null;return}}catch(ma){}var z=\"dtPC\",I=\"dtCookie\",fa=\"x-dtPC\",ha=\"x-dtReferer\";e.gSCN=da;e.gPCHN=ea;e.gRHN=ga;e.pageContextCookieName= z;e.latencyCookieName=\"dtLatC\";e.cfg=d;var A=6E8,s=e.agentStartTime%A+\"_\"+u(1E3*Math.random());e.gFId=ia;e.frameId=s;P(1);e.gPC=Q;e.cPC=P;e.sPC=R;var p;try{p=v.getItem(\"dtDisabled\")}catch(na){}!d.auto&&(!d.legacy&&!p)&&(g=d.agentname||d.csu||\"dtagent\",g=q(\"dtUseDebugAgent\")?d.debugName||g+\"dbg\":d.name||g,p=d.agentUri||d.agentLocation+\"/\"+g+\"_\"+d.featureHash+\"_\"+d.buildNumber,d.async?(g=document.createElement(\"script\"),g.setAttribute(\"src\",p),d.async&&g.setAttribute(\"defer\",\"true\"),g.setAttribute(\"crossorigin\", \"anonymous\"),p=document.getElementsByTagName(\"script\")[0],p.parentElement.insertBefore(g,p)):document.write(\'<script type=\"text/javascript\" src=\"\'+p+\'\">\\x3c/script>\'));var t=\"rxVisitor\",ja=32,T=45,U=!1;S();e.iNV=ka;e.gVID=S}})(); </script><script type=\"text/javascript\" src=\"/dtagent_23bijpr_6000500001230.js\"></script><link href=\"img/favicon_orange_plane.ico\" rel=\"shortcut icon\" /> \t\t<link href=\"img/favicon_orange_plane.png\" rel=\"apple-touch-icon\" />  \t\t<link href=\"css/BaseProd.css\" rel=\"stylesheet\" type=\"text/css\" /> \t\t<link href=\"css/footer.css\" rel=\"stylesheet\" type=\"text/css\" />         <link href=\"css/rime.css\" rel=\"stylesheet\" type=\"text/css\" />         <link href=\"css/rating.css\" rel=\"stylesheet\" type=\"text/css\" />...";

	@Test
	public void testExtractDTConfig() {
		HashMap<String, String> params = JavaScriptAgent.extractDTConfig(OLD_HTML);
		assertEquals(6, params.size());
		assertEquals("RID_-1981805739", params.get("rid"));
		assertEquals("-434015995", params.get("rpid"));
		assertEquals("500,50,0", params.get("tp"));
		assertEquals("300", params.get("bandwidth"));
		assertEquals("dynaTraceMonitor", params.get("reportUrl"));
		assertEquals("/dtagent650_23bijpr_1166.js", params.get("agentUri"));

		params = JavaScriptAgent.extractDTConfig(NEW_HTML);
		assertEquals(9, params.size());
		assertEquals("RID_2418", params.get("rid"));
		assertEquals("-2124250923", params.get("rpid"));
		assertEquals("500,50,0", params.get("tp"));
		assertEquals("300", params.get("bandwidth"));
		assertEquals("dynaTraceMonitor", params.get("reportUrl"));
		assertEquals("/dtagent_23bijpr_6000500001230.js", params.get("agentUri"));
		assertEquals("1", params.get("auto"));
		assertEquals("dynatrace.vmta", params.get("domain"));
		assertEquals("#APP#", params.get("app"));

	}

	@Ignore ("integration test for parsing agent string in nodejs app")
	@Test
	public void nodejstest() throws ClientProtocolException, IOException {
		HttpClient httpclient = HttpClientBuilder.create().build();		
		
		HttpGet httpget = new HttpGet("http://easytravel-win.ruxitlabs.com:8100/forecast?loc=Saint+Paris&days=9&from=2016-12-14");
		httpget.addHeader("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.0.3; de-at; HTC Sensation Build/IML74K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
		httpget.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		httpget.addHeader("Accept-Language", "en-US,en;q=0.5");
		httpget.addHeader("Accept-Encoding", "gzip, deflate");
		httpget.addHeader("Connection", "keep-alive");
		httpget.addHeader("Pragma", "no-cache");
		httpget.addHeader("Cache-Control", "no-cache");
		
		
		HttpResponse response = httpclient.execute(httpget);
		
		String status = response.getStatusLine().toString();
		System.out.println(status);
		 
		Header[] headers = response.getAllHeaders();
		System.out.println(Arrays.toString(headers));
		 
		HttpEntity entity = response.getEntity();
		BufferedReader rd = new BufferedReader(
		        new InputStreamReader(entity.getContent()));
		String line;
		while ((line = rd.readLine()) != null) {
		    System.out.println(line);
		}
	}
	
	@Test
	public void testAgentInjectionIntoAmpCheck() throws IOException {
    	String notInjectedErr = "JavaScript has not been injected! UEM enabled? Verify that User Experience sensor is placed and injection is configured correctly.";
    	
		final Logger log = Logger.getLogger(JavaScriptAgent.class.getName());
		OutputStream logCapturingStream;
		StreamHandler customLogHandler;

		logCapturingStream = new ByteArrayOutputStream();
		Handler[] handlers = log.getParent().getHandlers();
		customLogHandler = new StreamHandler(logCapturingStream, handlers[0].getFormatter());
		log.addHandler(customLogHandler);
		
		final String ampWebsite = "<!doctype html>"+BaseConstants.CRLF
				+ "<html amp lang=\"en\">"+BaseConstants.CRLF
				+ "	<head>"+BaseConstants.CRLF
				+ "		<meta charset=\"utf-8\">"+BaseConstants.CRLF
				+ "		<script async src=\"https://cdn.ampproject.org/v0.js\"></script>"+BaseConstants.CRLF
				+ "		<title>Hello, AMPs</title>"+BaseConstants.CRLF
				+ "		<link rel=\"canonical\" href=\"https://www.dynatrace.com/\">"+BaseConstants.CRLF
				+ "		<meta name=\"viewport\" content=\"width=device-width,minimum-scale=1,initial-scale=1\">"+BaseConstants.CRLF
				+ "	</head>"+BaseConstants.CRLF
				+ "	<body>"+BaseConstants.CRLF
				+ "		<h1>Welcome to the mobile web</h1>"+BaseConstants.CRLF
				+ "	</body>"+BaseConstants.CRLF
				+ "</html>";
		final String ampWebsite2 = "<!doctype html>"+BaseConstants.CRLF
				+ "<html amp>"+BaseConstants.CRLF
				+ "	<head>"+BaseConstants.CRLF
				+ "		<meta charset=\"utf-8\">"+BaseConstants.CRLF
				+ "		<script async src=\"https://cdn.ampproject.org/v0.js\"></script>"+BaseConstants.CRLF
				+ "		<title>Hello, AMPs</title>"+BaseConstants.CRLF
				+ "		<link rel=\"canonical\" href=\"https://www.dynatrace.com/\">"+BaseConstants.CRLF
				+ "		<meta name=\"viewport\" content=\"width=device-width,minimum-scale=1,initial-scale=1\">"+BaseConstants.CRLF
				+ "	</head>"+BaseConstants.CRLF
				+ "	<body>"+BaseConstants.CRLF
				+ "		<h1>Welcome to the mobile web</h1>"+BaseConstants.CRLF
				+ "	</body>"+BaseConstants.CRLF
				+ "</html>";
		final String htmlWebsite = "<!doctype html>"+BaseConstants.CRLF
				+ "<html>"+BaseConstants.CRLF
				+ "	<head>"+BaseConstants.CRLF
				+ "		<meta charset=\"utf-8\">"+BaseConstants.CRLF
				+ "		<title>Hello, HTML</title>"+BaseConstants.CRLF
				+ "	</head>"+BaseConstants.CRLF
				+ "	<body>"+BaseConstants.CRLF
				+ "		<h1>Welcome to the mobile web</h1>"+BaseConstants.CRLF
				+ "	</body>"+BaseConstants.CRLF
				+ "</html>";
		
		JavaScriptAgent.extractDTConfig(ampWebsite);
		customLogHandler.flush();
		assertFalse("Message about JavaScript injection appeared in logs.", logCapturingStream.toString().contains(notInjectedErr));
		
		JavaScriptAgent.extractDTConfig(ampWebsite2);
		customLogHandler.flush();
		assertFalse("Message about JavaScript injection appeared in logs.", logCapturingStream.toString().contains(notInjectedErr));
		
		JavaScriptAgent.extractDTConfig(htmlWebsite);
		customLogHandler.flush();
		assertTrue("Message about JavaScript injection didn't appeared in logs.", logCapturingStream.toString().contains(notInjectedErr));
	}
}
