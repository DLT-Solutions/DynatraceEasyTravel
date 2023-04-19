package com.dynatrace.easytravel.business.cache;

import java.util.Collection;
import java.util.Random;

import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.persistence.provider.LocationProvider;

/**
 *
 *
 * @author peter.kaiser
 */
public class LocationCache implements LocationProvider {

	private static final int MAX_LOCATION_VERIFICATION_TIME = 10000;
	private static final Random random = new Random();

	private LocationProvider locationProvider;

    /**
     *
     *
     * @author peter.kaiser
     */
    public LocationCache() {
    }


    /* (non-Javadoc)
     * @see com.dynatrace.easytravel.business.cache.JourneyProvider#setDatabaseAccess(com.dynatrace.easytravel.model.DatabaseAccess)
     */
	public void setDatabaseAccess(DataAccess dataAccess) {
		this.locationProvider = dataAccess.getLocationProvider();
    }


    @Override
	public synchronized Collection<Location> getAll() {
		return locationProvider.getAll();
    }


    @Override
    public synchronized boolean deleteLocation(String name) {
		return locationProvider.deleteLocation(name);
    }


    public synchronized boolean addLocation(String name) {
		locationProvider.verifyLocation(random.nextInt(MAX_LOCATION_VERIFICATION_TIME));
		locationProvider.add(new Location(name));
		return true;
    }


    @Override
	public synchronized Collection<Location> getLocations(int fromIdx, int count) {
		return locationProvider.getLocations(fromIdx, count);
    }


    @Override
	public synchronized int getCount() {
		return locationProvider.getCount();
    }


	@Override
	public Location add(Location location) {
		return locationProvider.add(location);
	}


	@Override
	public Location getLocationByName(String locationName) {
		return locationProvider.getLocationByName(locationName);
	}


	@Override
	public Collection<Location> getMatchingLocations(String locationNamePart) {
		return locationProvider.getMatchingLocations(locationNamePart);
	}


	@Override
	public Location update(Location value) {
		return locationProvider.update(value);
	}


	@Override
	public Collection<Location> getWithLimit(int limit) {
		return locationProvider.getWithLimit(limit);
	}


	@Override
	public void verifyLocation(int sleepTime) {
		locationProvider.verifyLocation(sleepTime);
	}


	@Override
	public void reset() {
		locationProvider.reset();
	}

}