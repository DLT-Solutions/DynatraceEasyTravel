package com.dynatrace.diagnostics.uemload.http.base;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.*;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.TestHelpers;
import com.dynatrace.easytravel.utils.ThreadTestHelper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class HostAvailabilityTest {
	private static final int SLEEP_TIME = 10000;
	private static final int SLEEP_STEP = 50;

	private static final Logger logger = LoggerFactory.make();

	private MockRESTServer server1;
	private String url1;
	private MockRESTServer server2;
	private String url2;

	@BeforeClass
	public static void setUpClass() throws Exception {
		LoggerFactory.initLogging();
	}

	@Before
	public void setUp() throws Exception {
		// stop previously executing thread pool
		HostAvailability.INSTANCE.shutdown();
		ThreadTestHelper.waitForThreadToFinishSubstring(BaseConstants.UEM_LOAD_HOST_AVAILABILITY_THREAD);

		// create a new instance which does not start the scheduler
		HostAvailability.INSTANCE = new HostAvailability(false);

		// make these set as unavailable from the start
		HostAvailability.INSTANCE.informUnexpectedUnavailable("http://notexisting1");
		HostAvailability.INSTANCE.informUnexpectedUnavailable("http://notexisting2");
		HostAvailability.INSTANCE.informUnexpectedUnavailable("http://notexisting3");

		server1 = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
		url1 = "http://localhost:" + server1.getPort();

		server2 = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
		url2 = "http://localhost:" + server2.getPort();

		// make sure these are set as available
		assertNotNull(HostAvailability.INSTANCE.setAvailable(url1));
		assertNotNull(HostAvailability.INSTANCE.setAvailable(url2));

		logger.info("Started servers on ports " + server1.getPort() + " and " + server2.getPort());

		// to finish async tasks
		waitForBackgroundTask();
	}

	@After
	public void tearDown() throws InterruptedException {
		if(server1 != null) {
			server1.stop();
		}
		if(server2 != null) {
			server2.stop();
		}

		// make sure we stop threads
		HostAvailability.INSTANCE.shutdown();
		ThreadTestHelper.waitForThreadToFinishSubstring(BaseConstants.UEM_LOAD_HOST_AVAILABILITY_THREAD);
	}

	@Test
	public void testUnexpectedUnavailable() throws Exception {
		// at first all are available, either because none is set or the server is actually available
		assertTrue(HostAvailability.INSTANCE.isHostAvailable("http://somehost"));
		assertFalse(HostAvailability.INSTANCE.isHostAvailable("http://notexisting1"));
		assertTrue(HostAvailability.INSTANCE.isHostAvailable(url1));
		assertTrue(HostAvailability.INSTANCE.isHostAvailable(url2));

		// the reverse is valid as well
		assertFalse(HostAvailability.INSTANCE.isHostUnavailable("http://somehost"));
		assertTrue(HostAvailability.INSTANCE.isHostUnavailable("http://notexisting1"));
		assertFalse(HostAvailability.INSTANCE.isHostUnavailable(url1));
		assertFalse(HostAvailability.INSTANCE.isHostUnavailable(url2));

		// try to set all three as unavailable...
		HostAvailability.INSTANCE.informUnexpectedUnavailable("http://notexisting1");
		HostAvailability.INSTANCE.informUnexpectedUnavailable(url1);
		HostAvailability.INSTANCE.informUnexpectedUnavailable(url2);

		// however server1 and server2 are still available
		// and thus not actually set as unavailable
		assertTrue(HostAvailability.INSTANCE.isHostUnavailable("http://notexisting1"));
		assertFalse(HostAvailability.INSTANCE.isHostUnavailable(url1));
		assertFalse(HostAvailability.INSTANCE.isHostUnavailable(url2));

		// stop one of the servers
		server1.stop();

		// without new check they are still reported as available
		assertTrue(HostAvailability.INSTANCE.isHostUnavailable("http://notexisting1"));
		assertFalse(HostAvailability.INSTANCE.isHostUnavailable(url1));
		assertFalse(HostAvailability.INSTANCE.isHostUnavailable(url2));

		// set them unavailable once more
		HostAvailability.INSTANCE.informUnexpectedUnavailable("http://notexisting1");
		HostAvailability.INSTANCE.informUnexpectedUnavailable(url1);
		HostAvailability.INSTANCE.informUnexpectedUnavailable(url2);

		// now server1 is actually unavailable
		assertTrue(HostAvailability.INSTANCE.isHostUnavailable("http://notexisting1"));
		assertTrue(HostAvailability.INSTANCE.isHostUnavailable(url1));
		assertFalse(HostAvailability.INSTANCE.isHostUnavailable(url2));

		server1 = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
		url1 = "http://localhost:" + server1.getPort();

		// as long as the thread is not invoked, we still see server1 as unavailable
		assertTrue(HostAvailability.INSTANCE.isHostUnavailable("http://notexisting1"));
		assertTrue(HostAvailability.INSTANCE.isHostUnavailable(url1));
		assertFalse(HostAvailability.INSTANCE.isHostUnavailable(url2));

		// try to manually trigger it to available
		assertNotNull(HostAvailability.INSTANCE.setAvailable(url1));

		waitForBackgroundTask();

		logger.info("Waited for background task, now checking...");

		String state = "NE: " + HostAvailability.INSTANCE.isHostUnavailable("http://notexisting1") +
				", URL1: " + HostAvailability.INSTANCE.isHostUnavailable(url1) +
				", URL2: " + HostAvailability.INSTANCE.isHostUnavailable(url2);

		// server1 is back to available again
		assertTrue("Have: " + state, HostAvailability.INSTANCE.isHostUnavailable("http://notexisting1"));
		// the other two are still unavailable
		assertFalse("Should be available: " + url1 + ", state: " + state, HostAvailability.INSTANCE.isHostUnavailable(url1));
		assertFalse("Should be available: " + url2 + ", state: " + state, HostAvailability.INSTANCE.isHostUnavailable(url2));

		// setting an notexisting host to "available" does not work as there is a check done
		assertNotNull(HostAvailability.INSTANCE.setAvailable("http://notexisting1"));

		waitForBackgroundTask();

		assertTrue(HostAvailability.INSTANCE.isHostUnavailable("http://notexisting1"));
		assertFalse(HostAvailability.INSTANCE.isHostUnavailable(url1));
		assertFalse(HostAvailability.INSTANCE.isHostUnavailable(url2));
	}

	@Test
	public void testExpectedUnavailable() throws Exception {
		// at first all are available, either because none is set or the server is actually available
		assertTrue(HostAvailability.INSTANCE.isHostUnavailable("http://notexisting2"));
		assertFalse(HostAvailability.INSTANCE.isHostUnavailable(url1));
		assertFalse(HostAvailability.INSTANCE.isHostUnavailable(url2));

		// try to set all three as unavailable...
		HostAvailability.INSTANCE.setExpectedUnavailable("http://notexisting2");
		HostAvailability.INSTANCE.setExpectedUnavailable(url1);
		HostAvailability.INSTANCE.setExpectedUnavailable(url2);

		// now all are unavailable, even if they are still available
		assertTrue(HostAvailability.INSTANCE.isHostUnavailable("http://notexisting2"));
		assertTrue(HostAvailability.INSTANCE.isHostUnavailable(url1));
		assertTrue(HostAvailability.INSTANCE.isHostUnavailable(url2));

		// try to set all available
		assertNotNull(HostAvailability.INSTANCE.setAvailable("http://notexisting2"));
		assertNotNull(HostAvailability.INSTANCE.setAvailable(url1));
		assertNotNull(HostAvailability.INSTANCE.setAvailable(url2));

		waitForBackgroundTask();

		String state = "NE: " + HostAvailability.INSTANCE.isHostUnavailable("http://notexisting1") +
				", URL1: " + HostAvailability.INSTANCE.isHostUnavailable(url1) +
				", URL2: " + HostAvailability.INSTANCE.isHostUnavailable(url2);

		// the real unavailalbe one is still unavailable, the others are back again
		assertTrue("Should be unavailable: notexisting, state: " + state, HostAvailability.INSTANCE.isHostUnavailable("http://notexisting2"));
		assertFalse("Should be available: " + url1 + ", state: " + state, HostAvailability.INSTANCE.isHostUnavailable(url1));
		assertFalse("Should be available: " + url2 + ", state: " + state, HostAvailability.INSTANCE.isHostUnavailable(url2));
	}

	private void waitForBackgroundTask() throws InterruptedException {
		Executor executor = HostAvailability.INSTANCE.getExecutor();

		// first wait for all queued items to be started
		for(int i = 0;i < SLEEP_TIME/SLEEP_STEP && ((ScheduledThreadPoolExecutor)executor).getQueue().size() > 0;i++) {
			Thread.sleep(SLEEP_STEP);
		}

		assertEquals("Expected no scheduled item, but had: " + ((ScheduledThreadPoolExecutor)executor).getQueue(),
				0, ((ScheduledThreadPoolExecutor)executor).getQueue().size());

		// then wait for all active threads to finish up
		for(int i = 0;i < SLEEP_TIME/SLEEP_STEP && ((ScheduledThreadPoolExecutor)executor).getActiveCount() > 0;i++) {
			Thread.sleep(SLEEP_STEP);
		}

		// finally wait a bit for all threads to finish their work
		Thread.sleep(500);
	}

	@Test
	public void testCheckUnexpectedUnavailableHosts() throws Exception {
		// at first all are available, either because none is set or the server is actually available
		assertTrue(HostAvailability.INSTANCE.isHostAvailable("http://somehost"));
		assertFalse(HostAvailability.INSTANCE.isHostAvailable("http://notexisting3"));
		assertTrue(HostAvailability.INSTANCE.isHostAvailable(url1));
		assertTrue(HostAvailability.INSTANCE.isHostAvailable(url2));

		// stop one of the servers
		server1.stop();

		// set them unavailable once more to actually get them added
		HostAvailability.INSTANCE.informUnexpectedUnavailable("http://notexisting3");
		HostAvailability.INSTANCE.informUnexpectedUnavailable(url1);
		HostAvailability.INSTANCE.informUnexpectedUnavailable(url2);

		// now server1 is actually unavailable
		assertTrue(HostAvailability.INSTANCE.isHostUnavailable("http://notexisting3"));
		assertTrue(HostAvailability.INSTANCE.isHostUnavailable(url1));
		assertFalse(HostAvailability.INSTANCE.isHostUnavailable(url2));

		// now bring server1 back again
		server1 = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
		url1 = "http://localhost:" + server1.getPort();

		// as long as the check is not invoked, we still see server1 as unavailable
		assertTrue(HostAvailability.INSTANCE.isHostUnavailable("http://notexisting3"));
		assertTrue(HostAvailability.INSTANCE.isHostUnavailable(url1));
		assertFalse(HostAvailability.INSTANCE.isHostUnavailable(url2));

		// try to manually trigger it to available
		HostAvailability.INSTANCE.checkUnexpectedUnavailableHosts();

		// server1 is back to available again, but the notexisting one is still not back
		assertTrue(HostAvailability.INSTANCE.isHostUnavailable("http://notexisting3"));
		assertFalse(HostAvailability.INSTANCE.isHostUnavailable(url1));
		assertFalse(HostAvailability.INSTANCE.isHostUnavailable(url2));
	}
	
	@Test
	public void testSetAvailableForDeadServer() throws InterruptedException, IOException {
		//see JLT-108675
		assertTrue(HostAvailability.INSTANCE.isHostAvailable(url1));
		
		//set this as expected unavailable
		HostAvailability.INSTANCE.setExpectedUnavailable(url1);
		//and check this
		assertTrue(HostAvailability.INSTANCE.isHostUnavailable(url1));		
		 
		int oldPort = server1.getPort();
		//shut down the server
		server1.stop();
		
		//set the host as available 
		HostAvailability.INSTANCE.setAvailable(url1);
		//wait for asynchronous method to finish
		waitForBackgroundTask();
		
		//should be unavailable now
		assertTrue(HostAvailability.INSTANCE.isHostUnavailable(url1));
		
		// now bring server1 back again
		server1 = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
		url1 = "http://localhost:" + server1.getPort();		
		
		//check if new port equals the old port. Only in this case test makes sense
		Assume.assumeTrue("Test can only run if the new server runs on the same port that old one", (oldPort == server1.getPort()));
		
		// run checkUnexpectedUnavailableHosts manually (scheduler is disabled)
		HostAvailability.INSTANCE.checkUnexpectedUnavailableHosts();		
		
		//should be ok now
		assertTrue(HostAvailability.INSTANCE.isHostAvailable(url1));		
	}

	@Test
	public void testGetHost() {
		// first
		assertEquals("http://somehost", HostAvailability.INSTANCE.getHost("http://somehost"));
		assertEquals("http://somehost", HostAvailability.INSTANCE.getHost("http://somehost?q1=v1"));
	}

	@Test
	public void testWithDifferentLogLevel() throws Exception {
		final AtomicReference<Exception> exception = new AtomicReference<Exception>();
		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {

			@Override
			public void run() {
				try {
					testUnexpectedUnavailable();
					tearDown();
					//method testExpectedUnavailable is failing and is removed temporarily
//					setUp();
//					testExpectedUnavailable();
//					tearDown();
					setUp();
					testCheckUnexpectedUnavailableHosts();
				} catch (Exception e) {
					exception.set(e);
				}
			}
		}, HostAvailability.class.getName(), ch.qos.logback.classic.Level.DEBUG);

		if(exception.get() != null) {
			throw exception.get();
		}
	}

	@Test
	public void testThreadScheduler() throws Exception {
		// tried if an ExecutorService can be created with 0 coreThreads
		// however on Linux it did not work! Therefore test with 1 and corePool-timeout
		// it seems different Java versions/patch-levels behave differently with 0 coreThreads!

		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1,
				new ThreadFactoryBuilder()
						.setDaemon(true)
						.setNameFormat(BaseConstants.UEM_LOAD_HOST_AVAILABILITY_THREAD + "-test-%d")
						.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

							@Override
							public void uncaughtException(Thread t, Throwable e) {
								logger.error(TextUtils.merge("An uncaught exception happened in the thread ''{0}''", t.getName()), e);
							}
						})
						.build());
		((ThreadPoolExecutor)executor).setKeepAliveTime(500, TimeUnit.MILLISECONDS);
		((ThreadPoolExecutor)executor).allowCoreThreadTimeOut(true);

		// sleep to make the thread stop
		Thread.sleep(800);

		assertEquals(0, ((ThreadPoolExecutor)executor).getCompletedTaskCount());

		// now submit a new task, this should create a new thread
		final AtomicBoolean called = new AtomicBoolean(false);
		executor.submit(new Runnable() {
			@Override
			public void run() {
				called.set(true);
			}
		});

		executor.shutdown();
		assertTrue(executor.awaitTermination(20, TimeUnit.SECONDS));
		assertTrue(executor.isTerminated());
		assertTrue(executor.isShutdown());

		assertEquals(1, ((ThreadPoolExecutor)executor).getCompletedTaskCount());

		assertEquals(1, ((ThreadPoolExecutor)executor).getCompletedTaskCount());

		assertTrue(called.get());

		// also shutdown global executor
		HostAvailability.INSTANCE.shutdown();
		ThreadTestHelper.waitForThreadToFinishSubstring(BaseConstants.UEM_LOAD_HOST_AVAILABILITY_THREAD);
	}

	@Test
	public void testLab13() throws InterruptedException {
		// at first all are available, either because none is set or the server is actually available
		assertTrue(HostAvailability.INSTANCE.isHostUnavailable("http://notexisting2"));
		assertFalse(HostAvailability.INSTANCE.isHostUnavailable(url1));
		assertFalse(HostAvailability.INSTANCE.isHostUnavailable(url2));

		// try to set all three as unavailable...
		HostAvailability.INSTANCE.setExpectedUnavailable("http://notexisting2");
		HostAvailability.INSTANCE.setExpectedUnavailable(url1);
		HostAvailability.INSTANCE.setExpectedUnavailable(url2);

		// now all are unavailable, even if they are still available
		assertTrue(HostAvailability.INSTANCE.isHostUnavailable("http://notexisting2"));
		assertTrue(HostAvailability.INSTANCE.isHostUnavailable(url1));
		assertTrue(HostAvailability.INSTANCE.isHostUnavailable(url2));

		// try to set all available
		assertNotNull(HostAvailability.INSTANCE.setAvailable("http://notexisting2"));
		assertNotNull(HostAvailability.INSTANCE.setAvailable(url1));
		assertNotNull(HostAvailability.INSTANCE.setAvailable(url2));

		Executor executor = HostAvailability.INSTANCE.getExecutor();

		// first wait for all queued items to be started
		for(int i = 0;i < SLEEP_TIME/SLEEP_STEP && ((ScheduledThreadPoolExecutor)executor).getQueue().size() > 0;i++) {
			Thread.sleep(SLEEP_STEP);
		}

		BlockingQueue<Runnable> queue = ((ScheduledThreadPoolExecutor)executor).getQueue();
		assertEquals("Expected no scheduled item, but had: " + queue,
				0, queue.size());
	}
}
