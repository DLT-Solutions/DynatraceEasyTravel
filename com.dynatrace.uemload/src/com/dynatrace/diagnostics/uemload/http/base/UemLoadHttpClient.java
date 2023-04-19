package com.dynatrace.diagnostics.uemload.http.base;

import java.io.Closeable;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.impl.cookie.DefaultCookieSpec;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;

import com.dynatrace.diagnostics.uemload.Bandwidth;
import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.diagnostics.uemload.NavigationTiming;
import com.dynatrace.diagnostics.uemload.VisitorId;
import com.dynatrace.diagnostics.uemload.VisitorInfo;
import com.dynatrace.diagnostics.uemload.http.base.HttpRequest.Type;
import com.dynatrace.diagnostics.uemload.http.callback.HttpResponseCallback;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.UEMLoadSession;
import com.dynatrace.diagnostics.uemload.utils.UemLoadHttpUtils;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.Http.Headers;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.base.Strings;

import ch.qos.logback.classic.Logger;

/**
 * Wrapper class around {@link HttpClient} that enriches the Http Client with
 * UemLoad specific features.
 *
 * @author stefan.moschinski
 */
public class UemLoadHttpClient implements Closeable {

	private final Bandwidth bandwidth;
	private final BrowserType browserType;
	private final CloseableHttpClient client;
	private HttpClientBuilder clientBuilder;
	private UemLoadCookieStore cookieStore;

	private static final Logger logger = LoggerFactory.make();

	private Set<Header> defaultHeaders = new HashSet<Header>();

	//ruxit only - store the serverID and visitID per Visit instead of recalculating it on every page
	private String serverID;
	private String visitID;
	private final VisitorId visitorId;
	private String sessionId;
	private String userAgent;

	private static final String EASYTRAVEL_COOKIE_SPEC = "easyTravelCookieSpec";

	/**
	 *
	 * @param bandwidth defines which bandwith is available for requests using this client
	 * @author stefan.moschinski
	 */
	public UemLoadHttpClient(Bandwidth bandwidth, BrowserType browserType) {
		this.bandwidth = bandwidth;
		this.browserType = browserType;
		this.cookieStore = new UemLoadCookieStore();
		this.clientBuilder = configure(this.clientBuilder);
		this.clientBuilder.setDefaultCookieStore(cookieStore);
		this.client = this.clientBuilder.build();
		this.visitorId = VisitorInfo.getRandomVisitorId();
	}

	public UemLoadHttpClient(Bandwidth bandwidth, BrowserType browserType, VisitorId visitor){
		this.bandwidth = bandwidth;
		this.browserType = browserType;
		this.cookieStore = new UemLoadCookieStore();
		this.clientBuilder = configure(this.clientBuilder);
		this.clientBuilder.setDefaultCookieStore(cookieStore);
		this.client = this.clientBuilder.build();
		this.visitorId = visitor;
	}
	
	public UemLoadHttpClient(Bandwidth bandwidth, BrowserType browserType, Iterable<Cookie> cookies, String userAgent) {		
		this(bandwidth, browserType);		
		
		for (Cookie cookie : cookies) {
			cookieStore.addCookie(cookie);
		}
		
		if( !Strings.isNullOrEmpty(userAgent) ) {
			this.userAgent = userAgent;
			defaultHeaders.add(new BasicHeader(Headers.USER_AGENT, userAgent));	
		}		
	}

	private HttpClientBuilder configure(HttpClientBuilder clientBuilder) {
    	CookieSpecProvider cookieSpec = new CookieSpecProvider() {
			@Override
			public CookieSpec create(HttpContext arg0) {
				return new DefaultCookieSpec() {
					@Override
					public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
						// NOSONAR - empty on purpose, we accept all cookies
					}
				};
			}
		};

