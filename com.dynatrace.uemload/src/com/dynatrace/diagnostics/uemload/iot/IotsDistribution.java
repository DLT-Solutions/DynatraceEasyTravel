package com.dynatrace.diagnostics.uemload.iot;

import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.iot.car.RentalCarParams;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Michal.Bakula
 */
public class IotsDistribution {

	private static final Map<String, RandomSet<RentalCarParams>> RENTAL_CARS = createRentalCarsDistribution();

	private IotsDistribution() {
		throw new IllegalAccessError("Utility class");
	}

	public static Map<String, RandomSet<RentalCarParams>> getRentalCars() {
		return RENTAL_CARS;
	}

	private static Map<String, RandomSet<RentalCarParams>> createRentalCarsDistribution() {
		Map<String, RandomSet<RentalCarParams>> tmpCars = new HashMap<>();

		for (RentalCarParams car : RentalCarParams.getRentalCars()) {
			String country = car.getCountry();
			RandomSet<RentalCarParams> rs = tmpCars.get(country);
			if (rs == null) {
				rs = new RandomSet<>();
				rs.add(car, 1);
			} else {
				rs.add(car, 1);
			}
			tmpCars.put(country, rs);
		}

		return tmpCars;
	}

}
