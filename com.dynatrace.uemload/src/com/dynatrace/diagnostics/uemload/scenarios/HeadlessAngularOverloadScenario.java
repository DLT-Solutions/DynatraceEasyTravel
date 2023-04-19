package com.dynatrace.diagnostics.uemload.scenarios;

import com.dynatrace.diagnostics.uemload.HeadlessAngularSimulator;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.headless.HeadlessAngularOverloadVisit;
import com.dynatrace.easytravel.constants.BaseConstants.ScenarioNames;
/**
 * Scenario to be used when config.headlessLoadScenario=OverloadDetectionHeadlessTraffic
 * Contains a single Selenium script to run which simulates selecting a number of menu options per visit
 *
 * @author Paul.Johnson
 *
 */
public class HeadlessAngularOverloadScenario extends HeadlessAngularOneVisitScenario  {

	@Override
	public Simulator createSimulator() { return new HeadlessAngularSimulator(this); }

	@Override
	protected String getName() {
		return ScenarioNames.HEADLESS_ANGULAR;
	}

	@Override
	protected Visit createVisitForHost(String host) {
		return new HeadlessAngularOverloadVisit(host);
	}
}
