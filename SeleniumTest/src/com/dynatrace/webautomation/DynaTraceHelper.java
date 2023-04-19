package com.dynatrace.webautomation;

public abstract class DynaTraceHelper {

	protected abstract void openUrl(String url);

	/**
	 * Opens a new URL and sets a new timer name with the calling method
	 */
	public void open(String url) {
		openUrl(url);
	}

	/**
	 * Opens a new URL<br/>
	 * Either uses the existing timername or creates a new one with the callers method name
	 */
	public void open(String url, boolean useExistingTimerName) {
		openUrl(url);
	}

}
