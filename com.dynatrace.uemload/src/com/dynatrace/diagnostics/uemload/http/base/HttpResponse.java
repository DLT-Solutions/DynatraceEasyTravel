package com.dynatrace.diagnostics.uemload.http.base;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.cookie.Cookie;

import com.dynatrace.diagnostics.uemload.Bandwidth;
import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.diagnostics.uemload.NavigationTiming;
import com.dynatrace.diagnostics.uemload.http.callback.HttpReaderCallback;
import com.dynatrace.diagnostics.uemload.http.callback.HttpResponseCallback;
import com.dynatrace.diagnostics.uemload.utils.UemLoadHttpUtils;
import com.dynatrace.easytravel.constants.BaseConstants;



public class HttpResponse {

	private UemLoadConnection connection;
	protected byte[] responseBody;

	protected HttpResponse(UemLoadConnection connection, byte[] responseBody) throws IOException {
		this.connection = connection;
		this.responseBody = ArrayUtils.clone(responseBody);
	}

	public static void getHttpResponse(final UemLoadConnection connection, Bandwidth bandwidth, BrowserType browserType, NavigationTiming nt,
			final HttpResponseCallback callback) throws IOException {

		UemLoadHttpUtils.readResponse(connection, bandwidth, browserType, nt, new HttpReaderCallback() {
			@Override
			public void readDone(byte[] bytes) throws IOException {
				callback.readDone(new HttpResponse(connection, bytes));
			}
		});
	}

	public int getStatusCode() throws IOException {
		return connection.getStatusCode();
	}

	public List<Cookie> getCookies() {
		return connection.getHttpCookies();
	}

	public String getTextResponse() throws IOException {
		return new String(responseBody, BaseConstants.UTF8);
	}

	public ResponseHeaders getResponseHeaders() {
		return new ResponseHeaders(connection.getResponseHeaders());
	}
}