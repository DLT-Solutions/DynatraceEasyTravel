package com.dynatrace.diagnostics.uemload.mobileopenkit.action.type;

import com.dynatrace.diagnostics.uemload.openkit.action.EventType;

public enum MobileEventType implements EventType {
	LOGIN_SUCCESSFUL("LoginSuccessful"),
	LOGIN_FAILED("LoginFailed"),

	BOOKING_JOURNEY_AMOUNT("bookJourneyAmount"),
	BOOKING_JOURNEY_DESTINATION("bookJourneyDestination"),

	BOOKING_FAILED_ERROR("failed to resolve booking status"),
	BOOKING_FAILED("bookingFailed"),

	JOURNEYS_FOUND("JourneysFound"),
	REQUEST_ID("requestId"),
	OPERATION_KEY("operationKey"),
	RESULT_SIZE("resultSize"),

	AD_FAILED("failed to display Ad"),
	NS_ERROR("NSError");

	public final String value;

	MobileEventType(String value) {
		this.value = value;
	}
}
