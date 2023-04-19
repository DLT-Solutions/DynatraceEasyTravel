/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: BasicIntegrationTest.java
 * @date: 14.09.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.integration;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.integration.spring.BookingTest;
import com.dynatrace.easytravel.integration.spring.FindJourneysTest;
import com.dynatrace.easytravel.integration.spring.LoginTest;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.DynamicPluginTest;
import com.dynatrace.easytravel.spring.SpringTestBase;
import com.dynatrace.easytravel.spring.StartFrontendTest;


/**
 * Integration Test which executes the HtmlUnitTest testcase
 *
 * @author dominik.stadler
 */
@RunWith(Suite.class)
@SuiteClasses({
	BookingTest.class,
	DynamicPluginTest.class,
	FindJourneysTest.class,
	LoginTest.class,
	StartFrontendTest.class
})
public class SpringIntegrationTest {
	private static final Logger log = LoggerFactory.make();

	@BeforeClass
	public static void setUpClass() throws Exception {
		IntegrationTestBase.cleanupAndStart(false, null, null);

		log.info("Initialize Spring");
		SpringTestBase.init();

		log.info("Started launcher, now running tests");
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		log.info("Not shutdown Spring for now, but should be revisited!!");
		//SpringTestBase.dispose();
		/* TODO:
		appContext is not initialized

		java.lang.IllegalStateException: appContext is not initialized
		at com.dynatrace.easytravel.spring.SpringUtils.getAppContext(SpringUtils.java:124)
		at com.dynatrace.easytravel.spring.SpringUtils.getBean(SpringUtils.java:112)
		at com.dynatrace.easytravel.spring.SpringUtils.getPluginHolder(SpringUtils.java:93)
		at com.dynatrace.easytravel.spring.PluginList.fetchAllPlugins(PluginList.java:93)
		at com.dynatrace.easytravel.spring.PluginList.getAllPlugins(PluginList.java:58)
		at com.dynatrace.easytravel.spring.PluginLifeCycle.executeAll(PluginLifeCycle.java:23)
		at com.dynatrace.easytravel.spring.SpringUtils.disposeBusinessBackendContext(SpringUtils.java:57)
		at com.dynatrace.easytravel.spring.SpringTestBase.dispose(SpringTestBase.java:16)
		at com.dynatrace.easytravel.integration.BasicIntegrationTest.testSpring(BasicIntegrationTest.java:111)

			 */

		log.info("Shutdown launcher");
		IntegrationTestBase.stop();
	}
}
