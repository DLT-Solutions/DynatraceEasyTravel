package com.dynatrace.diagnostics.uemload;

public class IncompleteAction {

	public static void main(String[] args) throws Exception {
		Browser browser = new Browser(BrowserType.FF_530, null, 0, Bandwidth.DSL_MED, BrowserWindowSize._1280x1024);
		try {
		browser.loadPage("http://localhost:8080/ajax/logging/nocaching/correlation/index.htm", "Tests", null, 1000, null, null);

		// action does not continue on next page
		browser.startCustomAction("Click on pageWithoutResources", "ice", "-");
		browser.stopCustomAction(false);
		browser.loadPage("http://localhost:8080/ajax/logging/nocaching/correlation/fast/01_page.htm", "Tests", null, 1000, null, null);

		// action continues on next page
		browser.startCustomAction("Click on pageWithResource", "ice", "-");
		browser.stopCustomAction(true);
		browser.loadPage("http://localhost:8080/ajax/logging/nocaching/correlation/fast/02_pageWithResource.htm", "Tests", null, 1000, null, null);
		} finally {
			browser.close();
		}
	}

}
