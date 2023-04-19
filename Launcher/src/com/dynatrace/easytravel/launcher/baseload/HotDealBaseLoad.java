package com.dynatrace.easytravel.launcher.baseload;

import com.dynatrace.diagnostics.uemload.scenarios.HotDealScenario;
import com.dynatrace.easytravel.launcher.engine.Procedure;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.engine.StatefulProcedure;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;


public class HotDealBaseLoad extends BaseLoad {

	private static final String PROCEDURE_ID = Constants.Procedures.BUSINESS_BACKEND_ID;

	protected HotDealBaseLoad(int value, double ratio, boolean taggedWebRequest) {
		super(new HotDealScenario(), PROCEDURE_ID, value, ratio, taggedWebRequest);
	}

	@Override
	protected void addHost2Scenario(String host) {
		getScenario().getHostsManager().addBackendHost(host);
	}

	@Override
	protected void removeHostFromScenario(String host) {
		getScenario().getHostsManager().removeBackendHost(host);
	}

	@Override
	protected boolean hasHost() {
		return getScenario().getHostsManager().hasBackendHost();
	}

	@Override
	public void notifyProcedureStateChanged(StatefulProcedure subject, State oldState, State newState) {
		super.notifyProcedureStateChanged(subject, oldState, newState);

		String procedureId = subject.getMapping().getId();

		if (PROCEDURE_ID.equals(procedureId)) {
			Procedure procedure = subject.getDelegate();
			updateScenarioFromSettings(procedure.getMapping());

	    	if (State.STOPPED == newState) {
	    		stopSimulator();
	    	} else if (State.OPERATING == newState) {
	    		startSimulator();
	    	}
		}

	}

	private void updateScenarioFromSettings(ProcedureMapping mapping) {
		HotDealScenario scenario = (HotDealScenario) getScenario();

		String jms = mapping.getSettingValue("plugin", "HotDealServerJMS");
		String rmi = mapping.getSettingValue("plugin", "HotDealServerRMI");

		scenario.setTechnologies(isOn(jms), isOn(rmi));
	}

	private boolean isOn(String value) {
		return "on".equals(value);
	}



}
