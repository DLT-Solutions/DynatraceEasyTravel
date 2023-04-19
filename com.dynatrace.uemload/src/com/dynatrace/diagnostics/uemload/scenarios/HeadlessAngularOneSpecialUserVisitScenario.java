package com.dynatrace.diagnostics.uemload.scenarios;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.HeadlessAngularSimulator;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.UEMLoadScenario;

public class HeadlessAngularOneSpecialUserVisitScenario extends HeadlessAngularSpecialUserVisitScenario {

	private ExtendedCommonUser user;
	
	public HeadlessAngularOneSpecialUserVisitScenario(ExtendedCommonUser user) {
		this.user = user;
	}
	
	public void setUser(ExtendedCommonUser extendedCommonUser) {
		this.user = extendedCommonUser;
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
