package com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages;

import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.B2BPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.B2BSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType;


public class B2BSimplePage extends B2BPage {

	public B2BSimplePage(EtPageType page, B2BSession session) {
		super(page, session);
	}

	@Override
	public void runInBrowser(Browser browser, UEMLoadCallback continuation) throws Exception {
		loadPage(browser, continuation);
	}

}
