package com.dynatrace.diagnostics.uemload.headless;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.ActionExecutor;
import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.ExtendedDemoUser;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.headless.HeadlessStatistics.StatisticsType;
import com.dynatrace.diagnostics.uemload.headless.HeadlessVisit.HeadlessGetAction;
import com.dynatrace.easytravel.misc.CommonUser;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD;

/**
 * Test case to exercise creation of headless visits makes sure that when we
 * request for visits the frame work will create them through the ObjectPool
 *
 * @author Paul.Johnson
 *
 */
public class HeadlessVisitRunnableTest {

	// map of visit names vs flag to indicate if they were executed - we use this to
	// test
	// which visits were executed
	private Map<String, String> visitMap = new HashMap<String, String>();

	// These provide us with a dummy server to perform requests against on localhost
	Browser browser;
	MockRESTServer server;

	//pool sizes
	int browserPoolSize;
	int mobileBrowserPoolSize;
	
	// Override the simple HeadlessGetAction action so that we can add code to
	// detect if run is called for each visit
	class DummyHeadlessGetAction extends HeadlessGetAction {

		String visitName = "";

		public DummyHeadlessGetAction(String url, String visitName) {
			super(url);
			this.visitName = visitName;
		}

		@Override
		public void run(ActionExecutor browser, UEMLoadCallback continuation) throws Exception {
			super.run(browser, continuation);
			System.out.println("DummyHeadlessGetAction: executing visit [" + visitName + "]");
			// when the visit is executed check set flag to indicate it was run
			String status = visitMap.get(visitName);
			if (status != null) {
				visitMap.put(visitName, "1");
			} else {
				visitMap.put(visitName, "visitName not found in visitMap"); // should not happen
			}
		}
	}

	// a dummy visit to create for each HeadlessVisitRunnable object created
	class DummyVisit implements Visit {

		String visitName;

		DummyVisit(String visitName) {
			this.visitName = visitName;
		}

		@Override
		public Action[] getActions(CommonUser user, Location location) {
			String url = "http://localhost:" + server.getPort();
			List<Action> actions = new ArrayList<>();
			actions.add(new DummyHeadlessGetAction(url, visitName));
			return actions.toArray(new Action[actions.size()]);
		}

		@Override
		public String getVisitName() {
			return "DummyVisit [" + visitName + "]";
		}
	}

	@Before
	public void setUp() throws Exception {
		HeadlessVisitTestUtil.setup(false);
		HeadlessVisitTestUtil.setup(true);		

		HTTPRunnable runnable = new HTTPRunnable() {
			@Override
			public void run(String uri, String method, Properties header, Properties parms) {
			}
		};
		String html = "<table><tr><td>Hello World</td></tr></table>";
		server = new MockRESTServer(runnable, NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, html);
		
		browserPoolSize = DriverEntryPoolSingleton.getInstance().getPool().getMaxActive();
		mobileBrowserPoolSize = MobileDriverEntryPoolSingleton.getInstance().getPool().getMaxActive();
	}

	@After
	public void Finish() throws Exception {
		server.stop();
		DriverEntryPoolSingleton.getInstance().getPool().stopAll();
		MobileDriverEntryPoolSingleton.getInstance().getPool().stopAll();
		DriverEntryPoolSingleton.getInstance().getPool().setMaxActive(browserPoolSize);
		MobileDriverEntryPoolSingleton.getInstance().getPool().setMaxActive(mobileBrowserPoolSize);
	}

	// with the maximumChromeDrivers set to 10 we should be able to create
	// because default number is 5, we need to update it int the test
	// 7 visits without any being lost
	@Test
	public void testCreatingSevenVisits() throws InterruptedException, IOException {
		DriverEntryPool pool = DriverEntryPoolSingleton.getInstance().getPool(); 
		pool.setMaxActive(10);
		// create 7 visits on separate threads
		for (int cnt = 0; cnt < 7; cnt++) {
			String visitName = "visit-" + cnt;
			visitMap.put(visitName, "0"); // zero flag indicates visit created
			Thread t = new Thread(visitName) {
				@Override
				public void run() {
					System.out.println("creating HeadlessVisitRunnable [" + getName() + "]");
					HeadlessVisitRunnable hvr = createHeadlessVisitRunnable(getName(), false);
					// Run a visit
					hvr.run(); // will wait one second
				}
			};
			t.start();
		}

		int waitTime = 120;
		System.out.println(String.format("Pool Info before loop: Active Objects [%d], Idle Objects [%d]",
				pool.getNumActive(), pool.getNumIdle()));
		int visitsFinished = howManyVisitsAreFinished(null);
		while (--waitTime > 0 && visitsFinished < 7) {
			try {
				Thread.sleep(1000); // one second
				visitsFinished = howManyVisitsAreFinished(null);
			} catch (InterruptedException e) {
				System.out.println("shutDownChromeDrivers: thread sleep interupted.");
				e.printStackTrace();
			}
			System.out.println(String.format("Pool Info [max %d]: Active Objects [%d], Idle Objects [%d], wait [%d], visits finished: %d/7",
					pool.getMaxActive(), pool.getNumActive(), pool.getNumIdle(), waitTime, visitsFinished));
		}
		System.out.println(String.format("Pool Info after loop: Active Objects [%d], Idle Objects [%d]",
				pool.getNumActive(), pool.getNumIdle()));

		System.out.println(String.format("Pool Info-END : Active Objects [%d], Idle Objects [%d]",
				pool.getNumActive(), pool.getNumIdle()));

		assertTrue("Checking if the number of finished visits [" + visitsFinished + "] is equal to 7", 7 == visitsFinished);

		visitMap.clear();
		pool.stopAll();
	}

