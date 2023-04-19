/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: WebLauncherIntegrationTest.java
 * @date: 22.09.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.integration;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;


/**
 *
 * @author dominik.stadler
 */
public class AntProcedureIntegrationTest {
	private static final Logger log = LoggerFactory.make();

	@BeforeClass
	public static void setUpClass() throws Exception {
		IntegrationTestBase.cleanupAndStart(false, MessageConstants.SCENARIO_GROUP_DEVTEAM_TITLE, MessageConstants.DEVTEAM_SCENARIO_UNITTESTING_TITLE);
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		log.info("Finished testing, now shutting down launcher");
		IntegrationTestBase.stop();
		log.info("Done stopping the integration testing procedures.");
	}

	@Test
	public void test() throws Exception {
		// let the Ant-Procedure do some more work
		Thread.sleep(5000);

		// TODO: verify that the AntProcedure really did some work, how do we do that?
	}
}
