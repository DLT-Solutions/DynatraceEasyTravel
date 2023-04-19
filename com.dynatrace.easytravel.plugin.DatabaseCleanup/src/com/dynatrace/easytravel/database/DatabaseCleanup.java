package com.dynatrace.easytravel.database;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.model.GenericDataAccess;
import com.dynatrace.easytravel.persistence.Database;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.spring.SpringUtils;

import ch.qos.logback.classic.Logger;

public class DatabaseCleanup extends AbstractGenericPlugin {

	private static Logger log = LoggerFactory.make();

	private static final int DELETE_INTERVALL = 5 * 60 * 1000; // 5 Minutes
	private static final String SKIP_USER = "monica";

	private static AtomicBoolean active = new AtomicBoolean(false);

	private int maxToKeep = 5000;

	// remember when we last deleted to only run in some intervals
	private long lastRun = 0;

	public void setMaxToKeep(int max) {
		maxToKeep = max;
	}

	@Override
	public Object doExecute(String location, Object... context) {
		if (log.isDebugEnabled())
			log.debug("Database Cleanup: Had extension point: " + location + ", context: " + Arrays.toString(context));

		// only one instance should delete rows at a time, others should continue and not be blocked
		if (active.compareAndSet(false, true)) {
			try {
				if (lastRun + DELETE_INTERVALL > System.currentTimeMillis()) {
					return null;
				}

				lastRun = System.currentTimeMillis();

				final DataAccess access;

				Database database;
				if (context.length > 0 && context[0] instanceof Database) {
					// if we get passed a database we use it (this is used for tests!)
					database = (Database) context[0];
				} else {
					// otherwise retrieve the database via Spring
					database = SpringUtils.getBean("database", Database.class);
				}

				access = new GenericDataAccess(database.createNewBusinessController());

				try {
					cleanupBooking(access);
					cleanupLoginHistory(access);
				} finally {
					access.close();
				}
			} finally {
				active.set(false);
			}
		} else {
			log.debug("Not running database cleanup because another instance is already active.");
		}

		return null;
	}

	private void cleanupBooking(DataAccess access) {
		int count = access.getBookingCountExcludingUser(SKIP_USER);

		if (count > maxToKeep) {
			log.info("Removing " + (count - maxToKeep) + " old bookings to avoid filling up the database");

			access.startTransaction();

			Collection<String> bookings = access.getBookingIdsExcludingUser(SKIP_USER, count - maxToKeep);

			for (String bookingId : bookings) {
				access.removeBookingById(bookingId);
			}

			access.commitTransaction();
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Not removing " + count + " elements.");
			}
		}
	}

	private void cleanupLoginHistory(DataAccess access) {
		User userToExclude = access.getUser(SKIP_USER);

		int count = access.getTotalLoginCountExcludingUser(userToExclude);
		if (count > maxToKeep) {
			log.info("Removing " + (count - maxToKeep) + " old login history entries to avoid filling up the database");

			access.startTransaction();
			Collection<Integer> loginHistories = access.getLoginIdsExcludingUser(userToExclude, count - maxToKeep);

			for (Integer id : loginHistories) {
				access.removeLoginHistoryById(id);
			}
			access.commitTransaction();
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Not removing " + count + " elements.");
			}
		}
	}
}
