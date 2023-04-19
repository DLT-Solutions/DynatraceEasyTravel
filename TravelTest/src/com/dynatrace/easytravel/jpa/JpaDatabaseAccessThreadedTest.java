package com.dynatrace.easytravel.jpa;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.database.DatabaseWithContentAndPlugins;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.model.GenericDataAccess;
import com.dynatrace.easytravel.persistence.Database;
import com.dynatrace.easytravel.persistence.SqlDatabase;
import com.dynatrace.easytravel.utils.ThreadTestHelper;


public class JpaDatabaseAccessThreadedTest extends DatabaseWithContentAndPlugins {

	private static final Logger log = LoggerFactory.make();

	private static final int NUMBER_OF_THREADS = 50;
//	private static final int NUMBER_OF_TESTS = 1000;
	private static final int NUMBER_OF_TESTS = 10;

	@Test
	public void testAddLocationMultipleThreads() throws Throwable {
		ThreadTestHelper helper =
				new ThreadTestHelper(NUMBER_OF_THREADS, NUMBER_OF_TESTS);

		final Database sqlDatabase = new SqlDatabase();

		helper.executeTest(new ThreadTestHelper.TestRunnable() {

			@Override
			public void doEnd(int threadnum) throws Exception {
				// do stuff at the end ...
			}

			@Override
			public void run(int threadnum, int iter) throws Exception {
				DataAccess access = null;
				try {
					if (iter % (NUMBER_OF_TESTS / 10) == 0) {
						log.info("Running " + threadnum + "-" + iter);
					}
					access = new GenericDataAccess(sqlDatabase.createNewBusinessController());

					assertTrue(access.addLocation("testlocation" + threadnum));

					// make sure it is removed at first
					access.deleteLocation("testlocation" + threadnum);

					assertTrue(access.addLocation("testlocation" + threadnum));

					// delete afterwards
					access.deleteLocation("testlocation" + threadnum);
				} finally {
					if (access != null)
						access.close();
				}
			}
		});
	}
}
