/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: WebLauncherIntegrationTest.java
 * @date: 22.09.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.integration;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;


/**
 *
 * @author dominik.stadler
 */
public class WebLauncherIntegrationTest {
	private static final Logger log = LoggerFactory.make();

	private static final int TIMEOUT = 60000;

	@BeforeClass
	public static void setUpClass() throws Exception {
		IntegrationTestBase.cleanupAndStart(true, MessageConstants.SCENARIO_GROUP_PRODCUTION_TITLE, MessageConstants.PRODUCTION_SCENARIO_STANDARD_TITLE);
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		log.info("Finished testing, now shutting down launcher");
		IntegrationTestBase.stop();
		log.info("Done stopping the integration testing procedures.");
	}

	@Test
	public void test() throws Exception {
		EasyTravelConfig config = EasyTravelConfig.read();

		String states = UrlUtils.retrieveData("http://localhost:" + config.weblauncherPort + "/scenario/states", TIMEOUT);
		log.info("Had states: " + states);
		assertNotNull(states);

		assertEquals("Production/Standard",
				UrlUtils.retrieveData("http://localhost:" + config.weblauncherPort + "/scenario/which", TIMEOUT));

		byte[] logs = UrlUtils.retrieveRawData("http://localhost:" + config.weblauncherPort + "/scenario/log", TIMEOUT);
		log.info("Had " + logs.length + " bytes of zipped log");
		assertTrue("Expected some size of the zipped logs, but had: " + logs.length,
				logs.length > 100);

		// try to switch to another scenario, first stop the current one
		assertEquals("OK",
				UrlUtils.retrieveData("http://localhost:" + config.weblauncherPort + "/scenario/stop", 10000));
		IntegrationTestBase.waitForWebLauncherScenario(State.STOPPED.toString());

		// then we need to wait for all procedures to stop, we get "STOPPED" above too early as the stop on the Batch is done async
		// and the status is gone as soon as stop() is called as we forget the static Batch in LaunchEngine immediately.
		for(int i = 0;i < 20;i++) {
			try {
				IntegrationTestBase.checkProcedurePorts(config);

				// done, no port is used any more
				break;
			} catch (AssertionError e) {
				// continue waiting
			}

			Thread.sleep(1000);
		}

		// ensure that all Procedures are really gone now
		IntegrationTestBase.checkProcedurePorts(config);

		// then start the new Scenario and wait until it is operating
		IntegrationTestBase.startWebLauncheScenario(MessageConstants.SCENARIO_GROUP_DEVTEAM_TITLE, MessageConstants.DEVTEAM_SCENARIO_FUNCTIONALTESTING_TITLE, config);
		IntegrationTestBase.waitForWebLauncherScenario(State.OPERATING.toString());
	}
}
