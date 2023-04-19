/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: LocationProviderTest.java
 * @date: 04.02.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.persistence.provider.LocationProvider;
import com.google.common.collect.Lists;


/**
 *
 * @author stefan.moschinski
 */
public class LocationProviderTest extends EasyTravelPersistenceProviderTest<LocationProvider> {

	private static final Logger log = Logger.getLogger(LocationProviderTest.class.getName());

	@Test
	public void testGetUserByName() throws Exception {
		Location dresden = new Location("dresden");
		Location linz = new Location("linz");

		provider.add(dresden);
		provider.add(linz);

		assertThat(provider.getCount(), is(2));
		assertThat(provider.getLocationByName("dresden"), is(dresden));
		assertThat(provider.getLocationByName("linz"), is(linz));
	}

	@Test
	public void testGetLocationsByName() throws Exception {
		Location mannheim = new Location("Mannheim");
		Location anaheim = new Location("Anaheim");

		provider.add(mannheim);
		provider.add(anaheim);

		assertThat(provider.getMatchingLocations("mann"), contains(mannheim));
		assertThat(provider.getMatchingLocations("ana"), contains(anaheim));
		assertThat(provider.getMatchingLocations("heim"), containsInAnyOrder(mannheim, anaheim));

		assertThat(provider.getMatchingLocations("hEIm"), containsInAnyOrder(mannheim, anaheim));
	}

	@Test
	@Ignore("Check was moved to GenericAccess class")
	public void testGetLocationsByNameNullOrEmpty() throws Exception {
		Location mannheim = new Location("mannheim");
		Location anaheim = new Location("anaheim");

		provider.add(mannheim);
		provider.add(anaheim);

		assertThat(provider.getMatchingLocations(null), emptyCollectionOf(Location.class));
		assertThat(provider.getMatchingLocations(""), emptyCollectionOf(Location.class));
		assertThat(provider.getMatchingLocations("heims"), emptyCollectionOf(Location.class));
	}

	@Test
	public void testDeleteLocation() throws Exception {
		Location mannheim = new Location("Mannheim");
		Location anaheim = new Location("Anaheim");

		provider.add(mannheim);
		provider.add(anaheim);

		assertThat(provider.getCount(), is(2));

		assertThat(provider.deleteLocation("Mannheim"), is(true));
		assertThat(provider.deleteLocation("Mannheim"), is(false));
		assertThat(provider.getCount(), is(1));

		assertThat(provider.deleteLocation("Anaheim"), is(true));
		assertThat(provider.deleteLocation("Anaheim"), is(false));
		assertThat(provider.getCount(), is(0));

	}

	@Test
	public void testGetLocations() throws Exception {
		int no = 200;
		ArrayList<Location> expected = Lists.newArrayList();

		long start = System.currentTimeMillis();
		for (int i = 0; i < no; i++) {
			Location location = new Location("location" + i);
			provider.add(location);
			expected.add(location);
		}
		log.info(format("It took %d ms to create %d journeys", System.currentTimeMillis() - start, no));

		assertThat(provider.getLocations(100, 100).size(), is(100));
//		assertThat(provider.getLocations(100, 100), contains(expected.subList(100, 200).toArray(new Location[0])));
	}
}
