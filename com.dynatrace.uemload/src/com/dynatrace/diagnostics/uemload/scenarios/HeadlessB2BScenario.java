package com.dynatrace.diagnostics.uemload.scenarios;

import com.dynatrace.diagnostics.uemload.HeadlessB2BSimulator;
import com.dynatrace.diagnostics.uemload.IterableSet;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.headless.HeadlessB2BAllMenuOptionsVisit;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.constants.BaseConstants.B2BAccount;
import com.dynatrace.easytravel.constants.BaseConstants.ScenarioNames;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Headless scenario for b2b frontend.
 * @author: Krzysztof Sajko
 * @Date: 04.10.2021
 */
public class HeadlessB2BScenario extends HeadlessScenario {
	
	private static final Logger LOGGER = LoggerFactory.make();
	
	public HeadlessB2BScenario(String host) {
		getHostsManager().addB2BFrontendHost(host);
	}
	
	public HeadlessB2BScenario() {};

	@Override
	public boolean hasHosts() {
		return getHostsManager().hasB2BFrontendHost();
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
		return ScenarioNames.HEADLESS_B2B;
	}

	@Override
	public Simulator createSimulator() {
		return new HeadlessB2BSimulator(this);
	}

	@Override
	protected IterableSet<Visit> createVisits() {
		LOGGER.info("Calling createVisits() for " + getName());
		RandomSet<Visit> visits = new RandomSet<>();
		B2BAccount account = UemLoadUtils.getRandomElement(B2BAccount.values());
		for(String host : getHostsManager().getB2bFrontendHosts()) {
			visits.add(new HeadlessB2BAllMenuOptionsVisit(host, account), 10);
		}
		return visits;
	}

}
