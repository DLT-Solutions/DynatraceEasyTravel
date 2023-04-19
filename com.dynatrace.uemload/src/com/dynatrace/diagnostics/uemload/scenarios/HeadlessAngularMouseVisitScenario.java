package com.dynatrace.diagnostics.uemload.scenarios;

import com.dynatrace.diagnostics.uemload.HeadlessAngularSimulator;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.headless.HeadlessMouseMoveVisit;
import com.dynatrace.easytravel.constants.BaseConstants.ScenarioNames;

public class HeadlessAngularMouseVisitScenario extends HeadlessAngularOneVisitScenario {
	@Override
	public Simulator createSimulator() { return new HeadlessAngularSimulator(this); }

	@Override
	protected String getName() {
		return ScenarioNames.HEADLESS_ANGULAR_MOUSE_VISIT;
	}

	@Override
	protected Visit createVisitForHost(String host) {
		return new HeadlessMouseMoveVisit(host);
	}

}
