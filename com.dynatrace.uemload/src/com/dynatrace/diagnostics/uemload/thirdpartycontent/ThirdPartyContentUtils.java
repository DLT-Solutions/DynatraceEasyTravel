package com.dynatrace.diagnostics.uemload.thirdpartycontent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.http.base.HtmlResourceParser;
import com.dynatrace.diagnostics.uemload.http.base.HttpResponse;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;

/**
 * class with static utility methods regarding third party content
 * @author Jakob.Springer
 */
public class ThirdPartyContentUtils {

	/**
	 * Creates and reports Third Party Resources for XHR requests
	 * @param xhrRequestUrl URL of XHR request
	 * @param response XHR response
	 * @param browser
	 * @param xhrActionId XHR user action id
	 * @param session
	 * @throws IOException
	 */
	public static void createXhrThirdPartyResources(final String xhrRequestUrl, final HttpResponse response, final Browser browser, final int xhrActionId, final CustomerSession session) throws IOException {
		if(xhrActionId >= 0) {
			final HtmlResourceParser htmlResourceParser = new HtmlResourceParser(true);

			final String resourcesUrl = getResourcesUrl(xhrRequestUrl);
			final Collection<String> xhrThirdPartyRsourceUrls = htmlResourceParser.listResourceReferences(resourcesUrl, response.getTextResponse());

			createXhrThirdPartyResources(xhrRequestUrl, browser, xhrActionId, session, xhrThirdPartyRsourceUrls);
		}
	}

	/**
	 * Creates and reports Third Party Resources for XHR requests
	 * @param xhrRequestUrl URL of XHR request
	 * @param browser
	 * @param xhrActionId XHR user action id, must be >= 0
	 * @param session
	 * @param xhrThirdPartyResourceUrls
	 * @throws IOException
	 */
	public static void createXhrThirdPartyResources(final String xhrRequestUrl, final Browser browser,
			final int xhrActionId, final CustomerSession session, final Collection<String> xhrThirdPartyResourceUrls)
					throws IOException {
		final List<ResourceRequestSummary> resources = new ArrayList<>();
		browser.loadResources(xhrThirdPartyResourceUrls, session, xhrRequestUrl, false, new UEMLoadCallback() {
			@Override
			public void run() throws IOException {
				browser.reportThirdPartyForXhr(resources, xhrActionId);
			}
		}, resources, xhrRequestUrl);
	}

	/**
	 * returns the resources URL for a given XHR request URL
	 * @param xhrRequestUrl
	 * @return
	 * @throws MalformedURLException
	 */
	private static String getResourcesUrl(final String xhrRequestUrl) throws MalformedURLException {
		final URL url = new URL(xhrRequestUrl);
		String resourceUrl = url.getProtocol() + "://" + url.getHost();
		if (url.getPort() != -1) {
			resourceUrl = resourceUrl + ":" + url.getPort();
		}
		return resourceUrl;
	}
}
