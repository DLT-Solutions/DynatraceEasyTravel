package com.dynatrace.easytravel.launcher.baseload;

import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelCustomerMobile;
import com.dynatrace.easytravel.launcher.misc.Constants;


public class MobileBrowserBaseLoad extends BaseLoad {

    protected MobileBrowserBaseLoad(int value, double ratio, boolean taggedWebRequest) {
		super(new EasyTravelCustomerMobile(false), Constants.Procedures.CUSTOMER_FRONTEND_ID, value, ratio, taggedWebRequest);
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