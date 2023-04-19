package com.dynatrace.easytravel.database;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.util.ExecuteInMultipleThreads;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

public final class DBExecutorThread extends ExecuteInMultipleThreads implements Closeable {
    private static Logger log = LoggerFactory.make();

	private final DataAccess access;

	DBExecutorThread(int threadCount, int nr, long reportStart, DataAccess dataAccess) {
		super(threadCount, nr, reportStart);

		access = dataAccess;

		log.info("Retrieved the Entity Manager, now starting some SQLs on it");
	}

	@Override
	public void close() {
		access.close();
	}

	@Override
	protected boolean runSomeWork() {
		executeSearch(access);

		return true;
	}

	private void executeSearch(DataAccess access) {
		findLocations(access, new String(new char[] { TextUtils.randomChar() }));
	}

	private Location[] findLocations(DataAccess access, String name)
	{
		log.debug(getStates() + " - Thread: " + nr + " - Looking for Locations: " + name);

		Collection<Location> locations = access.getMatchingLocations(name);

		List<Location> retList = new ArrayList<Location>();
		for(Location location : locations) {
			if (access.isLocationUsedByJourney(location.getName())) {
				retList.add(location);
			}

			// stop early if requested
			if(shouldStop()) {
				return locations.toArray(new Location[locations.size()]);
			}
		}
		log.trace(retList.size() + " locations used by journey.");

		locations = access.allLocations();

		log.debug(getStates() + " - Thread: " + nr + " - Done looking for Locations: " + name);
		return locations.toArray(new Location[locations.size()]);
	}
}
