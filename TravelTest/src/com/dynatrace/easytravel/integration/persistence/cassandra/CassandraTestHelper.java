/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: CassandraTestHelper.java
 * @date: 11.02.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence.cassandra;

import java.io.IOException;

import com.dynatrace.easytravel.cassandra.CassandraDatabase;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.procedures.CassandraProcedure;
import com.dynatrace.easytravel.persistence.controller.BusinessDatabaseController;


/**
 *
 * @author stefan.moschinski
 */
public class CassandraTestHelper {

	// we do not want to start new Cassandra nodes for each test class
	private static SuiteCassandraTestHelper testSuiteHelper;

	private Iterable<CassandraProcedure> cassandraNodes;
	private CassandraDatabase database;
	private BusinessDatabaseController controller;


	public static CassandraTestHelper setUpNodes() throws IOException, CorruptInstallationException, InterruptedException {
		if (testSuiteHelper != null) {
			return testSuiteHelper;
		}
		return new CassandraTestHelper();
	}

	private CassandraTestHelper() throws IOException, CorruptInstallationException, InterruptedException {
		cassandraNodes = CassandraProcedureRunner.startCassandraNode(3);
		database = new CassandraDatabase();
		controller = database.createNewBusinessController("easyTravelBusinessTest");
		database.setupPersistenceLayer();
	}

	public BusinessDatabaseController getController() {
		return controller;
	}

	public void tearDown() {
		database.closeConnection();
		for (CassandraProcedure node : cassandraNodes) {
			node.stop();
		}
	}

	public static SuiteCassandraTestHelper registerSingleton() throws IOException, CorruptInstallationException,
			InterruptedException {
		testSuiteHelper = new SuiteCassandraTestHelper();
		return testSuiteHelper;
	}

	static class SuiteCassandraTestHelper extends CassandraTestHelper {

		private SuiteCassandraTestHelper() throws IOException, CorruptInstallationException, InterruptedException {
			super();
		}

		@Override
		public void tearDown() {
			// we do not want to stop the test here
		}

		void tearDownTestSuite() {
			super.tearDown();
		}

	}
}
