package com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages;

import java.io.IOException;

import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.http.base.HttpRequest;
import com.dynatrace.diagnostics.uemload.http.base.HttpRequest.Type;
import com.dynatrace.diagnostics.uemload.http.base.HttpResponse;
import com.dynatrace.diagnostics.uemload.http.base.UemLoadFormBuilder;
import com.dynatrace.diagnostics.uemload.http.callback.HttpResponseCallback;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.B2BSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType.PageAction;
import com.dynatrace.easytravel.constants.BaseConstants.Http;

public class B2BAuthenticatedPage extends B2BSimplePage {
	public static final String B2BLoginFormTrigger = "Please enter your username and password.";

	public B2BAuthenticatedPage(EtPageType page, B2BSession session) {
		super(page, session);
	}

	@Override
	protected void loadPage(final Browser browser, final String url, PageAction pageAction, final String pageLoadReferer, final UEMLoadCallback pageLoadCallback) throws IOException {

		super.loadPage(browser, url, pageAction, null, new UEMLoadCallback() {
			@Override
			public void run() throws IOException {
				UemLoadFormBuilder formbuilder = new UemLoadFormBuilder();

				formbuilder.add("UserName", getSession().getUser().name);
				formbuilder.add("Password", getSession().getUser().password);
				formbuilder.add("submit", "Log On");

				HttpRequest newRequest = browser.createRequest(url).setMethod(Type.POST).setHeader(Http.Headers.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8");

				newRequest.setFormParams(formbuilder.getFormParms());
				browser.send(newRequest, new HttpResponseCallback() {
					@Override
					public void readDone(HttpResponse response) throws IOException {
						if (pageLoadCallback != null)
							pageLoadCallback.run();
					}
				});
			}
		});
	}
}
