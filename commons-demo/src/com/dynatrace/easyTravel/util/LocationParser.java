package com.dynatrace.easytravel.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomUtils;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.google.common.util.concurrent.AtomicDouble;


/**
 * Helper-Classes to spend some CPU time.
 *
 * It works by doing a CPU calibration during construction time and then
 * causing CPU load that is equivalent to the given number of milliseconds.
 *
 *  NOTE: we are not waiting for a certain time to pass on purpose here as we want to
 *  have the same amount of CPU spent independent of it's runtime. This allows to show the effect
 *  of other processes consuming CPU which makes the loop here run longer than before!
 *
 * @author cwat-dstadler
 */
public class LocationParser {
	private static final Logger log = LoggerFactory.make();

	private static AtomicDouble calibrationValueDynamic = new AtomicDouble(0.0);
	
	static ArrayList<Double> calResults = new ArrayList<>();
	static final int MAX_CAL_SIZE = 16; // max size of the calibration list of recent results.
										// so as to get the sample over about 200 minutes.
	
	public static void startCalibration () {
		
		final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
		
		class CalibrationTask implements Runnable {
			public void run() {
				reCalibrate();
				
				// Re-schedule yourself for later - at a random intervals, between 10 and 15 minutes,
				// but only if we have not gathered a large enough calibration sample.
				int calSize = calResults.size();
				
				// NOTE: It is important to keep this if-condition as '<' and not '<=', because the list will never grow beyond
				// MAX_CAL_SIZEM as it is dynamically trimmed, and thus using '<=' would mean we would always schedule further calibrations,
				// The point is, however, NOT to schedule new calibrations, once the list has grown to its full size.
				if (calSize < MAX_CAL_SIZE) {
					scheduledThreadPool.schedule( this, RandomUtils.nextInt(10, 15), TimeUnit.MINUTES);
				}
				
			}
		};
		
		CalibrationTask myTask = new CalibrationTask();
		
		// At start, wait a random number of seconds between 60 and 120 to reduce the chance of two
		// calibration - on two business back-ends - running concurrently.
		// Experiments show that a difference of 12 seconds is enough for two calibrations not to be mutually affected.
		// We leave the first 60 seconds free as it is expected to be busy CPU time.
		scheduledThreadPool.schedule(myTask, RandomUtils.nextInt(60, 120), TimeUnit.SECONDS); 
		
	}
	
