package com.dynatrace.diagnostics.uemload.headless;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class to represent a set of statistics for a given time period - ie hourly, five minutely, minutely
 *
 * @author Paul.Johnson
 *
 */
public class StatObject {
	long startTimeForThisObject = 0;	// start time for this object - The hour or five minute or minute that this object is for
	int visitsStarted=0;				// count of instances of visits started
	int visitsCompleted=0;				// count of instances of visits completed
	int visitsSkipped=0;				// count of instances of visits skipped (All DriverEntry's in use)
	int visitsExceptions=0;				// count of instances of exceptions - usually because of automation timeout
	long timeStampOfFirstEntry = 0;		// time when this stat was first created (allows us to work out how many minutes old it is for hourly and five minutely stats)

	public StatObject(long startTime) {
		this.startTimeForThisObject = startTime;
		timeStampOfFirstEntry = System.currentTimeMillis();;
		timeStampOfFirstEntry = timeStampOfFirstEntry - (timeStampOfFirstEntry % (1000*60));		// round down to the last minute
	}
	public void incStarted() {
		visitsStarted++;
	}
	public void incCompleted() {
		visitsCompleted++;
	}
	public void incSkipped() {
		visitsSkipped++;
	}
	public void incExceptions() {
		visitsExceptions++;
	}
	/**
	 * prints the current state of the statistics object
	 * @param timespan  - number of minute that this Object represents 1, 5, or 60 perhaps
	 * @return          - log record
	 */
	public String print(int timespan) {

		SimpleDateFormat sdf = new SimpleDateFormat( "MMM dd HH:mm:ss" );
		DecimalFormat df = new DecimalFormat( "#.##");
		String key = sdf.format( new Date(startTimeForThisObject) );
		String msg = key + ": Visits: "
				+ "started [" + visitsStarted +
				"] Completed [" + visitsCompleted +
				"] Skipped [" + visitsSkipped +
				"] Exceptions [" + visitsExceptions +
				"]";
		if (timespan>1) {
			// minutes passed = startTime - which represents the hour the stat started at
			// + 60 minutes - takes us to the next hour
			// - statTimeStamp = the time the object was first created
			//
			String from = sdf.format( new Date(timeStampOfFirstEntry) );
			String to = sdf.format( new Date( startTimeForThisObject + (timespan*60*1000) - (60*1000)));		// subtract a minute as to give span 5 to 9 not 5 to 10
			long secondsPassed = startTimeForThisObject + (timespan*60*1000) - timeStampOfFirstEntry;
			secondsPassed = secondsPassed /1000 ; // minutes spanned by this hour in whole minutes
			long minutesPassed = secondsPassed/60;
			if (minutesPassed>0) {
				double visitsStartedPerMinute = (double)visitsStarted/minutesPassed;
				String visitsPerMin = " Average [" + df.format(visitsStartedPerMinute) + "] started/minute.";
				msg = msg + visitsPerMin;
				msg = msg + " from:[" + from + "] to:[" + to + "]" ;
			}
		}
		return msg;

	}
}
