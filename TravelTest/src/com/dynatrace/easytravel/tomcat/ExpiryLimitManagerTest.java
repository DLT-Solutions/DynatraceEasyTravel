package com.dynatrace.easytravel.tomcat;

import static com.dynatrace.easytravel.tomcat.AutomaticMemoryManager.LOW_HEAP_SESSION_CLEAR_RATE;
import static com.dynatrace.easytravel.tomcat.AutomaticMemoryManager.STANDARD_SESSION_CLEAR_RATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;

import org.apache.catalina.session.ManagerTestBase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.MemoryUtils;
import com.dynatrace.easytravel.utils.ThreadTestHelper;

/**
 *
 * @author stefan.moschinski
 * @author dominik.stadler
 */
@RunWith(MockitoJUnitRunner.class)
public class ExpiryLimitManagerTest {
	static final Logger log = LoggerFactory.make();

	@BeforeClass
	public static void setUpClass() throws IOException {
		LoggerFactory.initLogging();
	}

	private static final int NUMBER_OF_THREADS = 10;
	private static final int NUMBER_OF_TESTS = 1000;

	CleaningTestUtil util = new CleaningTestUtil();

	@Before
	public void setUp() {
		util.setUp();
	}


	@Test
	public void testSessionCleaning() throws Exception {
		int longLifetime = 1000 * 10;
		validateSessionExpiry(10, STANDARD_SESSION_CLEAR_RATE, longLifetime, util.setStandardHeapCallable());
		validateSessionExpiry(10, LOW_HEAP_SESSION_CLEAR_RATE, Math.round(longLifetime / 2.0), util.setLowHeapCallable());
		validateSessionExpiry(500, STANDARD_SESSION_CLEAR_RATE, longLifetime, util.setStandardHeapCallable());
		validateSessionExpiry(500, LOW_HEAP_SESSION_CLEAR_RATE, longLifetime, util.setLowHeapCallable());
		// do not need to test EXTREME_LOW_HEAP_SESSION_CLEAR_RATE, because it is ignores the session expiry
	}


	private void validateSessionExpiry(int testRuns, double clearRate, long timeout, Callable<Void> setHeap) throws Exception {
		AutomaticMemoryManager manager = new AutomaticMemoryManager("Test_Server", new MemoryUtils(util.getMemoryBean()));
		manager.setMinSessionLifetime(timeout);
		ManagerTestBase managerTest = new ManagerTestBase();
		managerTest.start(manager);

		util.setStandardHeap();

		int i = 1;
		for(;i <= testRuns / 2.0; i++) {
			manager.createSession("Session-" + i);

			// have to sleep until we get a new timestamp
			util.nextMillisecond();

			assertEquals("While creating session " + i + ": Sessions: " + Arrays.toString(manager.findSessions()),
					i, manager.getActiveSessions());
			assertNotNull("While creating session " + i + ": Sessions: " + Arrays.toString(manager.findSessions()),
					manager.findSession("Session-" + i));
		}

		util.waitFor(timeout);
		setHeap.call();


		for(;i <= testRuns;i++) {
			int expected = util.getExpectSessionNo(manager, clearRate) + 1;
			manager.createSession("Session" + i);

			// have to sleep until we get a new timestamp
			util.nextMillisecond();

			assertEquals("While creating session " + i + ": Sessions: " + Arrays.toString(manager.findSessions()),
					expected, manager.getActiveSessions());
			assertNotNull("While creating session " + i + ": Sessions: " + Arrays.toString(manager.findSessions()),
					manager.findSession("Session" + i));
			manager.setMinSessionLifetime(1);
		}

		managerTest.stop();
	}

	@Test
	public void testThreaded() throws Throwable {
		final AutomaticMemoryManager manager = new AutomaticMemoryManager("Test_Server", new MemoryUtils(util.getMemoryBean()));

		ManagerTestBase managerTest = new ManagerTestBase();
		managerTest.start(manager);

        ThreadTestHelper helper =
            new ThreadTestHelper(NUMBER_OF_THREADS, NUMBER_OF_TESTS);

        helper.executeTest(new ThreadTestHelper.TestRunnable() {
            @Override
			public void doEnd(int threadnum) throws Exception {
                // do stuff at the end ...
            }

            @Override
			public void run(int threadnum, int iter) throws Exception {
    			manager.createSession("Session-" + threadnum + "-" + iter);
            }
        });

		managerTest.stop();
	}
}
