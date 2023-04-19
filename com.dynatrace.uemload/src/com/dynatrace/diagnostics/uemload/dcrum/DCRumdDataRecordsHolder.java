package com.dynatrace.diagnostics.uemload.dcrum;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.RandomUtils;

import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;


/**
 * This class holding the data records, until their are accessed or a threshold ({@link #maxEntryNumber}) is reached
 *
 * @author stefan.moschinski
 */
public class DCRumdDataRecordsHolder {

	private static final Logger log = Logger.getLogger(DCRumdDataRecordsHolder.class.getName());

	private static final int MIN_DELAY_SEC = 13;

	private int maxEntryNumber = EasyTravelConfig.read().dcRumMaxDataRecords;
	private int recordsToClean = (int) Math.round(maxEntryNumber * 0.25);

	private volatile static DCRumdDataRecordsHolder singleton;
	private ConcurrentNavigableMap<Integer, DCRumDataRecord> dcRumRecords;
	private AtomicInteger counter = new AtomicInteger();
	private long interval = System.currentTimeMillis();
	private AtomicBoolean cleanUpRunning = new AtomicBoolean();

	private static final Object lock = new Object();
	private final OldPaths oldPaths;


	public static DCRumdDataRecordsHolder getInstance() {
		if (singleton == null) {
			synchronized (lock) {
				if (singleton == null) {
					singleton = new DCRumdDataRecordsHolder();
					if (log.isLoggable(Level.FINE)) {
						log.fine(TextUtils.merge("New instance of the class ''{0}'' created",
								DCRumdDataRecordsHolder.class.getName()));
					}
				}
			}
		}
		return singleton;
	}

	/**
	 * ATTENTION: Not for use in production, use the {@link #getInstance()} method instead
	 *
	 * @param maxEntryNumber
	 * @param noToClean
	 */
	@TestOnly
	DCRumdDataRecordsHolder(int maxEntryNumber, int noToClean) {
		this();
		this.maxEntryNumber = maxEntryNumber;
		this.recordsToClean = noToClean;
	}

	private DCRumdDataRecordsHolder() {
		dcRumRecords = new ConcurrentSkipListMap<Integer, DCRumDataRecord>();
		this.oldPaths = new OldPaths(MIN_DELAY_SEC);
	}

	// TODO@(stefan.moschinski): paralyze it?
	Collection<String> getDataRecords() {
		List<String> list = new ArrayList<String>();

		DCRumDataRecordBuilder recBuilder = new DCRumDataRecordBuilder();
		for (Entry<Integer, DCRumDataRecord> entry : dcRumRecords.entrySet()) {
			// add current path for later use in another DC-RUM dimension
			oldPaths.addPath(entry.getValue().getPPID());

			list.add(recBuilder.build(interval, entry.getValue(), oldPaths.tryToGetPaths()));
			dcRumRecords.remove(entry.getKey());
		}
		return list;
	}

	public DCRumDataRecord addEntry(String url, String ip, String user) {
		return addEntry(new DCRumDataRecord(url, ip, user));
	}

	DCRumDataRecord addEntry(DCRumDataRecord record) {
		cleanUpIfNecessary();
		dcRumRecords.put(counter.getAndIncrement(), record);
		return record;
	}

	void cleanUpIfNecessary() {
		if (dcRumRecords.size() >= maxEntryNumber &&
				/* only one cleanup at a time: */cleanUpRunning.compareAndSet(false, true)) {
			try {
				Integer firstEntryToDel = dcRumRecords.firstKey();
				Integer lastEntryToDel = firstEntryToDel + recordsToClean;
				erase(firstEntryToDel, lastEntryToDel);
			} finally {
				cleanUpRunning.set(false);
			}
		}
	}

	private void erase(Integer from, Integer to) {
		Iterator<Integer> it = new HashSet<Integer>(dcRumRecords.keySet().subSet(from, to)).iterator();
		while (it.hasNext()) {
			dcRumRecords.remove(it.next());
		}
		log.info(TextUtils.merge("Clean up of DCRum data records was necessary, deleted {0} records", recordsToClean));
	}

	@TestOnly
	void setCleanUpProperties(int maxEntryNo, int noToClean) {
		this.maxEntryNumber = maxEntryNo;
		this.recordsToClean = noToClean;
	}

	@TestOnly
	Map<Integer, DCRumDataRecord> getRecordsMap() {
		return dcRumRecords;
	}


	private static class OldPaths {

		private final Deque<Entry<Long, String>> delayes;
		private final long minDelaySec;

		OldPaths(long minDelaySec) {
			this.delayes = Queues.newArrayDeque();
			this.minDelaySec = minDelaySec;
		}

		Collection<String> tryToGetPaths() {
			int noToCreate = RandomUtils.nextInt(0, 4) + 1;
			Set<String> olds = Sets.newHashSetWithExpectedSize(noToCreate);

			for (int i = 0; !delayes.isEmpty() && i < noToCreate; i++) {
				if (System.currentTimeMillis() > delayes.peek().getKey() + TimeUnit.SECONDS.toMillis(minDelaySec)) {
					olds.add(delayes.poll().getValue());
					continue;
				}

				// if the condition is not true, the entry is not polled... further checks would make no sense
				return olds;
			}

			return olds;
		}

		void addPath(String oldPath) {
			if (oldPath == null) {
				if (log.isLoggable(Level.FINE)) {
					log.fine("Ignore path, because it is null");
				}
				return;
			}

			delayes.add(new AbstractMap.SimpleEntry<Long, String>(System.currentTimeMillis(), oldPath));
		}

	}
}
