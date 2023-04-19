package com.dynatrace.diagnostics.uemload;

import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelFixedCustomer;


/**
 * @author Rafal Psciuk
 * Fixed customer location used in {@link EasyTravelFixedCustomer} 
 */
public class FixedLocation implements RandomLocation {

	private static final String IP = "178.188.101.111";
	private static final String COUNTRY = "Austria";
	private static final String CONTINENT = "Europe";
	private static final Integer TIMEZONE = 1;
	
	private static final Location LOCATION = new Location(CONTINENT, COUNTRY, IP, TIMEZONE);

		
	/* (non-Javadoc)
	 * @see com.dynatrace.diagnostics.uemload.RandomLocation#get()
	 */
	@Override
	public Location get() {
		return LOCATION;
	}			
}
