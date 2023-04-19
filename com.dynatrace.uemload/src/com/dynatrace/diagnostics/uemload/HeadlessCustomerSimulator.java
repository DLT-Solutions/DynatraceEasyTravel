package com.dynatrace.diagnostics.uemload;

import com.dynatrace.diagnostics.uemload.headless.HeadlessCustomerRunnable;

/**
 * @author Rafal.Psciuk
 * @Date 2018.11.05
 */
public class HeadlessCustomerSimulator extends Simulator {

	public HeadlessCustomerSimulator(UEMLoadScenario scenario) {
		super(scenario);
	}

	@Override
	protected void warmUp() throws Exception { /* not needed */ }

	@Override
	protected Runnable createActionRunnerForVisit() {
		ExtendedCommonUser user = getUserForVisit();
		HeadlessCustomerRunnable.startAll();
		return new HeadlessCustomerRunnable(getVisitForUser(getLocationForUser(user)), user, this);
	}
	
	@Override
	public boolean stop(boolean logging) {		
		HeadlessCustomerRunnable.stopAll();
		return super.stop(logging);
	}
}
