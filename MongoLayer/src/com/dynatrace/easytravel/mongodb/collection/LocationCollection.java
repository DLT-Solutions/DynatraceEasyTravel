/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 * 
 * @file: JourneyCollection.java
 * @date: 13.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb.collection;

import static com.dynatrace.easytravel.mongodb.SimpleQueryBuilder.buildQuery;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.mongodb.MongoConstants;
import com.dynatrace.easytravel.persistence.provider.LocationProvider;
import com.google.common.base.Strings;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteResult;

/**
 * 
 * @author stefan.moschinski
 */
public class LocationCollection extends MongoDbCollection<Location> implements
		LocationProvider {

	private static final String LOCATION_COLLECTION_NAME = "LocationCollection";

	public LocationCollection(DB database) {
		super(database, LOCATION_COLLECTION_NAME);
	}

	@Override
	public Location getLocationByName(String locationName) {
		return findOneByKeyValue(MongoConstants.ID, locationName);
	}

	@Override
	public boolean deleteLocation(String locationName) {
		WriteResult deleteResult = delete(buildQuery(MongoConstants.ID).value(locationName).create());
		int noOfdeletedLocations = deleteResult.getN();
		return noOfdeletedLocations > 0;
	}

	@Override
	public Collection<Location> getLocations(int fromIdx, int count) {
		return find(NO_CONDITION, fromIdx, count);
	}

	@Override
	public Collection<Location> getMatchingLocations(String locationNamePart) {
		if (Strings.isNullOrEmpty(locationNamePart) || locationNamePart.length() < 3) {
			return Collections.emptyList();
		}

		DBObject patternQuery = createPatternForLocationName(locationNamePart);
		return find(patternQuery);
	}

	private DBObject createPatternForLocationName(String locationNamePart) {
		Pattern pattern = Pattern.compile(".*" + locationNamePart + ".*", Pattern.CASE_INSENSITIVE);
		return QueryBuilder.start(MongoConstants.ID).regex(pattern).get();
	}

	@Override
	public void verifyLocation(int sleepTime) {
		// DO NOTHING
	}



}
