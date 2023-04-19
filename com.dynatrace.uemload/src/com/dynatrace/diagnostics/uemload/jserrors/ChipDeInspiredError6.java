package com.dynatrace.diagnostics.uemload.jserrors;

import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.diagnostics.uemload.BrowserType.BrowserFamily;

/**
 * Demo implementation of a js error seen at chip.de
 *
 * @author cwat-moehler
 */
public class ChipDeInspiredError6 extends JavaScriptError {


	private String[] chromeMessages = {
		"Uncaught TypeError: Cannot read property 'style' of undefined",
		"Uncaught exception: TypeError: Cannot convert 'document.getElementsByClassName(\"dl-btn-countdown\")[0]' to object",
	};

	private String[] ieMessages = {
		"F\u00fcr die Eigenschaft \"style\" kann kein Wert abgerufen werden: Das Objekt ist Null oder undefiniert",
		"Das Objekt unterst\u00fctzt diese Eigenschaft oder Methode nicht.",
	};

	private String[] messages = {
		"document.getElementsByClassName(\"dl-btn-countdown\")[0] is undefined",
		"TypeError: document.getElementsByClassName(...)[0] is undefined"
	};

	public ChipDeInspiredError6(String currentPageUrl) {

		super(currentPageUrl);

		setFile("http://www.chip.de/fec/www.chip.de/2.33.9/js/dl_countdown.js");
		setLine(1);
		setCode("0");
	}

	@Override
	public String getMessage(BrowserType browserType) {

		if (browserType.getBrowserFamily() == BrowserFamily.Chrome || browserType.getBrowserFamily() == BrowserFamily.Opera) {
			return chromeMessages[getNextRandom(0, chromeMessages.length - 1)];
		} else if (browserType.getBrowserFamily() == BrowserFamily.IE) {

			return ieMessages[getNextRandom(0, ieMessages.length - 1)];
		}

		return messages[getNextRandom(0, messages.length - 1)];
	}

}
