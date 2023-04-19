package com.dynatrace.diagnostics.uemload.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;
import com.dynatrace.easytravel.utils.TestHelpers;


public class UemLoadUrlUtilsTest {

	private String host1 = "http://host1.com";
	private String host2 = "http://host2.de/";

	private int journeyId1 = 1;
	private int journeyId2 = 2;

	@Test
	public void testGetUrlStringEasyTravelPageType() {
		assertEquals(host1 + "/" + EtPageType.ABOUT.getPath(), UemLoadUrlUtils.getUrl(host1, EtPageType.ABOUT));
		assertThat(host1 + EtPageType.ABOUT.getPath(), is(not(UemLoadUrlUtils.getUrl(host1, EtPageType.ABOUT))));
		assertEquals(host2 + EtPageType.CONTACT.getPath(), UemLoadUrlUtils.getUrl(host2, EtPageType.CONTACT));
		assertEquals(host2 + EtPageType.B2B_HOME.getPath(), UemLoadUrlUtils.getUrl(host2 + "//", EtPageType.B2B_HOME));
		assertEquals(host2 + EtPageType.SPECIAL_OFFERS.getPath(), UemLoadUrlUtils.getUrl(host2, EtPageType.SPECIAL_OFFERS));
	}

	@Test
	public void testGetUrlForJourneyStringEasyTravelPageTypeInt() {
		assertEquals(host1 + "/" + EtPageType.ABOUT.getPath() + "?journeyId=" + journeyId1, UemLoadUrlUtils.getUrlForJourney(host1, EtPageType.ABOUT, journeyId1));
		assertEquals(host2 + EtPageType.FINISH.getPath() + "?journeyId=" + journeyId2, UemLoadUrlUtils.getUrlForJourney(host2, EtPageType.FINISH, journeyId2));

		assertEquals(host2 + EtPageType.FINISH.getPath(), UemLoadUrlUtils.getUrlForJourney(host2, EtPageType.FINISH, 0));
		assertEquals(host2 + EtPageType.FINISH.getPath(), UemLoadUrlUtils.getUrlForJourney(host2, EtPageType.FINISH, Integer.MIN_VALUE));
	}

	@Test
	public void testGetUrl() {
		assertEquals("http://localhost/", UemLoadUrlUtils.getUrl("http://localhost", null, (NameValuePair[])null));
		assertEquals("http://localhost/", UemLoadUrlUtils.getUrl("http://localhost", "/", (NameValuePair[])null));
		assertEquals("http://localhost/", UemLoadUrlUtils.getUrl("http://localhost", "/", new NameValuePair[] {}));
		assertEquals("http://localhost/hallo", UemLoadUrlUtils.getUrl("http://localhost", "/hallo", new NameValuePair[] {}));
		assertEquals("http://localhost/?name1=value2", UemLoadUrlUtils.getUrl("http://localhost", "/", new NameValuePair[] {
				new BasicNameValuePair("name1", "value2")
		}));

		assertEquals("http://localhost/", UemLoadUrlUtils.getUrl("http://localhost", "/", (Collection<NameValuePair>)null));
		assertEquals("http://localhost/?$name1=value2$", UemLoadUrlUtils.getUrl("http://localhost", "/", Arrays.asList(new NameValuePair[] {
				new BasicNameValuePair("name1", "value2")
		})));

		try {
			UemLoadUrlUtils.getUrl("invalidurl", "/", new NameValuePair[] {});
			fail("Should catch exception here");
		} catch (RuntimeException e) {
			TestHelpers.assertContains(e.getCause(), "invalidurl");
			TestHelpers.assertContains(e.getCause(), "no protocol", "invalidurl");
		}
	}

	@Test
	public void testGetExtendedHostUrl() throws MalformedURLException {
		assertEquals("http://localhost:8080", UemLoadUrlUtils.getExtendedHostUrl("http://localhost:8080/orange.jsf"));
	}

	@Test
	public void testGetExtendedHostUrlTrailingSlash() throws MalformedURLException {
		assertEquals("http://localhost:8080/", UemLoadUrlUtils.getExtendedHostUrlTrailingSlash("http://localhost:8080/orange.jsf"));
	}

	@Test
	public void testGetHostUrl() throws MalformedURLException {
		assertEquals("localhost", UemLoadUrlUtils.getHost("http://localhost:8080/orange.jsf"));
		assertEquals("www.easytravel.com", UemLoadUrlUtils.getHost("SECRET"));

		try {
			UemLoadUrlUtils.getHost("invalidurl");
			fail("Should catch exception here");
		} catch (RuntimeException e) {
			TestHelpers.assertContains(e, "invalidurl");
			TestHelpers.assertContains(e.getCause(), "no protocol", "invalidurl");
		}
	}

	@Test
	public void encodeUrlToUTF8() {
		assertNull(UemLoadUrlUtils.encodeUrlUtf8(null));

		assertEquals("localhost_with_%2F%2F%2F", UemLoadUrlUtils.encodeUrlUtf8("localhost_with_///"));
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(UemLoadUrlUtils.class);
	}
}
