/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: BasicIntegrationTest.java
 * @date: 14.09.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.integration;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.html.HtmlUnitTest;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.procedures.MysqlContentCreationProcedure;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;


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
public class PHPIntegrationTest {
	private static final Logger log = LoggerFactory.make();

	@BeforeClass
	public static void setUpClass() throws Exception {
		IntegrationTestBase.cleanupAndStart(false, MessageConstants.SCENARIO_GROUP_UEM_TITLE, MessageConstants.PHP_SCENARIO_DEFAULT_TITLE,
				// need this tweak so that PHP Scenario is available on Windows as well by simulating it as "remote procedure" on the local host
				Collections.singleton("-Dcom.dynatrace.easytravel.host.apache_httpd_php=127.0.0.1"));

		EasyTravelConfig config = EasyTravelConfig.read();

		IntegrationTestBase.verifyProcedureState(Constants.Procedures.APACHE_HTTPD_PHP_ID, State.OPERATING);
		IntegrationTestBase.verifyPluginState(BaseConstants.Plugins.PHP_ENABLEMENT_PLUGIN, true);

		// also verify that the PHP site is available
		String url = "http://localhost:" + config.apacheWebServerPort + "/rating/ratings.php";
		log.info("Verifying that PHP pages are available at URL: " + url);
		String data = UrlUtils.retrieveData(url);
		assertNotNull(data);
		assertFalse(data.isEmpty());

		// verify that we can connect to the mysql database successfully
		runMysqlContentCreator();
		EasyTravelConfig.read().mysqlHost = java.net.InetAddress.getLocalHost().getHostName();
		runMysqlContentCreator();
	}

	private static void runMysqlContentCreator() {
		MysqlContentCreationProcedure proc = new MysqlContentCreationProcedure(new DefaultProcedureMapping(Constants.Procedures.MYSQL_CONTENT_CREATOR_ID));

		// run procedure
		assertEquals(Feedback.Neutral, proc.run());
		assertFalse(proc.isRunning());
		assertEquals(Feedback.Success, proc.stop());

		// run again to check that we correctly clean up
		assertEquals(Feedback.Neutral, proc.run());
		assertFalse(proc.isRunning());
		assertEquals(Feedback.Success, proc.stop());
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		log.info("Finished testing, now shutting down launcher");
		IntegrationTestBase.stop();
	}
}
