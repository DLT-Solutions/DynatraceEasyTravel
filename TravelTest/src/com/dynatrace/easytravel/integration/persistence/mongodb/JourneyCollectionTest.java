/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: UserCollectionTest.java
 * @date: 13.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence.mongodb;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.dynatrace.easytravel.integration.persistence.JourneyProviderTest;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.persistence.controller.BusinessDatabaseController;


/**
 *
 * @author stefan.moschinski
 */
public class JourneyCollectionTest extends JourneyProviderTest {


	@BeforeClass
	public static void setUpClass() throws IOException, CorruptInstallationException, InterruptedException {
		BusinessDatabaseController controller = MongoTestHelper.initializeController();
		initializeTest(controller, controller.getJourneyProvider());
	}

	@AfterClass
	public static void tearDown() throws Exception {
		MongoDbProcedureRunner.tearDownClass();
	}
}
