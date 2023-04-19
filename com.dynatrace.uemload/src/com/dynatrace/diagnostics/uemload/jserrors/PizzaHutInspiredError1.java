package com.dynatrace.diagnostics.uemload.jserrors;

import com.dynatrace.diagnostics.uemload.BrowserType.BrowserFamily;

/**
 * Demo implementation of a js error seen at pizzahut.com.au
 *
 * @author cwat-moehler
 */
public class PizzaHutInspiredError1 extends JavaScriptError {

	public PizzaHutInspiredError1(String currentPageUrl) {

		super(currentPageUrl);

		setDefaultMessage("Unable to get property 'find' of undefined or null reference");
		setLine(677);
		setColumn(13);
		setCode("0");
		setFile("<domain>/myres/order/byo.jsp");

		String stacktrace = "TypeError: Unable to get property 'find' of undefined or null reference\r\n"
			+ "   at MyResConfiguratorUtils.verifyDisabledToppingsDropdown (<domain>/myres/order/byo.jsp:6744:13)\n"
			+ "   at <anonymous> (<domain>/myres/order/byo.jsp:6783:9)\n"
			+ "   at bZ (<domain>/js/init-scripts.min.cacheversion.0000006161.js:17:8895)\n"
			+ "   at b7.fireWith (<domain>/js/init-scripts.min.cacheversion.0000006161.js:17:9873)\n"
			+ "   at ready (<domain>/js/init-scripts.min.cacheversion.0000006161.js:17_3710)\n"
			+ "   at aF (<domain>/js/init-scripts.min.cacheversion.0000006161.js:17:806)\n";

		setStackTrace(BrowserFamily.Chrome, stacktrace);
		setStackTrace(BrowserFamily.Firefox, stacktrace);
		setStackTrace(BrowserFamily.IE, stacktrace);
		setStackTrace(BrowserFamily.Safari, stacktrace);
	}

}