	@Ignore
	@Test
	public void testCreatingVisitsOnSeperatePools() throws InterruptedException, IOException {
		DriverEntryPool desktopPool = DriverEntryPoolSingleton.getInstance().getPool();
		DriverEntryPool mobilePool = MobileDriverEntryPoolSingleton.getInstance().getPool();

		// create 7 visits in desktop pool
		createVisitThreads("desktop", 7, false);
		int waitTime = 75;
		int visitsFinished = howManyVisitsAreFinished("desktop");
		int visitsCompletedFoundInStats = 0;
		while (--waitTime > 0 && visitsFinished < 7) {
			try {
				Thread.sleep(1000); // one second
				visitsFinished = howManyVisitsAreFinished("desktop");
			} catch (InterruptedException e) {
				System.out.println("shutDownChromeDrivers: thread sleep interupted.");
				e.printStackTrace();
			}

			assertTrue("Checking that mobile pool did not start working",0 == mobilePool.getNumActive() + mobilePool.getNumActive());

			System.out.println(String.format("DESKTOP Pool Info [max %d]: Active Objects [%d], Idle Objects [%d], wait [%d], visits finished: %d/7",
					desktopPool.getMaxActive(), desktopPool.getNumActive(), desktopPool.getNumIdle(), waitTime, visitsFinished));
			System.out.println(String.format("MOBILE Pool Info [max %d]: Active Objects [%d], Idle Objects [%d]",
					mobilePool.getMaxActive(), mobilePool.getNumActive(), mobilePool.getNumIdle()));
		}
		printStats(desktopPool.getStats());
		System.out.println("---DESKTOP test finished---");
		assertTrue("Checking if the number of finished visits [" + visitsFinished + "] is equal to 7", 7 == visitsFinished);
		visitsCompletedFoundInStats = desktopPool.getStats().getStatCount(desktopPool.getStats().getHourlyMap(), StatisticsType.VISIT_COMPLETED);
		assertTrue(String.format("Checking if the number of finished visits is equal to 7 via stats [%d visits found in stats]", visitsCompletedFoundInStats),
				7 == visitsCompletedFoundInStats);
		visitMap.clear();

		// create 10 visits in mobile pool
		createVisitThreads("mobile", 10, true);
		waitTime = 90;
		visitsFinished = howManyVisitsAreFinished("mobile");
		int desktopPoolOldCount = desktopPool.getNumActive() + desktopPool.getNumIdle();
		while (--waitTime > 0 && visitsFinished < 10) {
			try {
				Thread.sleep(1000); // one second
				visitsFinished = howManyVisitsAreFinished("mobile");
			} catch (InterruptedException e) {
				System.out.println("shutDownChromeDrivers: thread sleep interupted.");
				e.printStackTrace();
			}

			assertTrue("Checking that desktop pool did not start working", desktopPoolOldCount >= desktopPool.getNumActive() + desktopPool.getNumActive());

			System.out.println(String.format("MOBILE Pool Info [max %d]: Active Objects [%d], Idle Objects [%d], wait [%d], visits finished: %d/10",
					mobilePool.getMaxActive(), mobilePool.getNumActive(), mobilePool.getNumIdle(), waitTime, visitsFinished));
			System.out.println(String.format("DESKTOP Pool Info [max %d]: Active Objects [%d], Idle Objects [%d]",
					desktopPool.getMaxActive(), desktopPool.getNumActive(), desktopPool.getNumIdle()));
		}
		printStats(mobilePool.getStats());
		System.out.println("---MOBILE test finished---");
		assertTrue("Checking if the number of finished visits [" + visitsFinished + "] is equal to 10", 10 == visitsFinished);
		visitsCompletedFoundInStats = mobilePool.getStats().getStatCount(mobilePool.getStats().getHourlyMap(), StatisticsType.VISIT_COMPLETED);
		assertTrue(String.format("Checking if the number of finished visits is equal to 10 via stats [%d visits found in stats]", visitsCompletedFoundInStats),
				10 == visitsCompletedFoundInStats);
		visitMap.clear();

		// create 7 visits in desktop pool and 8 visits in mobile pool
		createVisitThreads("desktop", 7, false);
		createVisitThreads("mobile", 6, true);
		waitTime = 150;
		visitsFinished = howManyVisitsAreFinished("desktop");
		int visitsFinishedMobile = howManyVisitsAreFinished("mobile");
		while (--waitTime > 0 && (visitsFinished < 7 || visitsFinishedMobile < 6)) {
			try {
				Thread.sleep(1000); // one second
				visitsFinished = howManyVisitsAreFinished("desktop");
				visitsFinishedMobile = howManyVisitsAreFinished("mobile");
			} catch (InterruptedException e) {
				System.out.println("shutDownChromeDrivers: thread sleep interupted.");
				e.printStackTrace();
			}

			System.out.println(String.format("DESKTOP Pool Info [max %d]: Active Objects [%d], Idle Objects [%d], wait [%d], visits finished: %d/7",
					desktopPool.getMaxActive(), desktopPool.getNumActive(), desktopPool.getNumIdle(), waitTime, visitsFinished));
			System.out.println(String.format("MOBILE Pool Info [max %d]: Active Objects [%d], Idle Objects [%d], wait [%d], visits finished: %d/6",
					mobilePool.getMaxActive(), mobilePool.getNumActive(), mobilePool.getNumIdle(), waitTime, visitsFinishedMobile));
		}
		printStats(desktopPool.getStats());
		printStats(mobilePool.getStats());
		System.out.println("---DESKTOP/MOBILE test finished---");
		assertTrue("Checking if the number of finished visits [" + visitsFinished + "] is equal to 7", 7 == visitsFinished);
		visitsCompletedFoundInStats = desktopPool.getStats().getStatCount(desktopPool.getStats().getHourlyMap(), StatisticsType.VISIT_COMPLETED);
		assertTrue(String.format("Checking if the number of finished visits is equal to 7+7 via stats [%d visits found in stats]", visitsCompletedFoundInStats),
				14 == visitsCompletedFoundInStats);
		assertTrue("Checking if the number of finished visits [" + visitsFinishedMobile + "] is equal to 6", 6 == visitsFinishedMobile);
		visitsCompletedFoundInStats = mobilePool.getStats().getStatCount(mobilePool.getStats().getHourlyMap(), StatisticsType.VISIT_COMPLETED);
		assertTrue(String.format("Checking if the number of finished visits is equal to 10+6 via stats [%d visits found in stats]", visitsCompletedFoundInStats),
				16 == visitsCompletedFoundInStats);
		visitMap.clear();

		mobilePool.stopAll();
		desktopPool.stopAll();
	}

