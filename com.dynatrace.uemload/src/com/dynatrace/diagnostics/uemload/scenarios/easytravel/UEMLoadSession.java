package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import com.dynatrace.diagnostics.uemload.dtheader.DynaTraceHeader;
import com.dynatrace.diagnostics.uemload.http.base.ResponseHeaders;
import com.dynatrace.easytravel.misc.CommonUser;



public interface UEMLoadSession {

	boolean isLoadOfResourceNecessary(String url);

	void addResource(String url, long maxAgeInSeconds);

	DynaTraceHeader getHeader();

	String getHost();

	void setResponseHtml(String html);

	void setResponseHeaders(ResponseHeaders responseHeaders);

	void registerPageLoad(String url, String location);

	CommonUser getUser();

}
