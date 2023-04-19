package com.dynatrace.diagnostics.uemload.http.base;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.Bandwidth;
import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.easytravel.config.EasyTravelConfig;

/**
 *
 * @author Michal.Bakula
 *
 */
public class UemLoadHttpClientTest {
	private final EasyTravelConfig config = EasyTravelConfig.read();

	@Test
	public void cookieActionTest() {
		UemLoadHttpClient client = new UemLoadHttpClient(Bandwidth.BROADBAND, BrowserType.CHROME_57);

		client.addCookie(new BasicClientCookie("cookie", "cookie"));
		client.addCookie(new BasicClientCookie("cookie2", "cookie2"));
		assertTrue("Cookie wasn't added to HttpClient cookie store. Current cookies size:  " + client.getCookies().size(), client.getCookies().size() == 2);

		client.setCookie("cookie3", "cookie3", "http://cookie3.com");
		assertTrue("Cookie wasn't set properly. Current cookies size:  " + client.getCookies().size(), client.getCookies().size() == 3);

		client.removeCookie("cookie");
		assertTrue("Cookie wasn't removed. Current cookies size:  " + client.getCookies().size(), client.getCookies().size() == 2);

		BasicClientCookie cookie = new BasicClientCookie("cookie", "cookie1");
		cookie.setPath("path1");
		client.addCookie(cookie);

		cookie = new BasicClientCookie("cookie", "cookie2");
		cookie.setPath("path2");
		client.addCookie(cookie);

		cookie = new BasicClientCookie("cookie", "cookie3");
		cookie.setPath("path3");
		client.addCookie(cookie);
		assertTrue("Cookies not added correctly. Current cookies size:  " + client.getCookies().size(), client.getCookies().size() == 5);

		client.removeCookie("cookie");
		assertTrue("Cookies with the same name wasn't removed. Current cookies size: " + client.getCookies().size(), client.getCookies().size() == 2);

		client.close();
	}

	@Test
	@Ignore("Integration test")
	public void testHttpsSelfSignedCertificate() throws IOException {
		UemLoadHttpClient client = new UemLoadHttpClient(Bandwidth.BROADBAND, BrowserType.CHROME_57);
		assertTrue(client.isConnectable("SECRET"));
	}

}
