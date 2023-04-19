package com.dynatrace.easytravel.database;

import java.util.Date;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.jpa.business.LoginHistory;
import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.model.GenericDataAccess;
import com.dynatrace.easytravel.persistence.Database;


public class AccessDatabaseDirectly implements AccessDatabaseForLoading {

	private static Logger log = LoggerFactory.make();

	private static final String USER = "demouser";

	@Override
	public void execute(final Database database, int iterationCount) {
		log.info("Logging login history for user '" + USER + "' in " + iterationCount + " connections at the same time.");

		DataAccess[] managers = new DataAccess[iterationCount];

		// create new entity managers and do some work in them to use multiple connections from the pool here
		for (int i = 0; i < iterationCount; i++) {
			managers[i] = new GenericDataAccess(database.createNewBusinessController());
			User user = managers[i].getUser(USER);

			// start some transaction on each of them
			managers[i].startTransaction();

			// managers[i].createQuery("insert into LoginHistory)
			managers[i].addLoginHistory(new LoginHistory(user, new Date()));
		}

		// free up the items again
		for (int i = 0; i < iterationCount; i++) {
			managers[i].commitTransaction();
			managers[i].close();
		}
	}
}
