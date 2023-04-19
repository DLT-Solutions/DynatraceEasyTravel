package com.dynatrace.diagnostics.uemload.jserrors;

import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.diagnostics.uemload.BrowserType.BrowserFamily;

/**
 * Demo implementation of a js error seen at chip.de
 *
 * @author cwat-moehler
 */
public class ChipDeInspiredError4 extends JavaScriptError {

	private String[] messages =  {
		"Cxo_Adtech is not defined",
		"Cxo_Adtech_localStorage is not defined",
		"Cxo_Adtech_AdServer_OpenXXhr is not defined",
		"Cxo_Adtech_Decorator_Anzeige is not defined",
	};

	public ChipDeInspiredError4(String currentPageUrl) {

		super(currentPageUrl);

		setLine(1);
	}

	@Override
	public String getMessage(BrowserType browserType) {
		StringBuilder message = new StringBuilder();
		if (browserType.getBrowserFamily() == BrowserFamily.Chrome || browserType.getBrowserFamily() == BrowserFamily.Opera) {
			message.append("Uncaught ReferenceError: ");
		} else {
			message.append("ReferenceError: ");
		}
		message.append(messages[getNextRandom(0, messages.length - 1)]);
		return message.toString();
	}

	@Override
	public String getFile() {
		return replaceDomain("<domain>/orange-trip-details.jsf?journeyId=" + getNextRandom(0, 5000));
	}

}
