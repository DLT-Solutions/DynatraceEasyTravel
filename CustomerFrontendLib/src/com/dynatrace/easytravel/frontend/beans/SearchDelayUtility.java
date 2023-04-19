/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: SearchDelayUtility.java
 * @date: 01.02.2011
 * @author: peter.lang
 */
package com.dynatrace.easytravel.frontend.beans;

import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Simple utility class which implements a strategy for busy waiting.
 *
 * @author peter.lang
 */
public class SearchDelayUtility {

	private static final Logger LOGGER = LoggerFactory.make();

	/**
	 * Implements a "busy wait" strategy. The calling thread will by delayed by a given amount of time.
	 *
	 * @param time2wait amount of time to delay thread.
	 * @throws IllegalArgumentException if time2wait lower zero
	 * @author peter.lang
	 */
	public void doBusyWait(long time2wait) {
		if (time2wait < 0) {
			throw new IllegalArgumentException("Time2wait must not be lower zero.");
		}

		long startTime = System.currentTimeMillis();
		long endTime = startTime + time2wait;
		long delaycounter = Long.MIN_VALUE;
		while (System.currentTimeMillis() < endTime) {
			delaycounter += 1;
			if (delaycounter >= Long.MAX_VALUE) {
				delaycounter = Long.MIN_VALUE;
			}
		}
	}

	/**
	 * Implements a "busy wait" strategy using another objects getter / setter methods.
	 * The calling thread will by delayed by a given amount of time.
	 *
	 * @param time2wait amount of time to delay thread.
	 * @throws IllegalArgumentException if time2wait lower zero
	 * @author peter.lang
	 */
	public void doBusyWaitGetter(long time2wait) {
		if (time2wait < 0) {
			throw new IllegalArgumentException("Time2wait must not be lower zero.");
		}

		long startTime = System.currentTimeMillis();
		long endTime = startTime + time2wait;

		SearchDelayHelper delayHelper = new SearchDelayHelper();
		delayHelper.setCurrenTime(startTime);
		while (delayHelper.getCurrenTime() < endTime) {
			delayHelper.setCurrenTime(System.currentTimeMillis());
		}
	}


	/**
	 * Implements a "idle wait" strategy. The calling thread will by delayed by a given amount of time.
	 * The current thread will check every ten milliseconds if the desired amount of time already passed.
	 *
	 * @param time2wait amount of time to delay thread.
	 * @throws IllegalArgumentException if time2wait lower zero
	 * @author peter.lang
	 */
	public void doIdleWait(long time2wait) {
		if (time2wait < 0) {
			throw new IllegalArgumentException("Time2wait must not be lower zero.");
		}

		long startTime = System.currentTimeMillis();
		long endTime = startTime + time2wait;
		while (System.currentTimeMillis() < endTime) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				LOGGER.error("Unexpected InterreupedException cauhgt", e);
			}
		}
	}

	/**
	 * performs either a busy or "idle" delay of the current thread.
	 *
	 * @param time2wait amount of time to wait in milliseconds
	 * @param useBusyWaiting whether use busy or idle waiting strategy.
	 *
	 * @author peter.lang
	 */
	public void doWait(long time2wait, SearchDelayStrategyEnum strategy) {
		switch (strategy) {
			case IDLE:
				doIdleWait(time2wait);
				break;
			case BUSY:
				doBusyWait(time2wait);
				break;
			case BUSY_GETTER:
				doBusyWaitGetter(time2wait);
				break;
			case NONE:
				break;
			default:
				doBusyWait(time2wait);
				break;
		}
	}

}
