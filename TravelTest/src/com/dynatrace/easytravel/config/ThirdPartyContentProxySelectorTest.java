package com.dynatrace.easytravel.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.google.common.base.Optional;

public class ThirdPartyContentProxySelectorTest {
	private static final Logger LOGGER = LoggerFactory.make();

	private MockRESTServer server;

	@Before
	public void setUp() throws Exception {
		// clear config to avoid running into the 10 second-refresh-timeframe
		EasyTravelConfig.resetSingleton();

		EasyTravelConfig config = EasyTravelConfig.read();

		// if proxy is not available, create a dummy one
		if(StringUtils.isEmpty(config.proxyHost) || !UrlUtils.checkServiceAvailability(config.proxyHost, config.proxyPort)) {
			LOGGER.info("Starting local proxy as global one is not available");
			server = new MockRESTServer(NanoHTTPD.HTTP_OK, "", "");

			config.proxyHost = "localhost";
			config.proxyPort = server.getPort();
		}
	}

	@After
	public void tearDown() {
		if(server != null) {
			server.stop();
			server = null;
		}

		// reset after tests to not fail other tests
		EasyTravelConfig.resetSingleton();
	}

	@Test
	public void testApplyProxy() {
		// default settings
		ThirdPartyContentProxySelector.applyProxy();
		assertTrue("Proxy should be applied now",
				ProxySelector.getDefault() instanceof ThirdPartyContentProxySelector);

		// unavailable proxy set
		EasyTravelConfig.read().proxyHost="invalidhost";
		ThirdPartyContentProxySelector.clearProxy();
		ThirdPartyContentProxySelector.applyProxy();
		assertFalse("No proxy should be applied now because of invalid proxy host",
				ProxySelector.getDefault() instanceof ThirdPartyContentProxySelector);

		// no proxy set
		EasyTravelConfig.read().proxyHost=null;
		ThirdPartyContentProxySelector.clearProxy();
		ThirdPartyContentProxySelector.applyProxy();
		assertFalse("No proxy should be applied now because of empty proxy host",
				ProxySelector.getDefault() instanceof ThirdPartyContentProxySelector);

		// clear up after test
		ThirdPartyContentProxySelector.clearProxy();
	}


	@Test
	public void testPatterns() throws Exception {
		ThirdPartyContentProxySelector.applyProxy();
		assertTrue("Proxy should be applied now",
				ProxySelector.getDefault() instanceof ThirdPartyContentProxySelector);

		// most patterns should not match
		noProxy("SECRET");
		noProxy("http://localhost");
		noProxy("http://localhost:8079/nonblocked/dynaTraceMonitor?title=easyTravel+-+Booking+-+Your+Journey&bw=3_15&pId=G_-8158801330828930786&fId=G_-8158801330828930786&pFId=&rId=RID_603449254&rpId=1778368346&actions=s%7Cclick%2Bon%2BBook%2BNow%7Cicefaces.ajax%7CG_939240413624463976%7C1336641388806%2C1%7C_load_%7C-%7C_load_%7C1336641388807%7C1336641388949%7C19%2C2%7C_onload_%7C-%7C_load_%7C1336641388949%7C1336641388949%7C19&dtV=undefined&time=1336641388949&3p=apis.google.com%7C0%7C0%7C0%7C%7C0%7C0%7C0%7C1%7C38_137%7C99%7C99%7C99%7C0%7C%7C0%7C0%7C0%3Bplatform.twitter.com%7C0%7C0%7C0%7C%7C0%7C0%7C0%7C1%7C137_139%7C2%7C2%7C2%7C0%7C%7C0%7C0%7C0%3Bconnect.facebook.net%7C0%7C0%7C0%7C%7C0%7C0%7C0%7C1%7C141_142%7C1%7C1%7C1%7C0%7C%7C0%7C0%7C0");
		noProxy("http://www.gmx.at/");
		noProxy("http://localhost:1697/start?property=config.proxiedPatterns:google.com,twitter.com,dynatrace.com,facebook.net,facebook.com");
		noProxy("http://localhost:1697/start?property=config.proxiedPatterns:google.com,twitter.com,dynatrace.com,facebook.net,facebook.com");
		noProxy("socket://omg/");

		// some should match
		hasProxy("http://google.com");
		hasProxy("http://twitter.com");
		hasProxy("http://dynatrace.com");
		hasProxy("http://facebook.net");
		hasProxy("http://facebook.com");
		hasProxy("http://api.google.com/api123?blabla=localhost");
		hasProxy("socket://somewhere.at.google.com/with/some/url");

		ThirdPartyContentProxySelector.clearProxy();
	}

	@Test
	public void testCreateProxyRegexPatternFor() {
		Optional<Pattern> nullProxiedSites = ThirdPartyContentProxySelector.createProxyRegexPatternFor(null);
		assertThat(nullProxiedSites.isPresent(), is(false));

		Optional<Pattern> emptyProxiedSites = ThirdPartyContentProxySelector.createProxyRegexPatternFor(new String[] {});
		assertThat(emptyProxiedSites.isPresent(), is(false));

		Optional<Pattern> oneProxiedSite = ThirdPartyContentProxySelector.createProxyRegexPatternFor(new String[] { "google" });
		assertThat(oneProxiedSite.isPresent(), is(true));
	}

	// for local testing only:
//	@Test
	public void testLocalConnection() throws Exception {
		ThirdPartyContentProxySelector.applyProxy();
		assertTrue("Proxy should be applied now",
				ProxySelector.getDefault() instanceof ThirdPartyContentProxySelector);

		CloseableHttpClient httpclient = HttpClientBuilder.create().
				// useSystemProperties should make HttpClient use the proxy available via ProxySelector.getDefault()
				useSystemProperties().
				build();

		try {
			HttpGet httpGet = new HttpGet("http://google.com");

			HttpResponse response1 = httpclient.execute(httpGet);

			try {
				System.out.println(response1.getStatusLine());
				HttpEntity entity1 = response1.getEntity();
				// do something useful with the response body
				// and ensure it is fully consumed
				EntityUtils.consume(entity1);
			} finally {
				httpGet.releaseConnection();
			}
		} finally {
			httpclient.close();
		}

		ThirdPartyContentProxySelector.clearProxy();
	}

	private Proxy getProxy(String url) throws URISyntaxException {
		return ProxySelector.getDefault().select(new URI(url)).get(0);
	}

	private void noProxy(String url) throws URISyntaxException {
		assertEquals(Proxy.NO_PROXY, getProxy(url));
	}

	private void hasProxy(String url) throws URISyntaxException {
		assertFalse(getProxy(url).equals(Proxy.NO_PROXY));
	}
}
