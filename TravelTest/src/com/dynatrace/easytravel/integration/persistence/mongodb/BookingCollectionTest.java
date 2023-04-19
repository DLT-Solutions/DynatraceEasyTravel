/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: TenantCollectionTest.java
 * @date: 13.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence.mongodb;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.dynatrace.easytravel.integration.persistence.BookingProviderTest;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.persistence.controller.BusinessDatabaseController;


/**
 *
 * @author stefan.moschinski
 */
public class BookingCollectionTest extends BookingProviderTest {


	@BeforeClass
	public static void setUpClass() throws IOException, CorruptInstallationException, InterruptedException {
		BusinessDatabaseController controller = MongoTestHelper.initializeController();
		initializeTest(controller, controller.getBookingProvider());
	}

	@AfterClass
	public static void tearDown() throws Exception {
		MongoDbProcedureRunner.tearDownClass();
	}
}
