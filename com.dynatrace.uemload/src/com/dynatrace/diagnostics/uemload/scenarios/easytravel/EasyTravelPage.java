package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.BrowserAction;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.UEMOnLoadCallback;
import com.dynatrace.diagnostics.uemload.dtheader.DynaTraceHeader;
import com.dynatrace.diagnostics.uemload.dtheader.HeaderEntry;
import com.dynatrace.diagnostics.uemload.http.base.HtmlResourceParser;
import com.dynatrace.diagnostics.uemload.http.base.HttpRequest;
import com.dynatrace.diagnostics.uemload.http.base.HttpRequest.Type;
import com.dynatrace.diagnostics.uemload.http.base.HttpResponse;
import com.dynatrace.diagnostics.uemload.http.base.ResponseHeaders;
import com.dynatrace.diagnostics.uemload.http.base.UemLoadFormBuilder;
import com.dynatrace.diagnostics.uemload.http.callback.HttpResponseCallback;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravel;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType.PageAction;
import com.dynatrace.easytravel.constants.BaseConstants.Http;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Title;


public class EasyTravelPage extends BrowserAction {

	public static volatile long sleep_time;
	protected boolean partialResponseLogging = false;
	private HtmlResourceParser htmlResourceParser;
	private final UEMLoadSession session;
	private final EtPageType page;
	private int activeSubRequests = 0;

	private static Map<String, String> gomezUrlMappig = new HashMap<String, String>();

	public EtPageType getPage() {
		return page;
	}

	public EasyTravelPage(EtPageType page, UEMLoadSession session, boolean loadDynaTraceResources) {
		this.page = page;
		this.session = session;
		this.htmlResourceParser = new HtmlResourceParser(loadDynaTraceResources);
	}

	@Override
	public void runInBrowser(Browser browser, UEMLoadCallback continuation) throws Exception {
		cont(continuation);
	}

	protected void loadPage(Browser browser, String url, UEMLoadCallback pageLoadCallback) throws IOException {
		loadPage(browser, url, null, null, pageLoadCallback);
	}

	protected void loadPage(final Browser browser, final String url, final PageAction pageAction, final String pageLoadReferer,
			final UEMLoadCallback pageLoadCallback) throws IOException
	{
		loadPage(browser, url, pageAction, pageLoadReferer, pageLoadCallback, false);
	}
	
	protected void loadPage(final Browser browser, final String url, final PageAction pageAction, final String pageLoadReferer,
			final UEMLoadCallback pageLoadCallback, final boolean loadIFrames) throws IOException {
		loadPage(browser, url, pageAction, pageLoadReferer, pageLoadCallback, null, false);
	
	}

	protected void loadPage(final Browser browser, final String url, final PageAction pageAction, final String pageLoadReferer,
			final UEMLoadCallback pageLoadCallback, final UEMOnLoadCallback pageOnLoadCallback, final boolean loadIFrames) throws IOException {
		session.getHeader().setMetaData(page.getMetaData());

		addGomezHeaders(url);

		if (EasyTravel.isUemCorrelationTestingMode) {
			System.err.println("page " + url);
		}

		browser.startPageLoad(url, page.getTitle(), session, null, pageLoadReferer, new UEMLoadCallback() {

			@Override
			public void run() throws IOException {
				session.registerPageLoad(url, browser.getLocation().getIp());
				final String responseHtml = browser.getHtml();
				ResponseHeaders responseHeaders = browser.getResponseHeaders();
				session.setResponseHtml(responseHtml);
				session.setResponseHeaders(responseHeaders);

				if (loadIFrames) {
					Collection<String> iFrameUrls = htmlResourceParser.listIframeReferences(responseHtml);
					for (String iframeUrl : iFrameUrls) {
						activeSubRequests++;

						browser.loadSubPage(iframeUrl, page.getTitle(), session, new UEMLoadCallback() {

							@Override
							public void run() throws IOException {
								activeSubRequests--;
								if (activeSubRequests == 0) {
									finishPageLoad(browser, url, pageAction, pageLoadCallback, pageOnLoadCallback, responseHtml);
								}
							}
						});
					}

				} else {
					finishPageLoad(browser, url, pageAction, pageLoadCallback, pageOnLoadCallback, responseHtml);
				}
			}

			private void finishPageLoad(
					final Browser browser,
					final String url,
					final PageAction pageAction,
					final UEMLoadCallback pageLoadCallback, 
					final UEMOnLoadCallback pageOnLoadCallback,
					final String responseHtml)
				throws IOException
			{
				final String host = getHost();
				loadResources(browser, responseHtml, host, url, browser.getSubPageHtml(), new UEMLoadCallback() {

					@Override
					public void run() throws IOException {
						if(pageOnLoadCallback!=null) {
							pageOnLoadCallback.run(new UEMLoadCallback() {
								
								@Override
								public void run() throws IOException {
									finishPageLoad();
								}
							});
						} else {
							finishPageLoad();
						}
					}
					
					private void finishPageLoad() {
						browser.finishPageLoad(
							(pageAction == null) ? page.getPageLoadTime() : page.getActionLoadTime(pageAction),
							pageLoadCallback);
					}
				});
			}
		});
	}

