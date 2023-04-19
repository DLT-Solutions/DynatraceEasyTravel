package com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.http.base.HttpRequest;
import com.dynatrace.diagnostics.uemload.http.base.HttpRequest.Type;
import com.dynatrace.diagnostics.uemload.http.base.HttpResponse;
import com.dynatrace.diagnostics.uemload.http.callback.HttpResponseCallback;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.google.common.base.Joiner;

public class EasyTravelFailingXHRPage extends CustomerPage {

    @Override
    public void runInBrowser(Browser browser, UEMLoadCallback continuation) throws Exception {
        sendFailingXHR(browser, getProcessingTime(), continuation);
    }

    public EasyTravelFailingXHRPage(CustomerSession session) {
        super(EtPageType.START, session);
    }

    public void sendFailingXHR(final Browser browser, final int processingTime, final UEMLoadCallback continuation) throws IOException {
        startCustomAction("Like", "C", "icefaces.ajax", browser);
        
        final List<String> additionalActions = new ArrayList<>();
		additionalActions.add(Joiner.on(BaseConstants.PIPE).join(2, "<actionId>", "500", "_rC_", BaseConstants.MINUS, "<endtime>", "<endtime>", -1));
		additionalActions.add(Joiner.on(BaseConstants.PIPE).join(2, "<actionId>", "server error", "_rM_", BaseConstants.MINUS, "<endtime>", "<endtime>", -1));

		final HttpRequest request = new HttpRequest(super.getHost() + "error500").setMethod(Type.GET)
				.setReferer(browser.getCurrentPage())
				.setHeader("Accept-Encoding", "gzip, deflate")
				.setHeader("Connection", "keep-alive")
				.setHeader("Accept", "application/json, text/plain, */*")
				.setHeader("X-Requested-With", "XMLHttpRequest");
		browser.send(request, new HttpResponseCallback() {
			@Override
			public void readDone(HttpResponse response) throws IOException {
		        finishCustomAction(browser, processingTime, false, additionalActions);
			}
		}, new HttpResponseCallback() {
			@Override
			public void readDone(HttpResponse response) throws IOException {
		        finishCustomAction(browser, processingTime, false, additionalActions);
			}
		});
    }
}
