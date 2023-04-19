package com.dynatrace.diagnostics.uemload;

import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;


public class Pageview extends BrowserAction {


	private final String url;
	private final String title;
	private final String[] resources;
	private final int pageLoadTime;

	public Pageview(String url, String title, String[] resources, int pageLoadTime) {
		this.url = url;
		this.title = title;
		this.resources = ArrayUtils.clone(resources);
		this.pageLoadTime = pageLoadTime;
	}

	@Override
	public void runInBrowser(Browser browser, UEMLoadCallback continuation) throws IOException {
		browser.loadPage(url, title, resources, pageLoadTime, null, continuation);
	}

	public String getUrl() {
		return url;
	}


	public String getTitle() {
		return title;
	}


	public String[] getResources() {
		return resources;
	}


	public int getPageLoadTime() {
		return pageLoadTime;
	}

}
