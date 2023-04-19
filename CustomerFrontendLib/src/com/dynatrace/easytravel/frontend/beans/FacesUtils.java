package com.dynatrace.easytravel.frontend.beans;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Collection of utility methods so that beans don't have to use servlet API.
 *
 * @author philipp.grasboeck
 */
class FacesUtils {

	static int ERROR_404 = HttpServletResponse.SC_NOT_FOUND;
	static int ERROR_500 = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

	static HttpServletRequest getRequest() {
		return (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
	}

	static HttpServletResponse getResponse() {
        return (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
	}

	static void setHeader(String name, String value) {
		getResponse().setHeader(name, value);
	}

	static void sendError(int errorCode, String message) {
		try {
			HttpServletResponse response = getResponse();
			if (!response.isCommitted()) {
				response.sendError(errorCode, message);
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	static void setStatusCode(int errorCode) {
		getResponse().setStatus(errorCode);
	}

	static String getCurrentViewId() {
		String uri = getRequest().getRequestURI();
		int begin = uri.lastIndexOf('/') + 1;
		int end = uri.lastIndexOf('.');
		if (end == -1) {
			end = uri.length();
		}
		return uri.substring(begin, end);
	}
}
