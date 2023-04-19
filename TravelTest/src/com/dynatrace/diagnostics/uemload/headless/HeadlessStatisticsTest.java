package com.dynatrace.diagnostics.uemload.headless;

import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.logging.Logger;

import org.junit.Test;

import com.dynatrace.diagnostics.uemload.headless.HeadlessStatistics.StatisticsType;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.AbstractUEMLoadSessionTest;

public class HeadlessStatisticsTest {

	private static final Logger LOGGER = Logger.getLogger(AbstractUEMLoadSessionTest.class.getName());

	public class Statistic implements Runnable {

		private StatisticsType statType;
		private String ipAddress;
		private HeadlessStatistics stats;
		public Statistic( StatisticsType statType, String ipAddress, HeadlessStatistics stats) {
			this.statType = statType;
			this.ipAddress = ipAddress;
			this.stats = stats;
		}
		@Override
		public void run() {
			DriverEntry driverEntry = new DriverEntry( ipAddress );
			switch ( statType ) {
				case VISIT_STARTED:
					stats.addVisitStarted(driverEntry);
					break;

				case VISIT_COMPLETED:
					stats.addVisitCompleted(driverEntry);
					break;

				case VISIT_EXCEPTION:
					stats.addVisitException(driverEntry);
					break;

				case VISIT_SKIPPED:
					stats.addVisitSkipped();
					break;
			}
		}
	}

    private boolean WaitForStats(HeadlessStatistics stats, Map<Long, StatObject> map, StatisticsType statType, int waitCount, String msg ) {
		// wait for max 2 minutes for all visits to have started
		int maxWait = 120;			// max loop of 2 minute - we should be done by then
		while ( (--maxWait > 0) &&
				(stats.getStatCount( map, statType) != waitCount)
				) {
			try {
				Thread.sleep(1000);			// one second
			} catch (InterruptedException e) {
				LOGGER.info( "WaitForStats: thread sleep interupted."  );
				e.printStackTrace();
			}
			LOGGER.info( msg + "Current stat [" + stats.getStatCount( map, statType) + "], wait [" + maxWait + "]"  );
		}
		LOGGER.info( msg + "Current stat [" + stats.getStatCount( map, statType) + "], wait [" + maxWait + "]"  );
    	return (maxWait>1);
    }

	@Test
	public void testStatistics( ) throws InterruptedException {
		HeadlessStatistics stats = new HeadlessStatistics();

		Map<Long, StatObject> hourlyMap = stats.getHourlyMap();
		Map<Long, StatObject> FiveMinuteMap = stats.getFiveMinuteMap();
		Map<Long, StatObject> MinuteMap = stats.getMinuteMap();

		// Generate each of the four stats
		//
		for (int x = 0 ; x < 15 ; x++ ) {
			Thread t = new Thread( new Statistic( StatisticsType.VISIT_STARTED, "192.168.0." + Integer.toString(x), stats) );
			t.start();
		}
		boolean waitResult = WaitForStats(stats, hourlyMap, StatisticsType.VISIT_STARTED, 15, "Waiting for 15 visits to start in hourly map. ");
		assertTrue( "Failed to START 15 visits " , waitResult );

		// first 10 complete
		for (int x = 0 ; x < 10 ; x++ ) {
			Thread t = new Thread( new Statistic( StatisticsType.VISIT_COMPLETED, "192.168.0." + Integer.toString(x),stats) );
			t.start();
		}
		waitResult = WaitForStats(stats, hourlyMap, StatisticsType.VISIT_COMPLETED, 10, "Waiting for first 10 visits to complete in hourly map. ");
		assertTrue( "Failed to COMPLETE 10 visits " , waitResult );

		// next 2 skip  - 10 and 11
		for (int x = 10 ; x < 12 ; x++ ) {
			Thread t = new Thread( new Statistic( StatisticsType.VISIT_SKIPPED, "192.168.0." + Integer.toString(x), stats) );
			t.start();
		}
		waitResult = WaitForStats(stats, hourlyMap, StatisticsType.VISIT_SKIPPED, 2, "Waiting for 10 and 11 visits to skip in hourly map. ");
		assertTrue( "Failed to SKIP visit 10 and 11 " , waitResult );

		// last 3 exception 12, 13, 14
		for (int x = 12 ; x < 15 ; x++ ) {
			Thread t = new Thread( new Statistic( StatisticsType.VISIT_EXCEPTION, "192.168.0." + Integer.toString(x), stats) );
			t.start();
		}
		waitResult = WaitForStats(stats, hourlyMap, StatisticsType.VISIT_EXCEPTION, 3, "Waiting for first 10 visits to complete in hourly map. ");
		assertTrue( "Failed to VISIT_EXCEPTION 3 visits " , waitResult );

		// now check all the stats
		int count = 0;
		// Hourly ============================================================
		count = stats.getStatCount( hourlyMap, StatisticsType.VISIT_STARTED );
		assertTrue( "HourlyMap STARTED should be 15 [" + count + "]" , count==15 ) ;

		count = stats.getStatCount( hourlyMap, StatisticsType.VISIT_COMPLETED );
		assertTrue( "HourlyMap COMPLETED should be 10 [" + count + "]" , count==10 ) ;

		count = stats.getStatCount( hourlyMap, StatisticsType.VISIT_SKIPPED );
		assertTrue( "HourlyMap SKIPPED should be 2 [" + count + "]" , count==2 ) ;

		count = stats.getStatCount( hourlyMap, StatisticsType.VISIT_EXCEPTION );
		assertTrue( "HourlyMap EXCEPTION should be 3 [" + count + "]" , count==3 ) ;

		// Five minutely ============================================================
		count = stats.getStatCount( FiveMinuteMap, StatisticsType.VISIT_STARTED );
		assertTrue( "FiveMinuteMap STARTED should be 15 [" + count + "]" , count==15 ) ;

		count = stats.getStatCount( FiveMinuteMap, StatisticsType.VISIT_COMPLETED );
		assertTrue( "FiveMinuteMap COMPLETED should be 10 [" + count + "]" , count==10 ) ;

		count = stats.getStatCount( FiveMinuteMap, StatisticsType.VISIT_SKIPPED );
		assertTrue( "FiveMinuteMap SKIPPED should be 2 [" + count + "]" , count==2 ) ;

		count = stats.getStatCount( FiveMinuteMap, StatisticsType.VISIT_EXCEPTION );
		assertTrue( "FiveMinuteMap EXCEPTION should be 3 [" + count + "]" , count==3 ) ;

		// minutely ============================================================
		count = stats.getStatCount( MinuteMap, StatisticsType.VISIT_STARTED );
		assertTrue( "MinuteMap Started should be 15 [" + count + "]" , count==15 ) ;

		count = stats.getStatCount( MinuteMap, StatisticsType.VISIT_COMPLETED );
		assertTrue( "MinuteMap Started should be 10 [" + count + "]" , count==10 ) ;

		count = stats.getStatCount( MinuteMap, StatisticsType.VISIT_SKIPPED );
		assertTrue( "MinuteMap Started should be 2 [" + count + "]" , count==2 ) ;

		count = stats.getStatCount( MinuteMap, StatisticsType.VISIT_EXCEPTION );
		assertTrue( "MinuteMap Started should be 3 [" + count + "]" , count==3 ) ;
	}




}
