package com.dynatrace.diagnostics.uemload.iot.visit;

import com.dynatrace.diagnostics.uemload.openkit.action.EventType;

/**
 * @author Michal.Bakula
 */
public enum IotActionType implements EventType {
	AUTHENTICATION("User authentication"),
	AUTHENTICATION_FAILURE("User authentication"),
	START("Start engine"),
	STOP("Stop engine"),
	LOCK("Lock car"),
	UNLOCK("Unlock car"),
	TRACKING_POINT("New tracking point"),
	GPS_ERROR("GPS signal lost"),
	PARK("Park"),
	CRASH("Crash"),
	EMERGENCY("Emergency stop"),
	REPORT("Crash report");

	private final String name;

	IotActionType(String name) {
		this.name = name;
	}

	public String getValue() {
		return name;
	}
}
