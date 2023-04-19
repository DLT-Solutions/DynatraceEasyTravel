package com.dynatrace.webautomation;

import java.util.HashMap;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class DynaTraceWebDriverHelper extends DynaTraceHelper {

	private WebDriver defaultDriver;
	private JavascriptExecutor jsExecutor;

	protected DynaTraceWebDriverHelper(WebDriver defaultDriver) {
		this.defaultDriver = defaultDriver;

		if(defaultDriver instanceof DynaTraceWebDriver)
			this.jsExecutor = (JavascriptExecutor)((DynaTraceWebDriver)defaultDriver).getWebDriver();
		else
			this.jsExecutor = (JavascriptExecutor)defaultDriver;
	}

	public JavascriptExecutor getJsExecutor() {
		return jsExecutor;
	}

	private static HashMap<WebDriver, DynaTraceWebDriverHelper> driverMap = new HashMap<WebDriver, DynaTraceWebDriverHelper>();
	public static DynaTraceWebDriverHelper forDriver(WebDriver defaultDriver) {
		if(defaultDriver instanceof DynaTraceWebDriver)
			defaultDriver = ((DynaTraceWebDriver)defaultDriver).getWebDriver();

		DynaTraceWebDriverHelper helper = driverMap.get(defaultDriver);
		if(helper == null) {
			helper = new DynaTraceWebDriverHelper(defaultDriver);
			driverMap.put(defaultDriver, helper);
		}
		return helper;
	}

	@Override
	protected void openUrl(String url) {
		defaultDriver.navigate().to(url);
	}
}