	/**
	 * Adds the gomez synthetic monitoring IDs to the x-dynatrace header.
	 *
	 * For the same url always the gomez ID combination is used.
	 *
	 * @param url
	 * @author cwat-hgrining
	 */
	private void addGomezHeaders(final String url) {
		// Add Gomez headers to dynaTrace header tag
		SyntheticTestHeadersGenerator gen = new SyntheticTestHeadersGenerator();
		gen.next();

		// Do we already know a ID combination for this URL? (we want to use the same ID combination for each calls of the same url)
		String appId = null;
		String monitorId = null;
		String stepId = null;
		String prevUsedIdsForUrl = gomezUrlMappig.get(url);
		if (prevUsedIdsForUrl == null) {
			// This is a new page
			appId = gen.getApplicationId();
			monitorId = gen.getMonitorId();
			stepId = gen.getStepId();
			gomezUrlMappig.put(url, appId + ";" + monitorId + ";" + stepId);
		} else {
			// This is a page we already know
			String[] parts = prevUsedIdsForUrl.split(";");
			appId = parts[0];
			monitorId = parts[1];
			stepId = parts[2];
		}
		session.getHeader().setApplicationId(appId);
		session.getHeader().setMonitorId(monitorId);
		session.getHeader().setStepId(stepId);
	}

	protected void loadResources(Browser browser, String html, String host, String referrerUrl,
			UEMLoadCallback loadResourcesCallback)
			throws IOException {
		loadResources(browser, html, host, referrerUrl, null, loadResourcesCallback);
	}

	protected void loadResources(final Browser browser, String html, final String host, final String referrerUrl,
			Map<String, String> subPages, final UEMLoadCallback loadResourcesCallback)
			throws IOException {

		final Collection<String> resourceUrls = htmlResourceParser.listResourceReferences(host, html);

		if (subPages == null || subPages.size() == 0) {
			browser.loadResources(resourceUrls, session, referrerUrl, false, loadResourcesCallback);
		} else {
			for (Entry<String, String> subPage : subPages.entrySet()) {
				activeSubRequests++;
				final String subPageUrl = subPage.getKey();
				String subPageHost = subPageUrl.substring(0, subPageUrl.indexOf('/', 8));
				Collection<String> subPageResourceUrls = htmlResourceParser.listResourceReferences(subPageHost, subPage.getValue());
				browser.loadResources(subPageResourceUrls, session, subPage.getKey(), false, new UEMLoadCallback() {

					@Override
					public void run() throws IOException {
						activeSubRequests--;
						if (browser.getSubPageAgents().containsKey(subPageUrl)) {
							browser.getSubPageAgents().get(subPageUrl).subPageLoadFinished(
									browser.getSubPageResources().get(subPageUrl),
									browser.getSubPageNavigationTimings().get(subPageUrl),
									browser.getBrowserWindowSize(),
									false, subPageUrl, browser.getViewDuration());
						}
						if (activeSubRequests == 0) {
							browser.loadResources(resourceUrls, session, referrerUrl, false, loadResourcesCallback);
						}
					}
				}, browser.getSubPageResources().get(subPageUrl), subPageUrl);
			}
		}
	}

