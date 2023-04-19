package com.dynatrace.easytravel.launcher.baseload;

import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelLauncherScenario;
import com.dynatrace.diagnostics.uemload.scenarios.HeadlessAngularOverloadScenario;
import com.dynatrace.diagnostics.uemload.scenarios.HeadlessAngularScenario;
import com.dynatrace.easytravel.config.HeadlessTrafficScenarioEnum;
import com.dynatrace.easytravel.launcher.misc.Constants;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class HeadlessAngularBaseLoad extends BaseLoad {

	protected HeadlessAngularBaseLoad(HeadlessTrafficScenarioEnum headlessTrafficScenario, int value, double ratio) {
		super(createScenario(headlessTrafficScenario), Constants.Procedures.ANGULAR_FRONTEND_ID, value, ratio, false);
	}

    /**
     * Create traffic scenario
     * @return scenario to use based on the HeadlessTrafficScenarioEnum defined in easyTravelConfig.properties
     */
    private static EasyTravelLauncherScenario createScenario(HeadlessTrafficScenarioEnum headlessTrafficScenario) {
    	switch (headlessTrafficScenario) {
		case OverloadDetectionHeadlessTraffic:
				return new HeadlessAngularOverloadScenario();
		default:
			// StandardHeadlessTraffic:
	    	return new HeadlessAngularScenario();
		}
    }

	@Override
	protected void addHost2Scenario(String host) {
		getScenario().getHostsManager().addAngularFrontendHost(host);
	}

	@Override
	protected void removeHostFromScenario(String host) {
		getScenario().getHostsManager().removeAngularFrontendHost(host);
	}

	@Override
	protected boolean hasHost() {
		return getScenario().getHostsManager().hasAngularFrontendHost();
	}

	@Override
	protected boolean useAngularFrontend() {
		return true;
	}
	
	

}