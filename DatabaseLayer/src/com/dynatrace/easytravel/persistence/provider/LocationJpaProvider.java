/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: LocationJpaProvider.java
 * @date: 19.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence.provider;

import static com.dynatrace.easytravel.jpa.business.Location.LOCATION_NAME;

import java.util.Collection;

import javax.persistence.Query;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.jpa.QueryNames;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.persistence.JpaDatabaseController;
/**
 *
 * @author stefan.moschinski
 */
public class LocationJpaProvider extends JpaProvider<Location> implements LocationProvider {

	/**
	 *
	 * @param controller
	 * @author stefan.moschinski
	 */
	public LocationJpaProvider(JpaDatabaseController controller) {
		super(controller, Location.class);
	}

	@Override
	public boolean deleteLocation(String name) {
		Location find = find(name);
		if (find == null)
			return false;
		remove(find);
		return true;
	}

	@Override
	public Collection<Location> getLocations(int fromIdx, int count) {
		return createNamedQuery(QueryNames.LOCATION_ALL, Location.class)
				.setFirstResult(fromIdx)
				.setMaxResults(count)
				.getResultList();
	}

	@Override
	public Location getLocationByName(String locationName) {
		return find(locationName);
	}

	@Override
	public Collection<Location> getMatchingLocations(String locationName) {
		return createNamedQuery(QueryNames.LOCATION_FIND).setParameter(LOCATION_NAME, locationName).getResultList();
	}

	@Override
	public void verifyLocation(int sleepTime) {
		String queryStr;
		if (EasyTravelConfig.isDerbyDatabase() || EasyTravelConfig.isMySqlDatabase()) {
			queryStr = "call verify_location(?)";
		} else if (EasyTravelConfig.isOracleDatabase()) {
			queryStr = "{call verify_location(?)}";
		} else { 
			queryStr = "exec sp_verifyLocation @location=?";
		}

		Query query = createNativeQuery(queryStr);
		query.setParameter(1, sleepTime);
		query.executeUpdate();
	}
}
