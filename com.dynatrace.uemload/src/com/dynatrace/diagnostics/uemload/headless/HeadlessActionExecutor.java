package com.dynatrace.diagnostics.uemload.headless;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.dynatrace.diagnostics.uemload.ActionExecutor;
import com.dynatrace.diagnostics.uemload.Bandwidth;
import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.VisitorId;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class HeadlessActionExecutor extends ActionExecutor {
	
	private static final Logger LOGGER = LoggerFactory.make();

	private ChromeDriver driver;
	private WebDriverWait wait;

	public HeadlessActionExecutor(Location location, int latency, Bandwidth bandwidth, BrowserType browserType,
			String userAgent) {
		super(location, latency, bandwidth, browserType, userAgent);
	}

	public HeadlessActionExecutor(Location location, int latency, Bandwidth bandwidth, BrowserType browserType,
			String userAgent, VisitorId visitorId) {
		super(location, latency, bandwidth, browserType, userAgent, visitorId);
	}
	
	public ChromeDriver getDriver() {
		return driver;
	}
	
	public void setDriver(ChromeDriver driver) {
		this.driver = driver;
		wait = new WebDriverWait(driver, 20);
	}
	
	public WebDriverWait getWait() {
		return wait;
	}
	
	@Override
	public void close() {
		if(driver != null) {
			try {
				driver.quit();
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}
}
