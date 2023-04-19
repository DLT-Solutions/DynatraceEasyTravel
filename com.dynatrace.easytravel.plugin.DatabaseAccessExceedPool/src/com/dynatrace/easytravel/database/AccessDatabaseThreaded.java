package com.dynatrace.easytravel.database;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.exception.ExceptionUtils;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.model.GenericDataAccess;
import com.dynatrace.easytravel.persistence.Database;
import com.dynatrace.easytravel.util.ExecuteInMultipleThreads;


public class AccessDatabaseThreaded implements AccessDatabaseForLoading {
    private static Logger log = LoggerFactory.make();

	// how long to run the load test
	private static final int TEST_DURATION = 10*1000;	// 10 seconds

    @Override
	public void execute(final Database database, int iterationCount) {
        final Thread[] threads = new Thread[iterationCount];
        final DBExecutorThread[] runners = new DBExecutorThread[iterationCount];

		log.info("Starting " + iterationCount + " Threads for executing SQLs in parallel");
		// start the configured number of threads
		for (int i = 0; i < iterationCount; i++) {
			log.debug("Starting thread number: " + i);
			runners[i] = new DBExecutorThread(iterationCount, i, System.currentTimeMillis(), new GenericDataAccess(
					database.createNewBusinessController()));
			threads[i] = new Thread(runners[i], "ExecuteInMultipleThreads-" + i);

			threads[i].start();
		}

		// Wait the time that is configured to allow some reports to be created
		try {
			ExecuteInMultipleThreads.waitForThreads(TEST_DURATION, threads);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}

		// tell the threads to stop
		ExecuteInMultipleThreads.stop();

		// wait for threads to finish
		for (int i = 0; i < iterationCount; i++) {
			log.info("Joining thread number: " + i);
			try {
				// first make sure the thread is done
				threads[i].join();
				// only then we can close the database connection safely
				runners[i].close();
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}

		ConcurrentLinkedQueue<Throwable> exceptions = ExecuteInMultipleThreads.getExceptions();

		log.info("Had " + exceptions.size() + " exceptions reported.");
		// now check if we had any exceptions
		if(exceptions.size() > 0) {
			StringBuilder builder = new StringBuilder();
			for (Throwable t : exceptions) {
				builder.append("Exception: ").append(ExceptionUtils.getStackTrace(t)).append("\n");
			}
			throw new IllegalStateException(builder.toString());
		}

		AtomicInteger goodReports = ExecuteInMultipleThreads.getGoodReports();

		log.info("Executed " + goodReports.get() + " sets in " + TEST_DURATION/1000 + " seconds.");
	}
}
