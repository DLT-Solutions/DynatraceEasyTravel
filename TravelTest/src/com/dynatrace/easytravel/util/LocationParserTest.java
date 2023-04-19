package com.dynatrace.easytravel.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Ignore;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.ThreadTestHelper;

public class LocationParserTest {
	private static final Logger log = LoggerFactory.make();

	private static final int NUMBER_OF_THREADS = 20;

	private static final double CALIBRATION_MIN = 5.5;

	// Do this as static to run this as first CPUHelper, otherwise the code is already well optimized and we do not get
	// any better in CI machines where nothing else is running...
	private static final LocationParser lowCPUHelper = getLowCalibrationValue();

	@Test
	public void testSimple() {
		// this test does not verify that actual values, but just ensures that we get sane results
		LocationParser.reCalibrate();
		assertTrue("Expect a sane calibration-value in range ]0,100[, but had: " + LocationParser.getCalibrationValue(),
				LocationParser.getCalibrationValue() > 0 && LocationParser.getCalibrationValue() < 100);

		LocationParser.parseSection(100);
	}

	@Ignore("This test takes too long. TBD: introduce static getter/setter methods in CPUHelper to reduce wait time in test conditions.")
	@Test
	public void testRecalibrationInThread() {
		// this test does not verify that actual values, but just ensures that we get sane results
		LocationParser.startCalibration();
		
		
		try {
			Thread.sleep(10000); // we know that the first calibration should NOT have happened before 60 was over, so there should be no sane value yet
			assertFalse("Did not expect a sane calibration-value in range ]0,100[, but got it... to early: " + LocationParser.getCalibrationValue(),
				LocationParser.getCalibrationValue() > 0 && LocationParser.getCalibrationValue() < 100);
		
			// We know that the first calibration must have finished within the first 120 seconds. We give it a 10 second margin (see above) to be safe.
			Thread.sleep(120000);
			
			assertTrue("Expect a sane calibration-value in range ]0,100[, but had: " + LocationParser.getCalibrationValue(),
				LocationParser.getCalibrationValue() > 0 && LocationParser.getCalibrationValue() < 100);
			
		} catch (InterruptedException e) {
			fail("Exception when in sleep, waiting for calibration.");
		}

		LocationParser.parseSection(100);
	}

	@Ignore("This test is flaky and there is always another CI machine which is a bit slower or a bit faster or where the CPU is loaded with other tasks, therefore I am disabling this one now to make overall unit tests run green")
	@Test
	public void test() {
		//EasyTravelConfig.read().backendCPUCycleTime
		assertTrue("Expect calibration value higher than " + CALIBRATION_MIN + ", but had: " + LocationParser.getCalibrationValue(), LocationParser.getCalibrationValue() > CALIBRATION_MIN);

		for(int i = 0;i < 20;i++) {
			long start = System.currentTimeMillis();
			LocationParser.parseSection(20);
			long end = System.currentTimeMillis();

			assertTrue("Calibration: " + LocationParser.getCalibrationValue() +" (" + i + ") " +
					"Had: " + (end-start) + "ms,  but expected between [13,40]", (end-start) >= 14 && (end-start) <= 27);
		}

		for(int i = 0;i < 20;i++) {
			long start = System.currentTimeMillis();
			LocationParser.parseSection(50);
			long end = System.currentTimeMillis();

			assertTrue("Calibration: " + LocationParser.getCalibrationValue() +" (" + i + ") " +
					"Had: " + (end-start) + "ms, but expected between [39, 75]", (end-start) >= 39 && (end-start) <= 75);
		}

		for(int i = 0;i < 10;i++) {
			long start = System.currentTimeMillis();
			LocationParser.parseSection(100);
			long end = System.currentTimeMillis();

			assertTrue("Calibration: " + LocationParser.getCalibrationValue() +" (" + i + ") " +
					"Had: " + (end-start) + "ms, but expected between [90,140]", (end-start) >= 90 && (end-start) <= 140);
		}
	}

