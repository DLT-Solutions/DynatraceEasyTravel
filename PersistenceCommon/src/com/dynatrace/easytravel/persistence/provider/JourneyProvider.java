package com.dynatrace.easytravel.persistence.provider;

import java.util.Collection;
import java.util.Date;

import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.jpa.business.Location;



public interface JourneyProvider extends EasyTravelPersistenceProvider<Journey> {

	/**
	 * 
	 * 
	 * @param destination
	 * @param fromDate
	 * @param toDate
	 * @return
	 * @author peter.kaiser
	 */
	public abstract Collection<Journey> findJourneys(String destination, Date fromDate, Date toDate, boolean normalize);

	/**
	 * 
	 * 
	 * @param id
	 * @return
	 * @author peter.kaiser
	 */
	public abstract Journey getJourneyById(Integer id);

	/**
	 * 
	 * 
	 * @param id
	 * @return
	 * @author kasper.kulikowski
	 */
	public abstract Journey getJourneyByIdNormalize(Integer id, boolean normalize);
	
	/**
	 * 
	 * 
	 * @param tenantName
	 * @return
	 * @author peter.kaiser
	 */
	public abstract Collection<Journey> getJourneysByTenant(String tenantName);

	/**
	 * 
	 * 
	 * @param tenantName
	 * @param fromIdx
	 * @param count
	 * @return
	 * @author peter.kaiser
	 */
	public abstract Collection<Journey> getJourneysByTenant(String tenantName, int fromIdx, int count);

	/**
	 * 
	 * 
	 * @param tenantName
	 * @return
	 * @author peter.kaiser
	 */
	public abstract int getJourneyCountByTenant(String tenantName);

	/**
	 * 
	 * @param id
	 * @return
	 * @author dominik.gruber
	 */
	public abstract int getJourneyIndexByName(String tenantName, String journeyName);

	/**
	 * 
	 * @param journeyName
	 * @return
	 * @author stefan.moschinski
	 */
	public abstract Journey getJourneyByName(String journeyName);

	/**
	 * 
	 * @return
	 * @author stefan.moschinski
	 */
	public abstract Collection<Integer> getAllJourneyIds();

	/**
	 * Checks whether the given location (name) is the destination of a {@link Journey}
	 * 
	 * @param locationName name of the Location to check
	 * @return <code>true</code> if the location is the destination of a journey
	 * @author stefan.moschinski
	 */
	boolean isJourneyDestination(String locationName);

	/**
	 * Checks whether the given location (name) is the start of a {@link Journey}
	 * 
	 * @param locationName name of the Location to check
	 * @return <code>true</code> if the location is the start of a journey
	 * @author stefan.moschinski
	 */
	boolean isJourneyStart(String locationName);

	/**
	 * 
	 * @param name
	 * @return
	 * @author stefan.moschinski
	 */
	public abstract Collection<Location> getMatchingJourneyDestinations(String name, boolean normalize);

	/**
	 * 
	 * @param id
	 * @author stefan.moschinski
	 */
	public abstract void removeJourneyById(int id);


	/**
	 * 
	 * @author stefan.moschinski
	 * @return
	 */
	public abstract int refreshJourneys();


}