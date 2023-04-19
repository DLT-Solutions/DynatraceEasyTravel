package com.dynatrace.diagnostics.uemload;

import com.dynatrace.diagnostics.uemload.headless.HeadlessOnlineBoutiqueRunnable;

public class HeadlessOnlineBoutiqueSimulator extends Simulator {
	

	public HeadlessOnlineBoutiqueSimulator(UEMLoadScenario scenario) {
		super(scenario);
	}

	@Override
	protected void warmUp() throws Exception { /* not needed */ }

	@Override
	protected Runnable createActionRunnerForVisit() {
		ExtendedCommonUser user = getUserForVisit();
		HeadlessOnlineBoutiqueRunnable.startAll();
		return new HeadlessOnlineBoutiqueRunnable(getVisitForUser(getLocationForUser(user)), user, this);
	}
	
	@Override
	public boolean stop(boolean logging) {		
		HeadlessOnlineBoutiqueRunnable.stopAll();
		return super.stop(logging);
	}
}
