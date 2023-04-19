/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: EasyTravelConnection.java
 * @date: 29.06.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.diagnostics.uemload.http.base;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;

import com.dynatrace.diagnostics.uemload.http.base.HttpRequest.Type;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


/**
 * An instance of this class represents a connection to a component of easyTravel (CustomerFronted, B2BFrontend).
 *
 * @author stefan.moschinski
 */
public class UemLoadConnection implements Closeable {

	private static final int MAX_RETRIES = 3;

	private static final Logger logger = Logger.getLogger(UemLoadConnection.class.getName());

	private final String url;
	private HttpClient client;
	private HttpRequestBase request;

	private HttpResponse response;

	private HttpEntity entity;

	private UemLoadCookieStore cookieStore;

	// throttle logging of failed URLs somewhat via a cache based on URL
	private static final Cache<String, String> LOGGED = CacheBuilder.newBuilder()
			// allow multiple threads to access this concurrently
		    .concurrencyLevel(4)
		    // only keep a few entries at max to not create a memory problem via keeping many strings here
		    .maximumSize(30)
		    // keep entries for 5 minutes, then log again
		    .expireAfterWrite(5, TimeUnit.MINUTES)
		    .build();


	public UemLoadConnection(HttpClient client, String url, Type requestMethod, UemLoadCookieStore cookieStore, byte[] payload)
			throws IOException {
		this.client = client;
		this.request = Type.GET == requestMethod ? new HttpGet(url) : new HttpPost(url);
		if(Type.POST == requestMethod && payload != null){
			HttpPost postRequest = (HttpPost) request;
			postRequest.setEntity(new ByteArrayEntity(payload));
		}
		this.url = url;
		this.cookieStore = cookieStore;
	}

	/**
	 * Note: the connect method should only be called once on an UemLoadConnection object, a
	 * second invokation will fail!
	 *
	 * @param formParams
	 * @author stefan.moschinski
	 * @return
	 */
	public boolean connect(Collection<Header> headers, HttpEntity formParams) {
		Preconditions.checkNotNull(client);
		Preconditions.checkNotNull(request);

		if (formParams != null) {
			((HttpPost) request).setEntity(formParams);
		}

		request.setHeaders(headers.toArray(new Header[headers.size()]));

		if (logger.isLoggable(Level.FINEST)) {
			logger.finest(TextUtils.merge("Request to ''{0}'' has following request headers: {1}", url, headers));
		}
		
		//to prevent some unexpected errors do 3 retries before we say that we cannot connect
		//do not log WARNINGS until last retry 
		int retry = 0;
		Level logLevel = Level.FINE;
		do {
			try {
				retry++;
				if (retry >= MAX_RETRIES) {
					logLevel = Level.WARNING;
				}				
				
				response = client.execute(request);

				// memory optimization: clean out members early to aid in garbage collection, this is necessary as
				// we keep the connection-object referenced because of the recursive inner calls via the callbacks/continuations!
				client = null;
				request = null;

				if (logger.isLoggable(Level.FINEST)) {
					logger.finest(TextUtils.merge("Response of ''{0}'' has following response headers: {1}", url,
							Arrays.toString(response.getAllHeaders())));
				}

				entity = response.getEntity();
				return true;
			} catch (ClientProtocolException e) {
				logger.log(logLevel, TextUtils.merge("The connection to ''{0}'' failed", url) + ": " + e.getClass().getName() + ": " + e.getMessage() + ": retries: " + retry + "/" + MAX_RETRIES);
				logger.log(Level.FINE, TextUtils.merge("The connection to ''{0}'' failed", url), e);
			} catch (IOException e) {
				logger.log(logLevel, TextUtils.merge("The connection to ''{0}'' failed", url) + ": " + e.getClass().getName() + ": " + e.getMessage() + ": retries: " + retry+ "/" + MAX_RETRIES);
				logger.log(Level.FINE, TextUtils.merge("The connection to ''{0}'' failed", url), e);
			} 
		} while (retry < MAX_RETRIES);
		return false;
	}


	public boolean connect(Collection<Header> headers) {
		return connect(headers, null);
	}

	/**
	 *
	 * @return
	 * @author stefan.moschinski
	 */
	public boolean isResponseValid() {
		if (response == null) {
			logger.info(TextUtils.merge("The connection to ''{0}'' could not be established", url));
			return false;
		}
		int responseCode = response.getStatusLine().getStatusCode();
		if (responseCode > -1 && responseCode < HttpStatus.SC_BAD_REQUEST
				|| responseCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {	//JLT-76974
			return true;
		}

		if(logger.isLoggable(Level.INFO)) {
			// use the cache to throttle logging to not spam with this once the frontend goes awry
			if(LOGGED.getIfPresent(url) == null) {
				logger.warning(TextUtils.merge("Response code for ''{0}'' is invalid: {1}", url, responseCode));
				LOGGED.put(url, url);
			}
		}
		return false;
	}

	/**
	 *
	 * @return
	 * @author stefan.moschinski
	 */
	public String getUrl() {
		return url;
	}

	/**
	 *
	 * @return
	 * @author stefan.moschinski
	 * @throws IOException
	 * @throws IllegalStateException
	 */
	public InputStream getInputStream() throws IllegalStateException, IOException {
		return entity.getContent();
	}

	@Override
	public void close() throws IOException {
		EntityUtils.consumeQuietly(entity);

		// memory optimization: clear out members to aid in garbage collection
		entity = null;
		if(response != null) {
			response.setEntity(null);
			// this is still accessed after the connection is closed: response = null;
		}
	}

	/**
	 *
	 * @param string
	 * @return
	 * @author stefan.moschinski
	 */
	public String getHeaderValue(String name) {
		if (response == null) {
			return null;
		}
		Header lastHeader = response.getLastHeader(name);
		return lastHeader == null ? null : lastHeader.getValue();
	}

	/**
	 *
	 * @author stefan.moschinski
	 */
	public HttpEntity getHttpEntity() {
		return entity;
	}

	/**
	 *
	 * @return the status code of the connection or -1 if no connection was established
	 * @author stefan.moschinski
	 */
	public int getStatusCode() {
		return response == null ? -1 : response.getStatusLine().getStatusCode();
	}

	/**
	 *
	 * @return the currently stored {@link Cookie}s
	 * @author stefan.moschinski
	 */
	public List<Cookie> getHttpCookies() {
		return cookieStore.getCookies();
	}

	public Header[] getResponseHeaders() {
		return response.getAllHeaders();
	}

}
