package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.http.base.ResponseHeaders;
import com.dynatrace.easytravel.misc.CommonUser;



public class B2BSession extends EasyTravelSession {

	private String html;

	public B2BSession(String host, CommonUser user, Location location, boolean taggedWebRequest) {
		super(host, location, user, taggedWebRequest);
	}


	//Location	/Account/LogOn?ReturnUrl=%2fJourney
	@Override
	public void setResponseHtml(String html) {
		this.html = html;
	}

	@Override
	public void setResponseHeaders(ResponseHeaders responseHeaders) {
		super.setResponseHeaders(responseHeaders);
	}


	public String getHtml() {
		return html;
	}

}
