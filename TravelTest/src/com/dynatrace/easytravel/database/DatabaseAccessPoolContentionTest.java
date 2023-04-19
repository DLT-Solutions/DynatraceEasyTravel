package com.dynatrace.easytravel.database;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.utils.ThreadTestHelper;

public class DatabaseAccessPoolContentionTest extends DatabaseWithContent {
    private static final Logger log = LoggerFactory.make();

    @Test
    public void testDBAccessPoolContention() {
		DatabaseAccessPoolContention plugin = new DatabaseAccessPoolContention();
		plugin.callLocationVerificationProcedure(database);
    }

    @Test
    public void testDBAccessPoolContentionThreaded() throws Exception {
		DataAccess dataAccess = createNewAccess();

        try {
	        DatabaseAccessPoolContention plugin = new DatabaseAccessPoolContention();
	        plugin.setSleepTime(1000);
	        plugin.setStartAsync(true);
	        plugin.setExtensionPoint(new String[] {"backend.journeyservice.location.search"});
	        plugin.setEnabled(true);

			plugin.execute("backend.journeyservice.location.search", database);

	        log.info("Waiting for thread, expect it to take some time");
	        long start = System.currentTimeMillis();
	        ThreadTestHelper.waitForThreadToFinish("LocationVerificationThread");
	        assertTrue("Expect the thread to run for at least 1 seconds, but did only run for " + (System.currentTimeMillis()-start) + " millisecond.",
	        		(System.currentTimeMillis()-start) > 980);	// use a bit lower than 1000ms as sleep in function may not be exactly one second
	        log.info("done.");
		} finally {
			dataAccess.close();
		}
    }

    @Test
    public void testDBAccessPoolContentionSync() {
		DataAccess dataAccess = createNewAccess();

		try {
	        DatabaseAccessPoolContention plugin = new DatabaseAccessPoolContention();
	        plugin.setSleepTime(1000);
	        plugin.setStartAsync(false);
	        plugin.setExtensionPoint(new String[] {"backend.journeyservice.location.search"});
	        plugin.setEnabled(true);

	        log.info("Running execute, expect it to take some time");
	        long start = System.currentTimeMillis();
			plugin.execute("backend.journeyservice.location.search", database);
	        assertTrue("Expect the sync-run to run for at least 1 seconds, but did only run for " + (System.currentTimeMillis()-start) + " millisecond.",
	        		(System.currentTimeMillis()-start) >= 990);		// use a bit lower as Thread.sleep(1000) can return a bit earlier sometimes
	        log.info("done.");
		} finally {
			dataAccess.close();
		}
    }
}
