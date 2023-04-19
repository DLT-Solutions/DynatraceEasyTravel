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
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;


/**
 * Integration Test which starts the NoSql/Cassandra scenario and then
 * executes the Cassandra specific tests and also the default HtmlUnitTest testcase
 *
 * @author dominik.stadler
 */
@RunWith(Suite.class)
@SuiteClasses({
	HtmlUnitTest.class
})
public class CassandraIntegrationTest {
	private static final Logger log = LoggerFactory.make();

	@BeforeClass
	public static void setUpClass() throws Exception {
		IntegrationTestBase.cleanCassandraNodes();
		IntegrationTestBase.cleanupAndStart(false, MessageConstants.SCENARIO_GROUP_PRODCUTION_TITLE,
				MessageConstants.PRODUCTION_SCENARIO_CASSANDRA_TITLE,
				Collections.singleton("-Dconfig.enableRecommendationBean=false"));
		IntegrationTestBase.verifyProcedureState(Constants.Procedures.CASSANDRA_ID, State.OPERATING);
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		log.info("Finished testing, now shutting down launcher");
		IntegrationTestBase.stop();
	}
}
