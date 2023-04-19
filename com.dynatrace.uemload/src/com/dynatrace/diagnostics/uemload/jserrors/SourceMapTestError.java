package com.dynatrace.diagnostics.uemload.jserrors;

import com.dynatrace.diagnostics.uemload.BrowserType.BrowserFamily;

public class SourceMapTestError extends JavaScriptError {
	
	public SourceMapTestError(String currentPageUrl) {

		super(currentPageUrl);

		setDefaultMessage("Uncaught Error: Syntax error, unrecognized expression: /test");
		setLine(2);
		setColumn(13470);
		setCode("0");
		setFile("<domain>/jquery-3.1.1.min.js");

		String stacktrace = "Error: Syntax error, unrecognized expression: /test\r\n"
	    	+ "   at Function.ga.error (<domain>/jquery-3.1.1.min.js:2:13470)\n"
	        + "   at ga.tokenize (<domain>/jquery-3.1.1.min.js:2:19459)\n"
	        + "   at ga.select (<domain>/jquery-3.1.1.min.js:2:22349)\n"
	        + "   at Function.ga [as find] (<domain>/jquery-3.1.1.min.js:2:7308)\n"
	        + "   at r.find (<domain>/jquery-3.1.1.min.js:2:24958)\n"
	        + "   at r.fn.init (<domain>/jquery-3.1.1.min.js:2:25448)\n"
	        + "   at r (<domain>/jquery-3.1.1.min.js:2:601)\n"
	        + "   at window.test (<domain>/test.min.js:1:42)\n"
	        + "   at HTMLAnchorElement.onclick (<domain>/index.html:22:63)\n";

		setStackTrace(BrowserFamily.Chrome, stacktrace);
		setStackTrace(BrowserFamily.Firefox, stacktrace);
		setStackTrace(BrowserFamily.IE, stacktrace);
		setStackTrace(BrowserFamily.Safari, stacktrace);
	}

}
