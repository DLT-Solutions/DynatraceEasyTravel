package com.dynatrace.diagnostics.uemload.utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.diagnostics.uemload.Bandwidth;
import com.dynatrace.diagnostics.uemload.BrowserType;


public class UemLoadUtils {

	private static final Random random = new Random(System.nanoTime());

	private static final Logger logger = Logger.getLogger(UemLoadUtils.class.getName());

	public static int randomInt(int n) {
		return random.nextInt(n);
	}

	public static int randomInt(int min, int max) {
		return max > min ? randomInt(max - min) + min : min;
	}
	
	public static long randomPositiveLong() {
		return Math.abs(random.nextLong());
	}

	public static int randomGaussian(int avg, int stddev) {
		int res;
		do {
			res = (int) (avg + random.nextGaussian() * stddev);
		} while(res < 0);
		return res;
	}

	/**
	 * simulates a random connection duration
	 *
	 * @param bw bandwidth of connection
	 * @param maximum
	 * @return 0 if no delay, random value greater 0 and lower than maximum to wait
	 * @author peter.lang
	 */
	public static void waitRandomConnectionDuration(Bandwidth bw, int maximum) {
		int res=0;
		switch (bw) {
			case DIALUP:
				res =  10 + randomInt(maximum*2);
				break;
			case DSL_LOW:
				res=  7 + randomInt(maximum);
				break;
			case DSL_MED:
				res = 5 + randomInt((int)(maximum*0.5));
				break;
			case DSL_HIGH:
				res = 1 + randomInt((int)(maximum*0.25));
				break;
			default:
		}
//		logger.info("randomConnectionDuration: bw=" + bw + " max="+maximum+ " waitfor="+res);
		suspendThread(res);
	}

	public static long getRequestDuration(Bandwidth bw, long duration) {
		int percentage = 0;
		switch (bw) {
			case DIALUP:
				percentage =  10 + randomInt(10);
				break;
			case DSL_LOW:
				percentage =  20 + randomInt(10);
				break;
			case DSL_MED:
				percentage = 40 + randomInt(15);
				break;
			case DSL_HIGH:
				percentage = 55 + randomInt(15);
				break;
			default:
				percentage = 75 + randomInt(15);
		}
		return Math.round(duration * percentage / 100);
	}

	public static long getRandomOnloadDuration(int maximum) {
		int res = 20 + randomInt(maximum);
		return res;
	}

	public static void waitRandomDuration(int maximum) {
		int res = randomInt(maximum);
		suspendThread(res);
	}

	public static void waitRandomFetchStartDuration(int maximum) {
		int res = 10 + randomInt(maximum);
		suspendThread(res);
	}

	/**
	 * simulates a random dns timing
	 *
	 * @param bw bandwidth of connection
	 * @param maximum
	 * @param factor
	 * @return 0 if no delay, random value greater 0 and lower than maximum to wait
	 * @author peter.lang
	 */
	public static void waitRandomDnsDuration(Bandwidth bw, int maximum, int factor) {

		int res = 0;

		// add at least half of maximum * factor (worldMapDNSFail plugin patterns)
		if (factor > 1) {
			int newMaximum = maximum * factor;
			res = randomInt(newMaximum / 2, newMaximum);
		}

		switch (bw) {
			case DIALUP:
				res +=  7 + randomInt(maximum * 2);
				break;
			case DSL_LOW:
				res +=  5 + randomInt(maximum);
				break;
			case DSL_MED:
				res += 2 + randomInt((int)(maximum * 0.5));
				break;
			case DSL_HIGH:
				res += 1 + randomInt((int)(maximum * 0.1));
				break;
			default:
		}

		suspendThread(res);
	}

	private static void suspendThread(long suspendTime) {
		if (suspendTime>0) {
			long startTime = System.currentTimeMillis();
			while (System.currentTimeMillis() < (startTime+suspendTime)) {
				try {
					Thread.sleep(suspendTime);
				} catch (InterruptedException e) {
					logger.fine("Thread interrupted: " + e.getMessage());
				}
			}
		}
	}

	public static int randomBandwidth(Bandwidth bandwidth, BrowserType browserType) {
		return randomBandwidth((int)(bandwidth.get() * 1024 * browserType.getSpeed()));
	}

	private static int randomBandwidth(int maximum) {
		return maximum - (int) (maximum / 4 * random.nextDouble());
	}

	public static void close(Closeable closeable) {
		if(closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not close "+ closeable, e);
		}
	}

	/**
	 *
	 * @param array
	 * @return a randomly selected element of the given array
	 * @author stefan.moschinski
	 */
	public static <T> T getRandomElement(T[] array) {
		return array[randomInt(array.length)];
	}

	public static long getDomContentLoadedStart(long domLoading, long domComplete) {
		long duration = domComplete - domLoading;
		int percentage = 15 + randomInt(10);

		return domLoading + Math.round(duration * percentage / 100);
	}

	public static long getDomContentLoadedEnd(long domLoading, long domComplete, long domContentLoadedStart) {
		long duration = domComplete - domLoading;
		int percentage = 15 + randomInt(10);

		long domContentLoadedEnd = domComplete - Math.round(duration * percentage / 100);
		if (domContentLoadedEnd <= domContentLoadedStart) {
			return Math.min(domContentLoadedStart + 1, domComplete);
		}
		return domContentLoadedEnd;
	}
}
