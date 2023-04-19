package com.dynatrace.diagnostics.uemload;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.Header;

import com.dynatrace.diagnostics.uemload.BrowserType.BrowserFamily;
import com.dynatrace.diagnostics.uemload.JavaScriptAgent.JavaScriptAgentCallback;
import com.dynatrace.diagnostics.uemload.dtheader.DynaTraceHeader;
import com.dynatrace.diagnostics.uemload.http.base.HttpRequest;
import com.dynatrace.diagnostics.uemload.http.base.HttpResponse;
import com.dynatrace.diagnostics.uemload.http.base.ResponseHeaders;
import com.dynatrace.diagnostics.uemload.http.callback.HttpResponseCallback;
import com.dynatrace.diagnostics.uemload.http.exception.InvalidStatusCodeException;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.UEMLoadSession;
import com.dynatrace.diagnostics.uemload.thirdpartycontent.ResourceRequestSummary;
import com.dynatrace.diagnostics.uemload.utils.UemLoadHttpUtils;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.constants.BaseConstants.Http;

public class Browser extends ActionExecutor {

	private static final Logger log = Logger.getLogger(Browser.class.getName());

	private final BrowserType type;
	private final boolean isJavaScriptEnabled;
	private String dtAdkCookie;

	private String currentPage;
	private JavaScriptAgent agent;
	private String html;
	private List<ResourceRequestSummary> loadedResources = new ArrayList<ResourceRequestSummary>();
	private ResponseHeaders responseHeaders;
	private NavigationTiming navTiming;
	private int dnsSlowdownFactor = 1;
	private BrowserWindowSize bws;

	// Data for subpages (e.g. frames, iframes, popups, ..) that should not modify the main browser state
	// when loaded.
	private Map<String, String> subPageHtml = new HashMap<String, String>();
	private Map<String, ResponseHeaders> subPageResponseHeaders = new HashMap<String, ResponseHeaders>();
	private Map<String, JavaScriptAgent> subPageAgents = new HashMap<String, JavaScriptAgent>();
	private Map<String, List<ResourceRequestSummary>> subPageResources = new HashMap<String, List<ResourceRequestSummary>>();
	private Map<String, NavigationTiming> subPageNavigationTiming = new HashMap<String, NavigationTiming>();

	private long time = 0;
	protected long viewDuration = 0;

	public Browser(BrowserType browserType, Location location, int latency, Bandwidth bandwidth, BrowserWindowSize bws) {
		super(location, latency, bandwidth, browserType, browserType.getUserAgent());
		this.type = browserType;
		this.isJavaScriptEnabled = type.isJavaScriptSupported();
		this.bws = bws;
	}
	
	public Browser(BrowserType browserType, Location location, int latency, Bandwidth bandwidth, BrowserWindowSize bws, VisitorId visitorId) {
		super(location, latency, bandwidth, browserType, browserType.getUserAgent(), visitorId);
		this.type = browserType;
		this.isJavaScriptEnabled = type.isJavaScriptSupported();
		this.bws = bws;
	}

	public BrowserType getType() {
		return type;
	}

	public boolean isRuxitSynthetic() {
		return type.isRuxitSynthetic();
	}

