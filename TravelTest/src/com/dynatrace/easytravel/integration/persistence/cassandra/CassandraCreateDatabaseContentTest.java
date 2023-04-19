/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: CreateDatabaseContent.java
 * @date: 11.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence.cassandra;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;

import com.dynatrace.easytravel.constants.BaseConstants.BusinessBackend.Persistence;
import com.dynatrace.easytravel.integration.persistence.AbstractCreateDatabaseContentTest;
import com.dynatrace.easytravel.launcher.procedures.CassandraProcedure;


/**
 *
 * @author stefan.moschinski
 */
@Ignore
public class CassandraCreateDatabaseContentTest extends AbstractCreateDatabaseContentTest {

	private static Iterable<CassandraProcedure> cassandraNodes;

	@BeforeClass
	public static void setUpClass() throws Exception {
		cassandraNodes = CassandraProcedureRunner.startCassandraNode(3);
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		for (CassandraProcedure node : cassandraNodes) {
			node.stop();
		}
	}

	@Override
	protected String getPersistenceMode() {
		return Persistence.CASSANDRA;
	}
}
