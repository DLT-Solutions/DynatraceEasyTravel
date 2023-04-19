/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: LocationCacheTest.java
 * @date: 03.10.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.business.cache;

import static org.easymock.EasyMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;

import java.util.Collection;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.database.DatabaseWithContentAndPlugins;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.model.GenericDataAccess;
import com.dynatrace.easytravel.persistence.Database;
import com.dynatrace.easytravel.persistence.SqlDatabase;
import com.dynatrace.easytravel.persistence.provider.LocationProvider;


/**
 *
 * @author dominik.stadler
 */
public class LocationCacheTest extends DatabaseWithContentAndPlugins {

	private Database sqlDatabase;

	private LocationProvider locationProvider;


	@Before
	public void setUp() {
		sqlDatabase = new SqlDatabase().initialize();

		locationProvider = sqlDatabase.getBusinessController().getLocationProvider();
		assertNotNull(locationProvider);
		sqlDatabase.getBusinessController().startTransaction();
	}

	@After
	public void tearDown() {
		sqlDatabase.getBusinessController().rollbackTransaction();
		sqlDatabase.closeConnection();
	}

	/**
	 * Test method for {@link com.dynatrace.easytravel.business.cache.LocationCache#LocationCache()}.
	 */
	@Test
	public void testLocationCache() {
		LocationCache cache = new LocationCache();
		cache.setDatabaseAccess(new GenericDataAccess(sqlDatabase.getBusinessController()));

		int count = cache.getCount();
		assertTrue(cache.addLocation("location1"));
		assertEquals(count + 1, cache.getCount());

		Collection<Location> locations = cache.getMatchingLocations("location");
		assertThat(locations, contains(new Location("location1")));

		cache.deleteLocation("location1");

		assertEquals(count, cache.getAll().size());

		locations = cache.getLocations(0, 3);

		assertNotNull(locations);
		assertEquals(3, locations.size());

		cache.add(new Location("test"));
		assertNotNull(cache.getLocationByName("test"));
		cache.update(new Location("test"));
		assertEquals(100, cache.getWithLimit(100).size());
		cache.verifyLocation(100);

		try {
			cache.reset();
			fail("Expect to fail with NotImplementedException");
		} catch (NotImplementedException e) {
			// nothing to do
		}
	}

	@Test
	public void testWithDummy() {
		LocationCache cache = new LocationCache();

		DataAccess data = createNiceMock(DataAccess.class);
		LocationProvider provider = createNiceMock(LocationProvider.class);

		expect(data.getLocationProvider()).andReturn(provider);

		replay(data, provider);

		cache.setDatabaseAccess(data);
		cache.reset();

		verify(data, provider);
	}
}
