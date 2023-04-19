/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: SearchDelayHelper.java
 * @date: 10.02.2011
 * @author: peter.lang
 */
package com.dynatrace.easytravel.frontend.beans;


/**
 *
 * @author peter.lang
 */
public class SearchDelayHelper {

	/**
	 * time in milliseconds for delay in setter
	 */
	private static final int SETTER_DELAYTIME = 1;

	private long currenTime = Long.MIN_VALUE;


	/**
	 * @return the counter
	 */
	public long getCurrenTime() {
		return currenTime;
	}


	/**
	 * Setter consumes some with a simple busy waiting strategy.
	 *
	 * @param counter the counter to set
	 */
	public void setCurrenTime(long counter) {
		// wait for some time.
		long startTime = System.currentTimeMillis();
		long endTime = startTime + SETTER_DELAYTIME;
		long delaycounter = Long.MIN_VALUE;
		while (System.currentTimeMillis() < endTime) {
			delaycounter += 1;
			if (delaycounter >= Long.MAX_VALUE) {
				delaycounter = Long.MIN_VALUE;
			}
		}
		this.currenTime = counter;
	}


}
