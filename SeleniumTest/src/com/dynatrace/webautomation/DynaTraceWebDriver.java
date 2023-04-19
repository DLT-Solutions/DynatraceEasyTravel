package com.dynatrace.webautomation;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class DynaTraceWebDriver implements WebDriver {

	private WebDriver driver;
	private DynaTraceWebDriverNavigation dynaTraceNavigation;
	private static HashMap<WebDriver, DynaTraceWebDriver> drivers = new HashMap<WebDriver, DynaTraceWebDriver>();

	public DynaTraceWebDriver(WebDriver driver) {
		this.driver = driver;
		this.dynaTraceNavigation = new DynaTraceWebDriverNavigation(driver);
	}

	public static DynaTraceWebDriver forWebDriver(WebDriver driver) {
		DynaTraceWebDriver dtDriver = drivers.get(driver);
		if(dtDriver == null) {
			dtDriver = new DynaTraceWebDriver(driver);
			drivers.put(driver, dtDriver);
		}
		return dtDriver;
	}

	@Override
	public void close() {
		driver.close();
	}

	@Override
	public WebElement findElement(By arg0) {
		return driver.findElement(arg0);
	}

	@Override
	public List<WebElement> findElements(By arg0) {
		return driver.findElements(arg0);
	}

	@Override
	public void get(String arg0) {
		driver.get(arg0);
	}

	@Override
	public String getCurrentUrl() {
		return driver.getCurrentUrl();
	}

	@Override
	public String getPageSource() {
		return driver.getPageSource();
	}

	@Override
	public String getTitle() {
		return driver.getTitle();
	}

	@Override
	public String getWindowHandle() {
		return driver.getWindowHandle();
	}

	@Override
	public Set<String> getWindowHandles() {
		return driver.getWindowHandles();
	}

	@Override
	public Options manage() {
		return driver.manage();
	}

	@Override
	public Navigation navigate() {
		return dynaTraceNavigation;
	}

	@Override
	public void quit() {
		driver.quit();
	}

	@Override
	public TargetLocator switchTo() {
		return driver.switchTo();
	}

	private class DynaTraceWebDriverNavigation implements WebDriver.Navigation {

		protected WebDriver driver;

		public DynaTraceWebDriverNavigation(WebDriver driver) {
			this.driver = driver;
		}

		@Override
		public void back() {
			driver.navigate().back();
		}

		@Override
		public void forward() {
			driver.navigate().forward();

		}

		@Override
		public void refresh() {
			driver.navigate().refresh();
		}

		@Override
		public void to(String arg0) {
			driver.navigate().to(arg0);
		}

		@Override
		public void to(URL arg0) {
			try {
				to(arg0.toURI().toString());
			} catch (URISyntaxException e) {
			}
		}

	}

	public WebDriver getWebDriver() {
		return driver;
	}
}
