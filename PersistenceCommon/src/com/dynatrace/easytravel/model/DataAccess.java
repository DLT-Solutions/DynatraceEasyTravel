package com.dynatrace.easytravel.model;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.dynatrace.easytravel.jpa.business.*;
import com.dynatrace.easytravel.persistence.controller.DatabaseController;
import com.dynatrace.easytravel.persistence.provider.LocationProvider;

/**
 * A service interface for retrieving data
 * a booking.
 */
public interface DataAccess extends DatabaseController {

    /**
     * Find bookings made by the given user
     * @param username the user's name
     * @return their bookings
     */
	public Collection<Booking> findBookings(String username);

    /**
     * Store a new booking.
     */
    public void storeBooking(Booking booking);

	Collection<User> allUsers();

	Collection<Tenant> allTenants();

	public User getUser(final String name);

	public Tenant getTenant(String name);

	public void updateUser(User user);

	public void addUser(User user);

	public void updateTenant(Tenant tenant);

	/**
	 *
	 * <b>ATTENTION:</b> this method may not implement for certain databaseses
	 *
	 * @param sleepTime
	 * @author stefan.moschinski
	 */
	public void verifyLocation(int sleepTime);

	public Collection<Booking> getBookingsByTenant(String tenantName);

	public Collection<Booking> getBookingsByTenant(String tenantName, int fromIdx, int count);

	public int getBookingCountByTenant(String tenantName);

	public String getStatistics();

	public double getTotalSalesByTenant(String tenantName);

	public Map<Location, Integer> getDeparturesByTenant(String tenantName, int maxDepartures);

	public Map<Location, Integer> getDestinationsByTenant(String tenantName, int maxDepartures);

	/**
	 *
	 * @return
	 * @author stefan.moschinski
	 */
	Collection<Location> allLocations();

	/**
	 *
	 * @return
	 * @author stefan.moschinski
	 */
	Collection<Journey> allJourneys();

//	/**
//	 *
//	 * @return
//	 * @author stefan.moschinski
//	 */
//	Collection<Integer> allJourneyIds();

	/**
	 *
	 * @param name
	 * @param start
	 * @param dest
	 * @param tenantName
	 * @param from
	 * @param to
	 * @param amount
	 * @param picture
	 * @return
	 * @author stefan.moschinski
	 */
	Journey createJourney(String name, String start, String dest, String tenantName, Date from, Date to, double amount,
			byte[] picture);

	/**
	 *
	 * @param id
	 * @return
	 * @author stefan.moschinski
	 */
	Journey getJourneyById(Integer id);
	
	/**
	 *
	 * @param id
	 * @return
	 * @author stefan.moschinski
	 */
	Journey getJourneyByIdNormalize(Integer id, boolean normalize);
	
	/**
	 *
	 * @param destination
	 * @param fromDate
	 * @param toDate
	 * @return
	 * @author stefan.moschinski
	 */
	Collection<Journey> findJourneys(String destination, Date fromDate, Date toDate, boolean normalize);

	/**
	 *
	 * @param name
	 * @return
	 * @author stefan.moschinski
	 */
	boolean deleteLocation(String name);

	/**
	 *
	 * @param name
	 * @return
	 * @author stefan.moschinski
	 */
	boolean addLocation(String name);

	void addLocation(Location name);

	/**
	 *
	 * @param id
	 * @author stefan.moschinski
	 */
	void deleteJourney(int id);

	/**
	 *
	 * @param tenantName
	 * @return
	 * @author stefan.moschinski
	 */
	Collection<Journey> getJourneysByTenant(String tenantName);

	/**
	 *
	 * @param fromIdx
	 * @param count
	 * @return
	 * @author stefan.moschinski
	 */
	Collection<Location> getLocations(int fromIdx, int count);

	/**
	 *
	 * @param tenantName
	 * @param fromIdx
	 * @param count
	 * @return
	 * @author stefan.moschinski
	 */
	Collection<Journey> getJourneysByTenant(String tenantName, int fromIdx, int count);

	/**
	 *
	 * @return
	 * @author stefan.moschinski
	 */
	int getLocationCount();

	/**
	 *
	 * @param tenantName
	 * @return
	 * @author stefan.moschinski
	 */
	int getJourneyCountByTenant(String tenantName);

