/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: NullJourneyProvider.java
 * @date: 08.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence.provider.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.persistence.provider.JourneyProvider;


/**
 *
 * @author stefan.moschinski
 */
public class NullJourneyProvider extends InMemoryEasytravelPersistenceProvider<Journey> implements JourneyProvider {

	@Override
	public Collection<Journey> findJourneys(String destination, Date fromDate, Date toDate, boolean normalize) {
		return Collections.emptyList();
	}

	@Override
	public Journey getJourneyById(Integer id) {
		return null;
	}

	@Override
	public Collection<Journey> getJourneysByTenant(String tenantName) {
		return Collections.emptyList();
	}

	@Override
	public Collection<Journey> getJourneysByTenant(String tenantName, int fromIdx, int count) {
		return Collections.emptyList();
	}

	@Override
	public int getJourneyCountByTenant(String tenantName) {
		return 0;
	}

	@Override
	public int getJourneyIndexByName(String tenantName, String journeyName) {
		return 0;
	}

	@Override
	public Journey getJourneyByName(String journeyName) {
		return null;
	}

	@Override
	public Collection<Integer> getAllJourneyIds() {
		return Collections.emptyList();
	}

	@Override
	public boolean isJourneyDestination(String locationName) {
		return false;
	}

	@Override
	public boolean isJourneyStart(String locationName) {
		return false;
	}

	@Override
	public Collection<Location> getMatchingJourneyDestinations(String name, boolean normalize) {
		return Collections.emptyList();
	}

	@Override
	public void removeJourneyById(int id) {
	}

	@Override
	public int refreshJourneys() {
		return 0;
	}

	@Override
	public Journey getJourneyByIdNormalize(Integer id, boolean normalize) {
		return getJourneyById(id);
	}


}
