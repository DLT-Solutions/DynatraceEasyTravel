package com.dynatrace.easytravel.config;

/**
 * Enum that holds different load scenarios.
 * @author rafal.psciuk
 *
 */
public enum CustomerTrafficScenarioEnum {
	/**
	 * Standard random load. Default value.
	 */
	EasyTravel, 
	
	/**
	 * Fixed load. Only one type of visit is performed. All customers are converted, have the same bandwidth etc.  
	 */	
	EasyTravelFixed,
	
	/**
	 * Predictable load. Number of bounce, search, almost and convert visits are function of traffic volume.   
	 */
	EasyTravelPredictable,
}
