/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: TestDatabase.java
 * @date: 21.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dynatrace.easytravel.persistence.controller.BusinessDatabaseController;
import com.dynatrace.easytravel.persistence.provider.JourneyProvider;
import com.dynatrace.easytravel.persistence.provider.LocationProvider;


/**
 *
 * @author stefan.moschinski
 */
public class TestDatabase extends AbstractDatabase {

	public TestDatabase(String name) {
		super(name);
	}


	@Override
	public BusinessDatabaseController createNewBusinessController() {
		BusinessDatabaseController mock = mock(BusinessDatabaseController.class);

		JourneyProvider mock2 = mock(JourneyProvider.class);
		when(mock.getJourneyProvider()).thenReturn(mock2);
		LocationProvider mock3 = mock(LocationProvider.class);
		when(mock.getLocationProvider()).thenReturn(mock3);

		return mock;
	}
}
