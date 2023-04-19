/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: ErrorHandlingHttpResonseCallbackAdapter.java
 * @date: 11.06.2012
 * @author: cwat-plang
 */
package com.dynatrace.diagnostics.uemload.http.callback;

import java.io.IOException;

import com.dynatrace.diagnostics.uemload.http.base.HttpResponse;
import com.dynatrace.diagnostics.uemload.http.base.UemLoadHttpClient;
import com.dynatrace.diagnostics.uemload.http.exception.PageNotAvailableException;


/**
 * default implemenentation of interface {@link #ErrorHandlingHttpResonseCallbackAdapter()}
 * @author cwat-plang
 */
public class ErrorHandlingHttpResonseCallbackAdapter implements ErrorHandlingHttpResponseCallback {

	/**
	 * do nothing
	 */
	@Override
	public void readDone(HttpResponse response) throws IOException {
		System.out.println("ErrorHandlingHttpResonseCallbackAdapter.readDone()");
	}

	/**
	 * Does not handle exception but, throws a new {@link IOException} with exception as root cause.
	 * @param exception exception thrown in {@link UemLoadHttpClient}
	 * @throws IOException containing exception as root cause
	 */
	@Override
	public void handleRequestError(PageNotAvailableException exception) throws IOException {
		System.out.println("ErrorHandlingHttpResonseCallbackAdapter.handleRequestError()");
		throw new IOException("Passing on unhandled exception", exception);
	}

}
