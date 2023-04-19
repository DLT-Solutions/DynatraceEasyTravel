package com.dynatrace.diagnostics.uemload.scenarios;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.IotDevicesSimulator;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.openkit.OpenKitParams;
import com.dynatrace.diagnostics.uemload.iot.IotsDistribution;
import com.dynatrace.diagnostics.uemload.iot.car.RentalCarParams;
import com.dynatrace.diagnostics.uemload.iot.visit.IotVisits;
import com.dynatrace.diagnostics.uemload.iot.visit.RentalCarVisit;
import com.dynatrace.diagnostics.uemload.iot.visit.RentalCarVisitWithCrash;
import com.dynatrace.diagnostics.uemload.iot.visit.RentalCarVisitWithHttpError;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class IotDevicesScenario extends OpenKitScenario<IotVisits, RentalCarVisit, OpenKitParams> {

	@Override
	public boolean hasHosts() {
		return hostManager.hasBackendHost();
	}

	@Override
	public Simulator createSimulator() {
		return new IotDevicesSimulator(this);
	}
	
	@Override
	protected RandomSet<IotVisits> createVisitSet() {
		RandomSet<IotVisits> visits = new RandomSet<>();
		visits.add(IotVisits.RENTAL_CAR_VISIT, 97);
		visits.add(IotVisits.RENTAL_CAR_VISIT_WITH_HTTP_ERRORS, 3);
		visits.add(IotVisits.RENTAL_CAR_VISIT_WITH_CRASH, 1);
		return visits;
	}
	
	public RentalCarVisit getRandomVisit(String host, OpenKitParams params, ExtendedCommonUser user) {
		IotVisits visitType = visits.getNext();
		switch (visitType) {
		case RENTAL_CAR_VISIT:
			return new RentalCarVisit(host, params, user);
		case RENTAL_CAR_VISIT_WITH_HTTP_ERRORS:
			return new RentalCarVisitWithHttpError(host, params, user);
		case RENTAL_CAR_VISIT_WITH_CRASH:
			return new RentalCarVisitWithCrash(host, params, user);
		default:
			return new RentalCarVisit(host, params, user);
		}
	}
	
	@Override
	protected String getName(){
		return "IoT Devices";
	}
	
	@Override
	public void setLoad(int value) {
		// NOSONAR - ignored
	}
	
	public RentalCarParams getRandomRentalCar(String country){
		return IotsDistribution.getRentalCars().get(country).getNext();
	}
}
