package com.dynatrace.diagnostics.uemload;

import com.dynatrace.diagnostics.uemload.iot.car.DynatraceRentalCar;
import com.dynatrace.diagnostics.uemload.iot.car.RentalCarParams;
import com.dynatrace.diagnostics.uemload.iot.visit.IotActionType;
import com.dynatrace.diagnostics.uemload.openkit.OpenKitSimulator;
import com.dynatrace.diagnostics.uemload.scenarios.IotDevicesScenario;

public class IotDevicesSimulator extends OpenKitSimulator {

	public IotDevicesSimulator(UEMLoadScenario scenario) {
		super(scenario);
	}

	@Override
	protected Runnable createActionRunnerForVisit() {
		IotDevicesScenario iotScenario = (IotDevicesScenario)getScenario();
		Location location = iotScenario.getRandomLocation();
		RentalCarParams car = iotScenario.getRandomRentalCar(location.getCountry());
		ExtendedCommonUser user = iotScenario.getRandomUser(location.getCountry());
		String host = iotScenario.getHostsManager().getCustomerFrontendHosts().stream().findAny().orElse("http://localhost:8079/");
		return new ActionRunner<>(iotScenario.getRandomVisit(host, car, user), this);
	}

	@Override
	public boolean stop(boolean logging) {
		return super.stop(logging);
	}
}
