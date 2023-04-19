package com.dynatrace.easytravel.launcher.procedures;

import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;


/**
 * Procedure which ensures that the Windows Service for the dynaTrace Host Agent is started as part of a Scenario.
 *
 * If the service was running before, it is restarted. If it is not running, it is started and later stopped when the scenario is
 * stopped again.
 *
 * @author dominik.stadler
 */
public class HostAgentControlProcedure extends WindowsServiceControlProcedure {

	public HostAgentControlProcedure(ProcedureMapping mapping) {
		super(mapping);
	}

	@Override
	String getServiceNamePattern() {
		return "dynaTrace Host Agent ${version}";
	}

}
