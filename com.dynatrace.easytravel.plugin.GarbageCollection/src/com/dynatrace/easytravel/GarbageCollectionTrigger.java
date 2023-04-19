package com.dynatrace.easytravel;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.util.MemoryUtils;

import ch.qos.logback.classic.Logger;


public class GarbageCollectionTrigger extends AbstractGenericPlugin {
	private static final Logger log = LoggerFactory.make();

	// how long to wait after an OOM was caused before we trigger another
	private final long gc_delay;
	private final int finalize_delay;
	private final int maxObjects;

	private long lastGC = 0;

	private final MemoryUtils memUtils;

	public GarbageCollectionTrigger() {
		memUtils = new MemoryUtils(ManagementFactory.getMemoryMXBean());
		EasyTravelConfig config = EasyTravelConfig.read();
		gc_delay = TimeUnit.SECONDS.toMillis(config.garbageCollectionTriggerGCDelay);
		finalize_delay = config.garbageCollectionTriggerFinalizeDelay;
		maxObjects = config.garbageCollectionTriggerMaxObjects;
	}

	@Override
	public Object doExecute(String location, Object... context) {
		if (System.currentTimeMillis() - lastGC > gc_delay) {
			log.debug("Triggering a manual garbage collection, next one in " + (gc_delay/1000) + " seconds.");
			// clear old data, we want to clear old Entry objects, because their finalization is expensive
			System.gc();
			System.gc();

			long total = 0;
			for (int i = 0; i < 10; i++) {
				for (; memUtils.getHeapUsage() < 0.9 && memUtils.getObjectPendingFinalizationCount() < maxObjects;) {
					new Entry();
				}
				long start = System.currentTimeMillis();
				System.gc();
				total += System.currentTimeMillis() - start;
			}
			log.debug("The artificially triggered GCs took: " + total + " ms");
			lastGC = System.currentTimeMillis();
		}

		return null;
	}

	private class Entry {

		@SuppressWarnings("deprecation")
		@Override
		protected void finalize() throws Throwable {
			if (isEnabled()) { // only delay finalization if plugin is active
				TimeUnit.MILLISECONDS.sleep(finalize_delay);
			}
			super.finalize();
		}

	}
}