	private void printStats(HeadlessStatistics stats) {
		Map<Long, StatObject> hourly = stats.getHourlyMap();
		for (StatObject stat : hourly.values()) {
			System.out.println("HOURLY STATS: " + stat.print(60));
		}

		Map<Long, StatObject> min5 = stats.getFiveMinuteMap();
		for (StatObject stat : min5.values()) {
			System.out.println("5 MINUTES STATS: " + stat.print(5));
		}

		Map<Long, StatObject> min = stats.getMinuteMap();
		for (StatObject stat : min.values()) {
			System.out.println("1 MINUTE STATS: " + stat.print(1));
		}
	}

	private void createVisitThreads(String threadNamePrefix, int threadCount, boolean isMobile) {
		for (int cnt = 0; cnt < threadCount; cnt++) {
			String visitName = String.format("%s-%d", threadNamePrefix, cnt);
			visitMap.put(visitName, "0"); // zero flag indicates visit created
			Thread t = new Thread(visitName) {
				@Override
				public void run() {
					System.out.println("creating HeadlessVisitRunnable [" + getName() + "]");
					HeadlessVisitRunnable hvr = createHeadlessVisitRunnable(getName(), isMobile);
					// Run a visit
					hvr.run(); // will wait one second
				}
			};
			t.start();
		}
	}

	private int howManyVisitsAreFinished(String namePrefix) {
		Iterator<Map.Entry<String, String>> iter = visitMap.entrySet().iterator();
		int visitsFinished = 0;
		while (iter.hasNext()) {
			Entry<String,String> elem = iter.next();
			if (elem.getValue() == "1" && (namePrefix == null || elem.getKey().contains(namePrefix))) {
				visitsFinished++;
			}
		}
		return visitsFinished;
	}

	private HeadlessVisitRunnable createHeadlessVisitRunnable(String visitName, boolean isMobile) {
		DummyVisit dv = new DummyVisit(visitName);
		Simulator simulator = HeadlessVisitTestUtil.getSimulator(isMobile);
		return new HeadlessAngularRunnable(dv, ExtendedDemoUser.DEMOUSER, simulator);
	}
}
