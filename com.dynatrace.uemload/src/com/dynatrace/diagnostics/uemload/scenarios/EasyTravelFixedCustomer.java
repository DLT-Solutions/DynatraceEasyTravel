package com.dynatrace.diagnostics.uemload.scenarios;

import java.util.Collections;
import java.util.Map;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.ExtendedDemoUser;
import com.dynatrace.diagnostics.uemload.IterableSet;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.RandomLocation;
import com.dynatrace.diagnostics.uemload.Visit;

/**
 * EasyTravelFixedCustomer load-scenario is used for generating reproducible and predictable load.
 * It uses constant values for bandwidth, browser, latency, hardware speed, browser window size.
 *
 * Only converted visitors are used.
 */
public class EasyTravelFixedCustomer extends EasyTravel {

	public EasyTravelFixedCustomer(boolean highLoadFromAsia) {
		this(null, null, highLoadFromAsia);
	}

	public EasyTravelFixedCustomer(String customerFrontendHost, String b2bFrontendHost, boolean highLoadFromAsia) {
		super(customerFrontendHost, b2bFrontendHost, highLoadFromAsia);
	}

	@Override
	protected String getName() {
		return "Customer Frontend (Java) - EasyTravelFixedCustomer";
	}

	@Override
	public boolean hasHosts() {
		return getHostsManager().getCustomerFrontendHostCount() > 0;
	}

	@Override
	protected IterableSet<Visit> createVisits() {
		VisitsModel visits = new VisitsModel.VisitsBuilder().setConvert(100).setSeo(10).setMagentoShort(10).build();
		return createVisits(visits);
	}

	@Override
	protected Map<String, IterableSet<Visit>> createVisitsByContinent() {
		return Collections.emptyMap();
	}

	@Override
	protected Map<String, IterableSet<Visit>> createVisitsByCountry() {
		return Collections.emptyMap();
	}

	@Override
	protected IterableSet<Visit> createRushHourVisits() {
		return createVisits();
	}

	@Override
	protected RandomLocation createRandomLocations() {
		return new RandomLocation() {
			Location location = ExtendedDemoUser.MARIA_USER.getLocation();
			@Override
			public Location get() {
				return location;
			}
		};
	}

	@Override
	public ExtendedCommonUser getRandomUser(String s){
		return ExtendedDemoUser.MARIA_USER;
	}

	@Override
	protected boolean useRandomFirstPage() {
		return false;
	}
}