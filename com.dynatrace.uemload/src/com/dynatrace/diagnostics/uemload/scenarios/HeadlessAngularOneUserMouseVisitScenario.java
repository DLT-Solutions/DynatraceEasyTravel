package com.dynatrace.diagnostics.uemload.scenarios;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.HeadlessAngularSimulator;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.UEMLoadScenario;

public class HeadlessAngularOneUserMouseVisitScenario extends HeadlessAngularMouseVisitScenario {

	private final ExtendedCommonUser user;
	
	public HeadlessAngularOneUserMouseVisitScenario(ExtendedCommonUser user) {
		this.user = user;
	}
	
	@Override
	public Simulator createSimulator() {
		return new HeadlessAngularOneUserSimulator(this);
	}
	
	
	class HeadlessAngularOneUserSimulator extends HeadlessAngularSimulator {

		public HeadlessAngularOneUserSimulator(UEMLoadScenario scenario) {
			super(scenario);
		}

		@Override
		protected ExtendedCommonUser getUserForVisit() {		
			return user;
		}			
	}

}
