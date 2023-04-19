/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: MongoDBController.java
 * @date: 20.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb;

import com.dynatrace.easytravel.persistence.controller.TransactionlessController;
import com.mongodb.DB;


/**
 *
 * @author stefan.moschinski
 */
public class MongoDbController extends TransactionlessController {

	private final MongoDbConnection mongoConn;
	private final String dbName;

	/**
	 * 
	 * @param dbConn
	 * @author stefan.moschinski
	 * @param dbName
	 */
	public MongoDbController(MongoDbConnection dbConn, String dbName) {
		this.mongoConn = dbConn;
		this.dbName = dbName;
	}

	@Override
	public void close() {
		mongoConn.closeConnection();
	}

	@Override
	public void dropContents() {
		mongoConn.dropDatabase(dbName);
	}

	/**
	 * 
	 * @return
	 * @author stefan.moschinski
	 */
	public DB getDatabase() {
		return mongoConn.getDatabase(dbName);
	}



}