	protected HttpRequest getXhr(Browser browser, String url, EtPageType page) {
		DynaTraceHeader header = session.getHeader().setMetaData(page.getMetaData());

		return getPostRequest(browser, url, header);
	}

	protected HttpRequest getPostRequest(Browser browser, String url, HeaderEntry headerEntry) {
		HttpRequest req =
				browser.createRequest(url)
						.setMethod(Type.POST)
						.setHeader(Http.Headers.FACES_REQUEST, "partial/ajax")
						.setHeader(Http.Headers.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8");

		if (headerEntry.hasValue()) {
			req.setHeader(headerEntry.getHeaderName(), headerEntry.getHeaderValue());
		}
		return req;
	}

	protected void sendForm(final Browser browser, UemLoadFormBuilder form, final String referrer, HttpRequest request,
			final HttpResponseCallback callback) throws IOException {
		sendForm(browser, form, referrer, request, false, callback);
	}

	protected void sendForm(final Browser browser, UemLoadFormBuilder form, final String referrer, HttpRequest request,
			final boolean loadIFrames, final HttpResponseCallback callback)
			throws UnsupportedEncodingException,
			IOException {
		request.setFormParams(form.getFormParms());

		if (EasyTravel.isUemCorrelationTestingMode) {
			System.err.println("form " + request.getUrl());
		}

		browser.send(request, new HttpResponseCallback() {

			@Override
			public void readDone(final HttpResponse response) throws IOException {
				if (EasyTravel.isUemCorrelationTestingMode) {
					// do not read resources (number varies)
					callback.readDone(response);
				} else {
					if (loadIFrames) {
						Collection<String> iFrameUrls = htmlResourceParser.listIframeReferences(response.getTextResponse());
						if (iFrameUrls.size() > 0) {
							for (final String iframeUrl : iFrameUrls) {
								activeSubRequests++;
								browser.loadSubPage(iframeUrl, Title.WEATHERFORECAST, session, new UEMLoadCallback() {

									@Override
									public void run() throws IOException {
										activeSubRequests--;

										if (activeSubRequests == 0) {
											loadResources(browser, response.getTextResponse(), session.getHost(), referrer, browser.getSubPageHtml(), new UEMLoadCallback() {

												@Override
												public void run() throws IOException {
													callback.readDone(response);
												}
											});
										}
									}
								});
							}
						} else {
							loadResources(browser, response.getTextResponse(), session.getHost(), referrer, new UEMLoadCallback() {

								@Override
								public void run() throws IOException {
									callback.readDone(response);
								}
							});
						}

					} else {
						loadResources(browser, response.getTextResponse(), session.getHost(), referrer, new UEMLoadCallback() {

							@Override
							public void run() throws IOException {
								callback.readDone(response);
							}
						});
					}
				}
			}
		});
	}

	protected int getProcessingTime() {
		return page.getPageLoadTime();
	}

	protected int getProcessingTime(PageAction pageAction) {
		return page.getActionLoadTime(pageAction);
	}

	protected HtmlResourceParser getHtmlResourceParser() {
		return htmlResourceParser;
	}

	protected void setHtmlResourceParser(HtmlResourceParser htmlResourceParser) {
		this.htmlResourceParser = htmlResourceParser;
	}

	protected UEMLoadSession getSession() {
		return this.session;
	}

	protected String getHost() {
		return this.session.getHost();
	}

	public void enablePartialResponseLogging() {
		this.partialResponseLogging = true;
	}

	public String getUrl() {
		return session.getHost() + page.getPath();
	}

}