	public void startPageLoad(final String url, final String title, final UEMLoadSession session, final String[] resources,
			final String pageLoadReferer, final UEMLoadCallback pageLoadCallback) throws IOException {
		this.currentPage = url;
		this.loadedResources.clear();

		// important to clear all subpages & their information or otherwise they might
		// be reloaded with every further page load
		this.subPageHtml.clear();
		this.subPageAgents.clear();
		this.subPageNavigationTiming.clear();
		this.subPageResources.clear();
		this.subPageResponseHeaders.clear();

		if(dtAdkCookie != null)
			http.setCookie("dtAdk", dtAdkCookie, url); //$NON-NLS-1$
		
		if(visitorId != null)
			http.setCookie("rxVisitor", visitorId.getVisitorId(), url);

		this.navTiming = NavigationTiming.start();
		this.navTiming.setDNSSlowdownFactor(dnsSlowdownFactor);

		DynaTraceHeader header = session.getHeader();
		Collection<Header> headers = UemLoadHttpUtils.getHeaderSingleton(header.getHeaderName(), header.getHeaderValue());

		http.request(url, headers, navTiming, new HttpResponseCallback() {

			private int statusCode;

			@Override
			public void readDone(HttpResponse response) throws IOException {
				this.statusCode = response.getStatusCode();

				throwPageNotAvailableExceptionIfInvalidStatusCodeIsInvalid();

				html = response.getTextResponse();
				responseHeaders = response.getResponseHeaders();

				afterPageLoaded(url, title, session, resources, pageLoadCallback);
			}

			private void afterPageLoaded(final String url, final String title,
					final UEMLoadSession session, final String[] resources,
					final UEMLoadCallback pageLoadCallback) throws IOException {
				if (isJavaScriptEnabled) {
					String sourceAction = agent == null ? null : agent.getSourceAction();

					JavaScriptAgent.getJavaScriptAgent(html, http, url, title, bandwidth, sourceAction, pageLoadReferer,
							new JavaScriptAgentCallback() {

								@Override
								public void run(JavaScriptAgent a) throws IOException {
									agent = a;
									agent.pageLoadStarted(html);
									pageLoadCallback.run();
								}
							});
				} else {
					if (resources != null) {
						loadResources(Arrays.asList(resources), session, url, true, pageLoadCallback);
					} else {
						pageLoadCallback.run();
					}
				}
			}

			private void throwPageNotAvailableExceptionIfInvalidStatusCodeIsInvalid() throws IOException {
				if (statusCode < InvalidStatusCodeException.RANGE_START) {
					return;
				}

				throw new InvalidStatusCodeException(url, statusCode);
			}
		});
	}

	/**
	 * uses the java script agent to send a third party resources beacon signal for a XHR request
	 * @param resources
	 * @param xhrActionId
	 * @throws IOException
	 */
	public void reportThirdPartyForXhr(List<ResourceRequestSummary> resources, int xhrActionId) throws IOException {
		this.agent.sendThirdPartyResourcesForXhr(resources, this.navTiming, xhrActionId, viewDuration);
	}

	/**
	 * Assert if the specified response is OK.
	 *
	 * @param response
	 * @throws IOException
	 * @author martin.wurzinger
	 */
	private void assertResponse(HttpResponse response) throws IOException {
		Assert.assertEquals(200, response.getStatusCode());
		Assert.assertEquals(0, response.getCookies().size());
	}

	public void loadResources(Collection<String> resourceUrls, final UEMLoadSession session, final String referrerUrl,
			final boolean isAssertionRequired,
			final UEMLoadCallback loadResourcesCallback) throws IOException {
		loadResources(resourceUrls, session, referrerUrl, isAssertionRequired, loadResourcesCallback, loadedResources, currentPage);
	}

	/**
	 * Load a set of web resources (JPG, CSS, JS,...)
	 *
	 * @param resourceUrls the URLs of the resources to load
	 * @param refererUrl the HTTP referer to set or <code>null</code> if no referer has to be set
	 * @param isAssertionRequired <code>true</code> if asserting response is required
	 */
	public void loadResources(Collection<String> resourceUrls, final UEMLoadSession session, final String referrerUrl,
			final boolean isAssertionRequired,
			final UEMLoadCallback loadResourcesCallback,
			final List<ResourceRequestSummary> loadedResources,
			final String url) throws IOException {
		final Iterator<String> iterator = resourceUrls.iterator();
		UEMLoadCallback callback = new UEMLoadCallback() {

			@Override
			public void run() throws IOException {
				synchronized (iterator) {
					if (iterator.hasNext()) {
						String temp = iterator.next();
						loadResource(temp, referrerUrl, session, isAssertionRequired, this, loadedResources, url);
					} else if (loadResourcesCallback != null) {
						loadResourcesCallback.run();
					}
				}
			}
		};

		callback.run();
	}

