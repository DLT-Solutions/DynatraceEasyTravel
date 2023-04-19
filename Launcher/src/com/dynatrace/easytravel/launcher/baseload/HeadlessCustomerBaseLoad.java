package com.dynatrace.easytravel.launcher.baseload;

import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelLauncherScenario;
import com.dynatrace.diagnostics.uemload.scenarios.HeadlessCustomerOverloadScenario;
import com.dynatrace.diagnostics.uemload.scenarios.HeadlessCustomerScenario;
import com.dynatrace.easytravel.config.HeadlessTrafficScenarioEnum;
import com.dynatrace.easytravel.launcher.misc.Constants;

public class HeadlessCustomerBaseLoad extends BaseLoad {

	protected HeadlessCustomerBaseLoad(HeadlessTrafficScenarioEnum headlessTrafficScenario, int value, double ratio) {
		super(createScenario(headlessTrafficScenario), Constants.Procedures.CUSTOMER_FRONTEND_ID, value, ratio, false);
	}

    /**
     * Create traffic scenario
     * @return scenario to use based on the HeadlessTrafficScenarioEnum defined in easyTravelConfig.properties
     */
    private static EasyTravelLauncherScenario createScenario(HeadlessTrafficScenarioEnum headlessTrafficScenario) {
    	switch (headlessTrafficScenario) {
		case OverloadDetectionHeadlessTraffic:
				return new HeadlessCustomerOverloadScenario();
		default:
			// StandardHeadlessTraffic:
	    	return new HeadlessCustomerScenario();
		}
    }

	@Override
    protected void addHost2Scenario(String host) {
    	getScenario().getHostsManager().addCustomerFrontendHost(host);
    }

    @Override
    protected void removeHostFromScenario(String host) {
    	getScenario().getHostsManager().removeCustomerFrontendHost(host);
    }

	@Override
	protected boolean hasHost() {
		return getScenario().getHostsManager().hasCustomerFrontendHost();
	}
}