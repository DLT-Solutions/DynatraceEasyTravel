/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: LoginHistoryCollectionTest.java
 * @date: 10.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence.mongodb;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.dynatrace.easytravel.integration.persistence.LoginHistoryProviderTest;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.persistence.controller.BusinessDatabaseController;


/**
 *
 * @author stefan.moschinski
 */
public class LoginHistoryCollectionTest extends LoginHistoryProviderTest {

	@BeforeClass
	public static void setUpClass() throws IOException, CorruptInstallationException, InterruptedException {
		BusinessDatabaseController controller = MongoTestHelper.initializeController();
		initializeTest(controller, controller.getLoginHistoryProvider());
	}

	@AfterClass
	public static void tearDown() throws Exception {
		MongoDbProcedureRunner.tearDownClass();
	}

}
