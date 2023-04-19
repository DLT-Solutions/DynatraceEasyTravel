/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: MongoDbBusinessController.java
 * @date: 10.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb;

import com.dynatrace.easytravel.mongodb.collection.BookingCollection;
import com.dynatrace.easytravel.mongodb.collection.JourneyCollection;
import com.dynatrace.easytravel.mongodb.collection.LocationCollection;
import com.dynatrace.easytravel.mongodb.collection.LoginHistoryCollection;
import com.dynatrace.easytravel.mongodb.collection.ScheduleCollection;
import com.dynatrace.easytravel.mongodb.collection.TenantCollection;
import com.dynatrace.easytravel.mongodb.collection.UserCollection;
import com.dynatrace.easytravel.persistence.controller.AbstractBusinessDatabaseController;
import com.dynatrace.easytravel.persistence.provider.BookingProvider;
import com.dynatrace.easytravel.persistence.provider.JourneyProvider;
import com.dynatrace.easytravel.persistence.provider.LocationProvider;
import com.dynatrace.easytravel.persistence.provider.LoginHistoryProvider;
import com.dynatrace.easytravel.persistence.provider.ScheduleProvider;
import com.dynatrace.easytravel.persistence.provider.TenantProvider;
import com.dynatrace.easytravel.persistence.provider.UserProvider;
import com.mongodb.DB;


/**
 *
 * @author stefan.moschinski
 */
public class MongoDbBusinessController extends AbstractBusinessDatabaseController {


	private final DB database;

	/**
	 * 
	 * @author stefan.moschinski
	 */
	public MongoDbBusinessController(MongoDbController delegateController) {
		super(delegateController);
		this.database = delegateController.getDatabase();
	}


	@Override
	protected UserProvider createUserProviderInternal() {
		return new UserCollection(database);
	}

	@Override
	protected JourneyProvider createJourneyProviderInternal() {
		return new JourneyCollection(database);
	}

	@Override
	protected TenantProvider createTenantProviderInternal() {
		return new TenantCollection(database);
	}

	@Override
	protected BookingProvider createBookingProviderInternal() {
		return new BookingCollection(database);
	}

	@Override
	protected LocationProvider createLocationProviderInternal() {
		return new LocationCollection(database);
	}


	@Override
	protected LoginHistoryProvider createLoginHistoryProviderInternal() {
		return new LoginHistoryCollection(database);
	}

	@Override
	protected ScheduleProvider createScheduleProviderInternal() {
		return new ScheduleCollection(database);
	}

}
