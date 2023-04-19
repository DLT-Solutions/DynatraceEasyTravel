package com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages;

import java.util.logging.Logger;

import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType;

public class EasyTravelWeatherForecastPopup extends CustomerPage {

    private static final Logger LOGGER = Logger.getLogger(EasyTravelWeatherForecastPopup.class.getName());

	public EasyTravelWeatherForecastPopup(CustomerSession session) {
		super(EtPageType.WEATHERFORECAST, session);
	}


	@Override
	public void runInBrowser(Browser browser, UEMLoadCallback continuation) throws Exception {
		String url = getSession().getAttribute("forecast-link");
		if (url == null) {
			LOGGER.fine("Weather forecase page was supposed to be loaded, however no URL for the weather forecast page could be found!");
			continuation.run();
		} else {
			loadPage(browser, url, continuation);
		}
	}

}
