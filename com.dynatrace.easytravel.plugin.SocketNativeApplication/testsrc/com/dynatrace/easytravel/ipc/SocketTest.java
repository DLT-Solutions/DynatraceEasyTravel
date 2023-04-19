package com.dynatrace.easytravel.ipc;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.SpringUtils;
import com.dynatrace.easytravel.util.ExecuteInMultipleThreads;

import ch.qos.logback.classic.Logger;

public class SocketTest {
	private static final Logger log = LoggerFactory.make();

	private static final int MULTIPLE_COUNT = 10000;

	// how long to run the load test
	private static final int TEST_DURATION = 3 * 60 * 1000; // 3 minutes
	private static final int EXPECTED_COUNT = 100;

	private static final int THREAD_COUNT = 20;

	private final Thread[] threads = new Thread[THREAD_COUNT];

	// @Before and @After are duplicated from SprintTestBase as we don't want a
    // dependency to TravelTest here...
	@Before
	public void init() {
		SpringUtils.initCustomerFrontendContext();
	}

	@After
	public void dispose() {
		SpringUtils.disposeCustomerFrontendContext();
	}

	@Test
	public void testInvalid() throws Exception {
		// all these should be invalid
		NativeApplication application = new SocketNativeApplication();
		application.setChannel("easyTravelPipe");
		String back;

		back = application.sendAndReceive("teststring");
		log.info("Got back: " + back);
		assertTrue("Had: " + back, back.startsWith("Invalid - "));

		back = application.sendAndReceive("0");
		log.info("Got back: " + back);
		assertTrue("Had: " + back, back.startsWith("Invalid - "));

		back = application.sendAndReceive("0234j23kj234kj2");
		log.info("Got back: " + back);
		assertTrue("Had: " + back, back.startsWith("Invalid - "));

		back = application.sendAndReceive("123456789");
		log.info("Got back: " + back);
		assertTrue("Had: " + back, back.startsWith("Invalid - "));

		back = application.sendAndReceive("123456789adslkjasdf");
		log.info("Got back: " + back);
		assertTrue("Had: " + back, back.startsWith("Invalid - "));

		back = application.sendAndReceive("1234567890123456789adslkjasdf");
		log.info("Got back: " + back);
		assertTrue("Had: " + back, back.startsWith("Invalid - "));
	}

	@Test
	public void testValid() throws Exception {
		// all these should be invalid
		NativeApplication application = new SocketNativeApplication();
		application.setChannel("easyTravelPipe");
		String back;

		back = application.sendAndReceive("1234567890");
		log.info("Got back: " + back);
		assertTrue("Had: " + back, back.startsWith("Valid - "));

		back = application.sendAndReceive("12345678901234567890123456789012345678901234567890123456789012345678901234567890");
		log.info("Got back: " + back);
		assertTrue("Had: " + back, back.startsWith("Valid - "));
	}

	@Test
	public void testMultiple() throws IOException {
		NativeApplication application = new SocketNativeApplication();
		application.setChannel("easyTravelPipe");
		String back;
		back = application.sendAndReceive("teststring");
		log.info("Got back: " + back);
		back = application.sendAndReceive("0");
		log.info("Got back: " + back);
		back = application.sendAndReceive("000000000000");
		log.info("Got back: " + back);

		for(int i = 0;i < MULTIPLE_COUNT;i++) {
			back = application.sendAndReceive("teststring" + i);
			log.info("Got back: " + back);
		}
	}

	@Test
	public void testThreaded() {
		log.info("Starting " + THREAD_COUNT + " Threads for executing cc-checks in parallel");

		HtmlUnitExecutorThread[] executors = new HtmlUnitExecutorThread[THREAD_COUNT];

		// start the configured number of threads
		for (int i = 0; i < THREAD_COUNT; i++) {
			executors[i] = new HtmlUnitExecutorThread(THREAD_COUNT, i, System.currentTimeMillis());

			log.debug("Starting thread number: " + i);
			threads[i] = new Thread(executors[i], "ExecuteInMultipleThreads-" + i);

			threads[i].start();
		}

		// Wait the time that is configured to allow some reports to be created
		try {
			ExecuteInMultipleThreads.waitForThreads(TEST_DURATION, threads);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}

		// tell the threads to stop
		//for (int i = 0; i < THREAD_COUNT; i++) {
			log.info("Stopping threads");
			ExecuteInMultipleThreads.stop();
		//}

		// wait for threads to finish
		for (int i = 0; i < THREAD_COUNT; i++) {
			log.info("Joining thread number: " + i + ", having count: " + executors[i].getCount());
			try {
				threads[i].join();

				assertTrue("Threads should execute at least have " + EXPECTED_COUNT + " executions, but only had: " + executors[i].getCount() + " for thread number " + i,
						executors[i].getCount() > EXPECTED_COUNT);
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}

		Collection<Throwable> exceptions = ExecuteInMultipleThreads.getExceptions();

		log.info("Had " + exceptions.size() + " exceptions reported.");
		// now check if we had any exceptions
		if (exceptions.size() > 0) {
			StringBuilder builder = new StringBuilder();
			for (Throwable t : exceptions) {
				builder.append(ExceptionUtils.getStackTrace(t));
			}
			throw new IllegalStateException(builder.toString());
		}

		AtomicInteger goodReports = ExecuteInMultipleThreads.getGoodReports();

		log.info("Executed " + goodReports.get() + " sets in " + TEST_DURATION / 1000 + " seconds.");
	}

	private final class HtmlUnitExecutorThread extends ExecuteInMultipleThreads {
		private int count = 0;

		HtmlUnitExecutorThread(int threadCount, int nr, long reportStart) {
			super(threadCount, nr, reportStart);

			log.info("Starting to run some request for credit card checks for thread number " + nr);
		}

		@Override
		protected boolean runSomeWork() throws Exception {
			String str = SocketNativeApplication.send("012345678901234567890000" + nr + "00000" + count, "", "localhost", 8080);
			assertNotNull("Expect some return from communication with the native application, but received null", str);
			assertTrue("Expected a valid cc-number, but had response: " + str, str.startsWith("Valid - "));

			count++;

			return true;
		}


		public int getCount() {
			return count;
		}
	}
}
