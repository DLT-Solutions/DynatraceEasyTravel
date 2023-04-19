/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: BasicIntegrationTest.java
 * @date: 14.09.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.integration;

import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.html.HtmlUnitTest;
import com.dynatrace.easytravel.launcher.engine.ProcedureFactory;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.utils.TestHelpers;


/**
 * Integration Test which runs the UEM Scenario which includes the
 * Apache Procedure
 *
 * @author dominik.stadler
 */
@RunWith(Suite.class)
@SuiteClasses({
	HtmlUnitTest.class
})
public class ApacheIntegrationTest {
	private static final Logger log = LoggerFactory.make();

	private static final int TIMEOUT = 60000;

	@BeforeClass
	public static void setUpClass() throws Exception {
		IntegrationTestBase.cleanupAndStart(false, MessageConstants.SCENARIO_GROUP_UEM_TITLE, MessageConstants.UEM_SCENARIO_DEFAULT_TITLE);

		EasyTravelConfig config = EasyTravelConfig.read();

		String states = UrlUtils.retrieveData("http://localhost:" + config.launcherHttpPort + "/statusAll", TIMEOUT);
		log.info("Had states: " + states);
		assertNotNull(states);

		TestHelpers.assertContains(states,
				ProcedureFactory.getNameOfProcedure(new DefaultProcedureMapping(Constants.Procedures.APACHE_HTTPD_ID)) + ": OPERATING" );
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		log.info("Finished testing, now shutting down launcher");
		IntegrationTestBase.stop();
	}
}
