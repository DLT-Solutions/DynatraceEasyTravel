/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: QueryWithSessionID.java
 * @date: 11.10.2012
 * @author: cwat-ruttenth
 */
package com.dynatrace.easytravel.booking;

import java.util.concurrent.atomic.AtomicBoolean;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

/**
 *
 * @author cwat-ruttenth
 */
public class CaptureBookingID extends AbstractGenericPlugin {

	 private static Logger log = LoggerFactory.make();

	/* (non-Javadoc)
	 * @see com.dynatrace.easytravel.spring.AbstractGenericPlugin#doExecute(java.lang.String, java.lang.Object[])
	 */
	@Override
	protected Object doExecute(String location, Object... context) {
		log.info("Plugin \"CaptureBookingID\" in business backend is enabled.");
		// set AtomicBoolean "measureExplosionByBookingIdEnabled" to true in BookingService
		((AtomicBoolean)context[3]).set(true);
		return null;
	}

}
