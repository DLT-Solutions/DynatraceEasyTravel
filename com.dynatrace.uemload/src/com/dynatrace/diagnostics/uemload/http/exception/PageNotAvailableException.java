package com.dynatrace.diagnostics.uemload.http.exception;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PageNotAvailableException extends IOException {
	private static final long serialVersionUID = 1L;

	private static final String EXCEPTION_MESSAGE = "The host %s is currently not available. " +
			"Probably, the Customer Frontend or, respectively, " +
			"the Business Frontend is not running.";

	public static final String NO_WARNING = "NO_WARNING";
	private static final String HOST_REGEX = "\\b(https?|ftp|file)://([-a-zA-Z0-9+&@#%?=~_|!:,.;]*)";
	private static final Pattern pattern = Pattern.compile(HOST_REGEX);

	private static Map<String, Long> unavailablePages = new ConcurrentHashMap<String, Long>();

	private final String hostName;

	private final long ONE_MINUTE = 60 * 1000;

	private final int httpResponseCode;
	private final boolean warning;

	public PageNotAvailableException(String url, int httpResponseCode) {
		this.hostName = getHostName(url);
		this.httpResponseCode = httpResponseCode;

		if(isThrowingExceptionRequired()) {
			unavailablePages.put(hostName, System.currentTimeMillis());
			warning = true;
		} else {
			warning = false;
		}
	}


	private String getHostName(String url) {
		Matcher matcher = pattern.matcher(url);
		if(matcher.find())
			return matcher.group(2);

		throw new IllegalArgumentException("The url " + url + " is no valid host name.");
	}

	@Override
	public String getMessage() {
		if(warning) {
			return getExceptionMessage();
		}
		return NO_WARNING;
	}

	private boolean isThrowingExceptionRequired() {
		if(!unavailablePages.containsKey(hostName))
			return true;
		if(System.currentTimeMillis() - unavailablePages.get(hostName) >= ONE_MINUTE)
			return true;

		return false;
	}

	private String getExceptionMessage() {
		return String.format(EXCEPTION_MESSAGE, hostName);
	}


	/**
	 * @return the httpResponseCode
	 */
	public int getHttpResponseCode() {
		return httpResponseCode;
	}
}
