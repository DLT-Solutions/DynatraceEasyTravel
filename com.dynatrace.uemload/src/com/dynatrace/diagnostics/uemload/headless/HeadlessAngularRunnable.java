package com.dynatrace.diagnostics.uemload.headless;

import java.util.concurrent.atomic.AtomicInteger;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.Visit;

/**
 * @author Rafal.Psciuk
 * @Date 2018.11.05
 */
public class HeadlessAngularRunnable extends HeadlessVisitRunnable {

	private static AtomicInteger runningVisits = new AtomicInteger(0);
	private static volatile boolean visitGenerationEnabled = false;		// indicates that the UEM>standard simulation has been started

	public HeadlessAngularRunnable (Visit visit, ExtendedCommonUser user, Simulator simulator) {
		super(visit, user, simulator);
	}

	public static void startAll() {
		visitGenerationEnabled = true;
	}

	public static void stopAll() {
		visitGenerationEnabled = false;
	}

	public static boolean isVisitGenerationEnabled() {
		return visitGenerationEnabled;
	}


	@Override
	boolean simulatorVisitsGenerationEnabled() {
		return isVisitGenerationEnabled();
	}

	@Override
	protected int incrementAndGetRunningVisits() {
		return runningVisits.incrementAndGet();
	}

	@Override
	protected int decrementAndGetRunningVisits() {
		return runningVisits.decrementAndGet();
	}

	@Override
	protected int getRunningVisits() {
		return runningVisits.get();
	}
}
