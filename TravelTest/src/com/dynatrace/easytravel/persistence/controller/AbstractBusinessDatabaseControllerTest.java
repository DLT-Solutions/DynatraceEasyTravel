package com.dynatrace.easytravel.persistence.controller;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.dynatrace.easytravel.persistence.provider.*;


public class AbstractBusinessDatabaseControllerTest {

	@Test
	public void test() {
		AbstractBusinessDatabaseController cont = new AbstractBusinessDatabaseController(null) {

			@Override
			protected UserProvider createUserProviderInternal() {
				return new UserJpaProvider(null);
			}

			@Override
			protected TenantProvider createTenantProviderInternal() {
				return new TenantJpaProvider(null);
			}

			@Override
			protected ScheduleProvider createScheduleProviderInternal() {
				return new ScheduleJpaProvider(null);
			}

			@Override
			protected LoginHistoryProvider createLoginHistoryProviderInternal() {
				return new LoginHistoryJpaProvider(null);
			}

			@Override
			protected LocationProvider createLocationProviderInternal() {
				return new LocationJpaProvider(null);
			}

			@Override
			protected JourneyProvider createJourneyProviderInternal() {
				return new JourneyJpaProvider(null, null);
			}

			@Override
			protected BookingProvider createBookingProviderInternal() {
				return new BookingJpaProvider(null);
			}
		};

		assertNotNull(cont.getJourneyProvider());
		assertNotNull(cont.getBookingProvider());
		assertNotNull(cont.getTenantProvider());
		assertNotNull(cont.getLocationProvider());
		assertNotNull(cont.getUserProvider());
		assertNotNull(cont.getLoginHistoryProvider());
		assertNotNull(cont.getScheduleProvider());

		// second time to get cached providers
		assertNotNull(cont.getJourneyProvider());
		assertNotNull(cont.getBookingProvider());
		assertNotNull(cont.getTenantProvider());
		assertNotNull(cont.getLocationProvider());
		assertNotNull(cont.getUserProvider());
		assertNotNull(cont.getLoginHistoryProvider());
		assertNotNull(cont.getScheduleProvider());
	}

}
