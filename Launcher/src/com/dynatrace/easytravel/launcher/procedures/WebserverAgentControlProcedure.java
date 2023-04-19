package com.dynatrace.easytravel.launcher.procedures;

import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;


/**
 * Procedure which ensures that the Windows Service for the dynaTrace Web Server Agent is started as part of a Scenario.
 *
 * If the service was running before, it is restarted. If it is not running, it is started and later stopped when the scenario is
 * stopped again.
 *
 * @author dominik.stadler
 */
public class WebserverAgentControlProcedure extends WindowsServiceControlProcedure {

	public WebserverAgentControlProcedure(ProcedureMapping mapping) {
		super(mapping);
	}

	@Override
	String getServiceNamePattern() {
		return "dynaTrace Web Server Agent ${version}";
	}

}
