package com.dynatrace.diagnostics.uemload.headless;

import java.util.concurrent.atomic.AtomicInteger;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.Visit;

/**
 * Copy of HeadlessAngularRunnable, maybe its functionality should be extracted.
 * @author krzysztof.sajko
 * @Date 2021.10.05
 */
public class HeadlessB2BRunnable extends HeadlessVisitRunnable {
	
	private static AtomicInteger runningVisits = new AtomicInteger(0);
	private static volatile boolean visitGenerationEnabled = false;		// indicates that the UEM>standard simulation has been started
	
	public HeadlessB2BRunnable (Visit visit, ExtendedCommonUser user, Simulator simulator) {
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
