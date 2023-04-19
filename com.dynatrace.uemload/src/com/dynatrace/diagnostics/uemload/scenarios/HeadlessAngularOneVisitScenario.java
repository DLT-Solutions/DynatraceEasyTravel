package com.dynatrace.diagnostics.uemload.scenarios;

import com.dynatrace.diagnostics.uemload.IterableSet;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

public abstract class HeadlessAngularOneVisitScenario extends HeadlessAngularScenarioBase {

	private static final Logger LOGGER = LoggerFactory.make();
		
	@Override
	public boolean hasHosts() {
		return getHostsManager().hasAngularFrontendHost();
	}

	@Override
	protected IterableSet<Visit> createRushHourVisits() {
		return createVisits();
	}

	@Override
	protected IterableSet<Visit> createAnonymousVisits() {
		return createVisits();
	}
	
	protected abstract Visit createVisitForHost(String host);

	@Override
	protected IterableSet<Visit> createVisits() {
		LOGGER.info("Calling createVisits() for " + getName());
		RandomSet<Visit> set = new RandomSet<>();
		for(String host : getHostsManager().getAngularFrontendHosts()) {
			set.add(createVisitForHost(host), 10);
		}
		return set;
	}
}
