package com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages;

import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Action;

import java.io.IOException;

public class EasyTravelSeoPage extends CustomerPage {


    @Override
    public void runInBrowser(Browser browser, UEMLoadCallback continuation) throws Exception {
        rewriteSeo(browser, getProcessingTime(), continuation);
    }

    public EasyTravelSeoPage(CustomerSession session) {
        super(EtPageType.SEO, session);
    }

    public void rewriteSeo(Browser browser, int processingTime, final UEMLoadCallback continuation) throws IOException {
        startCustomAction(Action.SEO, "C", "icefaces.ajax", browser);
        finishCustomAction(browser, processingTime, true);
        loadPage(browser, continuation);
    }

}
