package com.dynatrace.diagnostics.uemload;

import com.dynatrace.diagnostics.uemload.headless.HeadlessAngularConvertedVisit;
import com.dynatrace.diagnostics.uemload.headless.HeadlessAngularRunnable;
import com.dynatrace.diagnostics.uemload.headless.HeadlessAngularSpecialOffersConvertVisit;
import com.dynatrace.diagnostics.uemload.scenarios.HeadlessAngularScenarioBase;

/**
 * @author Rafal.Psciuk
 * @Date 2018.11.05
 */
public class HeadlessAngularSimulator extends Simulator {

	public HeadlessAngularSimulator(UEMLoadScenario scenario) {
		super(scenario);
	}

	@Override
	protected void warmUp() throws Exception { /* not needed */ }

	@Override
	protected Runnable createActionRunnerForVisit() {
		ExtendedCommonUser user = getUserForVisit();
		HeadlessAngularRunnable.startAll();
		return new HeadlessAngularRunnable(getVisit(user), user, this);
	}
	
	private Visit getVisit(ExtendedCommonUser user) {
		if (user.isSpecialMonthlyUser() == false && user.isSpecialWeeklyUser() == false) {
			return getVisitForUser(getLocationForUser(user));
		}
		else {
			return getSpecialUserVisit(user);
		}
	}
	
	private Visit getSpecialUserVisit(ExtendedCommonUser user) {
		Visit visit;
		
		do {
			visit = getVisitForUser(getLocationForUser(user));
		}
		while(
			(visit instanceof HeadlessAngularConvertedVisit) == false && 
			(visit instanceof HeadlessAngularSpecialOffersConvertVisit) == false
		);
		
		return visit;
	}

	@Override
	public boolean stop(boolean logging) {
		HeadlessAngularRunnable.stopAll();
		return super.stop(logging);
	}
}
