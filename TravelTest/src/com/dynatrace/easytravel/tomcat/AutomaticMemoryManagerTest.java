package com.dynatrace.easytravel.tomcat;

import static com.dynatrace.easytravel.tomcat.AutomaticMemoryManager.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Callable;

import org.apache.catalina.Session;
import org.apache.catalina.session.ManagerTestBase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.MemoryUtils;
import com.dynatrace.easytravel.utils.ThreadTestHelper;

public class AutomaticMemoryManagerTest {
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
		validateSessionCleaning(10, STANDARD_SESSION_CLEAR_RATE, 1, util.setStandardHeapCallable());
		validateSessionCleaning(10, LOW_HEAP_SESSION_CLEAR_RATE, 1, util.setLowHeapCallable());
		validateSessionCleaning(10, EXTREME_LOW_HEAP_SESSION_CLEAR_RATE, 1, util.setExtremeLowHeapCallable());
		validateSessionCleaning(500, STANDARD_SESSION_CLEAR_RATE, 1, util.setStandardHeapCallable());
		validateSessionCleaning(500, LOW_HEAP_SESSION_CLEAR_RATE, 1, util.setLowHeapCallable());
		validateSessionCleaning(500, EXTREME_LOW_HEAP_SESSION_CLEAR_RATE, 1, util.setExtremeLowHeapCallable());
	}

	private void validateSessionCleaning(int testRuns, double clearRate, long timeout, Callable<Void> setHeap) throws Exception {
		AutomaticMemoryManager manager = new AutomaticMemoryManager("Test_Server", new MemoryUtils(util.getMemoryBean()));
		ManagerTestBase managerTest = new ManagerTestBase();
		managerTest.start(manager);
		util.setStandardHeap();
		manager.setMinSessionLifetime(1000000); // do not clear when creating

		for(int i = 1;i <= testRuns;i++) {
			manager.createSession("Session-" + i);
			util.nextMillisecond();
			assertEquals("While creating session " + i, i, manager.getActiveSessions());
			assertNotNull("While creating session " + i, manager.findSession("Session-" + i));
		}
		setHeap.call();
		manager.setMinSessionLifetime(timeout);
		int expected = util.getExpectSessionNo(manager, clearRate);
		manager.clearSessions();
		assertEquals(expected, manager.getActiveSessions());
		managerTest.stop();
	}

	@Test
	public void testParameterCleaning() throws Exception {
		validateParameterCleaning(10, STANDARD_ATTRIBUTE_CLEAR_RATE, util.setStandardHeapCallable());
		validateParameterCleaning(10, LOW_HEAP_ATTRIBUTE_CLEAR_RATE, util.setLowHeapCallable());
		validateParameterCleaning(10, EXTREME_LOW_HEAP_ATTRIBUTE_CLEAR_RATE, util.setExtremeLowHeapCallable());
		validateParameterCleaning(500, STANDARD_ATTRIBUTE_CLEAR_RATE, util.setStandardHeapCallable());
		validateParameterCleaning(500, LOW_HEAP_ATTRIBUTE_CLEAR_RATE, util.setLowHeapCallable());
		validateParameterCleaning(500, EXTREME_LOW_HEAP_ATTRIBUTE_CLEAR_RATE, util.setExtremeLowHeapCallable());
	}

	private void validateParameterCleaning(int testRuns, double clearRate, Callable<Void> setHeap) throws Exception {
		AutomaticMemoryManager manager = new AutomaticMemoryManager("Test_Server", new MemoryUtils(util.getMemoryBean()));

		ManagerTestBase managerTest = new ManagerTestBase();
		managerTest.start(manager);
		manager.setMinSessionLifetime(1000000); // do not clear when creating
		int i = 1;
		util.setStandardHeap();
		for(;i <= testRuns;i++) {
			Session session = manager.createSession("Session-" + i);

			session.getSession().setAttribute("testatt", "value1");
			session.getSession().setAttribute("bookingBean", "bean1");

			// have to sleep until we get a new timestamp
			util.nextMillisecond();

			assertEquals("While creating session " + i, i, manager.getActiveSessions());
			assertNotNull("While creating session " + i, manager.findSession("Session-" + i));
		}

		setHeap.call();
		Thread.sleep(100);	// pass some time to trigger expiry
		manager.setMinSessionLifetime(1); // clear now
		manager.processExpires();
		i = 1;

		for(;i <= Math.round(testRuns * clearRate); i++) {
			Session session = manager.findSession("Session-" + i);
			assertEquals("expecting attribute 'testatt' while expecting cleaning " + i + ", rate: " + clearRate + ", size: " + testRuns +
					"\nHad attributes: " + Collections.list(session.getSession().getAttributeNames()),
					"value1", session.getSession().getAttribute("testatt"));
			assertNull("not expecting attribute 'bookingBean' while expecting cleaning " + i + ", rate: " + clearRate + ", size: " + testRuns +
					"\nHad attributes: " + Collections.list(session.getSession().getAttributeNames()),
					session.getSession().getAttribute("bookingBean"));
		}

		for(;i <= testRuns; i++) {
			Session session = manager.findSession("Session-" + i);
			assertEquals("while not expecting cleaning " + i + ", rate: " + clearRate + ", size: " + testRuns, "value1", session.getSession().getAttribute("testatt"));
			assertEquals("while not expecting cleaning " + i + ", rate: " + clearRate + ", size: " + testRuns, "bean1", session.getSession().getAttribute("bookingBean"));
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
            	if(iter % 100 == 0) {
            		manager.processExpires();
            	}

    			Session session = manager.createSession("Session-" + threadnum + "-" + iter);
    			session.getSession().setAttribute("testatt", "value1");
    			session.getSession().setAttribute("bookingBean", "bean1");
            }
        });

		managerTest.stop();
	}
}
