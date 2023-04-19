package com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages;

import java.io.IOException;

import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Action;



public class EasyTravelBlogPage extends CustomerPage {

	@Override
	public void runInBrowser(Browser browser, UEMLoadCallback continuation) throws IOException {
		flightReview(browser, getProcessingTime(), continuation);
	}

	public EasyTravelBlogPage(CustomerSession session) {
		super(EtPageType.BLOGDETAILS, session);
	}

	public void flightReview(Browser browser, int processingTime, final UEMLoadCallback continuation) throws IOException {
		startCustomAction(Action.BLOG, "C", "icefaces.ajax", browser);
		finishCustomAction(browser, processingTime, true);
		loadPage(browser, continuation);
	}
}
