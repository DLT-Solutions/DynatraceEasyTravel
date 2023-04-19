package com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages;

import java.io.IOException;

import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Action;


public class EasyTravelTripDetailsPage extends CustomerPage {

	@Override
	public void runInBrowser(Browser browser, UEMLoadCallback continuation) throws IOException {
		tripReview(browser, getProcessingTime(), continuation);
	}

	public EasyTravelTripDetailsPage(CustomerSession session) {
		super(EtPageType.TRIPDETAILS, session);
	}

	public void tripReview(Browser browser, int processingTime, final UEMLoadCallback continuation) throws IOException {
		startCustomAction(Action.DETAIL, "C", "icefaces.ajax", browser);
		finishCustomAction(browser, processingTime, true);
		loadPage(browser, continuation);
	}
}



