package com.dynatrace.easytravel.persistence.provider;

import java.util.Collection;

import com.dynatrace.easytravel.jpa.business.Location;



public interface LocationProvider extends EasyTravelPersistenceProvider<Location> {

//	/**
//	 * Find locations by a part of the name, case insensitive.
//	 * 
//	 * @param name
//	 * @return
//	 */
//	public abstract Collection<Location> findLocations(String name);
//	
//	/**
//	 *
//	 * @param name
//	 * @param checkForJourneys
//	 * @return
//	 * @author stefan.moschinski
//	 */
//	public abstract Collection<Location> findLocationsSlow(String name, boolean checkForJourneys);


	public abstract boolean deleteLocation(String name);

	public abstract Collection<Location> getLocations(int fromIdx, int count);

	/**
	 * 
	 * @param name
	 * @return
	 * @author stefan.moschinski
	 */
	public abstract Location getLocationByName(String locationName);

	/**
	 * 
	 * @param name
	 * @return
	 * @author stefan.moschinski
	 */
	public abstract Collection<Location> getMatchingLocations(String locationNamePart);

	/**
	 * 
	 * @param sleepTime
	 * @return
	 * @author stefan.moschinski
	 */
	public abstract void verifyLocation(int sleepTime);




}