package com.dynatrace.diagnostics.uemload.headless;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.metrics.Metrics;

import ch.qos.logback.classic.Logger;

/**
 * Class to log Headless visit generation statistics
 *
 * @author Paul.Johnson
 *
 */
public class HeadlessStatistics {

    private static final Logger LOGGER = LoggerFactory.make();

    private final Map<Long, StatObject> hourlyMap = new HashMap<Long, StatObject>();
	private final Map<Long, StatObject> fiveMinuteMap = new HashMap<Long, StatObject>();
	private final Map<Long, StatObject> minuteMap = new HashMap<Long, StatObject>();

	private long printHour=0;
	private long printFiveMinute=0;
	private long printMinute=0;

	private boolean showHourLog=true;
	private boolean showFiveMinuteLog=true;
	private boolean showMinuteLog=false;			// for debugging if necessary

	public enum StatisticsType { VISIT_STARTED, VISIT_COMPLETED, VISIT_EXCEPTION, VISIT_SKIPPED }

	/**
	 * Adds a visit started statistic
	 * @param drv - DriverEntry object we have just created and are generating statistics for
	 */
	public void addVisitStarted(DriverEntry drv) {
		// called from synchronized code
		// stats are based on totals over hours, 5 minutes and minute periods
		long hour=0, fiveMinute=0, minute=0;
		synchronized( minuteMap ) {
			long startTime = drv.getStartTime();
			hour = getHour( startTime );
			fiveMinute = getFiveMinute( startTime );
			minute = getMinute( startTime ) ;
			StatObject statObject = getHourObject( hour );
			statObject.incStarted();
			statObject = getFiveMinuteObject( fiveMinute );
			statObject.incStarted();
			statObject = getMinuteObject( minute );
			statObject.incStarted();
			printStats(hour, fiveMinute, minute );
		}
		incMetricStarted();
	}

	/**
	 * Adds a visit complete statistic
	 * @param drv - DriverEntry object that was completed
	 */
	public void addVisitCompleted(DriverEntry drv) {
		// called from synchronized code
		synchronized( minuteMap ) {
			long startTime = drv.getStartTime();
			StatObject statObject = getHourObject( getHour( startTime ) );
			statObject.incCompleted();
			statObject = getFiveMinuteObject( getFiveMinute( startTime ) );
			statObject.incCompleted();
			statObject = getMinuteObject( getMinute( startTime ) );
			statObject.incCompleted();
		}
		incMetricCompleted();
	}

	/**
	 * Adds a visit skipped statistic
	 * this occurs when all the ChromeDrivers are currently active and none available to reuse
	 */
	public void addVisitSkipped() {
		// called from synchronized code
		synchronized( minuteMap ) {
			long startTime = System.currentTimeMillis();
			StatObject statObject = getHourObject( getHour( startTime ) );
			statObject.incSkipped();
			statObject = getFiveMinuteObject( getFiveMinute( startTime ) );
			statObject.incSkipped();
			statObject = getMinuteObject( getMinute( startTime ) );
			statObject.incSkipped();
		}
		incMetricSkipped();
	}

	/**
	 * Adds an exception statistic
	 * This occurs for example if the automation for a specific visit resulted in a (15 second) timeout
	 * the visit gets abandoned
	 * @param drv - DriverEntry record being automated
	 */
	public void addVisitException(DriverEntry drv) {
		// not called from synchronized code
		synchronized( minuteMap ) {
			long startTime = drv.getStartTime();
			StatObject statObject = getHourObject( getHour( startTime ) );
			statObject.incExceptions();
			statObject = getFiveMinuteObject( getFiveMinute( startTime ) );
			statObject.incExceptions();
			statObject = getMinuteObject( getMinute( startTime ) );
			statObject.incExceptions();
		}
		incMetricException();
	}

	protected void incMetricStarted() {
		Metrics.incHeadlessStarted();
	}

	protected void incMetricCompleted() {
		Metrics.incHeadlessCompleted();
	}

	protected void incMetricSkipped() {
		Metrics.incHeadlessSkipped();
	}

	protected void incMetricException() {
		Metrics.incHeadlessException();
	}

	protected String getPartialName() {
		return "DESKTOP";
	}

	/**
	 * @param hour			- current hour record we are on
	 * @param fiveMinute	- current five minute record we are on
	 * @param minute		- cuirrent minute record we are on
	 */
	private void printStats(long hour, long fiveMinute, long minute) {
		// as a started statistic is logged, check to see whether it is time to print out that statistic (ie we are passed the time period)
		if (hour != printHour) {
			printHour = hour;
			printStats( hourlyMap, "***** Visit Generation Statistics " + getPartialName() + " for Every Hour *****", 12 /* over last 12 time periods */, 60 /* timespan of 60 minutes*/, showHourLog );
		}

		if (fiveMinute != printFiveMinute) {
			printFiveMinute = fiveMinute;
			printStats( fiveMinuteMap, "***** Visit Generation Statistics " + getPartialName() + " for Every Five Minutes *****", 8 /* over last 8 time periods*/, 5 /*minutes intervals*/, showFiveMinuteLog);
		}

		if (minute != printMinute) {
			printMinute = minute;
			printStats( minuteMap, "***** Visit Generation Statistics " + getPartialName() + " for Every Minute *****", 20, 1 /*minutes*/, showMinuteLog);		// show last 20 minutes
		}
	}

