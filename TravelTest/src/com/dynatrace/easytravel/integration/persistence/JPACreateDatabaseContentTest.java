/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: CreateDatabaseContent.java
 * @date: 11.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.dynatrace.easytravel.constants.BaseConstants.BusinessBackend.Persistence;
import com.dynatrace.easytravel.database.DatabaseBase;


/**
 *
 * @author stefan.moschinski
 */
public class JPACreateDatabaseContentTest extends AbstractCreateDatabaseContentTest {

	//private static Iterable<CassandraProcedure> cassandraNodes;

	@BeforeClass
	public static void setUpClass() throws Exception {
		//cassandraNodes = CassandraProcedureRunner.startCassandraNode(3);
		DatabaseBase.setUpClass();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		/*for (CassandraProcedure node : cassandraNodes) {
			node.stop();
		}*/
		DatabaseBase.tearDownClass();
	}

	@Override
	protected String getPersistenceMode() {
		return Persistence.JPA;
	}
}
