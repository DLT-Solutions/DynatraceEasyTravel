package com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.http.message.BasicNameValuePair;

import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.dtheader.DynaTraceHeader;
import com.dynatrace.diagnostics.uemload.http.base.HttpRequest;
import com.dynatrace.diagnostics.uemload.http.base.HttpResponse;
import com.dynatrace.diagnostics.uemload.http.base.UemLoadFormBuilder;
import com.dynatrace.diagnostics.uemload.http.callback.HttpResponseCallback;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CreditCard;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType;
import com.dynatrace.diagnostics.uemload.thirdpartycontent.ThirdPartyContentUtils;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Action;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Response;


public class EasyTravelFinishPage extends CustomerPage {

	private static final Logger LOGGER = Logger.getLogger(EasyTravelFinishPage.class.getName());

	@Override
	public void runInBrowser(final Browser browser, final UEMLoadCallback continuation) throws IOException {
		UEMLoadCallback paymentRunnable = new UEMLoadCallback() {
		    private volatile int cnt = 0;

		    @Override
		    public void run() throws IOException {
	            if (cnt < 5 && !getSession().isPaymentSuccessful()) {
		            cnt ++;
		            pay(browser, new CreditCard(getSession().getUser().name), getProcessingTime(), this);
		        } else if(getSession().isPaymentSuccessful() && !getSession().isBookingConfirmed()) {
	                endBooking(browser, getProcessingTime(), this);
	            } else if (getSession().isPaymentSuccessful() && getSession().isBookingConfirmed() && !getSession().isConfirmationPageDisplayed()) {
	            	finishedBooking(browser, getProcessingTime(), continuation);
	            }
		    }
		};
	    paymentRunnable.run();
	}

	public EasyTravelFinishPage(CustomerSession session) {
		super(EtPageType.FINISH, session);
	}



	public void pay(final Browser browser, CreditCard creditCard, final int processingTime, final UEMLoadCallback continuation) throws IOException {
		startCustomAction(Action.NEXT, "C", "icefaces.ajax", browser, getXhrUrl(EtPageType.PAYMENT));

		final HttpRequest request = getXhr(browser, EtPageType.PAYMENT);
		UemLoadFormBuilder form = getForm(false);
		form.add("ice.event.captured", "iceform:bookPaymentNext").
				add("ice.event.target", "iceform:bookPaymentNext").
				add("ice.focus", "iceform:bookPaymentNext").
				add("iceform:j_idcl", "").
				add("javax.faces.partial.event", "click").
				add("iceform:bookPaymentNext", "Next").
				add("iceform:creditCardNumber", String.valueOf(creditCard.getNumber())).
				add("iceform:creditCardOwner", creditCard.getOwner()).
				add("iceform:creditCardType", creditCard.getType()).
				add("iceform:expirationMonth", creditCard.getExpirationMonth()).
				add("iceform:expirationYear", String.valueOf(creditCard.getExpirationYear())).
				add("iceform:verificationNumber", String.valueOf(creditCard.getVerificationNumber())).
				add("javax.faces.source", "iceform:bookPaymentNext");

		sendForm(browser, form, EtPageType.PAYMENT, request, new HttpResponseCallback() {
            @Override
            public void readDone(HttpResponse response) throws IOException {
                final int xhrActionId = finishCustomAction(browser, processingTime, true);
                String txtResponse = response.getTextResponse();
				if (txtResponse.contains(Response.PAYMENT_SUCCESS) && response.getStatusCode() == 200) {
                    getSession().setPayementSuccessful(true);
                }
				
				//create XHR third party resources
                ThirdPartyContentUtils.createXhrThirdPartyResources(request.getUrl(), response, browser, xhrActionId, getSession());
				
                cont(continuation);
            }
        });
	}


	protected void endBooking(final Browser browser, final int processingTime, final UEMLoadCallback continuation) throws IOException {
		loadPage(browser, new UEMLoadCallback() {
		    @Override
		    public void run() throws IOException {
		    	startCustomAction(Action.FINISH, "C", "icefaces.ajax", browser, getXhrUrl());
		        DynaTraceHeader header = getSession().getHeader().setMetaData(getPage().getMetaData());

		        HttpRequest request = getXhr(browser, header);
		        UemLoadFormBuilder form = getForm(false);

		        form.add("iceform:destination", getSession().getDestination()).
		                add("ice.event.captured", "iceform:bookFinishFinish").
		                add("ice.event.target", "iceform:bookFinishFinish").
		                add("ice.focus", "iceform:bookFinishFinish").
		                add("iceform:bookFinishFinish", "Finish").
		                add("iceform:j_idcl", "").
		                add("javax.faces.partial.event", "click").
		                add("javax.faces.source", "iceform:bookFinishFinish");

	            sendForm(browser, form, request, new HttpResponseCallback() {
		            @Override
		            public void readDone(HttpResponse response) throws IOException {
						if (response.getTextResponse().contains(Response.BOOKING_ID)) {
		                    LOGGER.info("Booking of the journey to " + getSession().getDestination() + " was successful.");
		                }
						getSession().setBookingConfirmed(true);

		                finishCustomAction(browser, processingTime, true);
		                cont(continuation);
		            }
		        });
		    }
		});


	}

	/**
	 * Loads the confirmation page so that the JavaScript ADK code located on that page is
	 * executed. There is no action performed after the page load but the loading of the
	 * page is essential that the JavaScriptAgent can detect and send the ADK actions.
	 *
	 * @param browser
	 * @param processingTime
	 * @param continuation
	 * @throws IOException
	 */
	protected void finishedBooking(final Browser browser, final int processingTime,	final UEMLoadCallback continuation) throws IOException {

		loadPage(browser, getSession().getJourneyId(), null, new UEMLoadCallback() {

			@Override
			public void run() throws IOException {
				getSession().setConfirmationPageDisplayed(true);
				cont(continuation);
			}
		}, new BasicNameValuePair("success", "1"));
	}


}