    	clientBuilder = HttpClientBuilder.create();
		RequestConfig.Builder requestBuilder = RequestConfig.custom();
		requestBuilder = requestBuilder
			.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(30))
			.setConnectionRequestTimeout((int) TimeUnit.SECONDS.toMillis(30))
			.setSocketTimeout((int) TimeUnit.SECONDS.toMillis(30))
			.setCookieSpec(EASYTRAVEL_COOKIE_SPEC)
			.setStaleConnectionCheckEnabled(false);

		clientBuilder = UrlUtils.trustAllHttpsCertificates(clientBuilder);

		clientBuilder.setDefaultRequestConfig(requestBuilder.build());
		clientBuilder.setDefaultCookieSpecRegistry(RegistryBuilder.<CookieSpecProvider>create()
				.register(EASYTRAVEL_COOKIE_SPEC, cookieSpec).build());
		SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());
		clientBuilder.setRoutePlanner(routePlanner);

	    return clientBuilder;
	}

	public void request(String url, HttpResponseCallback callback) throws IOException {
		HttpRequest request = new HttpRequest(url);
		execute(request, NavigationTiming.NONE, callback, null);
	}

	public void request(Type method, String url, HttpResponseCallback callback, byte[] payload) throws IOException {
		HttpRequest request = new HttpRequest(url)
		.setMethod(method);
		execute(request, NavigationTiming.NONE, callback, payload);
	}

	public void request(String url, Collection<Header> headers, NavigationTiming nt, HttpResponseCallback callback)
			throws IOException {
		HttpRequest request = new HttpRequest(url, headers);
		execute(request, nt, callback, null);
	}

	public void request(Type method, String url, String refererUrl, HttpResponseCallback callback, byte[] payload) throws IOException {
		HttpRequest request = new HttpRequest(url, defaultHeaders)
				.setReferer(refererUrl)
				.setMethod(method);
		execute(request, NavigationTiming.NONE, callback, payload);
	}

	public void post(String url, String refererUrl, HttpResponseCallback callback, byte[] payload, List<NameValuePair> formParams) throws IOException {
		HttpRequest request = new HttpRequest(url, defaultHeaders)
				.setReferer(refererUrl)
				.setMethod(Type.POST);

		//this code doesn't seem to work
//		request.setFormParams(new UrlEncodedFormEntity(formParams, Consts.UTF_8));

		//this works but maybe it can be done in a better/easier way?
		StringBuilder queryBuilder = new StringBuilder("$");//$
		if (formParams != null && !formParams.isEmpty()) {
			for (NameValuePair nvp: formParams) {
				queryBuilder.append(URLEncodedUtils.format(Arrays.asList(nvp), BaseConstants.UTF8)).append("$");
			}
		}
		request.setFormParams(new StringEntity(queryBuilder.toString()));

		execute(request, NavigationTiming.NONE, callback, payload);
	}

	public void execute(HttpRequest request, final NavigationTiming navTiming, final HttpResponseCallback callback, byte[] payload) throws IOException {
		execute(request, navTiming, callback, payload, null);
	}

	public void execute(HttpRequest request, final NavigationTiming navTiming, final HttpResponseCallback callback, byte[] payload, HttpResponseCallback errorCallback)
			throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug(TextUtils.merge("Starting loading of ''{0}''", request.getUrl()));
		}

		if (HostAvailability.INSTANCE.isHostUnavailable(request.getUrl())) {
			logger.info(TextUtils.merge("The resource ''{0}'' will not be loaded, because the hosting server is not responding",
					request.getUrl()));

			// we do not call the callback here and thus might not close the connection. It is
			// however closed at least on the next garbage collection via the finalizer on the Socket
			return;
		}

		navTiming.createConnectionEstablishedData(bandwidth);

		final AtomicReference<UemLoadConnection> connection = new AtomicReference<UemLoadConnection>(new UemLoadConnection(client, request.getUrl(),
				request.method, cookieStore, payload));
		try {
			boolean connected = connection.get().connect(getCombinedHeaders(defaultHeaders, request.getHeaders()),
					request.getFormParams());

			if (!connected) {
				// informUnexpectedUnavailable performs more requests, therefore close connection here already to aid garbage collection
				connection.get().close();
				connection.set(null);

				HostAvailability.INSTANCE.informUnexpectedUnavailable(request.getUrl());
				return;
			}
			HttpResponse.getHttpResponse(connection.get(), bandwidth, browserType, navTiming, new HttpResponseCallback() {

				@Override
				public void readDone(HttpResponse response) throws IOException {
					boolean responseValid = connection.get().isResponseValid();
					connection.get().close();
					connection.set(null);

					navTiming.createNavigationTimingDataForResponseEnd(bandwidth);
					if (responseValid) {
						callback.readDone(response);
					} else if (errorCallback != null) {
						errorCallback.readDone(response);
					}
				}
			});
		} finally {
			UemLoadUtils.close(connection.get());
		}
	}


	/**
	 * Loads resources according to the given {@link HttpRequest} object
	 *
	 * @param request defines which resource should be loaded
	 * @param session needed to determine whether a resource should be reloaded
	 * @param callback {@link HttpResponseCallback} that is called if the method finishes
	 * @throws IOException
	 * @author stefan.moschinski
	 */
	public void executeResourceRequest(final HttpRequest request, final UEMLoadSession session,
			final HttpResponseCallback callback)
			throws IOException {
		if (logger.isTraceEnabled()) {
			logger.trace(TextUtils.merge("Starting loading of resource ''{0}''", request.getUrl()));
		}

		if (HostAvailability.INSTANCE.isHostUnavailable(request.getUrl())) {
			logger.info(TextUtils.merge("The resource ''{0}'' will not be loaded, because the hosting server is not responding",
					request.getUrl()));
			callback.readDone(null);
			return;
		}

		final AtomicReference<UemLoadConnection> connection = new AtomicReference<UemLoadConnection>(new UemLoadConnection(client, request.getUrl(), request.method, cookieStore, null));
		try {
			boolean connected = connection.get().connect(getCombinedHeaders(defaultHeaders, request.getHeaders()));
			if (!connected) {
				HostAvailability.INSTANCE.informUnexpectedUnavailable(request.getUrl());
			}
			if (!connected || !connection.get().isResponseValid()) {
				connection.get().close();
				connection.set(null);

				// although we could not load the resource, we should not skip the whole page loading
				callback.readDone(null);
				return;
			}

			if (!session.isLoadOfResourceNecessary(request.getUrl())) {
				connection.get().close();
				connection.set(null);

				callback.readDone(null);
				return;
			}

			HttpResponse.getHttpResponse(connection.get(), bandwidth, browserType, null, new HttpResponseCallback() {

				@Override
				public void readDone(HttpResponse response) throws IOException {
					session.addResource(request.getUrl(), UemLoadHttpUtils.getMaxAgeInMillis(response.getResponseHeaders()));
					connection.get().close(); // better to close it here, otherwise the connection may be open quite long
					connection.set(null);

					callback.readDone(response);
				}
			});
		} finally {
			UemLoadUtils.close(connection.get());
		}
	}

	/**
	 * Checks whether a connection to the given address can be established, <b>ignoring</b> the response code.
	 * May be misleading if you are using a proxy.
	 *
	 * @param address
	 * @return
	 * @throws IOException
	 * @author stefan.moschinski
	 */
	public boolean isConnectable(String address) throws IOException {
		UemLoadConnection connection = null;
		try {
			connection = new UemLoadConnection(client, address,
					Type.GET, cookieStore, null);
			return connection.connect(defaultHeaders);
		} finally {
			UemLoadUtils.close(connection);
		}
	}

	private HashSet<Header> getCombinedHeaders(Collection<Header> defaultHeaders, Collection<Header> requestHeaders) {
		HashSet<Header> combHeaders = new HashSet<Header>(defaultHeaders);
		combHeaders.addAll(requestHeaders);
		return combHeaders;
	}

	public UemLoadHttpClient setClientIP(String clientIP) {
		defaultHeaders.add(new BasicHeader(Headers.X_FORWARDED_FOR, clientIP));
		return this;
	}

	public UemLoadHttpClient setUserAgent(String userAgent) {
		defaultHeaders.add(new BasicHeader(Headers.USER_AGENT, userAgent));
		this.userAgent = userAgent;
		return this;
	}

	public UemLoadHttpClient addHeader(String name, String value) {
		defaultHeaders.add(new BasicHeader(name, value));
		return this;
	}

	public void setCookie(String name, String value, String url) {
		cookieStore.addCookie(new UemLoadCookie(name, value, url));
	}

	public void addCookie(Cookie cookie) {
		cookieStore.addCookie(cookie);
	}

	public void removeCookie(String name) {
		cookieStore.removeCookie(name);
	}

	public Collection<Cookie> getCookies() {
		return cookieStore.getCookies();
	}

	public String getServerID() {
		return serverID;
	}

	public void setServerID(String serverID) {
		this.serverID = serverID;
	}

	public String getVisitID() {
		return visitID;
	}

	public void setVisitID(String visitID) {
		this.visitID = visitID;
	}

	public VisitorId getVisitorId() {
		return visitorId;
	}
	
	public String getUserAgent() {
		return userAgent;
	}

	@Override
	public void close() {
		try {
			client.close();
		} catch (IOException e) {
			logger.error("Error while closing Closeable Client.", e);
		}
	}

	public BrowserType getBrowserType() {
		return browserType;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getSessionId(){
		return sessionId;
	}
}
