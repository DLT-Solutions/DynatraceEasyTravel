package com.dynatrace.diagnostics.uemload.jserrors;

import org.junit.Assert;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.util.DtVersionDetector;

public class JavaScriptErrorTest {

	@Test
	public void testJsErrorActionGeneration() {
		JavaScriptError error = new ChangeDetectionJavaScriptError(null);

		Assert.assertEquals("http://localhost:8080/problempatterns/changedetectionlib.js", error.getFile());
		Assert.assertEquals(1, error.getLine(BrowserType.CHROME_57), 1);
		Assert.assertEquals(1, error.getLine(BrowserType.FF_530), 1);

		Assert.assertEquals("Uncaught SyntaxError: Unexpected token ;", error.getMessage(BrowserType.ANDROID_22));
		Assert.assertEquals("Uncaught SyntaxError: Unexpected token ;", error.getMessage(BrowserType.CHROME_56));
		Assert.assertEquals("syntax error", error.getMessage(BrowserType.FF_520));
		Assert.assertEquals("SyntaxError: Unexpected token ';'", error.getMessage(BrowserType.SAFARI_5));

		Assert.assertEquals(-1, error.getColumn(BrowserType.IE_6));
		Assert.assertEquals(9, error.getColumn(BrowserType.IE_10));
	}

	@Test
	public void testGetBrowserSpecificError() {
		JavaScriptError error = new ChangeDetectionJavaScriptError(null);

		DtVersionDetector.enforceInstallationType(InstallationType.APM);

		BrowserSpecificJavaScriptError errorChrome = error.getBrowserSpecificJavaScriptError(BrowserType.CHROME_55);
		BrowserSpecificJavaScriptError errorFF = error.getBrowserSpecificJavaScriptError(BrowserType.FF_510);
		BrowserSpecificJavaScriptError errorIE = error.getBrowserSpecificJavaScriptError(BrowserType.IE_6);

		Assert.assertEquals("Uncaught SyntaxError: Unexpected token ;", errorChrome.getMessage());
		Assert.assertEquals("syntax error", errorFF.getMessage());
		// Assert.assertEquals("Syntax Error", errorIE.get());

		Assert.assertEquals(9, errorChrome.getColumn());
		Assert.assertEquals(8, errorFF.getColumn());
		Assert.assertEquals(-1, errorIE.getColumn());

	}

}
