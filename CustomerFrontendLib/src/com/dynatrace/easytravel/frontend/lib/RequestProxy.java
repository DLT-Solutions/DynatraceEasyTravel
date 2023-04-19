package com.dynatrace.easytravel.frontend.lib;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;

public final class RequestProxy {
	
	private static final String HEADER_TRANSFER_ENCODING = "transfer-encoding";
	private static final String HEADER_VALUE_CHUNKED = "chunked";
	
	private static final RequestProxy INSTANCE = new RequestProxy();
	
	private RequestProxy() {
		// singleton
	}
	
	public static RequestProxy instance() {
		return INSTANCE;
	}
	
	public void forward(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
		HttpClient client = new HttpClient();
		
		HttpMethodBase method = createMethod(request, url);
		method.setQueryString(request.getQueryString());
		forwardBody(method, request);
		
		// forward headers
		forwardHeaders(request, method);
		// execute
		int responseCode = client.executeMethod(method);
		// return data
		response.setStatus(responseCode);
		returnHeaders(response, method);
		returnBody(response, method);
	}

	private HttpMethodBase createMethod(HttpServletRequest request, String url) {
		switch (request.getMethod()) {
			case "GET":
				return new GetMethod(url);
			case "POST":
				return new PostMethod(url);
			case "PUT":
				return new PutMethod(url);
			case "DELETE":
				return new DeleteMethod(url);
			default:
				throw new UnsupportedOperationException("Forwarding for method " + request.getMethod() + " not supported.");
		}
	}

	private void forwardHeaders(HttpServletRequest request, HttpMethod method) {
		Enumeration<String> headers = request.getHeaderNames();
		while(headers.hasMoreElements()) {
			String key = headers.nextElement();
			String value = request.getHeader(key);
			method.addRequestHeader(key, value);
		}
	}
	
	private void forwardBody(HttpMethod method, HttpServletRequest request) throws IOException {
		if (method instanceof EntityEnclosingMethod) {
			((EntityEnclosingMethod) method).setRequestEntity(new InputStreamRequestEntity(request.getInputStream()));
		}	
	}

	private void returnHeaders(HttpServletResponse response, HttpMethod method) {
		for(Header header : method.getResponseHeaders()) {
			if(HEADER_TRANSFER_ENCODING.equalsIgnoreCase(header.getName()) && HEADER_VALUE_CHUNKED.equalsIgnoreCase(header.getValue())) {
				// we don't forward chunked
				continue;
			}
			response.addHeader(header.getName(), header.getValue());
		}
	}
	
	private void returnBody(HttpServletResponse response, HttpMethodBase method)
			throws IOException {
		// return body
		response.setContentLength((int)method.getResponseContentLength());
		byte[] buffer = new byte[1024];
		int length = 0;
		while((length = method.getResponseBodyAsStream().read(buffer)) != -1) {
			response.getOutputStream().write(buffer, 0, length);
		}
	}
}
