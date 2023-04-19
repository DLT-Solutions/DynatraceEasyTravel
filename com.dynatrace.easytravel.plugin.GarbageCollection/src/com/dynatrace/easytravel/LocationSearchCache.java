package com.dynatrace.easytravel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.spring.PluginConstants;

import ch.qos.logback.classic.Logger;

public class LocationSearchCache extends AbstractGenericPlugin {
	private static final Logger log = LoggerFactory.make();
	
	private static final int FREE_MEM_THRESHOLD = 50 * 1024 * 1024;
	private final long gc_delay;
	private final int grow_size;
	private final int max_cache_size;
	
	private volatile long lastGC = 0;
	private volatile boolean cleanupInProgress = false;
	List<Object> array = Collections.synchronizedList(new ArrayList<>());
	
	public LocationSearchCache() {
		EasyTravelConfig config = EasyTravelConfig.read();
			
		gc_delay = TimeUnit.SECONDS.toMillis(config.memoryLeakWithGCDelay);
		grow_size = config.memoryLeakWithGCCacheGrowSize;
		max_cache_size = config.memoryLeakWithGCMaxCacheSize;	
	}
	
	@Override
	protected Object doExecute(String location, Object... context) {		
		if (location.equals(PluginConstants.LIFECYCLE_PLUGIN_DISABLE)) {
			cleanupTable();
		} else if (shouldCleanupTable()) {
			cleanupTable();
		} else if (canCreateMoreObjects()) {
			createTable();
		}
		return null;
	}
	
	private void cleanupTable() {
		cleanupInProgress = true;
		long startTime = System.currentTimeMillis();
		log.debug("cleanup table start");
		synchronized(array) {
			for (int i=0; i<array.size(); i++) {
				array.set(i, null);
				System.gc();
			}
			array.clear();
			System.gc();
		}		
		lastGC= System.currentTimeMillis();
		printMemory();
		log.debug("cleanup table stop, took {}", System.currentTimeMillis() - startTime);
		cleanupInProgress = false;
	}
	
	private void createTable() {
		printMemory();
		try {			
			for( int i=0;i<grow_size;i++) {
				if (!canCreateMoreObjects()) {
					log.debug ("Memory exhaused, no more objects will be created");
					break;
				}
				Entry obj = new Entry(array, array.size());
				array.add(obj);				
			}
			log.trace("array size after creation " + array.size());
		} catch (OutOfMemoryError e) {
			log.warn("OutOfMemoryError occured in MemoryLeaakWithGc plugin ", e);
		} finally {
			printMemory();
		}
	}
	
	private boolean shouldCleanupTable() {
		long timeFromLastGC = System.currentTimeMillis() - lastGC;
		log.debug("Time to next GC {} ms (last GC time: {})", gc_delay - timeFromLastGC, lastGC);
		return timeFromLastGC > gc_delay;
	}
	
	private boolean canCreateMoreObjects() {
		return !cleanupInProgress && getTotalFreeMemeory() > FREE_MEM_THRESHOLD && array.size() < max_cache_size;
	}
	
	private void printMemory() {
		if (log.isDebugEnabled()) {
			long freeMemory = Runtime.getRuntime().freeMemory();
			long totalMemory = Runtime.getRuntime().totalMemory();
			long maxMemory = Runtime.getRuntime().maxMemory();		
			log.debug("freeMem {} ({}), threshold {}, totalMem {}, maxMem {} ({})", getTotalFreeMemeory(), freeMemory, FREE_MEM_THRESHOLD, totalMemory, maxMemory, getFreeMemoryPercent());
		}
	}
	
	private double getFreeMemoryPercent() {
		return (double)getTotalFreeMemeory()*100/Runtime.getRuntime().maxMemory();
	}
	
	private long getTotalFreeMemeory() {
		return Runtime.getRuntime().freeMemory() + (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory());
	}
		
	private class Entry {
		private final List<Object> fields = new ArrayList<>();
		
		public Entry(List<Object> objects, int idx) {
			int mod = idx % 2;
			synchronized (objects) {
				for(int i=mod; i<objects.size(); i+=2) {
					fields.add(objects.get(i));
				}
			}
		}
	}
}
