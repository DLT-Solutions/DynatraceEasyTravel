/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: MongoDB.java
 * @date: 18.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.persistence.AbstractDatabase;
import com.dynatrace.easytravel.persistence.controller.BusinessDatabaseController;


/**
 *
 * @author stefan.moschinski
 */
public class MongoDb extends AbstractDatabase {

	/**
	 *
	 * @param name
	 * @author stefan.moschinski
	 */
	public MongoDb() {
		super("MongoDB");
	}

	@Override
	public BusinessDatabaseController createNewBusinessController() {
		EasyTravelConfig cfg = EasyTravelConfig.read();
		MongoDbConnection dbConn = MongoDbConnection.openConnection(cfg.mongoDbInstances);
		return new MongoDbBusinessController(new MongoDbController(dbConn, "easyTravel-Business"));
	}
}
