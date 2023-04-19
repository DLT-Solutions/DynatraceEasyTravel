package com.dynatrace.diagnostics.uemload.http.exception;

import java.io.IOException;


public class InvalidStatusCodeException extends IOException {

	private static final long serialVersionUID = 7449659369140088563L;
	
	public final static int RANGE_START = 400;
	
	private String resourceUrl;
	private int illegalStatusCode;

	
	public InvalidStatusCodeException(String resourceUrl, int illegalStatusCode) {
		this.resourceUrl = resourceUrl;
		this.illegalStatusCode = illegalStatusCode;
	}


	@Override
	public String getMessage() {
		return "Requesting '" + resourceUrl + "' resulted in status code " + illegalStatusCode;
	}


	
}
