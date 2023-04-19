package com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages;

import java.io.IOException;
import java.util.logging.Logger;

import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.http.base.HttpRequest;
import com.dynatrace.diagnostics.uemload.http.base.HttpResponse;
import com.dynatrace.diagnostics.uemload.http.base.UemLoadFormBuilder;
import com.dynatrace.diagnostics.uemload.http.callback.HttpResponseCallback;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType.PageAction;
import com.dynatrace.diagnostics.uemload.thirdpartycontent.ThirdPartyContentUtils;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Action;

public class EasyTravelPaymentPage extends CustomerPage {

	private static final Logger LOGGER = Logger.getLogger(EasyTravelPaymentPage.class.getName());

	public EasyTravelPaymentPage(CustomerSession session) {
		super(EtPageType.PAYMENT, session);
	}


	@Override
	public void runInBrowser(Browser browser, UEMLoadCallback continuation) throws IOException {
		execute(browser, getProcessingTime(PageAction.NEXT), continuation);
	}

	public void execute(final Browser browser, final int processingTime, final UEMLoadCallback continuation) throws IOException {

		startCustomAction(Action.NEXT, "C", "", browser);
		final HttpRequest request = getXhr(browser, EtPageType.REVIEW);
		UemLoadFormBuilder form = getForm(false);

		form.add("ice.event.captured", "iceform:travellers").
				add("ice.event.target", "iceform:travellers").
				add("ice.focus", "").
				add("iceform:destination_idx", "").
				add("javax.faces.partial.event", "click").
				add("iceform:travellers", Integer.toString(UemLoadUtils.randomInt(6)+1)).
				add("javax.faces.source", "iceform:travellers");

		sendForm(browser, form, EtPageType.REVIEW, request, new HttpResponseCallback() {

            @Override
            public void readDone(HttpResponse response) throws IOException {
                final int xhrActionId = finishCustomAction(browser, processingTime, true);
                
                //create XHR third party resources
                ThirdPartyContentUtils.createXhrThirdPartyResources(request.getUrl(), response, browser, xhrActionId, getSession());

                loadPage(browser, continuation);

                if (partialResponseLogging) {
                    LOGGER.info(response.getTextResponse());
                }   
                
            }
        });
	}

}