	@SuppressWarnings("static-access")
	@Ignore("This test is flaky and there is always another CI machine which is a bit slower or a bit faster or where the CPU is loaded with other tasks, therefore I am disabling this one now to make overall unit tests run green")
	@Test
	public void testWithOtherThreadsUsingCPU() throws Throwable {
		// do CPU calibration before the threads start to have a value without CPU Load
		final LocationParser cpuHelper = new LocationParser();
		assertTrue("Expect calibration value higher than " + CALIBRATION_MIN + ", but had: " + cpuHelper.getCalibrationValue() + ", it seems this machine is quite slow!",
				cpuHelper.getCalibrationValue() > CALIBRATION_MIN);

		// use a semaphore to synchronize starting the actual CPU usage
		final Semaphore sem = new Semaphore(NUMBER_OF_THREADS);
		sem.acquire(NUMBER_OF_THREADS-1);	// minus one as one thread is special!

		final AtomicBoolean shouldStop = new AtomicBoolean(false);

        ThreadTestHelper helper =
                new ThreadTestHelper(NUMBER_OF_THREADS, 1);

            helper.executeTest(new ThreadTestHelper.TestRunnable() {
                @Override
                public void doEnd(int threadnum) throws Exception {
                    // do stuff at the end ...
                }

                @Override
                public void run(int threadnum, int iter) throws Exception {
                	// first thread uses CPUHelper
                    if(threadnum == 0) {
                    	// need to wait until all threads are actively causing CPU, i.e. we need all semaphores here before this thread starts
                		sem.acquire(NUMBER_OF_THREADS);

                    	try {
                    		runCPULoadInThread(cpuHelper);
                    	} finally {
	                		// now indicate to all threads that they can stop working
	                		shouldStop.set(true);
                    	}
                    } else {
                    	int semaphore = 10;
                    	while(!shouldStop.get()) {
	                    	fib(20);

	                    	/*if(true)
	                    		throw new IllegalStateException("problem!");*/

	                    	// do a bit of sleep to not lock the machine hard in this test...
	                    	//Thread.sleep(2);

	                    	//log.fine("Thread: " + threadnum + ".");

	                    	// run the CPU task 10 times before we release the semaphore to ensure that the thread really consumes CPU already
	                    	if(semaphore == 0) {
	                    		// only start cpu helper when we already consume CPU in all the threads
	                    		log.info("Thread " + threadnum + " is warmed up");
	                    		sem.release();

	                    		// decrease once more to have semaphore == -1
	                    		semaphore--;
	                    	} else if (semaphore > 0) {
	                    		semaphore--;
	                    	}
                    	}
                    	log.info("Stopping thread number: " + threadnum);
                    }
                }

				private void runCPULoadInThread(final LocationParser cpuHelper) {
					log.info("Calibration: " + cpuHelper.getCalibrationValue() +" All Threads started to consume CPU, now starting to test");

					// then do the actual checking, i.e. run the CPU loop and ensure that it now runs slower than in the test above where
					// no CPU is used by other threads
					int violations=0;
					List<Long> times = new ArrayList<Long>();
					for(int i = 0;i < 50;i++) {

						long start = System.currentTimeMillis();
						cpuHelper.parseSection(100);
						long end = System.currentTimeMillis();

						// do a number of warmup-cycles, otherwise linux machines are still very fast as it seems the threads do not start to use CPU immediately
						long time = end-start;
						if(i >= 10) {
							log.info("(" + i + ") Had: " + time + "ms");
							/*assertTrue("(" + i + ") Had: " + (end-start) + "ms, but expect at least 140ms as CPU is used up by other threads!",
									(end-start) >= 140);*/

							times.add(time);
							if(time < 138) {
								violations++;
								cpuHelper.reCalibrate();
							}
						} else {
							log.info("(Warmup: " + i + ") Had: " + time + "ms");
						}
					}

					assertTrue("Calibration: " + cpuHelper.getCalibrationValue() +" Had: " + violations + " violations where actual time spent was below 138, but expect only up to 5, times: " + times,
							violations <= 5);
				}
            });
	}

	private static long fib(int n) {
        if (n <= 1) return n;
        else return fib(n-1) + fib(n-2);
    }

	/*public static void main(String[] args) throws Throwable {
		CPUHelperTest test = new CPUHelperTest();
		log.info("Test");
		test.test();
		log.info("MTTest");
		test.testWithOtherThreadsUsingCPU();
	}*/

	@SuppressWarnings("static-access")
	@Test
	@Ignore("I finally give up on this test, it works locally and sometimes in CI, but still fails sometimes in CI and causes useless test-failure notifications")
	public void testReCalibrate() {
		// first use the lowest value from a few runs to ensure that we can have a higher one in re-calibrate
		double initial = lowCPUHelper.getCalibrationValue();
		assertTrue("Expect calibration value higher than " + CALIBRATION_MIN + ", but had: " + initial, initial > CALIBRATION_MIN);

		boolean seenDifference = false;
		for(int i = 0;i < 20;i++) {
			lowCPUHelper.reCalibrate();
			if(initial != lowCPUHelper.getCalibrationValue()) {
				seenDifference = true;
				break;
			}
		}
		assertTrue("Expected to see a difference calibration value at least once, but seems to have been the same always!", seenDifference);
		assertFalse("Expected to at least get a higher calibration value once, but still have the initial one", initial == lowCPUHelper.getCalibrationValue());
	}

	@SuppressWarnings("static-access")
	private static LocationParser getLowCalibrationValue() {
		LocationParser cpuHelper = new LocationParser();
		for(int i = 0;i < 10;i++) {
			LocationParser newHelper = new LocationParser();
			if(newHelper.getCalibrationValue() < cpuHelper.getCalibrationValue()) {
				log.info("Found lower calibration value " + newHelper.getCalibrationValue() + " compared to " + cpuHelper.getCalibrationValue() + " before, using lower of those for reclibration test.");
				cpuHelper = newHelper;
				break;
			}
		}
		log.info("Done finding lowest calibration value, found: " + cpuHelper.getCalibrationValue());
		return cpuHelper;
	}
}
