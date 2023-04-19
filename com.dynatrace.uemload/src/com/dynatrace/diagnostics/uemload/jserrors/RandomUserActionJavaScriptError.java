package com.dynatrace.diagnostics.uemload.jserrors;

import com.dynatrace.diagnostics.uemload.BrowserType;

public class RandomUserActionJavaScriptError extends JavaScriptError {

	private String[] messages = {
		"TypeError: f.curCSS is not a function",
		"SyntaxError: Unexpected token ;",
		"L'oggetto non supporta questa propriet\u00e0 o metodo",
		"Error: Objeto no admite esta propiedad o m\u00e9todo",
		"Error: Object doesn't support this property or method",
		"Unbehandelter Typfehler: Objekt function ( selector^c context ) {// The jQuery object is actually just the init constructor 'enhanced'return new jQuery.fn.init( selector^c context^c rootjQuery )^s} hat keine Methode 'curCSS'"
	};

	private String[] fileNames = {
		"/js/mootools-1.0.5.js",
		"/js/mootools-2.0.js:",
		"/js/jquery-ui-1.10.2.js",
		"/js/jquery-ui-1.8.0.js",
		"/js/d3-3.0.js",
		"/js/d3-2.5.js",
		"https://cdnjs.cloudflare.com/ajax/libs/backbone.js/1.1.2/backbone-min.js",
		"http://cdnjs.cloudflare.com/ajax/libs/codemirror/4.4.0/addon/comment/continuecomment.js",
		"/js/jquery-1.7.3.js",
		"/js/jquery-1.10.1.js",
		"/js/jquery-2.0.js",
		"/js/easytravel/easytravel.js"
	};

	public RandomUserActionJavaScriptError(String currentPageUrl) {
		super(null);
	}

	@Override
	public String getMessage(BrowserType browserType) {
		return messages[getNextRandom(0, messages.length - 1)];
	}

	@Override
	public String getFile() {
		return fileNames[getNextRandom(0, fileNames.length - 1)];
	}

	@Override
	public int getLine(BrowserType browserType) {
		return getNextRandom(1, 150);
	}

	@Override
	public int getColumn(BrowserType browserType) {
		if (browserType.isColumnNumberInOnErrorAvailable()) {
			return getNextRandom(1, 300);
		}
		return -1;
	}


}