	/**
	 *
	 * @param tenantName
	 * @param journeyName
	 * @return
	 * @author stefan.moschinski
	 */
	int getJourneyIndexByName(String tenantName, String journeyName);

	/**
	 *
	 * @return
	 * @author stefan.moschinski
	 */
	public LocationProvider getLocationProvider();

	/**
	 *
	 * @return
	 * @author stefan.moschinski
	 */
	Collection<Integer> allJourneyIds();


	/**
	 *
	 * @param name
	 * @return
	 * @author stefan.moschinski
	 */
	public Collection<Location> getMatchingJourneyDestinations(String locationName, boolean normalize);

	/**
	 *
	 * @param locationName
	 * @return
	 * @author stefan.moschinski
	 */
	boolean isLocationUsedByJourney(String locationName);

	/**
	 *
	 * @param object
	 * @return
	 * @author stefan.moschinski
	 */
	public Collection<Location> getMatchingLocations(String locationName);

	/**
	 *
	 * @param userToExclude
	 * @return
	 * @author stefan.moschinski
	 */
	public int getTotalLoginCountExcludingUser(User userToExclude);

	/**
	 *
	 * @param userToExclude
	 * @param i
	 * @return
	 * @author stefan.moschinski
	 */
	public Collection<Integer> getLoginIdsExcludingUser(User userToExclude, int resultLimit);

	/**
	 *
	 * @param id
	 * @author stefan.moschinski
	 */
	public void removeLoginHistoryById(Integer id);

	/**
	 *
	 * @param booking
	 * @author stefan.moschinski
	 */
	public void addBooking(Booking booking);

	/**
	 *
	 * @param loginHistory
	 * @author stefan.moschinski
	 */
	public void addLoginHistory(LoginHistory loginHistory);

	/**
	 *
	 * @param string
	 * @author stefan.moschinski
	 */
	public int getBookingCountByUser(String string);

	/**
	 *
	 * @param string
	 * @return
	 * @author stefan.moschinski
	 */
	public int getLoginCountForUser(String string);

	/**
	 *
	 * @param userToExclude
	 * @return
	 * @author stefan.moschinski
	 */
	public int getBookingCountExcludingUser(String userToExclude);
	
	/**
	 * 
	 * @param bookingId
	 * @return
	 * @author Michal.Bakula
	 */
	public Booking getBookingById(String bookingId);
	
	/**
	 * 
	 * @param bookingsLimit
	 * @return
	 * @author Michal.Bakula
	 */
	public Collection<Booking> getRecentBookings(int bookingsLimit);

	/**
	 *
	 * @param bookingId
	 * @author stefan.moschinski
	 */
	public void removeBookingById(String bookingId);

	/**
	 *
	 * @param userToExclude
	 * @param resultLimit
	 * @return
	 * @author stefan.moschinski
	 */
	Collection<String> getBookingIdsExcludingUser(String userToExclude, int resultLimit);

	/**
	 *
	 * @param id
	 * @param d
	 * @author stefan.moschinski
	 */
	public void updateJourney(Journey updatedJourney);

	/**
	 *
	 * @param i
	 * @return
	 * @author stefan.moschinski
	 */
	public Collection<Journey> getJourneys(int maxResults);

	/**
	 *
	 * @param tenant
	 * @author stefan.moschinski
	 */
	public void addTenant(Tenant tenant);

	/**
	 *
	 * @return
	 * @author stefan.moschinski
	 */
	public int getJourneyCount();

	/**
	 *
	 * @param journey
	 * @author stefan.moschinski
	 */
	public void addJourney(Journey journey);

	Tenant createTenant(String name, String password, String desc);

	Location createLocation(String name, boolean checkExistence);

	Location getLocation(final String name);


	void createJourney(String name, Location start, Location dest, Tenant tenant,
			Date from, Date to, double amount, byte[] picture);


	int refreshJourneys();

	Journey getJourney(final String name);

	@Deprecated
	Schedule createSchedule(String name, long period);

	@Deprecated
	Schedule getSchedule(final String name);

	Booking createBooking(String bookingId, Journey journey, User user, Date bookingDate);

	int getBookingCount(User user);

	LoginHistory createLoginHistory(User user, Date loginDate);

	int getLoginCount(User user);


}
