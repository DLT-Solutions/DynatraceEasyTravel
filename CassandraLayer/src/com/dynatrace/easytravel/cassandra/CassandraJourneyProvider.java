package com.dynatrace.easytravel.cassandra;

import java.util.Collection;
import java.util.Date;

import com.dynatrace.easytravel.cassandra.tables.objectmapper.JourneyTable;
import com.dynatrace.easytravel.cassandra.tables.objectmapper.LocationSearchTable;
import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.persistence.provider.JourneyProvider;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class CassandraJourneyProvider implements JourneyProvider {

	private final JourneyTable journeyTable;
	private final LocationSearchTable locationSearchTable;
	
	public CassandraJourneyProvider(JourneyTable journeyTable, LocationSearchTable locationSearchTable) {
		this.journeyTable = journeyTable;
		this.locationSearchTable = locationSearchTable;
	}
	
	@Override
	public Journey add(Journey value) {
		journeyTable.addModel(value);
		return value;
	}

	@Override
	public Journey update(Journey value) {
		journeyTable.updateModel(value);
		return value;
	}

	@Override
	public Collection<Journey> getAll() {
		return journeyTable.getAllModels();
	}

	@Override
	public Collection<Journey> getWithLimit(int limit) {
		return journeyTable.getModelsWithLimit(limit);
	}

	@Override
	public int getCount() {
		return journeyTable.getCount();
	}

	@Override
	public void reset() {
		journeyTable.reset();
	}

	@Override
	public Collection<Journey> findJourneys(String destination, Date fromDate, Date toDate, boolean normalize) {
		return journeyTable.findJourneys(destination, fromDate, toDate, normalize);
	}

	@Override
	public Journey getJourneyById(Integer id) {
		return journeyTable.getJourneyById(id);
	}

	@Override
	public Journey getJourneyByIdNormalize(Integer id, boolean normalize) {
		return journeyTable.getJourneyById(id);
	}

	@Override
	public Collection<Journey> getJourneysByTenant(String tenantName) {
		return journeyTable.getJourneysByTenant(tenantName);
	}

	@Override
	public Collection<Journey> getJourneysByTenant(String tenantName, int fromIdx, int count) {
		return journeyTable.getJourneysByTenant(tenantName, fromIdx, count);
	}

	@Override
	public int getJourneyCountByTenant(String tenantName) {
		return journeyTable.getJourneyCountByTenant(tenantName);
	}

	@Override
	public int getJourneyIndexByName(String tenantName, String journeyName) {
		return journeyTable.getJourneyIndexByName(tenantName, journeyName);
	}

	@Override
	public Journey getJourneyByName(String journeyName) {
		return journeyTable.getJourneyByName(journeyName);
	}

	@Override
	public Collection<Integer> getAllJourneyIds() {
		return journeyTable.getAllJourneyIds();
	}

	@Override
	public boolean isJourneyDestination(String locationName) {
		return !journeyTable.getJourneyIdsForDestination(locationName).isEmpty();
	}

	@Override
	public boolean isJourneyStart(String locationName) {
		return !journeyTable.getJourneyIdsForDeparture(locationName).isEmpty();
	}

	@Override
	public Collection<Location> getMatchingJourneyDestinations(String name, boolean normalize) {
		return locationSearchTable.getMatchingLocations(name);
	}

	@Override
	public void removeJourneyById(int id) {
		journeyTable.removeJourneyById(id);
	}

	@Override
	public int refreshJourneys() {
		return 0;
	}

}
