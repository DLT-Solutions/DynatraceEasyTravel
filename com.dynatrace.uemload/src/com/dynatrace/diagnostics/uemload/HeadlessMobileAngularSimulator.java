package com.dynatrace.diagnostics.uemload;

import com.dynatrace.diagnostics.uemload.headless.HeadlessAngularRunnable;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 *
 * @author tomasz.wieremjewicz
 * @date 23 sty 2019
 *
 */
public class HeadlessMobileAngularSimulator extends Simulator {
	private static final Logger LOGGER = LoggerFactory.make();
	
	public HeadlessMobileAngularSimulator(UEMLoadScenario scenario) {
		super(scenario);
	}

	@Override
	protected void warmUp() throws Exception { /* not needed */ }

	@Override
	protected Runnable createActionRunnerForVisit() {
		ExtendedCommonUser user = getUserForVisit();
		HeadlessAngularRunnable.startAll();
		return new HeadlessAngularRunnable(getVisitForUser(getLocationForUser(user)), user, this);
	}

	@Override
	protected ExtendedCommonUser getUserForVisit() {
		ExtendedCommonUser user = super.getUserForVisit();
		int cnt = 0;
		while(user.getMobileDevice() == null && cnt++ < 10) {
			user = super.getUserForVisit();
		}
		if (cnt == 10) {
			LOGGER.error("Couldn't find user with mobile device");
		}
		return user;
	}

	@Override
	public boolean stop(boolean logging) {
		HeadlessAngularRunnable.stopAll();
		return super.stop(logging);
	}
}
