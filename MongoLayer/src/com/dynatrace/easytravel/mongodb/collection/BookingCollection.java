/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: BookingCollection.java
 * @date: 17.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb.collection;

import static com.dynatrace.easytravel.jpa.business.Booking.BOOKING_ID;
import static com.dynatrace.easytravel.jpa.business.Booking.BOOKING_JOURNEY;
import static com.dynatrace.easytravel.jpa.business.Booking.BOOKING_USER;
import static com.dynatrace.easytravel.jpa.business.Journey.JOURNEY_AMOUNT;
import static com.dynatrace.easytravel.jpa.business.Journey.JOURNEY_TENANT;
import static com.dynatrace.easytravel.jpa.business.Tenant.TENANT_NAME;
import static com.dynatrace.easytravel.mongodb.SimpleQueryBuilder.buildQuery;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.dynatrace.easytravel.jpa.business.Booking;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.mongodb.MongoConstants;
import com.dynatrace.easytravel.persistence.provider.BookingProvider;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryOperators;


/**
 *
 * @author stefan.moschinski
 */
public class BookingCollection extends MongoDbCollection<Booking> implements BookingProvider {

	/**
	 * 
	 */
	private static final String BOOKING_COLLECTION_NAME = "BookingCollection";

	/**
	 * 
	 * @param database
	 * @param collectionName
	 * @param marshaller
	 * @author stefan.moschinski
	 */
	public BookingCollection(DB database) {
		super(database, BOOKING_COLLECTION_NAME);


	}

	@Override
	public Collection<Booking> getBookingsByUserName(String username) {
		return find(new BasicDBObject(BOOKING_USER + "." + MongoConstants.ID, username));
	}

	@Override
	public Collection<Booking> getBookingsByTenant(String tenantName) {
		return find(buildQuery(BOOKING_JOURNEY, JOURNEY_TENANT, MongoConstants.ID).value(tenantName).create());
	}

	@Override
	public Collection<Booking> getBookingsByTenant(String tenantName, int fromIdx, int count) {
		return find(buildQuery(BOOKING_JOURNEY, JOURNEY_TENANT, MongoConstants.ID).value(tenantName).create(), fromIdx, count);
	}


	@Override
	public double getTotalSalesByTenant(String tenantName) {
		BasicDBObject match = new BasicDBObject(MongoConstants.MATCH, new BasicDBObject(BOOKING_JOURNEY + "." + JOURNEY_TENANT +
				"." +
				MongoConstants.ID, tenantName));

		// projection
		DBObject fields = new BasicDBObject(TENANT_NAME, "$" + BOOKING_JOURNEY + "." + JOURNEY_TENANT + "." + MongoConstants.ID);
		fields.put(JOURNEY_AMOUNT, "$" + BOOKING_JOURNEY + "." + JOURNEY_AMOUNT);
		fields.put(MongoConstants.ID, 0);
		DBObject project = new BasicDBObject(MongoConstants.PROJECT, fields);

		// grouping
		DBObject groupFields = new BasicDBObject(MongoConstants.ID, "");
		groupFields.put("sum", new BasicDBObject(MongoConstants.SUM, "$" + JOURNEY_AMOUNT));
		DBObject group = new BasicDBObject(MongoConstants.GROUP, groupFields);

		AggregationOutput aggregate2 = aggregate(match, project, group);

		Iterator<DBObject> resultIt = aggregate2.results().iterator();
		
		if (resultIt.hasNext()) {
			return ((BasicDBObject) resultIt.next()).getDouble("sum", 0.0);
		}

		return 0.0;
	}

	@Override
	public int getBookingCountForUser(String name) {
		return getSelectiveCount(buildQuery(BOOKING_USER, MongoConstants.ID).value(name).create());
	}

	@Override
	public int getBookingCountForTenant(String tenantName) {
		return getSelectiveCount(buildQuery(BOOKING_JOURNEY, JOURNEY_TENANT, MongoConstants.ID).value(tenantName).create());
	}

	@Override
	public Map<Location, Integer> getDestinationsByTenant(String tenantName, int limit) {
		return mapLocations(tenantName, limit, /* departure */false);
	}

	@Override
	public Map<Location, Integer> getDeparturesByTenant(String tenantName, int limit) {
		return mapLocations(tenantName, limit, /* departure */true);
	}

	private Map<Location, Integer> mapLocations(String tenantName, int limit, boolean departure) {
		Collection<Booking> bookings = find(new BasicDBObject(BOOKING_JOURNEY + "." + JOURNEY_TENANT + "." + MongoConstants.ID,
				tenantName), limit);
		Map<Location, Integer> destinations = Maps.newHashMap();
		for (Booking booking : bookings) {
			Location destination = departure ? booking.getJourney().getStart() : booking.getJourney().getDestination();
			int locationNo = 0;

			if (destinations.containsKey(destination)) {
				locationNo = destinations.get(destination);
			}

			destinations.put(destination, ++locationNo);
		}
		return destinations;
	}

	@Override
	public Collection<String> getBookingIdsExcludingUser(String userToExclude, int resultLimit) {
		DBCursor find = getCollection().find(createExcludeUserQuery(userToExclude),
				buildQuery(MongoConstants.ID).value(1).create()).limit(
				resultLimit); // we only need the id field

		List<String> ids = Lists.newArrayListWithCapacity(find.size());
		while (find.hasNext()) {
			BasicDBObject next = (BasicDBObject) find.next();
			ids.add(next.getString(MongoConstants.ID));
		}
		return ids;
	}

	private DBObject createExcludeUserQuery(String userToExclude) {
		return buildQuery(BOOKING_USER, MongoConstants.ID).value(
				buildQuery(QueryOperators.NE).value(userToExclude).create()).create();
	}

	@Override
	public void removeBookingById(String bookingId) {
		delete(buildQuery(MongoConstants.ID).value(bookingId).create());
	}

	@Override
	public int getBookingCountExcludingUser(String userToExclude) {
		return getSelectiveCount(createExcludeUserQuery(userToExclude));
	}

	@Override
	public Booking getBookingById(String bookingId) {
		return find(new BasicDBObject(BOOKING_ID + "." + MongoConstants.ID, bookingId)).stream().findFirst().orElse(null);
	}

	@Override
	public Collection<Booking> getRecentBookings(int bookingsLimit) {
		return findLatest(bookingsLimit);
	}



}