	/**
	 *
	 * @param map  - statistics map to log statistics for
	 * @param msg  - title of this report
	 * @param maxToPrint - last number of records to print (the rest will be deleted)
	 * @param timespan   - time period between each record - ie 1 minute, 5 minutes, 60 minutes
	 * @param printMap   - prints the records if set to true (otherwise just clears up old records in the map)
	 */
	private void printStats( Map<Long, StatObject> map, String msg, int maxToPrint, int timespan, boolean printMap) {

		// sort the HashMap
		List<StatObject> objects = new ArrayList<StatObject>(map.values());
		Collections.sort( objects,  new Comparator<StatObject>() {
			@Override
			public int compare(StatObject o1, StatObject o2) {
				return ((o1.startTimeForThisObject - o2.startTimeForThisObject) >0) ? -1 : 1 ;
			}

		});

		// print the most recent 'maxToPrint' records and delete the rest from the map
		int cnt = 0;
		if (printMap) {
			LOGGER.info(msg);
		}
		for (StatObject obj : objects) {
			if (cnt > maxToPrint) {
				long key = obj.startTimeForThisObject;
				map.remove(key);
			} else {
				// don't report the first statistic as it will be incomplete - ie the part time period we are currently on
				if (cnt > 0 && printMap) {
					LOGGER.info(obj.print(timespan));
				}
			}
			cnt++;
		}

	}

	/**
	 * For a given time returns the last hour period that the statistic will be logged against
	 * i.e   12:33:09 will be logged against 12:00:00
	 * @param   startTime
	 * @return  the last hour period that the statistic will be logged against
	 */
	private long getHour( long startTime ) {
		return startTime - (startTime % (1000*60*60));			// 12:33:09  => 12:00:00
	}

	/**
	 * For a given time returns the last five minute period that the statistic will be logged against
	 * i.e   12:33:09 will be logged against 12:30:00
	 * @param   startTime
	 * @return  the last five minute period that the statistic will be logged against
	 */
	private long getFiveMinute( long startTime ) {
		return startTime - (startTime % (1000*60*5));			// 12:33:09  => 12:30:00
	}

	/**
	 * For a given time returns the last minute period that the statistic will be logged against
	 * i.e   12:33:09 will be logged against 12:33:00
	 * @param   startTime
	 * @return  the last  minute period that the statistic will be logged against
	 */
	private long getMinute( long startTime ) {
		return startTime - (startTime % (1000*60));				// 12:33:09  => 12:33:00
	}

	/**
	 * Queries the hourly map for a statistics object for the given time period
	 * and creates one if it does not exist
	 * @param hour
	 * @return StatObject to log this statistic against
	 */
	private StatObject getHourObject( long hour ) {
		StatObject statObject = hourlyMap.get( hour );
		if (statObject==null) {
			statObject = new StatObject( hour );
			hourlyMap.put( hour, statObject);
		}
		return statObject;
	}

	/**
	 * Queries the five minute Map for a statistics object for the given time period
	 * and creates one if it does not exist
	 * @param fiveMinute
	 * @return StatObject to log this statistic against
	 */
	private StatObject getFiveMinuteObject( long fiveMinute) {
		StatObject statObject = fiveMinuteMap.get( fiveMinute );
		if (statObject==null) {
			statObject = new StatObject( fiveMinute );
			fiveMinuteMap.put( fiveMinute, statObject);
		}
		return statObject;
	}

	/**
	 * Queries the minute Map for a statistics object for the given time period
	 * and creates one if it does not exist
	 * @param minute
	 * @return StatObject to log this statistic against
	 */
	private StatObject getMinuteObject( long minute ) {
		StatObject statObject = minuteMap.get( minute );
		if (statObject==null) {
			statObject = new StatObject( minute );
			minuteMap.put(minute, statObject );
		}
		return statObject;
	}

	public Map<Long, StatObject>  getHourlyMap( ) {
		return hourlyMap;
	}
	public Map<Long, StatObject>  getFiveMinuteMap( ) {
		return fiveMinuteMap;
	}
	public Map<Long, StatObject>  getMinuteMap( ) {
		return minuteMap;
	}

	public int getStatCount( Map<Long, StatObject> map, StatisticsType statType ) {
		int count = 0;
		synchronized( map ) {
		 	// count all the visits created
			Iterator <Map.Entry<Long, StatObject>> iter = map.entrySet().iterator();
			while(iter.hasNext()) {
				Map.Entry<Long, StatObject> entry = iter.next();
				StatObject statObject = entry.getValue();
				switch ( statType ) {
					case VISIT_STARTED:
						count = count + statObject.visitsStarted;
						break;

					case VISIT_COMPLETED:
						count = count + statObject.visitsCompleted;
						break;

					case VISIT_EXCEPTION:
						count = count + statObject.visitsExceptions;
						break;

					case VISIT_SKIPPED:
						count = count + statObject.visitsSkipped;
						break;
				}
			}
		}
		return count;
	}


}
