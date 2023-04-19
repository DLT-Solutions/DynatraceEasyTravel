package com.dynatrace.diagnostics.uemload;

import com.dynatrace.diagnostics.uemload.headless.HeadlessB2BRunnable;

/**
 * Simulator for headless b2b frontend based on HeadlessAngularSimulator.
 * @author krzysztof.sajko
 * @Date 2021.10.05
 */
public class HeadlessB2BSimulator extends Simulator {

	public HeadlessB2BSimulator(UEMLoadScenario scenario) {
		super(scenario);
	}

	@Override
	protected void warmUp() throws Exception {}

	@Override
	protected Runnable createActionRunnerForVisit() {
		ExtendedCommonUser user = getUserForVisit();
		Visit visit = getVisitForUser(getLocationForUser(user));
		HeadlessB2BRunnable.startAll();
		return new HeadlessB2BRunnable(visit, user, this);
	}
	
	@Override
	public boolean stop(boolean logging) {
		HeadlessB2BRunnable.stopAll();
		return super.stop(logging);
	}
}
