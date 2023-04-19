/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: InMemoryLocationProvider.java
 * @date: 09.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence.provider.util;

import java.util.Collection;

import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.persistence.provider.LocationProvider;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;


/**
 *
 * @author stefan.moschinski
 */
public class InMemoryLocationProvider extends InMemoryEasytravelPersistenceProvider<Location> implements LocationProvider {


	@Override
	public boolean deleteLocation(String name) {
		return delete(getLocationByName(name));
	}

	@Override
	public Collection<Location> getLocations(int fromIdx, int count) {
		return null;
	}

	@Override
	public Location getLocationByName(final String locationName) {
		Collection<Location> filterBy = filterBy(new Predicate<Location>() {

			@Override
			public boolean apply(Location location) {
				return location.getName().equals(locationName);
			}
			
			public boolean test(Location location) {
				return apply(location);
			}
		});
		if (filterBy.isEmpty()) {
			return null;
		}

		return filterBy.iterator().next();
	}

	@Override
	public Collection<Location> getMatchingLocations(final String locationNamePart) {
		return filterBy(new Predicate<Location>() {

			@Override
			public boolean apply(Location location) {
				return location.getName().contains(locationNamePart);
			}
			
			public boolean test(Location location) {
				return apply(location);
			}
		});
	}

	private Collection<Location> filterBy(final Predicate<Location> predicate) {
		return FluentIterable.from(getValues()).filter(predicate).toList();
	}


	@Override
	public void verifyLocation(int sleepTime) {
	}

}
