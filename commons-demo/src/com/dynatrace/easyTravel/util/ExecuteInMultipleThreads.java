/*****************************************************
  *  dynaTrace Diagnostics (c) dynaTrace software GmbH
  *
  * @file: ReportLoadRunnable.java
  * @date: 12.03.2010
  * @author: dominik.stadler
  *
  */

package com.dynatrace.easytravel.util;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;

/**
 * Helper class for load testing. This runnable is executed multiple times in threads to
 * trigger an actions many times at once.
 *
 * @author dominik.stadler
 *
 */
public abstract class ExecuteInMultipleThreads implements Runnable {
	private final static Logger log = LoggerFactory.make();

	// used for displaying states of the threads
	public static final int STATE_ON = 1;
	public static final int STATE_OFF = 0;

    // static members for information that is collected accross all instances of ReportLoadRunnable
	private static volatile boolean stop = false;
	private static int threadCount;
	private volatile static AtomicIntegerArray states;
	private final static ConcurrentLinkedQueue<Throwable> exceptions = new ConcurrentLinkedQueue<Throwable>();
	private final static AtomicInteger goodResults = new AtomicInteger();

	// non-static members for information per instance of ReportLoadRunnable
	protected final int nr;
	protected final long reportStart;

	public ExecuteInMultipleThreads(final int threadCount, final int nr, long reportStart) {
		this.nr = nr;
		this.reportStart = reportStart;

		// need a static sync here to initialize the states-array only once
		synchronized (ExecuteInMultipleThreads.class) {
			if(states == null) {
				ExecuteInMultipleThreads.threadCount = threadCount;
				states = new AtomicIntegerArray(threadCount);
			}
		}
	}

	@Override
	public final void run() {
		states.set(nr, STATE_ON);
		try {
			// endlessly request reports
			while (!stop) {
				if(!runSomeWork()) {
					break;
				}

				int reports = goodResults.incrementAndGet();
				if(goodResults.get() % 200 == 0) {
	    			printInformation(getStates() + " - Thread: " + nr + " - ", reports, reportStart);
	    			printExtraInfo();
	    		}
			}
		} catch (Throwable e) {	// NOPMD - on purpose here to report all things that happen in the thread
			log.error("Failed to run: " + e.getClass().getName(), e);
			exceptions.add(e);
		} finally {
			states.set(nr, STATE_OFF);
		}
	}

	protected void printExtraInfo() throws Exception {
	}


	protected abstract boolean runSomeWork() throws Throwable;

	private static void printInformation(String thread, int reports, long reportStart) {

		long diff = (System.currentTimeMillis()-reportStart)/1000;

		log.info(thread + "Executed set number " + reports +
				", EPS: " +  (diff == 0 ? 0 : (reports/diff)) + " (" + reports + " per " + diff + " secs)");
	}

	protected static String getStates() {
		StringBuilder sb = new StringBuilder("|");
		for(int i = 0;i < threadCount;i++) {
			int state = states.get(i);
			if(state == ExecuteInMultipleThreads.STATE_ON)
				sb.append('x');
			else
				sb.append('_');
		}

		return sb.toString();
	}

	public static void stop() {
		stop = true;
	}

	public static boolean shouldStop() {
		return stop;
	}

	public static ConcurrentLinkedQueue<Throwable> getExceptions() {
		return exceptions;
	}

	public static AtomicInteger getGoodReports() {
		return goodResults;
	}

	/**
	 * Wait the specified time, but check periodically, if any of the threads already terminated.
	 *
	 * @throws InterruptedException
	 * @author dominik.stadler
	 */
	public static void waitForThreads(int duration, Thread[] threads) throws InterruptedException {
		for(int j = 0;j < duration/5000;j++) {
			Thread.sleep(5000);		// check every 5 seconds...

			// check if any thread stopped already
			for (int i = 0; i < threadCount; i++) {
				if(!threads[i].isAlive()) {
					log.info("Thread with number " + i + " is not running any more, stopping test!");
					return;
				}
			}
		}
	}
}
