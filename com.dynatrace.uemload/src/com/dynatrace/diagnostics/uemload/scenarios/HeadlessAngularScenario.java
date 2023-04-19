package com.dynatrace.diagnostics.uemload.scenarios;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.HeadlessAngularSimulator;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.easytravel.constants.BaseConstants.ScenarioNames;

/**
 *
 * @author Michal.Bakula
 *
 */
public class HeadlessAngularScenario extends HeadlessAngularScenarioBase {

	@Override
	protected String getName() {
		return ScenarioNames.HEADLESS_ANGULAR;
	}

	@Override
	public Simulator createSimulator() {
		return new HeadlessAngularSimulator(this);
	}
	
	@Override
	public ExtendedCommonUser getRandomUser(String country) {
		return getRandomUser(country, false, true);
	}
}