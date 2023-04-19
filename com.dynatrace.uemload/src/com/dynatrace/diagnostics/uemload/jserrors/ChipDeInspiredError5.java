package com.dynatrace.diagnostics.uemload.jserrors;

import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.diagnostics.uemload.BrowserType.BrowserFamily;

/**
 * Demo implementation of a js error seen at chip.de
 *
 * @author cwat-moehler
 */
public class ChipDeInspiredError5 extends JavaScriptError {


	private String[] chromeMessages = {
		"Uncaught TypeError: Cannot read property 'footerad' of undefined",
		"Uncaught TypeError: 'undefined' is not an object (evaluating 'CXOA_output[f.zoneid]')",
		"Uncaught TypeError: Cannot read property 'zoneid' of undefined"
	};

	private String[] ieMessages = {
		"Die Eigenschaft \"location\" eines undefinierten oder Nullverweises kann nicht abgerufen werden.",
		"Die Eigenschaft \"footerad\" eines undefinierten oder Nullverweises kann nicht abgerufen werden.",
		"Syntaxfehler"
	};

	private String[] messages = {
		"TypeError: 'undefined' is not an object (evaluating 'CXOA_output[f.zoneid]')",
		"TypeError: 'undefined' is not an object (evaluating 'CXOA_output[zoneId]')",
		"Error: Permission denied to access property 'adtech_gSkip'"
	};


	public ChipDeInspiredError5(String currentPageUrl) {

		super(currentPageUrl);

		setFile("<domain>cxo_adtech/js/cxo_adtech-11d910d-min.js");
		setLine(1);
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
