package com.dynatrace.easytravel.launcher.baseload;

import com.dynatrace.diagnostics.uemload.scenarios.HeadlessMobileAngularScenario;
import com.dynatrace.easytravel.launcher.misc.Constants;

/**
 *
 * @author tomasz.wieremjewicz
 * @date 21 gru 2018
 *
 */
public class HeadlessMobileAngularBaseLoad extends BaseLoad {

	protected HeadlessMobileAngularBaseLoad(int value, double ratio) {
		super(new HeadlessMobileAngularScenario(), Constants.Procedures.ANGULAR_FRONTEND_ID, value, ratio, false);
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