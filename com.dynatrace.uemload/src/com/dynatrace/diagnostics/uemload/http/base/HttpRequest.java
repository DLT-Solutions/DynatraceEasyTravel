package com.dynatrace.diagnostics.uemload.http.base;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;

import com.dynatrace.easytravel.constants.BaseConstants.Http;
import com.dynatrace.easytravel.util.TextUtils;


public class HttpRequest {

	private static final Logger logger = Logger.getLogger(HttpRequest.class.getName());

	/**
	 * Defines the type of the {@link HttpRequest}
	 * 
	 * @author stefan.moschinski
	 */
	public enum Type {
		GET,
		POST;
	}

	private final String url;
	private final Set<Header> headers;
	public Type method = Type.GET; // default

	private HttpEntity formParams;

	public HttpRequest(String url) {
		this(url, new HashSet<Header>());
	}

	public HttpRequest(String url, Collection<Header> headers) {
		this.headers = getCleanedHeaders(headers);
		this.url = url;
	}

	public HttpRequest setHeader(String name, String value) {
		if (StringUtils.trimToNull(name) == null || StringUtils.isEmpty(value)) {
			// JLT-57384: IIS does not understand empty headers. HttpRequests with empty header entries returns response code 400.
			// APM-8225: improve debug information; display 'null' string in case of null name/value
			String nameS = (name == null ? "null" : name);
			String valueS = (value == null ? "null" : value);
			logger.info(TextUtils.merge("The header with name ''{0}'' and value ''{1}'' is not valid", nameS, valueS));
			return this;
		}
		headers.add(new BasicHeader(name, value));
		return this;
	}


	public HttpRequest setMethod(Type method) {
		this.method = method;
		return this;
	}

	/**
	 * Removes empty header entries.
	 * JLT-57384: IIS does not understand empty headers. HttpRequests with empty header entries returns response code 400.
	 * 
	 * @param headers
	 * @return
	 */
	private Set<Header> getCleanedHeaders(Collection<Header> headers) {
		Set<Header> cleanedHeaders = new HashSet<Header>();
		for (Header header : headers) {
			if (StringUtils.trimToNull(header.getName()) == null) {
				continue;
			}
			cleanedHeaders.add(header);
		}
		return cleanedHeaders;
	}

	/**
	 * 
	 * @param formParms
	 * @author stefan.moschinski
	 */
	public void setFormParams(HttpEntity formParams) {
		this.formParams = formParams;
	}

	public HttpEntity getFormParams() {
		return formParams;
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
	 */
	public Collection<Header> getHeaders() {
		return Collections.unmodifiableCollection(headers);
	}

	/**
	 * 
	 * @param referer sets the referer header if the passed {@link String} is not <code>null</code> or empty
	 * @return the object the method is invoked on
	 * @author stefan.moschinski
	 */
	public HttpRequest setReferer(String referer) {
		if(referer != null) {
			setHeader(Http.Headers.REFERER, referer);
		}
		return this;
	}
}
