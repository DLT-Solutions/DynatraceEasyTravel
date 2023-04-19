/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: JourneyColumnFamilyTest.java
 * @date: 07.02.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence.cassandra;

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
//@Ignore("Does not work properly - especially the reset of the column families in Cassandra often fails")
public class UserColumnFamilyTest extends UserProviderTest {

	private static CassandraTestHelper cassandraHelper;

	@BeforeClass
	public static void setUpClass() throws IOException, CorruptInstallationException, InterruptedException {
		cassandraHelper = CassandraTestHelper.setUpNodes();
		BusinessDatabaseController controller = cassandraHelper.getController();
		initializeTest(controller, controller.getUserProvider());
	}

	@AfterClass
	public static void tearDownClass() {
		cassandraHelper.tearDown();
	}

}
