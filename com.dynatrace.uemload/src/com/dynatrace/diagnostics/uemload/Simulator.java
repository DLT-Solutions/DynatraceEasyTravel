/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: Simulator.java
 * @date: 20.01.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelLauncherScenario;
import com.dynatrace.diagnostics.uemload.scenarios.HeadlessAngularScenario;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.metrics.Metrics;
import com.dynatrace.easytravel.util.SpecialUserData;
import com.dynatrace.easytravel.util.SpecialUserDataRow;

import ch.qos.logback.classic.Logger;


public abstract class Simulator {
	private static final Logger LOGGER = LoggerFactory.make();

	protected static /* final */ int THINK_TIME = 3000;	// not final to adjust it for testing
	protected static final int VISIT_SCHEDULE_INTERVAL = 60;

	static {
		System.setProperty("sun.net.client.defaultConnectTimeout", "30000");
		System.setProperty("sun.net.client.defaultReadTimeout", "30000");
	}

	private final Object LOCK = new Object();

	private final ReentrantReadWriteLock SCENARIO_LOCK = new ReentrantReadWriteLock();
	private final Lock SCENARIO_READ_LOCK = SCENARIO_LOCK.readLock();
	private final Lock SCENARIO_WRITE_LOCK = SCENARIO_LOCK.writeLock();
	//@GuardedBy(value = "SCENARIO_LOCK")
	private UEMLoadScenario scenario;

	private AtomicInteger finishedVisits = new AtomicInteger();

	private ScheduledFuture<?> currentVisitScheduler;

	private List<ScheduledFuture<?>> currentVisits;
	public boolean loggingActivated;


	public Simulator(UEMLoadScenario scenario) {
		scenario.init();
		this.scenario = scenario;
	}

	/*------- Abstract Methods ------------------------------------------------------*/

	abstract protected void warmUp() throws Exception;

	abstract protected Runnable createActionRunnerForVisit();

	/*------- base implementation ---------------------------------------------------*/
	public UEMLoadScenario getScenario() {
		SCENARIO_READ_LOCK.lock();
		try {
			return scenario;
		} finally {
			SCENARIO_READ_LOCK.unlock();
		}
	}

	private String getAdaptedVisitString(int selected) {
		return (selected > 1) ? "visits" : "visit";
	}

