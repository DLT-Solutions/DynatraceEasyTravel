package com.dynatrace.easytravel.launcher.baseload;

import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelCustomer;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelFixedCustomer;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelLauncherScenario;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelPredictableCustomer;
import com.dynatrace.easytravel.config.CustomerTrafficScenarioEnum;
import com.dynatrace.easytravel.launcher.misc.Constants;


public class CustomerBaseLoad extends BaseLoad {

    protected CustomerBaseLoad(CustomerTrafficScenarioEnum customerLoadScenario, int value, double ratio, boolean taggedWebRequest) {
        super(createScenario(customerLoadScenario, value, ratio), Constants.Procedures.CUSTOMER_FRONTEND_ID, value, ratio, taggedWebRequest);
    }

    /**
     * Create traffic scenario
     * @param baseLoad
     * @param ratio
     * @return
     */
    public static EasyTravelLauncherScenario createScenario(CustomerTrafficScenarioEnum customerLoadScenario, int baseLoad, double ratio) {
    	switch (customerLoadScenario) {
		case EasyTravelFixed:
				return new EasyTravelFixedCustomer(false);
		case EasyTravelPredictable:
				return new EasyTravelPredictableCustomer(false, BaseLoadUtil.getLoadNumberPerMinute(baseLoad, ratio));
		default:
				return new EasyTravelCustomer(false);
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