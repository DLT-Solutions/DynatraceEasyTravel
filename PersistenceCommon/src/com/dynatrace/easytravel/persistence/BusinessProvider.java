/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: BusinessProvider.java
 * @date: 21.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence;

import com.dynatrace.easytravel.persistence.provider.BookingProvider;
import com.dynatrace.easytravel.persistence.provider.JourneyProvider;
import com.dynatrace.easytravel.persistence.provider.LocationProvider;
import com.dynatrace.easytravel.persistence.provider.LoginHistoryProvider;
import com.dynatrace.easytravel.persistence.provider.ScheduleProvider;
import com.dynatrace.easytravel.persistence.provider.TenantProvider;
import com.dynatrace.easytravel.persistence.provider.UserProvider;


/**
 *
 * @author stefan.moschinski
 */
public interface BusinessProvider {

	/**
	 * 
	 * @return
	 * @author stefan.moschinski
	 */
	JourneyProvider getJourneyProvider();

	/**
	 * 
	 * @return
	 * @author stefan.moschinski
	 */
	BookingProvider getBookingProvider();

	/**
	 * 
	 * @return
	 * @author stefan.moschinski
	 */
	TenantProvider getTenantProvider();

	/**
	 * 
	 * @return
	 * @author stefan.moschinski
	 */
	LocationProvider getLocationProvider();

	/**
	 * 
	 * @return
	 * @author stefan.moschinski
	 */
	UserProvider getUserProvider();

	/**
	 * 
	 * @return
	 * @author stefan.moschinski
	 */
	LoginHistoryProvider getLoginHistoryProvider();

	/**
	 * 
	 * @return
	 * @author stefan.moschinski
	 */
	ScheduleProvider getScheduleProvider();


}