	public final void incNumberOfFinishedVisits() {
		finishedVisits.incrementAndGet();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getScenario().getName() + ": visitcount: " + finishedVisits.get());
		}
	}

	public void runSync(int count, int pause) throws Exception {
		for (int i = 0; i < count; i++) {
			warmUp();
			Thread.sleep(pause);
		}
	}

	public boolean stop() {
		return stop(loggingActivated);
	}

	public boolean stop(boolean logging) {
		if (logging) {
			LOGGER.info(getScenario().getName() + ": Stopped simulation");
		}

		if (currentVisitScheduler != null && !currentVisitScheduler.isDone()) {
			currentVisitScheduler.cancel(false);
		}
		synchronized (LOCK) {
			return ScheduledFutureUtil.stop(currentVisits);
		}
	}

	public AtomicInteger getFinishedVisits() {
		return finishedVisits;
	}


	public void run(Series numVisitsWalk, boolean performWarmup, boolean loggingActivated) {
		try {
			this.loggingActivated = loggingActivated;
			if (performWarmup) {
				performSafeWarmup();
			}
			if (currentVisitScheduler != null) {
				currentVisitScheduler.cancel(false);
				synchronized (LOCK) {
					if (currentVisits != null) {
						for (ScheduledFuture<?> scheduledFuture : currentVisits) {
							scheduledFuture.cancel(false);
						}
					}
				}
			}
			currentVisitScheduler = UemLoadScheduler.scheduleAtFixedRate(new VisitScheduler(numVisitsWalk), 0, VISIT_SCHEDULE_INTERVAL,
					TimeUnit.SECONDS);
		} catch (Exception e) {
			LOGGER.warn(e.getMessage(), e);
		}
	}

	private void performSafeWarmup() {
		try {
			warmUp();
		}
		catch (Exception e) {
			LOGGER.error("Warmup failed in simulator.", e);
		}
	}

	public void runNow(Series numVisitsWalk) {
		try {
			currentVisitScheduler = UemLoadScheduler.schedule(new ImmediateVisitScheduler(numVisitsWalk), 0, TimeUnit.SECONDS);
		} catch (Exception e) {
			LOGGER.warn(e.getMessage(), e);
		}
	}

	public void runOnce(int numVisits) {
		runOnceSkipShutdown(numVisits);
		shutdownUemLoadScheduler();
	}
	
	public void runOnceSkipShutdown(int numVisits) {
		UemLoadScheduler.schedule(new VisitScheduler(new LinearSeries(numVisits, 0)), 0, TimeUnit.SECONDS);
		while (finishedVisits.get() < numVisits) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}
	
	public void shutdownUemLoadScheduler() {
		UemLoadScheduler.shutdown(10, TimeUnit.SECONDS);
	}

	public void updateScenario(UEMLoadScenario newScenario) {
		SCENARIO_WRITE_LOCK.lock();
		try {
			this.scenario = newScenario;
		} finally {
			SCENARIO_WRITE_LOCK.unlock();
		}
	}

	private class VisitScheduler implements Runnable {

		protected final Series numVisitsWalk;

		public VisitScheduler(Series numVisitsWalk) {
			this.numVisitsWalk = numVisitsWalk;
		}

		@Override
		public void run() {
			try {
				int numVisits = (int) numVisitsWalk.next();
				if (loggingActivated) {
					String visitString = getAdaptedVisitString(numVisits);
					LOGGER.info(getScenario().getName() + ": Scheduling " + numVisits + " new " + visitString + " within next " +
							VISIT_SCHEDULE_INTERVAL + " seconds");

					if (getScenario() instanceof EasyTravelLauncherScenario) {
						EasyTravelLauncherScenario scenario = (EasyTravelLauncherScenario) getScenario();
						LOGGER.trace("Scenario " + getScenario().getName() + " running using hosts: " + scenario.getHostsManager().getAllHostsAsString());
					}
				}
				synchronized (LOCK) {
					List<ScheduledFuture<?>> futures = new ArrayList<ScheduledFuture<?>>(numVisits);
					scenario.setLoad(numVisits);
					for (int i = 0; i < numVisits; i++) {
						ScheduledFuture<?> visit = scheduleRandomVisit(UemLoadUtils.randomInt(VISIT_SCHEDULE_INTERVAL * 1000));
						if(visit != null) {
							futures.add(visit);
						}
					}
					currentVisits = futures;
				}
			} catch (Throwable e) {		// NOSONAR - on purpose here to report all problems that happen in the Runnable
				LOGGER.warn(e.getMessage(), e);
			}
		}

		protected ScheduledFuture<?> scheduleRandomVisit(int delay) {
			// also inform the Metrics of the new visit that is generated
			Metrics.incVisitCount();

			return UemLoadScheduler.schedule(createActionRunnerForVisit(), delay, TimeUnit.MILLISECONDS);
		}

	}

	/**
	 * Scheduler for immediate scheduling of desired number of Visits.
	 *
	 */
	private class ImmediateVisitScheduler extends VisitScheduler {

		public ImmediateVisitScheduler(Series numVisitsWalk) {
			super(numVisitsWalk);
		}

		@Override
		public void run() {
			try {
				int numVisits = (int) numVisitsWalk.next();
				String visitString = getAdaptedVisitString(numVisits);
				LOGGER.info(getScenario().getName() + ": Generate " + numVisits + " new " + visitString);
				synchronized (LOCK) {
					List<ScheduledFuture<?>> futures = new ArrayList<ScheduledFuture<?>>(numVisits);
					for (int i = 0; i < numVisits; i++) {
						ScheduledFuture<?> visit = scheduleRandomVisit(0);
						if(visit != null) {
							futures.add(visit);
						}
					}
					currentVisits = futures;
				}
			} catch (Throwable e) {		// NOSONAR - on purpose here to report all problems that happen in the Runnable
				LOGGER.warn(e.getMessage(), e);
			}
		}
	}

	/**
	 * @author Michal.Bakula
	 */
	protected ExtendedCommonUser getUserForVisit() {
		Location location = getScenario().getRandomLocation();
		if (!location.isUser()) {
			ExtendedCommonUser.ExtendedCommonUserBuilder userBuilder;
			RandomSet<BrowserType> browsers = new RandomSet<>();
			if (location.isRuxitSynthetic() || location.isSynthetic()) {
				browsers.add((location.isRuxitSynthetic()) ? BrowserType.RUXIT_SYNTHETIC
						: getScenario().getRandomSyntheticBrowser(), 1);
				userBuilder = new ExtendedCommonUser.ExtendedCommonUserBuilder("synthetic", null, null, "synthetic", 1)
						.setDesktopBrowsers(browsers);
			} else {
				browsers.add(getScenario().getRandomRobotBrowser(), 1);
				userBuilder = new ExtendedCommonUser.ExtendedCommonUserBuilder("", null, null, "", 1)
						.setDesktopBrowsers(browsers);
			}
			return userBuilder.setLocation(location).setBandwidth(getScenario().getRandomBandwidth(location))
					.setDesktopBrowserWindowSize(getScenario().getRandomBrowserWindowSize()).setDnsSlowdow(1).build();
		}
		return getScenario().getRandomUser(location.getCountry()); 
	}

	/**
	 * @author Michal.Bakula
	 */
	protected Location getLocationForUser(ExtendedCommonUser user) {
		return (user.getLocation() != null) ? user.getLocation() : getScenario().getRandomLocation();
	}

	/**
	 * @author Michal.Bakula
	 */
	protected Visit getVisitForUser(Location location) {
		if(location.isRobot()) {
			return getScenario().getRandomAnonymousVisit();
		}
		return getScenario().getRandomVisit(location);
	}
}
