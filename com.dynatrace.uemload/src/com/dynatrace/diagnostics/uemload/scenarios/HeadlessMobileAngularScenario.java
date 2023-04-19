package com.dynatrace.diagnostics.uemload.scenarios;

import com.dynatrace.diagnostics.uemload.HeadlessMobileAngularSimulator;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.easytravel.constants.BaseConstants.ScenarioNames;

/**
 *
 * @author tomasz.wieremjewicz
 * @date 3 sty 2019
 *
 */
public class HeadlessMobileAngularScenario extends HeadlessAngularScenarioBase {

	@Override
	protected String getName() {
		return ScenarioNames.HEADLESS_ANGULAR_MOBILE;
	}

	@Override
	public Simulator createSimulator() {
		return new HeadlessMobileAngularSimulator(this);
	}

}