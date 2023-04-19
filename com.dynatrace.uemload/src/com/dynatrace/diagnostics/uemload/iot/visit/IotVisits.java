package com.dynatrace.diagnostics.uemload.iot.visit;


import com.dynatrace.diagnostics.uemload.openkit.visit.Visits;

/**
 * @author Michal.Bakula
 */
public enum IotVisits implements Visits {

	RENTAL_CAR_VISIT,
	RENTAL_CAR_VISIT_WITH_HTTP_ERRORS,
	RENTAL_CAR_VISIT_WITH_CRASH;

}
