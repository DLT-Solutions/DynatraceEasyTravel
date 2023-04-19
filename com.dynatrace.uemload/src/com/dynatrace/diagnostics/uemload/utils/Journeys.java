/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: Locations.java
 * @date: 21.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.diagnostics.uemload.utils;


/**
 *
 * @author stefan.moschinski
 */
public class Journeys {

	public static final int NO_JOURNEY_FOUND = Integer.MIN_VALUE;
	

	public static boolean isValidJourneyId(int journeyId) {
		return journeyId != NO_JOURNEY_FOUND && journeyId != 0;
	}
}
