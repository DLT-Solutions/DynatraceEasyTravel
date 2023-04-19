package com.dynatrace.easytravel.cassandra;

import java.util.Collection;
import java.util.Map;

import com.dynatrace.easytravel.cassandra.tables.objectmapper.BookingTable;
import com.dynatrace.easytravel.jpa.business.Booking;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.persistence.provider.BookingProvider;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class CassandraBookingProvider implements BookingProvider {
	
	private final BookingTable bookingTable;
	
	public CassandraBookingProvider(BookingTable bookingTable) {
		this.bookingTable = bookingTable;
	}

	@Override
	public Booking add(Booking value) {
		bookingTable.addModel(value);
		return value;
	}

	@Override
	public Booking update(Booking value) {
		bookingTable.updateModel(value);
		return value;
	}

	@Override
	public Collection<Booking> getAll() {
		return bookingTable.getAllModels();
	}

	@Override
	public Collection<Booking> getWithLimit(int limit) {
		return bookingTable.getModelsWithLimit(limit);
	}

	@Override
	public int getCount() {
		return bookingTable.getCount();
	}

	@Override
	public void reset() {
		bookingTable.reset();
	}

	@Override
	public Collection<Booking> getBookingsByUserName(String username) {
		return bookingTable.getBookingsByUserName(username);
	}

	@Override
	public Collection<Booking> getBookingsByTenant(String tenantName) {
		return bookingTable.getBookingsByTenant(tenantName);
	}

	@Override
	public double getTotalSalesByTenant(String tenantName) {
		return bookingTable.getTotalSalesByTenant(tenantName);
	}

	@Override
	public int getBookingCountForTenant(String tenantName) {
		return bookingTable.getBookingCountForTenant(tenantName);
	}

	@Override
	public Map<Location, Integer> getDestinationsByTenant(String tenantName, int limit) {
		return bookingTable.getDestinationsByTenant(tenantName, limit);
	}

	@Override
	public Map<Location, Integer> getDeparturesByTenant(String tenantName, int limit) {
		return bookingTable.getDeparturesByTenant(tenantName, limit);
	}

	@Override
	public int getBookingCountForUser(String name) {
		return bookingTable.getBookingCountForUser(name);
	}

	@Override
	public Collection<Booking> getBookingsByTenant(String tenantName, int fromIdx, int count) {
		return bookingTable.getBookingsByTenant(tenantName, fromIdx, count);
	}

	@Override
	public Collection<String> getBookingIdsExcludingUser(String userToExclude, int resultLimit) {
		return bookingTable.getBookingIdsExcludingUser(userToExclude, resultLimit);
	}

	@Override
	public void removeBookingById(String bookingId) {
		bookingTable.removeBookingById(bookingId);
	}

	@Override
	public int getBookingCountExcludingUser(String userToExclude) {
		return bookingTable.getBookingCountExcludingUser(userToExclude);
	}

	@Override
	public Booking getBookingById(String bookingId) {
		return bookingTable.getBookingById(bookingId);
	}

	@Override
	public Collection<Booking> getRecentBookings(int bookingsLimit) {
		return getWithLimit(bookingsLimit);
	}

}