	/**
	 * Allows to re-run CPU calibration to use higher values if necessary.
	 *
	 * Note: It will only set a new value if calibration leads to a higher value than before.
	 */
	protected static final void reCalibrate() {
		// calibrate three times and take the highest value to ensure sporadic CPU bursts on the machine do not affect calibration
		// if the previously set calibration value was already set, keep it if it was higher before
		double newCandidate1;
		double newCandidate2;
		double newCandidate3;
		double newCandidate;
		double tmpD;
		tmpD = calibrationValueDynamic.get();
		
		// break this up to get at all the values, for diagnostic purposes
		// newCandidate = Math.max(calibrate(), Math.max(calibrate(), calibrate()));
		newCandidate1 = calibrate();
		log.debug("calibration: newCandidate1 <" + newCandidate1 + ">");
		newCandidate2 = calibrate();
		log.debug("calibration: newCandidate2 <" + newCandidate2 + ">");
		newCandidate3 = calibrate();
		log.debug("calibration: newCandidate3 <" + newCandidate3 + ">");
		newCandidate = Math.max(newCandidate1, Math.max(newCandidate2, newCandidate3));
	
		// Add the new candidate to the list of the last few calibrations,
		log.info("calibration: New candidate to be added to list: <" + newCandidate + ">");
		calResults.add(newCandidate);
		
		// Always trim the list to only the maximum number of allowed values.
		int calSize = calResults.size();

		// NOTE: it is important that this if-condition stays as '>' and not '>=' because if we keep the list smaller,
		// we will keep scheduling new calibrations, until the list grows to this MAX_CAL_SIZE, which it will never do.
		// The point is, however, NOT to schedule new calibrations, once the list has grown to its full size.
		if (calSize > MAX_CAL_SIZE) {
			log.debug("calibration: Trimming candidate list");
			calResults.remove(0);
		}
		
		log.debug("calibration: candidates currently in the list:");
		for (Double d: calResults) {
			log.debug("	calibration: list member: <" + d + ">");
		}
		
		// Take the maximum or perhaps better take the second or third biggest value,
		// to avoid extreme values.
		double max=0D;
		double max1=0D;
		double max2=0D;
		for (Double d: calResults) { double thisVal = d.doubleValue(); if (thisVal > max) { max = thisVal; } }
		for (Double d: calResults) { double thisVal = d.doubleValue(); if (thisVal > max1 && thisVal < max) { max1 = thisVal; } }
		for (Double d: calResults) { double thisVal = d.doubleValue(); if (thisVal > max2 && thisVal < max1) { max2 = thisVal; } }
		
		// In general we like to take the third highest value, but if there are only three values in the list,
		// this would mean taking the lowest value, and that was probably calculated at the very start of the
		// program, when there was a lot of CPU contention and thus the calibration was calculated lower than it should be.
		// Similarly, we refuse to take the second highest value, if there are only two items in the list.
		if (max2 != 0 && calSize > 3) {
			calibrationValueDynamic.compareAndSet(tmpD, max2);
		} else if (max1 != 0 && calSize > 2) {
			calibrationValueDynamic.compareAndSet(tmpD, max1);
		} else {
			calibrationValueDynamic.compareAndSet(tmpD, max);
		}
		
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		if (EASYTRAVEL_CONFIG.CPUCalibration > 0) {
			log.info("calibration: computed value: <" + calibrationValueDynamic + ">. config override: <" + EASYTRAVEL_CONFIG.CPUCalibration + ">");
		} else {
			log.info("calibration: computed value: <" + calibrationValueDynamic + ">");
		}
	}
	
	private static double calibrate() {
		long start = System.currentTimeMillis();
		BigInteger i = parseSectionInLoop(10000);

		long end = System.currentTimeMillis();
		double ret = i.doubleValue()/(end - start);
		log.info("calibration: Had " + i.longValue() + " cycles after " + (end-start) + " ms => CPU Calibration value is " + ret);

		return ret;
	}

	/**
	 * Causes CPU time to be used according to the given cycles.
	 *
	 * NOTE: The actual runtime of the call is not exactly the milliseconds value, but will be
	 * longer if there are other processes consuming CPU, i.e. there is an initial calibration and
	 * afterwards always the same number of CPU cycles is done
	 *
	 * @param section How much CPU cycles should be done.
	 */
	public static void parseSection(long section) {
		long adjustedCycles = (long)Math.ceil(section * getCalibrationValue());
		parseSectionInLoop(adjustedCycles);
	}

	/**
	 * The calibration value is an arbitrary measurement of CPU speed in order to have the CPUHelper have
	 * similar effect on different hardware.
	 *
	 * Higher values mean faster machines.
	 *
	 *  Note: The calibration takes place during construction of the CPUHelper and is negatively affected if there is
	 *  	high CPU usage on the machine at the same time.
	 *
	 * @return The currently used calibration value
	 */
	public static double getCalibrationValue() {
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		
		if (EASYTRAVEL_CONFIG.CPUCalibration > 0) {
			return EASYTRAVEL_CONFIG.CPUCalibration;
		} else {
			return calibrationValueDynamic.get();
		}
	}

	/**
	 * Use CPU time for the given number of cycles, doing some BigInteger calculation and
	 * calculating fibonacci numbers
	 *
	 * @param cycles The number of cycles to run
	 * @return The resulting BigInteger after adding "1" cycles-times, i.e. the same as the cycles-input
	 */
	private static BigInteger parseSectionInLoop(long cycles) {
		BigInteger i = new BigInteger("0", 10);
		while(i.compareTo(new BigInteger(Long.toString(cycles), 10)) < 0) {
			i = i.add(new BigInteger("1", 10));
			calculateSectionIndex(20);
		}
		return i;
	}

    private static long calculateSectionIndex(int n) {
        if (n <= 1) return n;
        else return calculateSectionIndex(n-1) + calculateSectionIndex(n-2);
    }
}