		/**
		 * Load a single web resource (JPG, CSS, JS,...)
		 *
		 * @param resourceUrl the URL of the resource to load
		 * @param refererUrl the HTTP referer to set or <code>null</code> if no referer has to be set
		 * @param isAssertionRequired <code>true</code> if asserting response is required
		 * @return detailed information about timing details of loading a resource by url
		 * @throws IOException if a network IO problem occur
		 * @author martin.wurzinger
		 */
		private void loadResource(final String resourceUrl, String refererUrl, final UEMLoadSession session,
				final boolean isAssertionRequired, final UEMLoadCallback loadResourceCallBack,
				final List<ResourceRequestSummary> resources, final String url) throws IOException {
		final ResourceRequestSummary summary = new ResourceRequestSummary();

		summary.setResourceUrl(resourceUrl);
		summary.setLoadstart(System.currentTimeMillis());

		//check if resource is an external third party resource (e.g. facebook)
		int responseSize = ThirdpartyResourceCache.getResponseSize(resourceUrl);
		if (responseSize != ThirdpartyResourceCache.NO_THIRD_PARTY_RESOURCE) {
			String mimeType = ThirdpartyResourceCache.getMimeType(resourceUrl);
			simulateThirdPartyDownload(loadResourceCallBack, summary, responseSize, mimeType);
			return;
		}

		HttpRequest request = new HttpRequest(resourceUrl).setReferer(refererUrl);

		DynaTraceHeader headerEntry = session.getHeader();
		if (headerEntry.hasValue()) {
			request.setHeader(headerEntry.getHeaderName(), headerEntry.getHeaderValue());
		}

		final boolean loadedFromCache = !session.isLoadOfResourceNecessary(resourceUrl);

		http.executeResourceRequest(request, session, new HttpResponseCallback() {

			@Override
			public void readDone(HttpResponse response) throws IOException {
				if (isAssertionRequired) {
					assertResponse(response);
				}

				summary.setLoadfinished(System.currentTimeMillis());
				summary.setLoadedFromCache(loadedFromCache);

				String resourceUrl = summary.getResourceUrl();
				boolean is3rdParty = summary.isThirdPartyResource(url);// internal third party resource (like node js weather)

				if (response == null) {
					if (!loadedFromCache) {
						log.warning(String.format(
								"Loading of %sresource [%s] for page [%s] failed. Set HTTP status to 404.",
								is3rdParty ? "third party " : "",
								resourceUrl,
								url));

						summary.setStatusCode(HttpStatus.SC_NOT_FOUND);
					} else {
						log.fine(String.format("Loaded %sresource [%s] for page [%s] from cache.",
								is3rdParty ? "third party " : "",
								resourceUrl,
								url));
					}

					// set mime type
					String mimeType = URLUtil.guessMimeType(resourceUrl);
					if (mimeType != null) {
						summary.setMimeType(mimeType);
					}
				} else {
					String mimeType = getMimeType(response, resourceUrl);
					summary.setStatusCode(response.getStatusCode());
					summary.setMimeType(mimeType);
					summary.setResponseSize(response.getTextResponse().length());
					ResponseHeaders responseHeaders = response.getResponseHeaders();
					summary.setResponseHeadersSize(responseHeaders==null?0:responseHeaders.toString().length()-2);
				}

				resources.add(summary);
				loadResourceCallBack.run();
			}

			private String getMimeType(HttpResponse response, String resourceUrl) {
				// set mime type
				ResponseHeaders resourceResponseHeaders = response.getResponseHeaders();
				String value = resourceResponseHeaders.getValue(Http.Headers.CONTENT_TYPE);
				if (value == null) {
					if (resourceUrl.endsWith(".ico")) {
						return "image/x-icon";
					} else {
						log.warning(String.format("No '%s' in header of response for '%s' found",
								Http.Headers.CONTENT_TYPE,
								resourceUrl));

						String mimeType = URLUtil.guessMimeType(resourceUrl);
						if (mimeType != null) {
							return mimeType;
						}
					}
				} else {
					return value;
				}
				return null;
			}
		});
	}

	private void simulateThirdPartyDownload(
			final UEMLoadCallback loadResourceCallBack,
			final ResourceRequestSummary summary, int responseSize, String mimeType)
			throws IOException {

		int bandwidthLimit = UemLoadUtils.randomBandwidth(bandwidth, http.getBrowserType()) / (8 * 1000 );
		try {
			if (log.isLoggable(Level.FINE)) {
				log.fine("Simulating ThirdParty Resource download  of " + summary.getResourceUrl() + " with size " + responseSize+ " byte and bandwidth " + bandwidthLimit + " by sleeping " + responseSize / bandwidthLimit + " ms");
			}
			Thread.sleep(responseSize / bandwidthLimit);
		} catch (InterruptedException e) {
			if (log.isLoggable(Level.INFO)) {
				log.info("ThirdPartyDomain download is only simulated, but Thread.sleep has been interrupted");
			}
		}
		summary.setLoadfinished(System.currentTimeMillis());
		summary.setLoadedFromCache(false);
		summary.setMimeType(mimeType);
		summary.setResponseSize(responseSize);

		loadedResources.add(summary);
		if (loadResourceCallBack != null) {
			loadResourceCallBack.run();
		}
		return;
	}

