package com.dynatrace.diagnostics.uemload.jserrors;

import com.dynatrace.diagnostics.uemload.BrowserType.BrowserFamily;

/**
 * Demo implementation of a js error seen at chip.de
 *
 * @author cwat-moehler
 */
public class ChipDeInspiredError7 extends JavaScriptError {


	public ChipDeInspiredError7(String currentPageUrl) {

		super(currentPageUrl);

		setDefaultMessage("Invalid argument.");
		setLine(1);
		setCode("0");
		setFile("<domain>/config/common/js/jquery.js");

		String stacktrace = "Error: Invalid argument.\n\r"
				+ "   at <anonymous> (<domain>/motortrade/index.php?page=1:2070:59)\r"
				+ "   at l (<domain>/config/common/js/jquery.js:1:19245)\r"
				+ "   at c.fireWith (<domain>/config/common/js/jquery.js:1:20141)\r"
				+ "   at N (<domain>/config/common/js/jquery.js:1:83254)\r"
				+ "   at n.onreadystatechange (<domain>/config/common/js/jquery.js:1:86856)";

		setStackTrace(BrowserFamily.Chrome, stacktrace);
		setStackTrace(BrowserFamily.Firefox, stacktrace);
		setStackTrace(BrowserFamily.IE, stacktrace);
		setStackTrace(BrowserFamily.Opera, stacktrace);
	}

}
