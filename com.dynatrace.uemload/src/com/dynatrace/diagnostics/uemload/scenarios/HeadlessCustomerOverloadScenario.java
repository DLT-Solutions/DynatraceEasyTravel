package com.dynatrace.diagnostics.uemload.scenarios;

import com.dynatrace.diagnostics.uemload.HeadlessCustomerSimulator;
import com.dynatrace.diagnostics.uemload.IterableSet;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.headless.HeadlessCustomerOverloadVisit;
import com.dynatrace.easytravel.constants.BaseConstants.ScenarioNames;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class HeadlessCustomerOverloadScenario extends HeadlessScenario {

	private static final Logger LOGGER = LoggerFactory.make();

	@Override
	public boolean hasHosts() {
		return getHostsManager().hasCustomerFrontendHost();
	}

	@Override
	protected String getName() {
		return ScenarioNames.HEADLESS_CUSTOMER;
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
	protected IterableSet<Visit> createVisits() {
		LOGGER.info("Calling createVisits for HeadlessCustomerOverloadScenario");
		RandomSet<Visit> set = new RandomSet<>();
		for(String host : getHostsManager().getCustomerFrontendHosts()) {
			set.add(new HeadlessCustomerOverloadVisit(host),10);
		}
		return set;
	}

	@Override
	public Simulator createSimulator() {
		return new HeadlessCustomerSimulator(this); 
	}

}