	/*
	 * TODO a exception in agent.pageLoadFinished() or continuation.run() might be swallowed as get() of the ScheduledFuture
	 * created on scheduling the Callable will never be called... (The ScheduledFuture) is thrown away
	 */
	public void finishPageLoad(int averagePageLoadTime, final UEMLoadCallback continuation) {
		try {
			int minimalDelayInMillis = 0;
			Callable<Void> finish = new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					try {
						if (agent != null) {
							agent.pageLoadFinished(loadedResources, navTiming, bws, viewDuration);
						}
					} finally {
						if (continuation != null) {
							continuation.run();
						}
					}
					return null;
				}
			};
			int sleepTime = minimalDelayInMillis + getLatency();

			if (getSlowdownFactor() > 1) {
				sleepTime = getSlowdownFactor() + sleepTime * (getSlowdownFactor() / 1000);
			}

			if (sleepTime > 0) {
				UemLoadScheduler.sleep(sleepTime, finish);
			} else {
				finish.call();
			}

		} catch (Exception e) {
			log.log(Level.WARNING, "Could not end JavaScript action.", e);
		}
	}

	public void loadPage(String url, String title, String[] resources, final int pageLoadTime, final String pageLoadReferer,
			final UEMLoadCallback pageLoadCallback)
			throws IOException {


		startPageLoad(url, title, null, resources, pageLoadReferer, new UEMLoadCallback() {

			@Override
			public void run() {
				finishPageLoad(pageLoadTime, pageLoadCallback);
			}
		});
	}

	public HttpRequest createRequest(String url) {
		return new HttpRequest(url).setReferer(url);
	}

	public HttpRequest createRequest(String url, String additionalHeader, String additionalHeaderValue) {
		HttpRequest req = createRequest(url);
		if (additionalHeader != null) {
			req.setHeader(additionalHeader, additionalHeaderValue);
		}
		return req;
	}
	
	public void send(HttpRequest req, HttpResponseCallback callback, HttpResponseCallback errorCallback) throws IOException {
		http.execute(req, navTiming, callback, null, errorCallback);
	}

	public void send(HttpRequest req, HttpResponseCallback callback) throws IOException {
		http.execute(req, navTiming, callback, null);
	}

	public void addHeader(String name, String value) {
		http.addHeader(name, value);
	}

	public String getHtml() {
		return html;
	}

	public Map<String, String> getSubPageHtml() {
		return subPageHtml;
	}

	public Map<String, ResponseHeaders> getSubPageResponseHeaders() {
		return subPageResponseHeaders;
	}

	public Map<String, JavaScriptAgent> getSubPageAgents() {
		return subPageAgents;
	}

	public Map<String, List<ResourceRequestSummary>> getSubPageResources() {
		return subPageResources;
	}

	public BrowserWindowSize getBrowserWindowSize() {
		return bws;
	}

	public String getCurrentPage() {
		return currentPage;
	}

	public Map<String, NavigationTiming> getSubPageNavigationTimings() {
		return subPageNavigationTiming;
	}

	public void startCustomAction(String name, String type, String info) {

		if (agent != null) {
			this.loadedResources.clear();
			this.navTiming = NavigationTiming.start();
			agent.startCustomAction(name, type, info);
		}
	}

	public void startCustomAction(String name, String type, String info, String xhrUrl) {

		if (agent != null) {
			this.loadedResources.clear();
			this.navTiming = NavigationTiming.start();
			agent.startCustomAction(name, type, info, xhrUrl);
		}
	}

	public void startCustomAction(String name, String type, String info, String xhrUrl, int hierarchy) {

		if (agent != null) {
			if(hierarchy<=1) {
				this.loadedResources.clear();
				this.navTiming = NavigationTiming.start();
			}
			agent.startCustomAction(name, type, info, xhrUrl, hierarchy);
		}
	}

	/**
	 * stops a custom action, returns the custom action id
	 * @param isIncomplete
	 * @return
	 * @throws IOException
	 */
	public int stopCustomAction(boolean isIncomplete) throws IOException {
		return stopCustomAction(isIncomplete, false);
	}

	public int stopCustomAction(boolean isIncomplete, boolean xhrInOnLoad) throws IOException {
		if (agent != null) {
			return agent.stopCustomAction(isIncomplete, xhrInOnLoad, loadedResources, navTiming, bws, false, null, viewDuration);
		}
		return -1;
	}

	/**
	 * stops a custom action, returns the custom action id
	 * @param isIncomplete
	 * @param actions
	 * @return
	 * @throws IOException
	 */
	public int stopCustomAction(boolean isIncomplete, Collection<String> actions) throws IOException {
		if (agent != null) {
			return agent.stopCustomAction(isIncomplete, false, loadedResources, navTiming, bws, false, actions, viewDuration);
		}
		return -1;
	}

	public void sendJavaScriptErrors(Collection<JavaScriptErrorAction> actions) throws IOException {
		if (agent != null) {
			agent.sendJavaScriptErrors(actions);
		}
	}

	public void sendSyntheticEndVisit() throws IOException {
		if (agent != null) {
			agent.sendSyntheticEndVisit();
		}
	}

	public ResponseHeaders getResponseHeaders() {
		return responseHeaders;
	}

	public void setDtAdkCookie(String dtAdkCookie) {
		this.dtAdkCookie = dtAdkCookie;
	}

	public void setDNSSlowdownFactor(int dnsSlowdownFactor) {
		this.dnsSlowdownFactor = dnsSlowdownFactor;
	}

	private int getSlowdownFactor() {
		if (PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.JAVASCRIPT_CHANGE_DETECTION_WITH_SLOW_BROWSER) && type.getBrowserFamily() == BrowserFamily.IE) {
			return 10000; // 10 sec
		}
		return 1;
	}

	public void loadSubPage(
			final String url,
			final String title,
			final UEMLoadSession session,
			final UEMLoadCallback uemLoadCallback)  throws IOException
	{
		subPageResources.put(url, new ArrayList<ResourceRequestSummary>());

		NavigationTiming subPageNavTiming = NavigationTiming.start();
		subPageNavigationTiming.put(url, subPageNavTiming);

		if (dtAdkCookie != null) {
			http.setCookie("dtAdk", dtAdkCookie, url); //$NON-NLS-1$
		}
		
		if(visitorId != null)
			http.setCookie("rxVisitor", visitorId.getVisitorId(), url);

		DynaTraceHeader header = session.getHeader();
		Collection<Header> headers = UemLoadHttpUtils.getHeaderSingleton(header.getHeaderName(), header.getHeaderValue());

		http.request(url, headers, subPageNavTiming, new HttpResponseCallback() {

			private int statusCode;

			@Override
			public void readDone(HttpResponse response) throws IOException {
				this.statusCode = response.getStatusCode();

				throwPageNotAvailableExceptionIfInvalidStatusCodeIsInvalid();

				String subPageHtml = response.getTextResponse();
				ResponseHeaders subPageResponseHeaders = response.getResponseHeaders();

				getSubPageHtml().put(url, subPageHtml);
				getSubPageResponseHeaders().put(url, subPageResponseHeaders);

				afterPageLoaded(url, title, session, uemLoadCallback);
			}

			private void throwPageNotAvailableExceptionIfInvalidStatusCodeIsInvalid() throws IOException {
				if (statusCode < InvalidStatusCodeException.RANGE_START) {
					return;
				}

				throw new InvalidStatusCodeException(url, statusCode);
			}

			private void afterPageLoaded(
					final String url,
					final String title,
					final UEMLoadSession session,
					final UEMLoadCallback uemLoadCallback)
				throws IOException
			{
				if (isJavaScriptEnabled) {
					String sourceAction = agent == null ? null : agent.getSourceAction();

					JavaScriptAgent.getJavaScriptAgent(getSubPageHtml().get(url), http, url, title, bandwidth, sourceAction, null, new JavaScriptAgentCallback() {

						@Override
						public void run(JavaScriptAgent a) throws IOException {
							a.pageLoadStarted(getSubPageHtml().get(url));
							getSubPageAgents().put(url, a);
							uemLoadCallback.run();
						}
					});
				} else {
					uemLoadCallback.run();
				}
			}
		});
	}

	/**
	 * Updates the time and calculates the current view duration between two browser actions.
	 */
	public void updateViewDuration() {
		if (time != 0) {
			viewDuration = System.currentTimeMillis() - time;
		}
		time = System.currentTimeMillis();
	}

	public long getViewDuration() {
		return viewDuration;
	}
	
	public NavigationTiming getNavigationTiming() {
		return navTiming;
	}
}
