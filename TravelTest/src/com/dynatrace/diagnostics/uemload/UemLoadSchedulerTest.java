package com.dynatrace.diagnostics.uemload;

import static org.junit.Assert.*;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;
import com.dynatrace.easytravel.utils.TestHelpers;


public class UemLoadSchedulerTest {
	@Before
	public void setUp() {
		// set default value as other tests might adjust it
		UemLoadScheduler.setMaxThreads(50);
	}

	@After
	public void tearDown() throws Exception {
		UemLoadScheduler.cleanup();

		// try to avoid strange failures sometimes
		if(UemLoadScheduler.getActiveCount() > 0) {
			Thread.sleep(5000);
		}

		// ensure that no work was left over in tests as this would affect other tests
		assertEquals(0, UemLoadScheduler.getActiveCount());
		assertNotNull(UemLoadScheduler.getQueue());
		assertEquals(0, UemLoadScheduler.getQueueSize());
		assertFalse(UemLoadScheduler.isShutdown());
	}

	@AfterClass
	public static void tearDownClass() {
		// do this only once as the static instance is not working any more afterwards
		UemLoadScheduler.shutdownNow();
		assertTrue(UemLoadScheduler.isShutdown());
		UemLoadScheduler.shutdown(100, TimeUnit.MILLISECONDS);

		try {
			UemLoadScheduler.schedule(null, 1000, TimeUnit.MILLISECONDS);
			fail("Should fail here");
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "scheduler is shut down");
		}
	}

	@Test
	public void testInitial() throws InterruptedException {
		assertEquals(0, UemLoadScheduler.getActiveCount());
		assertNotNull(UemLoadScheduler.getQueue());
		assertEquals(0, UemLoadScheduler.getQueueSize());
		assertFalse(UemLoadScheduler.isShutdown());
	}

	@Test
	public void testSleep() throws InterruptedException {
		final Semaphore called = new Semaphore(1);
		called.acquire();

		UemLoadScheduler.sleep(10, new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				called.release();

				return null;
			}

		});

		// wait on semaphore to make sure we get called
		called.acquire();

		// make sure the counts are updated as well
		while(UemLoadScheduler.getActiveCount() > 0 || UemLoadScheduler.getQueueSize() > 0) {
			Thread.sleep(100);
		}
	}

	@Test
	public void testSchedule() throws InterruptedException {
		final Semaphore called = new Semaphore(1);
		called.acquire();

		UemLoadScheduler.schedule(new Runnable() {

			@Override
			public void run() {
				called.release();
			}
		}, 10, TimeUnit.MILLISECONDS);

		// wait on semaphore to make sure we get called
		called.acquire();
	}

	@Test
	public void testScheduleAtFixedRate() throws InterruptedException {
		final AtomicInteger called = new AtomicInteger();

		UemLoadScheduler.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				called.incrementAndGet();
			}
		}, 10, 10, TimeUnit.MILLISECONDS);

		// should not take too long to run it 10 times, aprox. 100 MS
		long start = System.currentTimeMillis();
		while(called.get() < 10) {
			Thread.sleep(100);
		}
		assertTrue("Expected to have aprox. 100ms, but had: " + (System.currentTimeMillis()-start), (System.currentTimeMillis()-start) > 50 && (System.currentTimeMillis()-start) < 250);
	}

	@Test
	public void testTooManyTasks() throws InterruptedException {
		final AtomicInteger count = new AtomicInteger();
		for(int i = 0;i < 1000;i++) {
			UemLoadScheduler.schedule(new Runnable() {

				@Override
				public void run() {
					count.incrementAndGet();
				}
			}, 1000, TimeUnit.MILLISECONDS);
		}

		while(UemLoadScheduler.getActiveCount() > 0 || UemLoadScheduler.getQueueSize() > 0) {
			Thread.sleep(100);
		}

		assertEquals(151, count.get());
	}
	
	@Test
	public void testScheduleOnlyIfFree() throws InterruptedException {
		final AtomicInteger count = new AtomicInteger();
		for(int i=0; i<1000; i++) {
			UemLoadScheduler.scheduleOnlyIfFree(() -> count.incrementAndGet(), 1000, TimeUnit.MILLISECONDS); 
		}
		
		while(UemLoadScheduler.getActiveCount() > 0 || UemLoadScheduler.getQueueSize() > 0) {
			Thread.sleep(100);
		}

		assertEquals(50, count.get());
	}

	@Test
	public void testMaxThreads() throws InterruptedException {
		UemLoadScheduler.setMaxThreads(100);
		final AtomicInteger count = new AtomicInteger();
		for(int i = 0;i < 1000;i++) {
			UemLoadScheduler.schedule(new Runnable() {

				@Override
				public void run() {
					count.incrementAndGet();
				}
			}, 1000, TimeUnit.MILLISECONDS);
		}

		while(UemLoadScheduler.getActiveCount() > 0 || UemLoadScheduler.getQueueSize() > 0) {
			Thread.sleep(100);
		}

		assertEquals(301, count.get());
	}


	@Test
	public void testUncaughtException() throws InterruptedException {
		final Semaphore called = new Semaphore(1);
		called.acquire();

		UemLoadScheduler.schedule(new Runnable() {

			@Override
			public void run() {
				called.release();
				throw new Error("testexception");
			}
		}, 10, TimeUnit.MILLISECONDS);

		// wait on semaphore to make sure we get called
		called.acquire();
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(UemLoadScheduler.class);
	}
}
