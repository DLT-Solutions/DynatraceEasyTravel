/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: BasicIntegrationTest.java
 * @date: 14.09.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.integration;

import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.selenium.EasyTravelAdminWebDriverTest;
import com.dynatrace.easytravel.selenium.EasyTravelWebDriverExtendedTest;
import com.dynatrace.easytravel.selenium.EasyTravelWebDriverTest;


/**
 * Integration Test which starts up easyTravel and runs the Selenium/WebDriver test cases
 *
 * @author dominik.stadler
 */
@RunWith(Suite.class)
@SuiteClasses({
	EasyTravelWebDriverTest.class,
	EasyTravelWebDriverExtendedTest.class,
	EasyTravelAdminWebDriverTest.class
})
public class SeleniumIntegrationTest {
	private static final Logger log = LoggerFactory.make();

	@BeforeClass
	public static void setUpClass() throws Exception {
		IntegrationTestBase.cleanupAndStart(false, MessageConstants.SCENARIO_GROUP_PRODCUTION_TITLE, MessageConstants.PRODUCTION_SCENARIO_STANDARD_TITLE);

		// set some system properties for Selenium
		System.setProperty("webdriver.browser.default", "ff");
		//System.setProperty("testRunId", "${testrun.session}");
		if(SystemUtils.IS_OS_WINDOWS) {
			System.setProperty("firefoxbinary", "C:/Program Files (x86)/Mozilla Firefox/firefox.exe");
		} else {
			System.setProperty("firefoxbinary", "/usr/bin/firefox");
		}
		System.setProperty("customerFrontendHost", "localhost");
		System.setProperty("customerFrontendPort", Integer.toString(EasyTravelConfig.read().frontendPortRangeStart));
		System.setProperty("b2bFrontendHost", "localhost");
		System.setProperty("b2bFrontendPort", Integer.toString(EasyTravelConfig.read().b2bFrontendPortRangeStart));
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		log.info("Finished testing, now shutting down launcher");
		IntegrationTestBase.stop();
	}
}
