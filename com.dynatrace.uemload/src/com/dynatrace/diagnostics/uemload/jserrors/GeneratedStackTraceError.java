package com.dynatrace.diagnostics.uemload.jserrors;

import com.dynatrace.diagnostics.uemload.BrowserType.BrowserFamily;


/**
 * Some Error to add to a user action in easyTravel
 *
 * @author cwat-moehler
 */
public class GeneratedStackTraceError extends JavaScriptError {

	public GeneratedStackTraceError(String currentPageUrl) {

		super(currentPageUrl);

		setFile("");
		setLine(79);
		setColumn(80);

		setDefaultMessage("Demo Error for generated stacktrace");
		setCode("0");

		String stackTraceGeneratedDefault = "<generated>\r\n"
			 + "Error\r\n"
			 + "    at generateStacktrace (<domain>/ruxitagentjs_.min.js:6081:15)\r\n"
			 + "    at Object.reportError [as re] (<domain>/ruxitagentjs_.min.js:6007:54)\r\n"
			 + "    at onErrorCallback (<domain>/ruxitagentjs_.min.js:10389:13)\r\n"
             + "    at doSomething (<domain>/myscript.js:100:99)\r\n"
             + "    at doSomething (<domain>/myscript2.js:100:99)\r\n"
		     + "    at doSomething (<domain>/myscript3.js:100:99)";

		String stackTraceGeneratedFF = "<generated>\r\n"
			+ "re@<domain>/ajax/libs/jquery/1.11.2/ruxitagentjs_.min.js:41:355\r\n"
			+ "xc@<domain>/ajax/libs/jquery/1.11.2/ruxitagentjs_.min.js:40:375\r\n"
			+ "g@<domain>/ajax/libs/jquery/1.11.2/ruxitagentjs_.min.js:84:461\r\n"
			+ "rxehandler@<domain>/ruxitagentjs_.js:100:99\r\n"
			+ "doSomething@<domain>/myscript.js:100:99\r\n"
			+ "doSomething@<domain>/myscript2.js:100:99\r\n"
			+ "doSomething@<domain>/myscript3.js:100:99";

		String stackTraceGeneratedIE = "<generated-ie>\r\n"
			 + "generateStacktrace\r\n"
			 + "reportError\r\n"
			 + "onErrorCallback\r\n"
			 + "rxehandler\r\n"
			 + "doSomething\r\n"
			 + "doSomething\r\n"
			 + "doSomething";

		setStackTrace(BrowserFamily.Chrome, stackTraceGeneratedDefault);
		setStackTrace(BrowserFamily.Opera, stackTraceGeneratedDefault);
		setStackTrace(BrowserFamily.Firefox, stackTraceGeneratedFF);
		setStackTrace(BrowserFamily.Mobile, stackTraceGeneratedDefault);
		setStackTrace(BrowserFamily.Safari, stackTraceGeneratedFF);
		setStackTrace(BrowserFamily.IE, stackTraceGeneratedIE);
	}
}