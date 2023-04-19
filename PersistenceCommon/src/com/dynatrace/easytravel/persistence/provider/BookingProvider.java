/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: BookingProvider.java
 * @date: 14.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence.provider;

import java.util.Collection;
import java.util.Map;

import com.dynatrace.easytravel.jpa.business.Booking;
import com.dynatrace.easytravel.jpa.business.Location;


/**
 *
 * @author stefan.moschinski
 */
public interface BookingProvider extends EasyTravelPersistenceProvider<Booking> {

	/**
	 * 
	 * @param username
	 * @author stefan.moschinski
	 */
	Collection<Booking> getBookingsByUserName(String username);

	/**
	 * 
	 * @param tenantName
	 * @return
	 * @author stefan.moschinski
	 */
	Collection<Booking> getBookingsByTenant(String tenantName);

	/**
	 * 
	 * @param tenantName
	 * @return
	 * @author stefan.moschinski
	 */
	double getTotalSalesByTenant(String tenantName);


	/**
	 * 
	 * @param tenantName
	 * @return
	 * @author stefan.moschinski
	 */
	int getBookingCountForTenant(String tenantName);

	/**
	 * 
	 * @param tenantName
	 * @param limit
	 * @return
	 * @author stefan.moschinski
	 */
	Map<Location, Integer> getDestinationsByTenant(String tenantName, int limit);

	/**
	 * 
	 * @param tenantName
	 * @param limit
	 * @return
	 * @author stefan.moschinski
	 */
	Map<Location, Integer> getDeparturesByTenant(String tenantName, int limit);

	/**
	 * 
	 * @param name
	 * @return
	 * @author stefan.moschinski
	 */
	int getBookingCountForUser(String name);

	/**
	 * 
	 * @param tenantName
	 * @param fromIdx
	 * @param count
	 * @author stefan.moschinski
	 * @return
	 */
	Collection<Booking> getBookingsByTenant(String tenantName, int fromIdx, int count);

	/**
	 * 
	 * @param userToExclude
	 * @return
	 * @author stefan.moschinski
	 */
	Collection<String> getBookingIdsExcludingUser(String userToExclude, int resultLimit);
	
	/**
	 * 
	 * @param id
	 * @return
	 * @author Michal.Bakula
	 */
	Booking getBookingById(String bookingId);
	
	/**
	 * 
	 * @param time
	 * @return
	 * @author Michal.Bakula
	 */
	Collection<Booking> getRecentBookings(int bookingsLimit);

	/**
	 * 
	 * @param bookingId
	 * @return
	 * @author stefan.moschinski
	 */
	void removeBookingById(String bookingId);

	/**
	 * 
	 * @param userToExclude
	 * @return
	 * @author stefan.moschinski
	 */
	int getBookingCountExcludingUser(String userToExclude);
}
