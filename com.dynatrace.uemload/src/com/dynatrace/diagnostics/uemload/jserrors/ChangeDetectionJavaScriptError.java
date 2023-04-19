package com.dynatrace.diagnostics.uemload.jserrors;

import com.dynatrace.diagnostics.uemload.BrowserType.BrowserFamily;

/**
 * A simple syntax error that mimics the behaviour of a real browser that visits the
 * easyTravel page with the ChangeDetection problem pattern active.
 *
 * @author cwat-moehler
 *
 */
public class ChangeDetectionJavaScriptError extends JavaScriptError {

	public ChangeDetectionJavaScriptError(String currentPageUrl) {
		super(currentPageUrl);

		setDefaultMessage("Syntax error");
		setCode("1002");

		setLanguageSpecificMessage(GERMAN, "Syntaxfehler");
		setLanguageSpecificMessage(ITALIAN, "Errore di sintassi");
		setLanguageSpecificMessage(FRENCH, "Erreur de syntaxe");
		setLanguageSpecificMessage(SPANISH, "Error de sintaxis");

		setMessage(BrowserFamily.Firefox, "syntax error");
		setMessage(BrowserFamily.Chrome, "Uncaught SyntaxError: Unexpected token ;");
		setMessage(BrowserFamily.Opera, "Uncaught SyntaxError: Unexpected token ;");
		setMessage(BrowserFamily.IE, "Syntax error");
		setMessage(BrowserFamily.Safari, "SyntaxError: Unexpected token ';'");

		setMessageForMobileIOSDevices("SyntaxError: Unexpected token ';'");
		setMessageForMobileWindowsDevices("Syntax error");
		setMessageForMobileAndroidDevices("Uncaught SyntaxError: Unexpected token ;");

		setFile("<domain>/problempatterns/changedetectionlib.js");
		setLine(1);

		setColumn(9);
		setColumn(BrowserFamily.Firefox, 8);

		setStackTrace(BrowserFamily.Chrome, "SyntaxError: Unexpected token ;");
		setStackTrace(BrowserFamily.Opera, "SyntaxError: Unexpected token ;");
	}

}
