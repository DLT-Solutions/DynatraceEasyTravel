/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: ErrorHandlingHttpResponseCallback.java
 * @date: 11.06.2012
 * @author: cwat-plang
 */
package com.dynatrace.diagnostics.uemload.http.callback;

import java.io.IOException;

import com.dynatrace.diagnostics.uemload.http.exception.PageNotAvailableException;


/**
 * Extended interface for handling http-response code errors in
 *
 * @author cwat-plang
 */
public interface ErrorHandlingHttpResponseCallback extends HttpResponseCallback {

	 /**
	  * called if an {@link PageNotAvailableException} is caught during web request
	  *
	  * @param exception {@link PageNotAvailableException} caught
	  * @throws IOException
	  * @author cwat-plang
	  */
	 public void handleRequestError(PageNotAvailableException exception) throws IOException;

}
