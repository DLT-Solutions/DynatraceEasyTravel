/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: HbaseBookingColumnFamily.java
 * @date: 28.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.columnfamily;

import static com.dynatrace.easytravel.jpa.business.Booking.BOOKING_JOURNEY;
import static com.dynatrace.easytravel.jpa.business.Booking.BOOKING_USER;
import static com.dynatrace.easytravel.jpa.business.Journey.JOURNEY_TENANT;
import static com.dynatrace.easytravel.jpa.business.Tenant.TENANT_NAME;
import static com.dynatrace.easytravel.jpa.business.User.USER_NAME;
import static org.apache.hadoop.hbase.util.Bytes.toBytes;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.RandomRowFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueExcludeFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.util.Bytes;

import com.dynatrace.easytravel.hbase.HbaseDataController;
import com.dynatrace.easytravel.hbase.serializer.ColumnPrefix;
import com.dynatrace.easytravel.jpa.business.Booking;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.persistence.provider.BookingProvider;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;


/**
 *
 * @author stefan.moschinski
 */
public class HbaseBookingColumnFamily extends HbaseColumnFamily<Booking> implements BookingProvider {

	private static final String BOOKING_COUNTER_NAME = "bookingCnt";
	private static final String SALES_COUNTER_NAME = "salesCnt";

	public static final String BOOKING_COLUMN_FAMILY_NAME = "BookingColumnFamily";

	private final HbaseCounterColumnFamily counterColumnFamily;

	public HbaseBookingColumnFamily(HbaseDataController controller, HbaseCounterColumnFamily counterColumnFamily) {
		super(controller, BOOKING_COLUMN_FAMILY_NAME, "boo");
		this.counterColumnFamily = counterColumnFamily;
	}


	@Override
	public Booking add(Booking value) {
		double amount = value.getJourney().getAmount();

		String tenantName = value.getJourney().getTenant().getName();
		incrementCounters(tenantName, amount);
		String userName = value.getUser().getName();
		incrementCounters(userName, amount);

		return super.add(value);
	}

	protected void incrementCounters(String rowKey, double amount) {
		counterColumnFamily.incrementByOne(rowKey, BOOKING_COUNTER_NAME);
		counterColumnFamily.increment(rowKey, SALES_COUNTER_NAME, amount);
	}

	@Override
	public void reset() {
		counterColumnFamily.reset(); // also reset counter column family
		super.reset();
	}

	@Override
	public Collection<Booking> getBookingsByUserName(String username) {
		SingleColumnValueFilter filter = new SingleColumnValueFilter(
				Bytes.toBytes(columnFamilyName),
				Bytes.toBytes(ColumnPrefix.createPrefix("boo", BOOKING_USER).getPrefixedColumnName(USER_NAME)),
				CompareOp.EQUAL,
				new SubstringComparator(username));
		filter.setFilterIfMissing(true);
		filter.setLatestVersionOnly(true);

		return getFiltered(filter);
	}

	@Override
	public Collection<Booking> getBookingsByTenant(String tenantName) {
		SingleColumnValueFilter filter = new SingleColumnValueFilter(
				Bytes.toBytes(columnFamilyName),
				Bytes.toBytes(ColumnPrefix.apprehendPrefix(prefix, BOOKING_JOURNEY, JOURNEY_TENANT).getPrefixedColumnName(
						TENANT_NAME)),
				CompareOp.EQUAL,
				new BinaryComparator(toBytes(tenantName)));
		filter.setFilterIfMissing(true);
		filter.setLatestVersionOnly(true);

		return getFiltered(filter);
	}

	@Override
	public double getTotalSalesByTenant(String tenantName) {
		return counterColumnFamily.getDouble(tenantName, SALES_COUNTER_NAME);
	}

	@Override
	public int getBookingCountForTenant(String tenantName) {
		return (int) counterColumnFamily.getLong(tenantName, BOOKING_COUNTER_NAME);
	}

	@Override
	public int getBookingCountForUser(String userName) {
		return (int) counterColumnFamily.getLong(userName, BOOKING_COUNTER_NAME);
	}


	@Override
	public Map<Location, Integer> getDestinationsByTenant(String tenantName, int limit) {
		SingleColumnValueFilter filter = new SingleColumnValueFilter(
				Bytes.toBytes(columnFamilyName),
				Bytes.toBytes(ColumnPrefix.apprehendPrefix(prefix, BOOKING_JOURNEY, JOURNEY_TENANT).getPrefixedColumnName(
						TENANT_NAME)),
				CompareOp.EQUAL,
				new BinaryComparator(toBytes(tenantName)));
		filter.setFilterIfMissing(true);
		filter.setLatestVersionOnly(true);

		Collection<Booking> filtered = getFiltered(filter);

		Map<Location, Integer> destinations = Maps.newHashMapWithExpectedSize(filtered.size());

		Iterator<Booking> iterator = filtered.iterator();
		for (int i = 0; i < limit && iterator.hasNext(); i++) {
			Booking booking = iterator.next();
			Location departure = booking.getJourney().getDestination();
			Integer count = destinations.get(departure);
			Integer newCount = count == null ? 1 : ++count;

			destinations.put(departure, newCount);
		}
		return destinations;

	}

