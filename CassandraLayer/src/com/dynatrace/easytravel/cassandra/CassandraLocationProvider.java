package com.dynatrace.easytravel.cassandra;

import java.util.Collection;

import com.dynatrace.easytravel.cassandra.tables.objectmapper.LocationNameTable;
import com.dynatrace.easytravel.cassandra.tables.objectmapper.LocationSearchTable;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.persistence.provider.LocationProvider;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class CassandraLocationProvider implements LocationProvider {
	
	private final LocationNameTable locationNameTable;
	private final LocationSearchTable locationSearchTable;
	
	public CassandraLocationProvider(LocationNameTable locationNameTable, LocationSearchTable locationSearchTable) {
		this.locationNameTable = locationNameTable;
		this.locationSearchTable = locationSearchTable;
	}

	@Override
	public Location add(Location value) {
		locationNameTable.addModel(value);
		locationSearchTable.addModel(value);
		return value;
	}

	@Override
	public Location update(Location value) {
		locationNameTable.updateModel(value);
		locationSearchTable.updateModel(value);
		return value;
	}

	@Override
	public Collection<Location> getAll() {
		return locationSearchTable.getAllModels();
	}

	@Override
	public Collection<Location> getWithLimit(int limit) {
		return locationSearchTable.getModelsWithLimit(limit);
	}

	@Override
	public int getCount() {
		return locationSearchTable.getCount();
	}

	@Override
	public void reset() {
		locationNameTable.reset();
		locationSearchTable.reset();
	}

	@Override
	public boolean deleteLocation(String name) {
		return locationNameTable.deleteLocationByName(name);
	}

	@Override
	public Collection<Location> getLocations(int fromIdx, int count) {
		return locationNameTable.getLocations(fromIdx, count);
	}

	@Override
	public Location getLocationByName(String locationName) {
		return locationNameTable.getLocationByName(locationName);
	}

	@Override
	public Collection<Location> getMatchingLocations(String locationNamePart) {
		return locationSearchTable.getMatchingLocations(locationNamePart);
	}

	@Override
	public void verifyLocation(int sleepTime) {
		// TODO Auto-generated method stub
	}

}
