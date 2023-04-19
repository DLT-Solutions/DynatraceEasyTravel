/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: TenantMongoMarshaller.java
 * @date: 13.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb.dbobject;

import static com.dynatrace.easytravel.jpa.business.Booking.BOOKING_DATE;
import static com.dynatrace.easytravel.jpa.business.Booking.BOOKING_JOURNEY;
import static com.dynatrace.easytravel.jpa.business.Booking.BOOKING_USER;
import static com.dynatrace.easytravel.jpa.business.Journey.JOURNEY_DESTINATION;
import static com.dynatrace.easytravel.jpa.business.Journey.JOURNEY_START;
import static com.dynatrace.easytravel.jpa.business.Journey.JOURNEY_TENANT;

import java.util.Map;

import com.dynatrace.easytravel.jpa.Base;
import com.dynatrace.easytravel.jpa.business.Booking;
import com.dynatrace.easytravel.mongodb.MongoConstants;
import com.google.common.collect.Maps;
import com.mongodb.DBObject;

/**
 *
 * @author stefan.moschinski
 */
public class BookingMongoMarshaller extends MongoObjectMarshaller<Booking> {

	private static final long serialVersionUID = 1L;

	private static Map<String, Class<? extends MongoObjectMarshaller<? extends Base>>> BOOKING_SUB_MARSHALLER;

	static {
		BOOKING_SUB_MARSHALLER = Maps.newHashMapWithExpectedSize(6);
		BOOKING_SUB_MARSHALLER.put(BOOKING_JOURNEY, JourneyMongoMarshaller.class);
		BOOKING_SUB_MARSHALLER.put(BOOKING_JOURNEY + "." + JOURNEY_TENANT, TenantMongoMarshaller.class);
		BOOKING_SUB_MARSHALLER.put(BOOKING_JOURNEY + "." + JOURNEY_START, LocationMongoMarshaller.class);
		BOOKING_SUB_MARSHALLER.put(BOOKING_JOURNEY + "." + JOURNEY_DESTINATION, LocationMongoMarshaller.class);
		BOOKING_SUB_MARSHALLER.put(BOOKING_USER, UserMongoMarshaller.class);
	}


	/**
	 * 
	 * @author stefan.moschinski
	 */
	public BookingMongoMarshaller() {
		super(BOOKING_SUB_MARSHALLER);
	}

	@Override
	protected DBObject marshalTypeSpecific(Booking booking, boolean withId) {
		if (withId) {
			put(MongoConstants.ID, booking.getId());
		}
		put(BOOKING_JOURNEY, new JourneyMongoMarshaller().marshal(booking.getJourney()));
		put(BOOKING_USER, new UserMongoMarshaller().marshal(booking.getUser()));
		put(BOOKING_DATE, booking.getBookingDate());
		return this;
	}

	@Override
	protected Booking unmarshalTypeSpecific() {
		Booking booking = new Booking();
		booking.setId(getString(MongoConstants.ID));
		booking.setJourney(((JourneyMongoMarshaller) get(BOOKING_JOURNEY)).unmarshal());
		booking.setUser(((UserMongoMarshaller) get(BOOKING_USER)).unmarshal());
		booking.setBookingDate(getDate(BOOKING_DATE));
		return booking;
	}

	@Override
	public MongoObjectMarshaller<Booking> newInstance() {
		return new BookingMongoMarshaller();
	}

}
