/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: CreateDatabaseContent.java
 * @date: 11.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence.mongodb;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.dynatrace.easytravel.constants.BaseConstants.BusinessBackend.Persistence;
import com.dynatrace.easytravel.integration.persistence.AbstractCreateDatabaseContentTest;


/**
 *
 * @author stefan.moschinski
 */
public class MongoDbCreateDatabaseContentTest extends AbstractCreateDatabaseContentTest {

	@BeforeClass
	public static void setUpClass() throws Exception {
		MongoDbProcedureRunner.setUpClass();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		MongoDbProcedureRunner.tearDownClass();
	}

	@Override
	protected String getPersistenceMode() {
		return Persistence.MONGODB;
	}
}
