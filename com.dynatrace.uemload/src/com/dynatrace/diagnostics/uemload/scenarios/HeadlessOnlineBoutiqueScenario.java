package com.dynatrace.diagnostics.uemload.scenarios;

import com.dynatrace.diagnostics.uemload.HeadlessOnlineBoutiqueSimulator;
import com.dynatrace.diagnostics.uemload.IterableSet;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.Visit;

import com.dynatrace.diagnostics.uemload.headless.HeadlessOnlineBoutiqueBuyVisit;
import com.dynatrace.easytravel.constants.BaseConstants.ScenarioNames;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class HeadlessOnlineBoutiqueScenario extends HeadlessScenario {

	private static final Logger LOGGER = LoggerFactory.make();

	@Override
	public boolean hasHosts() {
		return getHostsManager().hasOnlineBoutiqueHost();
	}

	@Override
	protected IterableSet<Visit> createRushHourVisits() {
		return createVisits();
	}

	@Override
	protected IterableSet<Visit> createAnonymousVisits() {
		return createVisits();
	}

	@Override
	protected String getName() {
		return ScenarioNames.HEADLESS_ONLINE_BOUTIQUE;
	}

	@Override
	public Simulator createSimulator() {
		return new HeadlessOnlineBoutiqueSimulator(this);
	}

	@Override
	protected IterableSet<Visit> createVisits() {
		LOGGER.trace("Calling createVisits for HeadlessOnlineBoutiqueScenario");
		
		RandomSet<Visit> set = new RandomSet<>();
		
		for(String host : getHostsManager().getOnlineBoutiqueHosts()) {
			set.add(new HeadlessOnlineBoutiqueBuyVisit(host), 1);
		}

		return set;
	}

}
