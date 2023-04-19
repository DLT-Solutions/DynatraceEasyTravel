/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: AbstractBusinessDatabaseController.java
 * @date: 10.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence.controller;

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
public abstract class AbstractBusinessDatabaseController extends DelegateBasedDatabaseController implements BusinessDatabaseController {

	private TenantProvider tenantProvider;
	private JourneyProvider journeyProvider;
	private BookingProvider bookingProvider;
	private ScheduleProvider scheduleProvider;
	private UserProvider userProvider;
	private LoginHistoryProvider loginHistoryProvider;
	private LocationProvider locationProvider;

	protected AbstractBusinessDatabaseController(DatabaseController controller) {
		super(controller);
	}


	@Override
	public synchronized JourneyProvider getJourneyProvider() {
		if (journeyProvider == null) {
			journeyProvider = createJourneyProviderInternal();
		}
		return journeyProvider;
	}


	@Override
	public synchronized BookingProvider getBookingProvider() {
		if (bookingProvider == null) {
			bookingProvider = createBookingProviderInternal();
		}
		return bookingProvider;
	}


	@Override
	public synchronized TenantProvider getTenantProvider() {
		if (tenantProvider == null) {
			tenantProvider = createTenantProviderInternal();
		}
		return tenantProvider;
	}



	@Override
	public synchronized LocationProvider getLocationProvider() {
		if (locationProvider == null) {
			locationProvider = createLocationProviderInternal();
		}
		return locationProvider;
	}

	@Override
	public synchronized UserProvider getUserProvider() {
		if (userProvider == null) {
			userProvider = createUserProviderInternal();
		}
		return userProvider;
	}


	@Override
	public synchronized LoginHistoryProvider getLoginHistoryProvider() {
		if (loginHistoryProvider == null) {
			loginHistoryProvider = createLoginHistoryProviderInternal();
		}
		return loginHistoryProvider;
	}


	@Override
	public synchronized ScheduleProvider getScheduleProvider() {
		if (scheduleProvider == null) {
			scheduleProvider = createScheduleProviderInternal();
		}
		return scheduleProvider;
	}


	protected abstract UserProvider createUserProviderInternal();

	protected abstract JourneyProvider createJourneyProviderInternal();

	protected abstract TenantProvider createTenantProviderInternal();

	protected abstract BookingProvider createBookingProviderInternal();

	protected abstract LocationProvider createLocationProviderInternal();

	protected abstract LoginHistoryProvider createLoginHistoryProviderInternal();

	protected abstract ScheduleProvider createScheduleProviderInternal();

}
