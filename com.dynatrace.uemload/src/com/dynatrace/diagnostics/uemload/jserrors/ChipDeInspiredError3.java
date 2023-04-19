package com.dynatrace.diagnostics.uemload.jserrors;

import com.dynatrace.diagnostics.uemload.BrowserType;

/**
 * Demo implementation of a js error seen at chip.de
 *
 * @author cwat-moehler
 */
public class ChipDeInspiredError3 extends JavaScriptError {

	private String[] objects =  {
			"[object HTMLScriptElement]",
			"[object HTMLObjectElement]",
			"[object HTMLVideoElement]"
	};

	public ChipDeInspiredError3(String currentPageUrl) {

		super(currentPageUrl);
		setFile(null);
	}

	@Override
	public String getMessage(BrowserType browserType) {
		StringBuilder message = new StringBuilder();
		message.append("Uncaught Error: Script error for: ");
		message.append(objects[getNextRandom(0, objects.length - 1)]);
		return message.toString();
	}

}
