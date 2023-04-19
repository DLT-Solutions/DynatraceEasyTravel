package com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages;

import java.io.IOException;

import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.http.base.HttpRequest;
import com.dynatrace.diagnostics.uemload.http.base.HttpResponse;
import com.dynatrace.diagnostics.uemload.http.base.UemLoadFormBuilder;
import com.dynatrace.diagnostics.uemload.http.callback.HttpResponseCallback;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType;
import com.dynatrace.diagnostics.uemload.thirdpartycontent.ThirdPartyContentUtils;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Action;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class EasyTravelReviewPage extends CustomerPage {
	
	private final State state;
	
	public enum State {
		BOOK,
		BACK;
	}

	@Override
	public void runInBrowser(Browser browser, UEMLoadCallback continuation) throws IOException {
		if (State.BOOK.equals(state)){
			flightReview(browser, getProcessingTime(), continuation);
		} else if(State.BACK.equals(state)){
			backFromPaymentPage(browser, getProcessingTime(), continuation);
		} else {
			cont(continuation);
		}
	}

	public EasyTravelReviewPage(CustomerSession session) {
		super(EtPageType.REVIEW, session);
		this.state = State.BOOK;
	}
	
	public EasyTravelReviewPage(CustomerSession session, State state) {
		super(EtPageType.REVIEW, session);
		this.state = state;
	}

	public void flightReview(Browser browser, int processingTime, final UEMLoadCallback continuation) throws IOException {
		// Action info is left empty in order to mimic the original action
		startCustomAction(Action.BOOK, "C", "", browser);
		finishCustomAction(browser, processingTime, true);
		loadPage(browser, continuation);
	}
	
	public void backFromPaymentPage(final Browser browser, final int processingTime, final UEMLoadCallback continuation) throws IOException {
		startCustomAction(Action.BACK, "C", "", browser);

		final HttpRequest request = getXhr(browser, EtPageType.PAYMENT);
		UemLoadFormBuilder form = getForm(false);
		form.add("ice.event.captured", "iceform:bookPaymentBack").
				add("ice.event.target", "iceform:bookPaymentBack").
				add("ice.focus", "iceform:bookPaymentBack").
				add("iceform:j_idcl", "").
				add("javax.faces.partial.event", "click").
				add("iceform:bookPaymentBack", "Back").
				add("javax.faces.source", "iceform:bookPaymentBack");

		sendForm(browser, form, EtPageType.PAYMENT, request, new HttpResponseCallback() {
            @Override
            public void readDone(HttpResponse response) throws IOException {
                final int xhrActionId = finishCustomAction(browser, processingTime, true);
                ThirdPartyContentUtils.createXhrThirdPartyResources(request.getUrl(), response, browser, xhrActionId, getSession());
                loadPage(browser, continuation);
            }
        });
	}
}
