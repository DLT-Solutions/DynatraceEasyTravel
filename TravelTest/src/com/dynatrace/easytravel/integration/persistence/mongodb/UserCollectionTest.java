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

import com.dynatrace.easytravel.integration.persistence.UserProviderTest;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.persistence.controller.BusinessDatabaseController;


/**
 *
 * @author stefan.moschinski
 */
public class UserCollectionTest extends UserProviderTest {


	@BeforeClass
	public static void setUpClass() throws IOException, CorruptInstallationException, InterruptedException {
		BusinessDatabaseController controller = MongoTestHelper.initializeController();
		initializeTest(controller, controller.getUserProvider());
	}

	@AfterClass
	public static void tearDown() throws Exception {
		MongoDbProcedureRunner.tearDownClass();
	}
}
