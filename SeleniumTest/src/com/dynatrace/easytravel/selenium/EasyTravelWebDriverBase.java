package com.dynatrace.easytravel.selenium;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.dynatrace.webautomation.DynaTraceWebDriver;
import com.dynatrace.webautomation.DynaTraceWebDriverHelper;

public class EasyTravelWebDriverBase {

	private final static Logger log = Logger.getLogger(EasyTravelWebDriverBase.class.getName());

	/**
	 * Needed to properly send endVisit call before closing the browser
	 */
	private static final long TIMEOUT_AFTER_TEST = 10_000;

	protected static WebDriver driver = null;
	protected static DynaTraceWebDriverHelper dynaTrace = null;
	protected static String defaultBrowser = System.getProperty("defaultbrowser", "ie");

	@Rule public TestName name = new TestName();

	@BeforeClass
	public static void startUp() {


		// when adding dynaTrace
		setProperWebDriver();
		assertNotNull("Need to have a WebDriver now!", driver);
		driver = DynaTraceWebDriver.forWebDriver(driver);
		dynaTrace = DynaTraceWebDriverHelper.forDriver(driver);
	}

	@AfterClass
	public static void tearDown() {
		if(driver != null) {
			driver.close();

			driver.quit();
		}
	}

	@After
	public void afterTest() {
		dynaTrace.getJsExecutor().executeScript("delete sessionStorage.DT_TESTRUNID;");
		dynaTrace.getJsExecutor().executeScript("delete sessionStorage.DT_TESTNAME;");
		try {
			Thread.sleep(TIMEOUT_AFTER_TEST);
		} catch (InterruptedException e) {
			// ignore exception
		}
	}

	protected void initTestRunId() {
		String testRunId = System.getProperty("testRunId");
		log.info("Got testRunId: " + testRunId);
		dynaTrace.getJsExecutor().executeScript("sessionStorage.DT_TESTRUNID = '" + testRunId + "';");
	}

	protected void setTestName(String testName) {
		dynaTrace.getJsExecutor().executeScript("sessionStorage.DT_TESTNAME = '" + testName + "';");
	}

	protected void endVisit() {
		dynaTrace.getJsExecutor().executeScript("if (dynaTrace != null) { dynaTrace.endVisit(); }");
	}

	private static void setProperWebDriver() {
		if(defaultBrowser.equalsIgnoreCase("ie")) {
			driver = getInternetExplorerDriver();
		} else {
			driver = getFirefoxDriver();
		}
	}

	private static WebDriver getFirefoxDriver() {
		String sys = System.getProperty("os.name");

		if (StringUtils.containsIgnoreCase(sys, "linux")){
			String dir = System.getProperty("webdriver.ff.driver.linux");
			System.setProperty("webdriver.gecko.driver", dir);
			File file = new File(dir);
			if (file.exists()) {
				file.setExecutable(true);
			}
			log.info("WebDriver for Firefox " + dir);
			return new FirefoxDriver();
		}

		String dir = System.getProperty("webdriver.ff.driver.win");
		System.setProperty("webdriver.gecko.driver", dir);
		return new FirefoxDriver();
	}

	private static WebDriver getInternetExplorerDriver() {
		String dir = System.getProperty("webdriver.ie.driver");
		System.setProperty("webdriver.ie.driver", dir);
		
		DesiredCapabilities capability = DesiredCapabilities.internetExplorer();
		capability.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
		capability.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
		
		return new InternetExplorerDriver(capability);
	}
}
