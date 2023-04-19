package com.dynatrace.easytravel.launcher.baseload;

import com.dynatrace.diagnostics.uemload.iot.car.DynatraceRentalCar;
import com.dynatrace.diagnostics.uemload.scenarios.IotDevicesScenario;
import com.dynatrace.easytravel.launcher.misc.Constants;

public class IotDevicesBaseLoad extends BaseLoad {
	
	private static final String PROCEDURE_ID = Constants.Procedures.CUSTOMER_FRONTEND_ID;

	protected IotDevicesBaseLoad(int value, double ratio, boolean taggedWebRequest) {
		super(new IotDevicesScenario(), PROCEDURE_ID, value, ratio, taggedWebRequest);
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
	
	@Override
	public void setTaggeWebRequest(boolean taggedWebRequest) {
		if(DynatraceRentalCar.isConfigSet()) {
			super.setTaggeWebRequest(taggedWebRequest);
		}
	}

}
