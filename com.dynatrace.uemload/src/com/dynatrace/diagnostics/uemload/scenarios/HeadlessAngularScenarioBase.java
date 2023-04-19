package com.dynatrace.diagnostics.uemload.scenarios;

import com.dynatrace.diagnostics.uemload.IterableSet;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.SyntheticAndRobotRandomLocation;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.headless.HeadlessAngularUtils;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

public abstract class HeadlessAngularScenarioBase extends HeadlessScenario {

	private static final Logger LOGGER = LoggerFactory.make();
	
	@Override
	public boolean hasHosts() {
		return getHostsManager().hasAngularFrontendHost();
	}

	@Override
	protected IterableSet<Visit> createVisits() {
		LOGGER.info("Calling createVisits() for " + getName());
		return HeadlessAngularUtils.createVisits(getHostsManager());
	}

	@Override
	protected IterableSet<Visit> createRushHourVisits() {
		LOGGER.info("Calling createRushHourVisits() for " + getName());
		return HeadlessAngularUtils.createRushHourVisits(getHostsManager());
	}

	@Override
	protected IterableSet<Visit> createAnonymousVisits() {
		LOGGER.info("Calling createAnonymousVisits() for " + getName());
		return HeadlessAngularUtils.createAnonymousVisits(getHostsManager());
	}

	@Override
	public Location getRandomLocation() {
		if(SyntheticAndRobotRandomLocation.SINGLETON.isNextLocationRobotOrSynthetic()) {
			return SyntheticAndRobotRandomLocation.SINGLETON.get();
		}
		return super.getRandomLocation();
	}

}
