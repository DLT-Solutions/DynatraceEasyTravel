package com.dynatrace.easytravel.model;

import static java.lang.String.format;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.dynatrace.easytravel.jpa.business.*;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.persistence.controller.BusinessDatabaseController;
import com.dynatrace.easytravel.persistence.controller.DatabaseController;
import com.dynatrace.easytravel.persistence.provider.*;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import ch.qos.logback.classic.Logger;

@Repository
public class GenericDataAccess implements DataAccess {
	private static final Logger log = LoggerFactory.make();

	private final BookingProvider bookingProvider;
	private final JourneyProvider journeyProvider;
	private final TenantProvider tenantProvider;
	private final LocationProvider locationProvider;
	private final UserProvider userProvider;
	private final LoginHistoryProvider loginHistoryProvider;
	private final DatabaseController controller;

	private final static int MIN_CHARS_JOURNEY_SEARCH = 2;

	/**
	 * Creates all required column families and the respective content.
	 *
	 * @throws IOException
	 * @author stefan.moschinski
	 * @param controller
	 */
	public GenericDataAccess(BusinessDatabaseController controller) {
		bookingProvider = controller.getBookingProvider();
		tenantProvider = controller.getTenantProvider();
		locationProvider = controller.getLocationProvider();
		journeyProvider = controller.getJourneyProvider();
		userProvider = controller.getUserProvider();
		loginHistoryProvider = controller.getLoginHistoryProvider();
		this.controller = controller;
	}

	@Override
	@Transactional(readOnly = true)
	public Collection<Booking> findBookings(String username) {
		if (username == null) {
			log.info("Could not return bookings, no username specified");
			return null;
		}

		return bookingProvider.getBookingsByUserName(username);
	}

	@Override
	@Transactional
	public void storeBooking(Booking booking) {
		bookingProvider.add(booking);
	}

