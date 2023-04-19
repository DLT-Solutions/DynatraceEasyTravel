package com.dynatrace.easytravel.util;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class provides JVM heap related utility methods.
 * 
 * @author stefan.moschinski
 */
public class MemoryUtils {

	private static final Logger log = Logger.getLogger(MemoryUtils.class.getName());

	private static final long NO_HEAP_LIMIT = -1;

	private final MemoryMXBean memoryBean;

	/**
	 *
	 * @param memoryBean defines which {@link MemoryMXBean} is to use
	 * @author stefan.moschinski
	 */
	public MemoryUtils(MemoryMXBean memoryBean) {
		this.memoryBean = memoryBean;
	}


	/**
	 * 
	 * @return The current heap usage as a decimal number between <b>0.0</b> and <b>1.0</b>.
	 *         That is, if the returned value is 0.85, 85% of the max heap is used.
	 * 
	 *         If no max heap is set, the method returns <b>-1.0</b>.
	 * 
	 * @author stefan.moschinski
	 */
	public double getHeapUsage() {
		MemoryUsage heapProps = memoryBean.getHeapMemoryUsage();
		long heapUsed = heapProps.getUsed();
		long heapMax = heapProps.getMax();

		if (heapMax == NO_HEAP_LIMIT) {
			if (log.isLoggable(Level.FINE)) {
				log.fine("No maximum heap is set");
			}
			return NO_HEAP_LIMIT;
		}


		double heapUsage = (double) heapUsed / heapMax;
		if (log.isLoggable(Level.FINEST)) {
			log.finest(TextUtils.merge("Current heap usage is {0} percent", heapUsage * 100));
		}
		return heapUsage;
	}

	/**
	 *
	 * see {@link MemoryMXBean#getObjectPendingFinalizationCount()}
	 *
	 * @author stefan.moschinski
	 */
	public int getObjectPendingFinalizationCount() {
		return memoryBean.getObjectPendingFinalizationCount();
	}
}
