/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 * 
 * @file: MongoTestHelper.java
 * @date: 04.02.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence.mongodb;

import java.io.IOException;

import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.mongodb.MongoDb;
import com.dynatrace.easytravel.persistence.controller.BusinessDatabaseController;



/**
 * 
 * @author stefan.moschinski
 */
class MongoTestHelper {


	static BusinessDatabaseController initializeController() throws IOException, CorruptInstallationException,
			InterruptedException {
		MongoDbProcedureRunner.setUpClass();
		MongoDb database = new MongoDb();
		return database.createNewBusinessController();
	}
}
