package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUrlUtils;



public class EasyTravelSimplePage extends CustomerPage{

	private final CustomerSession session;

	public EasyTravelSimplePage(EtPageType pageType, CustomerSession session) {
		super(pageType, session);
		this.session = session;
	}

	@Override
	public void runInBrowser(Browser browser, UEMLoadCallback continuation) throws Exception {
		super.loadPage(browser, UemLoadUrlUtils.getUrl(session.getHost(), getPage()), continuation);
	}
}
