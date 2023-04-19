package com.dynatrace.easytravel.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;


public class TimeManager {

	private static Map<Class<?>, TimeManager> mapping = new HashMap<Class<?>, TimeManager>();
	private static AtomicLong currentId = new AtomicLong();
	private static final Logger log = LoggerFactory.make();

	private long maxTime;
	private ConcurrentMap<Long, Long> startTimes = new ConcurrentHashMap<Long, Long>();
	private List<Long> runTimes = new ArrayList<Long>();
	private AtomicBoolean contention = new AtomicBoolean();
	private double threshold;

	private TimeManager(long maxTime, double threshold) {
		this.maxTime = maxTime;
		this.threshold = threshold;
	}

	public static synchronized TimeManager initialize(Class<?> clazz, long maxTime, double threshold) {
		if (mapping.containsKey(clazz)) {
			return mapping.get(clazz);
		}
		TimeManager manager = new TimeManager(maxTime, threshold);
		mapping.put(clazz, manager);
		return manager;
	}

	public long start() {
		long id = currentId.incrementAndGet();
		startTimes.put(id, System.currentTimeMillis());
		return id;
	}

	public void stop(long id) {
		synchronized (runTimes) {
			if (startTimes.get(id) != null) {
				runTimes.add(System.currentTimeMillis() - startTimes.get(id));
				startTimes.remove(id);
			}
		}
		refreshContention();
	}

	private void refreshContention() {
		if(runTimes.size() < 3) {
			return;
		}

		long totalTime = 0;
		ArrayList<Long> times = new ArrayList<Long>(runTimes);
		for (int i = 0; i < times.size(); i++) {
			totalTime += times.get(i);
		}
		int average = Math.round(totalTime / times.size());
		log.info("Average was: " + average + "; maximum threshold is: " + getThreshold());
		synchronized (runTimes) {
			if (runTimes.size() >= times.size()) {
				if (average >= getThreshold()) {
					contention.set(true);
				} else {
					contention.set(false);
					runTimes.clear();
				}
			}
		}
	}

	public static void clearInstances() {
		mapping.clear();
	}

	public boolean isContended() {
		return contention.get();
	}

	private long getThreshold() {
		return (long) (threshold * maxTime);
	}

}
