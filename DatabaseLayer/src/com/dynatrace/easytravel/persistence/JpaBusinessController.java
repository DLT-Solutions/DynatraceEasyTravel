/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: JpaBusinessDatabaseController.java
 * @date: 10.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence;

import com.dynatrace.easytravel.persistence.controller.AbstractBusinessDatabaseController;
import com.dynatrace.easytravel.persistence.provider.BookingJpaProvider;
import com.dynatrace.easytravel.persistence.provider.BookingProvider;
import com.dynatrace.easytravel.persistence.provider.JourneyJpaProvider;
import com.dynatrace.easytravel.persistence.provider.JourneyProvider;
import com.dynatrace.easytravel.persistence.provider.LocationJpaProvider;
import com.dynatrace.easytravel.persistence.provider.LocationProvider;
import com.dynatrace.easytravel.persistence.provider.LoginHistoryJpaProvider;
import com.dynatrace.easytravel.persistence.provider.LoginHistoryProvider;
import com.dynatrace.easytravel.persistence.provider.ScheduleJpaProvider;
import com.dynatrace.easytravel.persistence.provider.ScheduleProvider;
import com.dynatrace.easytravel.persistence.provider.TenantJpaProvider;
import com.dynatrace.easytravel.persistence.provider.TenantProvider;
import com.dynatrace.easytravel.persistence.provider.UserJpaProvider;
import com.dynatrace.easytravel.persistence.provider.UserProvider;


/**
 *
 * @author stefan.moschinski
 */
public class JpaBusinessController extends AbstractBusinessDatabaseController {


	private final JpaDatabaseController controller;

	/**
	 * 
	 * @author stefan.moschinski
	 */
	public JpaBusinessController(JpaDatabaseController controller) {
		super(controller);
		this.controller = controller;
	}

	@Override
	public LoginHistoryProvider getLoginHistoryProvider() {
		return new LoginHistoryJpaProvider(controller);
	}

	@Override
	protected UserProvider createUserProviderInternal() {
		return new UserJpaProvider(controller);
	}

	@Override
	protected JourneyProvider createJourneyProviderInternal() {
		return new JourneyJpaProvider(controller, getTenantProvider());
	}

	@Override
	protected TenantProvider createTenantProviderInternal() {
		return new TenantJpaProvider(controller);
	}

	@Override
	protected BookingProvider createBookingProviderInternal() {
		return new BookingJpaProvider(controller);
	}


	@Override
	protected LocationProvider createLocationProviderInternal() {
		return new LocationJpaProvider(controller);
	}


	@Override
	protected LoginHistoryProvider createLoginHistoryProviderInternal() {
		return new LoginHistoryJpaProvider(controller);
	}


	@Override
	protected ScheduleProvider createScheduleProviderInternal() {
		return new ScheduleJpaProvider(controller);
	}



}
