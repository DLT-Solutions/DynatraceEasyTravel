package com.dynatrace.easytravel.launcher.baseload;

import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelHostManager;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.OpenKitMobileAppScenario;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.EasyTravelMobileAppScenario;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.misc.Constants;


public class MobileBaseLoad extends BaseLoad {

	private static final boolean USE_OPEN_KIT_LOAD = EasyTravelConfig.read().openKitMobileLoadGenerator;

    protected MobileBaseLoad(int value, double ratio, boolean taggedWebRequest) {
        super(USE_OPEN_KIT_LOAD ? new OpenKitMobileAppScenario() : new EasyTravelMobileAppScenario(),
		        USE_OPEN_KIT_LOAD ? Constants.Procedures.ANGULAR_FRONTEND_ID : Constants.Procedures.CUSTOMER_FRONTEND_ID, value, ratio, taggedWebRequest);
    }

    @Override
    protected void addHost2Scenario(String host) {
	    EasyTravelHostManager manager = getScenario().getHostsManager();
	    if (USE_OPEN_KIT_LOAD)
	    	manager.addAngularFrontendHost(host);
	    else
	    	manager.addCustomerFrontendHost(host);
    }

    @Override
    protected void removeHostFromScenario(String host) {
	    EasyTravelHostManager manager = getScenario().getHostsManager();
	    if (USE_OPEN_KIT_LOAD)
		    manager.removeAngularFrontendHost(host);
	    else
		    manager.removeCustomerFrontendHost(host);
    }

	@Override
	protected boolean hasHost() {
		EasyTravelHostManager manager = getScenario().getHostsManager();
		return USE_OPEN_KIT_LOAD ? manager.hasAngularFrontendHost() : manager.hasCustomerFrontendHost();
	}
	
	@Override
	protected boolean useAngularFrontend() {
		return USE_OPEN_KIT_LOAD;
	}

}
