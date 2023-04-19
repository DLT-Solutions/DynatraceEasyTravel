/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: BasicIntegrationTest.java
 * @date: 14.09.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.integration;

import java.util.Collections;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.html.HtmlUnitTest;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.misc.Constants.Procedures;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;


/**
 * Integration Test which starts the NoSql/HBase scenario and then
 * executes the HBase specific tests and also the default HtmlUnitTest testcase
 * 
 * @author cwat-ggsenger
 */
@RunWith(Suite.class)
@SuiteClasses({
		HtmlUnitTest.class
})
public class HbaseIntegrationTest {
	private static final Logger log = LoggerFactory.make();

	@BeforeClass
	public static void setUpClass() throws Exception {
		IntegrationTestBase.cleanupAndStart(false, MessageConstants.SCENARIO_GROUP_PRODCUTION_TITLE,
				MessageConstants.PRODUCTION_SCENARIO_HBASE_TITLE,
				Collections.singleton("-Dconfig.enableRecommendationBean=false"));
		IntegrationTestBase.verifyProcedureState(Procedures.HBASE_ID, State.OPERATING);
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		log.info("Finished testing, now shutting down launcher");
		IntegrationTestBase.stop();
	}
}
