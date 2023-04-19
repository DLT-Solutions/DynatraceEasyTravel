package com.dynatrace.easytravel.frontend.beans;

/**
 * A collection of include pages and navigation cases.
 *
 * @author philipp.grasboeck
 */
public class Pages {

	// include pages for booking
	private static final String INCLUDE_DIR = "/WEB-INF/includes/content/";
	public static final String  INCLUDE_BOOKING_LOGIN   = INCLUDE_DIR + "booking-login.xhtml";
	public static final String  INCLUDE_BOOKING_REVIEW  = INCLUDE_DIR + "booking-review.xhtml";
	public static final String  INCLUDE_BOOKING_PAYMENT = INCLUDE_DIR + "booking-payment.xhtml";
	public static final String  INCLUDE_BOOKING_FINISH  = INCLUDE_DIR + "booking-finish.xhtml";
	public static final String  INCLUDE_TRIP_DETAILS    = INCLUDE_DIR + "trip-details.xhtml";
	public static final String  INCLUDE_RATING_RESULTS    = INCLUDE_DIR + "rating-results.xhtml";
	public static final String  INCLUDE_RATING_ACTION    = INCLUDE_DIR + "rating-action.xhtml";

	// navigation cases defined in faces-config.xml
	public static final String NAVIGATION_CASE_ACCOUNT_CREATED = "accountCreated";
	public static final String NAVIGATION_CASE_PAYMENT_VALIDATED = "paymentValidated";
	public static final String NAVIGATION_CASE_BOOKING_FINISHED = "bookingFinished";
}
