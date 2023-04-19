package com.dynatrace.diagnostics.uemload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;

public class ThirdpartyResourceCacheTest {
	
	private static final Logger LOGGER = LoggerFactory.make();
	
    static {
        System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");
        LOGGER.warn("Using files from: " + System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION));
    }	

	@Test
	public void testCachedThirdPartyResource() {
		//Note: expected resource sizes can be found in the easyTravelThirdPartyResourcesizes.properties File!
		testCacheHttpHttps("//assets.pinterest.com/js/pinit.js", 313, "application/javascript");
		testCacheHttpHttps("//assets.dynatrace.com/global/icons/favicon.ico", 1142, "image/vnd.microsoft.icon");
		testCacheHttpHttps("//platform.twitter.com/widgets.js", 110117, "application/javascript charset=utf-8");
		testCacheHttpHttps("//connect.facebook.net/en_US/all.js", 173750, "application/x-javascript charset=utf-8");
		testCacheHttpHttps("//ajax.googleapis.com/ajax/libs/webfont/1.4.7/webfont.js", 17698, "text/javascript charset=UTF-8");
		testCacheHttpHttps("//cdnjs.cloudflare.com/ajax/libs/jquery/1.8.1/jquery.js", 261519, "text/javascript charset=UTF-8");
		testCacheHttpHttps("//cdnjs.cloudflare.com/ajax/libs/jquery/1.8.1/jquery.min.js", 92794, "text/javascript charset=UTF-8");
		testCacheHttpHttps("//assets.pinterest.com/images/pidgets/pinit_fg_en_rect_gray_20.png", 872, "image/png");
		testCacheHttpHttps("//connect.facebook.net/en_US/all.js#xfbml=1", 173750, "application/x-javascript charset=utf-8");
		testCacheHttpHttps("//apis.google.com/js/plusone.js", 37344, "application/javascript charset=utf-8");
		testCacheHttpHttps("//platform.linkedin.com/in.js", 3768, "text/javascriptcharset=UTF-8");
		testCacheHttpHttps("//secure.statcounter.com/counter/counter.js", 21400, "application/x-javascript");
		testCacheHttpHttps("//fonts.googleapis.com/css?family=Raleway:300,400,500,700,600",1178,"text/css");
		testCacheHttpHttps("//fonts.googleapis.com/css?family=Raleway:300,400,500,700,600",1178,"text/css");
		//openweathermap.org images; see ThirdpartyResourceCache.normalizeURL how they are handled
		testCacheHttpHttps("//openweathermap.org/img/w/09n.png", 3777, "image/png");
		testCacheHttpHttps("//openweathermap.org/img/w/10d.png", 2817, "image/png");
		testCacheHttpHttps("//openweathermap.org/img/w/01d.png", 2817, "image/png");
		testCacheHttpHttps("//openweathermap.org/img/w/02n.png", 3818, "image/png");
		testCacheHttpHttps("//openweathermap.org/img/w/01dd.png", 2817, "image/png");
		testCacheHttpHttps("//cdn.ampproject.org/v0.js", 72397, "text/javascript");
		testCacheHttpHttps("//cdn.ampproject.org/v0/amp-analytics-0.1.js", 26319, "text/javascript");
		testCacheHttpHttps("//cdn.ampproject.org/v0/amp-sidebar-0.1.js", 4159, "text/javascript");
		testCacheHttpHttps("//cdn.ampproject.org/v0/amp-carousel-0.1.js", 8116, "text/javascript");
		testCacheHttpHttps("//cdn.ampproject.org/v0/amp-bind-0.1.js", 26199, "text/javascript");
		testCacheHttpHttps("//cdn.ampproject.org/v0/amp-fit-text-0.1.js", 1423, "text/javascript");
		testCacheHttpHttps("//cdn.ampproject.org/v0/amp-iframe-0.1.js", 6133, "text/javascript");
		testCacheHttpHttps("//cdn.ampproject.org/v0/amp-social-share-0.1.js", 6725, "text/javascript");
		testCacheHttpHttps("//c.amazon-adsystem.com/aax2/assoc.js", 898, "application/x-javascript");
	}
	
	private void testCacheHttpHttps(String resource, int expectedResponseSize, String expectedMimeType) {
		testCache("http:"+resource, expectedResponseSize, expectedMimeType);
		testCache("https:"+resource, expectedResponseSize, expectedMimeType);
	}
	
	@Test
	public void testNotCachedThirdPartyResource() {
		testCache("http://connect.facebook.net/en_US/not_cached.js", ThirdpartyResourceCache.THIRD_PARTY_RESOURCESIZE_DEFAULT, null);
		testCache("https://apis.google.com/js/not_cached.js", ThirdpartyResourceCache.THIRD_PARTY_RESOURCESIZE_DEFAULT, null);
	}

	@Test
	public void testNoThirdPartyResource() {
		testCache("http://www.standard.at", ThirdpartyResourceCache.NO_THIRD_PARTY_RESOURCE, null);
	}
	
	@Test
	public void testMalformedUrl() {
		testCache("qwerasdfasdf", ThirdpartyResourceCache.NO_THIRD_PARTY_RESOURCE, null);
	}
		
	private void testCache(String resource, int expectedResponseSize, String expectedMimeType) {
		//1. test response size
		assertEquals("resource " +  resource, expectedResponseSize, ThirdpartyResourceCache.getResponseSize(resource));
		
		//2. test mime type
		assertEquals("resource " +  resource, expectedMimeType, ThirdpartyResourceCache.getMimeType(resource));
	}	
	
	@Test
	public void testIsThirdPartyDomain() {
		isThirdPartyDomain("www.gstatic.com");
		isThirdPartyDomain("fonts.gstatic.com");
		isThirdPartyDomain("apis.google.com");
		isThirdPartyDomain("assets.dynatrace.com");
		isThirdPartyDomain("redirector.gvt1.com");
		isThirdPartyDomain("redirector.gvt2.com");
		isThirdPartyDomain("www.google.com");		
	}
	
	private void isThirdPartyDomain(String url) {
		assertTrue("URL: " + url, ThirdpartyResourceCache.isThirdPartyDomain(url));
		assertTrue("URL: http://" + url, ThirdpartyResourceCache.isThirdPartyDomain("http://" + url));
		assertTrue("URL: https://" + url, ThirdpartyResourceCache.isThirdPartyDomain("https://" + url));
	}
	
	@Test
	public void test() {
		String NO_PROTO_PATTERN = "(https?|ftp|file)://.*";
		assertFalse("assets.dynatrace.com".matches(NO_PROTO_PATTERN));
		assertTrue("http://assets.dynatrace.com".matches(NO_PROTO_PATTERN));
	}

}
