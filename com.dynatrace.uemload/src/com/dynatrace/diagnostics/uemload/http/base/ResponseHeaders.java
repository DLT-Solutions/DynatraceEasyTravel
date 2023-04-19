/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 * 
 * @file: ResponseHeaders.java
 * @date: 02.07.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.diagnostics.uemload.http.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.http.Header;


/**
 * The objects of this class encapsulate the response headers of a
 * web request.
 * 
 * @author stefan.moschinski
 */
public class ResponseHeaders {

	private final Header[] headers;

	/**
	 * 
	 * @param responseHeaders
	 * @author stefan.moschinski
	 */
	public ResponseHeaders(Header[] responseHeaders) {
		this.headers = responseHeaders == null || responseHeaders.length == 0 ?
				new Header[] {}
				: responseHeaders;
	}

	/**
	 * 
	 * @param headerName
	 * @return if there are no values for the given header name it returns an empty {@link Collection}, but not <code>null</code>
	 * @author stefan.moschinski
	 */
	public Collection<String> getValues(String headerName) {
		List<String> headerValues = new ArrayList<String>(4);
		for (Header header : headers) {
			if (header.getName().equalsIgnoreCase(headerName)) {
				headerValues.add(header.getValue());
			}
		}
		return headerValues;
	}

	/**
	 * 
	 * @param headerName
	 * @return
	 * @author stefan.moschinski
	 */
	public String getValue(String headerName) {
		for (Header header : headers) {
			if (header.getName().equalsIgnoreCase(headerName)) {
				return header.getValue();
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return headers == null ? "[no headers]" : Arrays.toString(headers);
	}


}
