package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class PseudoRandomJourneyDestination {

	private final static List<String> TRAVEL_LOCATIONS;
	public final static int LOCATION_COUNT;
	private final static AtomicInteger COUNTER = new AtomicInteger(0);


	static {
		TRAVEL_LOCATIONS = Collections.unmodifiableList(Arrays.asList(
							"San Francisco",
							"Miami Beach",
							"Bodensdorf",
							"Boxford",
							"Nestor",
							"Berlin",
							"Paris",
							"Albury",
							"Stuttgart",
							"Shizunai",
							"Valparaiso",
							"Zurich",
							"Ramallah",
							"Jerusalem",
							"Jasper",
							"Kabul",
							"Newell",
							"Bugojno",
							"Bagdad",
				"New York"));
		LOCATION_COUNT = TRAVEL_LOCATIONS.size();
	}


	public static String get(){
		return TRAVEL_LOCATIONS.get(COUNTER.getAndIncrement() % LOCATION_COUNT);
	}

}
