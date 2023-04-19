package com.dynatrace.diagnostics.uemload.http.exception;

import java.io.IOException;


/**
 * Exception that is thrown if UEM load access a page and the session is 
 * already expired.
 * 
 * @author stefan.moschinski
 */
public class SessionExpiredException extends IOException {

	private static final long serialVersionUID = 1L;

	private static String SESSION_EXPIRED_EXCEPTION = "Session on host %s has expired.";

	public SessionExpiredException(String host) {
		super(String.format(SESSION_EXPIRED_EXCEPTION, host));
	}
}
