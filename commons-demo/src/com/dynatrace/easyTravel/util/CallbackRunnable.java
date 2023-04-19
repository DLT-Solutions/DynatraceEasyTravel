package com.dynatrace.easytravel.util;


/**
 * A simple aggregation of some runnables in sequence.
 *
 * @author philipp.grasboeck
 */
public class CallbackRunnable implements Runnable {

	/**
	 * Used to collect runnables for cleanup purposes.
	 */
	public static final CallbackRunnable CLEANUP = new CallbackRunnable();

	private Runnable callback;

	/**
	 * Add a Runnable to the end of this CallbackRunnable
	 *
	 * @param doRun
	 * @return
	 * @author philipp.grasboeck
	 */
	public CallbackRunnable add(final Runnable doRun) {
		if (doRun == null) {
			throw new IllegalArgumentException("doRun must not be null");
		}
		if (callback == null) {
			callback = doRun;
		} else {
			final Runnable firstRun = callback;
			callback = new Runnable() {
				@Override
				public void run() {
					firstRun.run();
					doRun.run();
				}
			};
		}
		return this;
	}

	/**
	 * Clear the registered Runnables.
	 *
	 * @author philipp.grasboeck
	 */
	public void clear() {
		callback = null;
	}

	/**
	 * Run the previously added Runnable in added order.
	 */
	@Override
	public void run() {
		if (callback != null) {
			callback.run();
		}
	}
}
