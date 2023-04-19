package com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages;

import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.B2BPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.B2BSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType;


public class EasyTravelB2bStartPage extends B2BPage {

	public EasyTravelB2bStartPage(B2BSession session) {
		super(EtPageType.B2B_HOME, session);
	}

	@Override
	public void runInBrowser(Browser browser, UEMLoadCallback continuation) throws Exception {
		super.cont(continuation);
	}



}
