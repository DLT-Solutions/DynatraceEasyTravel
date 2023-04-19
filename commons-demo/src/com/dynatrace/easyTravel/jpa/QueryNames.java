package com.dynatrace.easytravel.jpa;

/**
 * A collection of all query names for named queries of easyTravel.
 *
 * @author philipp.grasboeck
 */
public class QueryNames {
	public static final String JOURNEY_ALL = "Journey.all";
	public static final String JOURNEY_ALL_IDS = "Journey.all.ids";
	public static final String JOURNEY_GET = "Journey.get";
	public static final String JOURNEY_GET_INDEX = "Journey.getIndex";
	public static final String JOURNEY_ALL_ALPHABETICALLY = "Journey.allAlphabetically";
	public static final String JOURNEY_FIND = "Journey.find";
	public static final String JOURNEY_FIND_NORMALIZED = "Journey.find.normalized";
	public static final String JOURNEY_FIND_BY_ID_NORMALIZED = "Journey.findById.normalized";
	public static final String JOURNEY_FIND_BY_LOCATION_DEST = "Journey.findByLocationDest";
	public static final String JOURNEY_FIND_BY_LOCATION_START = "Journey.findByLocationStart";
	public static final String JOURNEY_FIND_BY_TENANT = "Journey.findByTenant";
	public static final String LOCATION_ALL = "Location.all";
	public static final String LOCATION_FIND = "Location.find";
	public static final String LOCATION_FIND_WITH_JOURNEYS = "Location.findWithJourneys";
	public static final String LOCATION_FIND_WITH_JOURNEYS_AND_NORMALIZE = "Location.findWithJourneysAndNormalize";
	public static final String USER_ALL = "User.all";
	public static final String TENANT_ALL = "Tenant.all";
	public static final String BOOKING_GET = "Booking.get";
	public static final String BOOKING_BY_JOURNEY = "Booking.byJourney";
	public static final String SCHEDULE_ALL = "Schedule.all";
	public static final String BOOKING_ALL = "Booking.all";
	public static final String LOGINHISTORY_ALL = "LoginHistory.all";
	public static final String PAYMENT_ALL = "Payment.all";
	public static final String CREDITCARD_ALL = "CreditCard.all";
}