	@Override
	public Map<Location, Integer> getDeparturesByTenant(String tenantName, int limit) {
		SingleColumnValueFilter filter = new SingleColumnValueFilter(
				Bytes.toBytes(columnFamilyName),
				Bytes.toBytes(ColumnPrefix.apprehendPrefix(prefix, BOOKING_JOURNEY, JOURNEY_TENANT).getPrefixedColumnName(
						TENANT_NAME)),
				CompareOp.EQUAL,
				new BinaryComparator(toBytes(tenantName)));
		filter.setFilterIfMissing(true);
		filter.setLatestVersionOnly(true);

		Collection<Booking> filtered = getFiltered(filter);

		Map<Location, Integer> departures = Maps.newHashMapWithExpectedSize(filtered.size());

		Iterator<Booking> iterator = filtered.iterator();
		for (int i = 0; i < limit && iterator.hasNext(); i++) {
			Booking booking = iterator.next();
			Location departure = booking.getJourney().getStart();
			Integer count = departures.get(departure);
			Integer newCount = count == null ? 1 : ++count;

			departures.put(departure, newCount);
		}

		return departures;
	}



	@Override
	public Collection<Booking> getBookingsByTenant(String tenantName, int fromIdx, int count) {
		SingleColumnValueFilter filter = new SingleColumnValueFilter(
				Bytes.toBytes(columnFamilyName),
				Bytes.toBytes(ColumnPrefix.apprehendPrefix(prefix, BOOKING_JOURNEY, JOURNEY_TENANT).getPrefixedColumnName(
						TENANT_NAME)),
				CompareOp.EQUAL,
				new BinaryComparator(toBytes(tenantName)));
		filter.setFilterIfMissing(true);
		filter.setLatestVersionOnly(true);

		return getFiltered(filter, count);
	}

	@Override
	public Collection<String> getBookingIdsExcludingUser(String userToExclude, int resultLimit) {
		SingleColumnValueFilter exludeFilter = new SingleColumnValueFilter(
				Bytes.toBytes(columnFamilyName),
				Bytes.toBytes(ColumnPrefix.apprehendPrefix(prefix, BOOKING_USER)
						.getPrefixedColumnName(USER_NAME)),
				CompareOp.NOT_EQUAL,
				new BinaryComparator(toBytes(userToExclude)));

		Collection<Booking> filtered = getFiltered(exludeFilter, resultLimit);


		return FluentIterable.from(filtered).transform(new Function<Booking, String>() {

			@Override
			public String apply(Booking input) {
				return input.getId();
			}
		}).toList();
	}

	@Override
	public void removeBookingById(String bookingId) {
		Booking booking = getByKey(bookingId);

		counterColumnFamily.decrement(booking.getJourney().getTenant().getName(), SALES_COUNTER_NAME,
				booking.getJourney().getAmount());
		counterColumnFamily.decrement(booking.getJourney().getTenant().getName(), BOOKING_COUNTER_NAME, 1);

		counterColumnFamily.decrement(booking.getUser().getName(), SALES_COUNTER_NAME,
				booking.getJourney().getAmount());
		counterColumnFamily.decrement(booking.getUser().getName(), BOOKING_COUNTER_NAME, 1);


		deleteByRowKey(bookingId);
	}

	@Override
	public int getBookingCountExcludingUser(String userToExclude) {
		SingleColumnValueExcludeFilter exludeFilter = new SingleColumnValueExcludeFilter(
				Bytes.toBytes(columnFamilyName),
				Bytes.toBytes(ColumnPrefix.apprehendPrefix(prefix, BOOKING_USER).getPrefixedColumnName(
						USER_NAME)),
				CompareOp.EQUAL,
				new BinaryComparator(toBytes(userToExclude)));

		int exclude = getFiltered(exludeFilter).size();
		return getCount() - exclude;
	}


	@Override
	public Booking getBookingById(String bookingId) {
		return getByKey(bookingId);
	}


	@Override
	public Collection<Booking> getRecentBookings(int bookingsLimit) {
		// Workaround: returns random bookings, not latest bookings
		return getFiltered(new RandomRowFilter(0.1f), bookingsLimit);
	}

}
