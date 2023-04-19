package com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages;

import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Action;

import java.io.IOException;

public class EasyTravelSeoAboutPage extends CustomerPage {

    @Override
    public void runInBrowser(Browser browser, UEMLoadCallback continuation) throws Exception {
        rewriteAbout(browser, getProcessingTime(), continuation);
    }

    public EasyTravelSeoAboutPage(CustomerSession session) {
        super(EtPageType.SEO_ABOUT, session);
    }

    public void rewriteAbout(Browser browser, int processingTime, final UEMLoadCallback continuation) throws IOException {
        startCustomAction(Action.ABOUT, "C", "icefaces.ajax", browser);
        finishCustomAction(browser, processingTime, true);
        loadPage(browser, continuation);
    }
}
