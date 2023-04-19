package com.dynatrace.easytravel.database;

import java.util.Arrays;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.model.GenericDataAccess;
import com.dynatrace.easytravel.persistence.Database;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.spring.SpringUtils;

public class DatabaseAccessPoolContention extends AbstractGenericPlugin {
    private static final Logger log = LoggerFactory.make();

    // these are overwritten by setter usually!
    private int sleepTime = 2000;
	private boolean startAsync = true;

	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}

	public void setStartAsync(boolean startAsync) {
		this.startAsync = startAsync;
	}

    @Override
	public Object doExecute(String location, Object... context) {
		log.info("DatabaseAccessPoolContention: Had extension point: " + location + ", context: " + Arrays.toString(context));

		// if we get passed a database we use it (this is used for tests!)
		final Database database;
		if (context.length > 0 && context[0] instanceof Database) {
			database = (Database) context[0];
		} else {
			// otherwise retrieve the entity manager via Spring
			database = SpringUtils.getBean("database", Database.class);
		}

		if(startAsync) {
			Thread t = new Thread("LocationVerificationThread") {
			    @Override
			    public void run() {
					callLocationVerificationProcedure(database);
			    }
			};
			t.setDaemon(false);
			t.start();
		} else {
			callLocationVerificationProcedure(database);
		}

		return null;
	}


	public void callLocationVerificationProcedure(Database database) {
		DataAccess dataAccess = new GenericDataAccess(database.createNewBusinessController());
        try {
			dataAccess.startTransaction();
			dataAccess.verifyLocation(sleepTime);
			dataAccess.commitTransaction();
		} finally {
			dataAccess.close();
		}
    }
}
