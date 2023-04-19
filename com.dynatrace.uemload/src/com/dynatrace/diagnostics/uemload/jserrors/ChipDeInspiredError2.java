package com.dynatrace.diagnostics.uemload.jserrors;

import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.diagnostics.uemload.BrowserType.BrowserFamily;

/**
 * Demo implementation of a js error seen at chip.de
 *
 * @author cwat-moehler
 */
public class ChipDeInspiredError2 extends JavaScriptError {

	private String[] files =  {
		"http://widgets.outbrain.com/outbrain.js",
		"http://connect.facebook.net/de_DE/all.js",
		"http://connect.facebook.net/de_DE/all.js",
		"https://script.ioam.de/iam.js"
	};

	public ChipDeInspiredError2(String currentPageUrl) {

		super(currentPageUrl);

		setFile("http://www.chip.de/fec/www.chip.de/2.33.9/component/requirejs/require.js");
		setLine(1);
	}

	@Override
	public String getMessage(BrowserType browserType) {
		StringBuilder message = new StringBuilder();
		if (browserType.getBrowserFamily() == BrowserFamily.Chrome || browserType.getBrowserFamily() == BrowserFamily.Opera) {
			message.append("Uncaught Error: Script error for: ");
		} else {
			message.append("Error: Script error for: ");
		}
		message.append(files[getNextRandom(0, files.length - 1)]);
		return message.toString();
	}

}