	@Override
	@Transactional(readOnly = true)
	public Collection<Journey> allJourneys() {
		return journeyProvider.getAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Collection<User> allUsers() {
		return userProvider.getAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Collection<Tenant> allTenants() {
		return tenantProvider.getAll();
	}


	@Override
	@Transactional(readOnly = true)
	public Collection<Location> allLocations() {
		return Lists.newArrayList(locationProvider.getAll());
	}


	@Override
	@Transactional(readOnly = true)
	public Tenant getTenant(final String tenantName) {
		return tenantProvider.getTenantByName(tenantName);
	}

	@Transactional
	public Location createLocation(String name) {
		return locationProvider.add(new Location(name));
	}

	@Override
	@Transactional(readOnly = true)
	public Location getLocation(final String name) {
		return locationProvider.getLocationByName(name);
	}

	@Override
	@Transactional(readOnly = true)
	public Journey getJourney(final String name) {
		return journeyProvider.getJourneyByName(name);
	}

	@Override
	@Transactional(readOnly = true)
	public User getUser(String name) {
		return userProvider.getUserByName(name);
	}

	@Override
	@Transactional(readOnly = true)
	public Journey getJourneyById(Integer id) {
		return journeyProvider.getJourneyById(id);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Journey getJourneyByIdNormalize(Integer id, boolean normalize) {
		return journeyProvider.getJourneyByIdNormalize(id, normalize);
	}
	
	@Override
	@Transactional
	public void updateUser(User user) {
		userProvider.update(user);
		if (log.isDebugEnabled()) {
			log.debug("Updated user: " + user);
		}
	}

	@Override
	@Transactional
	public void updateTenant(Tenant tenant) {
		tenantProvider.update(tenant);
		if (log.isDebugEnabled()) {
			log.debug("Updated tenant: " + tenant);
		}
	}

	@Override
	public void updateJourney(Journey updatedJourney) {
		journeyProvider.update(updatedJourney);
	}

	@Override
	@Transactional
	public void addUser(User user) {
		userProvider.add(user);
		if (log.isInfoEnabled()) {
			log.info("Added user: " + user);
		}
	}



	@Override
	public Collection<Journey> findJourneys(String destination, Date fromDate, Date toDate, boolean normalize) {
		if (Strings.isNullOrEmpty(destination) || destination.length() < MIN_CHARS_JOURNEY_SEARCH) {
			if (log.isDebugEnabled()) {
				log.debug(format("The destination string must have at least %d characters, but was '%s'",
						MIN_CHARS_JOURNEY_SEARCH, String.valueOf(destination)));
			}

			return Collections.emptyList();
		}

		return journeyProvider.findJourneys(destination, fromDate, toDate, normalize);
	}

	@Override
	@Transactional
	public boolean deleteLocation(String name) {
		return locationProvider.deleteLocation(name);
	}

	@Override
	@Transactional
	public boolean addLocation(String name) {
		addLocation(new Location(name));
		return true;
	}

	@Override
	@Transactional
	public void addLocation(Location location) {
		locationProvider.add(location);
	}

	@Override
	@Transactional
	public void deleteJourney(int journeyId) {
		log.info(format("Deleting journey with id '%d'", journeyId));
		if (log.isDebugEnabled()) {
			log.debug("The journey to delete is: " + getJourneyById(journeyId));
		}
		journeyProvider.removeJourneyById(journeyId);
	}

	@Override
	public Collection<Journey> getJourneysByTenant(String tenantName) {
		return journeyProvider.getJourneysByTenant(tenantName);
	}

	@Override
	public Collection<Booking> getBookingsByTenant(String tenantName) {
		return bookingProvider.getBookingsByTenant(tenantName);
	}

	@Override
	public Collection<Location> getLocations(int fromIdx, int count) {
		return locationProvider.getLocations(fromIdx, count);
	}

	@Override
	public Collection<Journey> getJourneysByTenant(String tenantName, int fromIdx, int count) {
		return journeyProvider.getJourneysByTenant(tenantName, fromIdx, count);
	}

	@Override
	public Collection<Booking> getBookingsByTenant(String tenantName, int fromIdx, int count) {
		return bookingProvider.getBookingsByTenant(tenantName, fromIdx, count);
	}

	@Override
	public int getLocationCount() {
		return locationProvider.getCount();
	}

	@Override
	public int getJourneyCountByTenant(String tenantName) {
		return journeyProvider.getJourneyCountByTenant(tenantName);
	}

	@Override
	public int getBookingCountByTenant(String tenantName) {
		return bookingProvider.getBookingCountForTenant(tenantName);
	}

	@Override
	public String getStatistics() {
		throw new IllegalStateException("Statistics are not supported by CassandraLayer");
	}

	@Override
	public double getTotalSalesByTenant(String tenantName) {
		return bookingProvider.getTotalSalesByTenant(tenantName);
	}

	@Override
	public Map<Location, Integer> getDeparturesByTenant(String tenantName, int limit) {
		return bookingProvider.getDeparturesByTenant(tenantName, limit);
	}

	@Override
	public Map<Location, Integer> getDestinationsByTenant(String tenantName, int limit) {
		return bookingProvider.getDestinationsByTenant(tenantName, limit);
	}

	@Override
	@Transactional
	public void verifyLocation(int sleepTime) {
		locationProvider.verifyLocation(sleepTime);
	}

	@Override
	public int getJourneyIndexByName(String tenantName, String journeyName) {
		return journeyProvider.getJourneyIndexByName(tenantName, journeyName);
	}


	@Override
	public LocationProvider getLocationProvider() {
		return locationProvider;
	}


	@Override
	public Collection<Integer> allJourneyIds() {
		return journeyProvider.getAllJourneyIds();
	}

	@Override
	public boolean isLocationUsedByJourney(String locationName) {
		return journeyProvider.isJourneyStart(locationName) || journeyProvider.isJourneyDestination(locationName);
	}

	@Override
	@Transactional(readOnly = true)
	public Collection<Location> getMatchingJourneyDestinations(String locationName, boolean normalize) {
		if (Strings.isNullOrEmpty(locationName) || locationName.length() < MIN_CHARS_JOURNEY_SEARCH) {
			if (log.isDebugEnabled()) {
				log.debug(format("The destination string must have at least %d characters, but was '%s'",
						MIN_CHARS_JOURNEY_SEARCH, String.valueOf(locationName)));
			}

			return Collections.emptyList();
		}

		return journeyProvider.getMatchingJourneyDestinations(locationName, normalize);
	}

	@Override
	public Collection<Location> getMatchingLocations(String locationNamePart) {
		return locationProvider.getMatchingLocations(locationNamePart);
	}

	@Override
	public void startTransaction() {
		controller.startTransaction();
	}

	@Override
	public void commitTransaction() {
		controller.commitTransaction();
	}

	@Override
	public void flushAndClear() {
		controller.flushAndClear();
	}

	@Override
	public void flush() {
		controller.flush();
	}

	@Override
	public void close() {
		controller.close();
	}

	@Override
	public void rollbackTransaction() {
		controller.rollbackTransaction();
	}

	@Override
	public int getTotalLoginCountExcludingUser(User userToExclude) {
		return loginHistoryProvider.getLoginCountExcludingUser(userToExclude);
	}

	@Override
	public Collection<Integer> getLoginIdsExcludingUser(User userToExclude, int resultLimit) {
		return loginHistoryProvider.getLoginIdsExcludingUser(userToExclude, resultLimit);
	}

	@Override
	public void removeLoginHistoryById(Integer id) {
		loginHistoryProvider.removeLoginHistoryById(id);
	}

	@Override
	public void dropContents() {
		controller.dropContents();
	}

	@Override
	public void addBooking(Booking booking) {
		bookingProvider.add(booking);
	}

	@Override
	public void addLoginHistory(LoginHistory loginHistory) {
		loginHistoryProvider.add(loginHistory);
	}

	@Override
	public int getBookingCountByUser(String userName) {
		return bookingProvider.getBookingCountForUser(userName);
	}

	@Override
	public int getLoginCountForUser(String userName) {
		return loginHistoryProvider.getLoginCountForUser(userName);
	}

	@Override
	public int getBookingCountExcludingUser(String userToExclude) {
		return bookingProvider.getBookingCountExcludingUser(userToExclude);
	}

	@Override
	public Collection<String> getBookingIdsExcludingUser(String userToExclude, int resultLimit) {
		return bookingProvider.getBookingIdsExcludingUser(userToExclude, resultLimit);
	}

	@Override
	public void removeBookingById(String bookingId) {
		bookingProvider.removeBookingById(bookingId);
	}


	@Override
	public Collection<Journey> getJourneys(int maxResults) {
		return journeyProvider.getWithLimit(maxResults);
	}

	@Override
	public void addTenant(Tenant tenant) {
		tenantProvider.add(tenant);
	}

	/**
	 *
	 * @return
	 * @author stefan.moschinski
	 */
	@Override
	public int getJourneyCount() {
		return journeyProvider.getCount();
	}

	@Override
	@Transactional
	public void addJourney(Journey journey) {
		journeyProvider.add(journey);
	}


	@Override
	public Tenant createTenant(String name, String password, String desc) {
		Tenant tenant = getTenant(name);
		if (tenant == null) {
			tenant = new Tenant(name, password, desc);
			tenantProvider.add(tenant);
		} else {
			log.debug("Tenant '" + name + "' already exists, updating values.");
			tenant.setDescription(desc);
            tenantProvider.update(tenant);
		}

		return tenant;
	}

	@Override
	public Location createLocation(String name, boolean checkExistence) {
		Location location = checkExistence ? getLocation(name) : null;
		if (location == null) {
			location = new Location(name);
			locationProvider.add(location);
		} else {
			log.debug("Location '" + name + "' already exists, updating values.");
			// no values to update for now
		}
		return location;
	}

	@Transactional
	@Override
	public Journey createJourney(String name, String start, String dest, String tenantName,
			Date from, Date to, double amount, byte[] picture) {
		Location startLocation = getLocation(start);
		if (startLocation == null) {
			throw new IllegalArgumentException("Could not find location '" + start + "'");
		}
		Location destLocation = getLocation(dest);
		if (destLocation == null) {
			throw new IllegalArgumentException("Could not find location '" + dest + "'");
		}
		Tenant tenant = getTenant(tenantName);
		if (tenant == null) {
			throw new IllegalArgumentException("Could not find tenant '" + tenantName + "'");
		}

		Journey journey = getJourney(name);
		if (journey == null) {
			journey = journeyProvider.add(new Journey(name, startLocation, destLocation, tenant, from, to, amount, picture));
		} else {
			log.debug("Journey '" + name + "' already exists, updating values.");
			journey.setStart(startLocation);
			journey.setDestination(destLocation);
			journey.setTenant(tenant);
			journey.setFromDate(from);
			journey.setToDate(to);
			journey.setAmount(amount);
			journey.setPicture(picture);

            // store updated journey
            journeyProvider.update(journey);

		}

		return journey;
	}


	@Override
	public void createJourney(String name, Location start, Location dest, Tenant tenant,
			Date from, Date to, double amount, byte[] picture) {
		journeyProvider.add(new Journey(name, start, dest, tenant, from, to, amount, picture));
	}

	@Override
	public int refreshJourneys() {
		return journeyProvider.refreshJourneys();
	}


	@Override
	public Schedule createSchedule(String name, long period) {
//		Schedule schedule = getSchedule(name);
//		if (schedule == null) {
//			schedule = new Schedule(name, period);
//			scheduleProvider.add(schedule);
//		} else {
//			log.fine("Schedule '" + name + "' already exists, updating values.");
//			schedule.setPeriod(period);
//		}
//
//		return schedule;

		return new Schedule(name, period);
	}

	@Override
	public Schedule getSchedule(final String name) {
		return null;
	}

	@Override
	public Booking createBooking(String bookingId, Journey journey, User user, Date bookingDate) {
		Booking booking = new Booking(bookingId, journey, user, bookingDate);
		return bookingProvider.add(booking);
	}

	@Override
	public int getBookingCount(User user) {
		return bookingProvider.getBookingCountForUser(user.getName());
	}

	@Override
	public LoginHistory createLoginHistory(User user, Date loginDate) {
		LoginHistory loginHistory = new LoginHistory(user, loginDate);
		return loginHistoryProvider.add(loginHistory);
	}

	@Override
	public int getLoginCount(User user) {
		return loginHistoryProvider.getLoginCountForUser(user.getName());
	}

	@Override
	public Booking getBookingById(String bookingId) {
		return bookingProvider.getBookingById(bookingId);
	}

	@Override
	public Collection<Booking> getRecentBookings(int bookingsLimit) {
		return bookingProvider.getRecentBookings(bookingsLimit);
	}


}
