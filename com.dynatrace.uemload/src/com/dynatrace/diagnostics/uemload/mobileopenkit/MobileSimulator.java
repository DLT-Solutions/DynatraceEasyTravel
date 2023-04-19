package com.dynatrace.diagnostics.uemload.mobileopenkit;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.UEMLoadScenario;
import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileDevice;
import com.dynatrace.diagnostics.uemload.mobileopenkit.parameters.MobileOpenKitParams;
import com.dynatrace.diagnostics.uemload.openkit.OpenKitSimulator;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.OpenKitMobileAppScenario;
import com.dynatrace.easytravel.config.EasyTravelConfig;

public class MobileSimulator extends OpenKitSimulator {
	private static final String API_PATH = "easytravel/rest";

	public MobileSimulator(UEMLoadScenario scenario) {
		super(scenario);
	}

	@Override
	protected Runnable createActionRunnerForVisit() {
		OpenKitMobileAppScenario mobileScenario = (OpenKitMobileAppScenario) getScenario();
		Location location = mobileScenario.getRandomLocation();
		ExtendedCommonUser user = mobileScenario.getRandomMobileUser(location.getCountry());
		MobileOpenKitParams params = mobileScenario.getRandomParams(user);
		String apiUrl = EasyTravelConfig.read().angularFrontendPublicUrl;
		if(apiUrl == null || apiUrl.isEmpty())
			apiUrl = mobileScenario.getHostsManager().getAngularFrontendHosts().stream().findAny().orElse("http://localhost:9080/");
		apiUrl += API_PATH;
		return new ActionRunner<>(mobileScenario.getRandomVisit(apiUrl, params, user), this);
	}

	@Override
	public boolean stop(boolean logging) {
		return super.stop(logging);
	}
}
