/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 * 
 * @file: JourneyCollection.java
 * @date: 13.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb.collection;

import static com.dynatrace.easytravel.jpa.business.Journey.*;
import static com.dynatrace.easytravel.mongodb.SimpleQueryBuilder.buildQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.mongodb.MongoConstants;
import com.dynatrace.easytravel.mongodb.MongoIndex;
import com.dynatrace.easytravel.mongodb.dbobject.LocationMongoMarshaller;
import com.dynatrace.easytravel.persistence.provider.JourneyProvider;
import com.google.common.collect.Lists;
import com.mongodb.*;

/**
 * 
 * @author stefan.moschinski
 */
public class JourneyCollection extends MongoDbCollection<Journey> implements JourneyProvider {
	private static final String JOURNEY_COLLECTION_NAME = "JourneyCollection";

	public JourneyCollection(DB database) {
		super(
				database,
				JOURNEY_COLLECTION_NAME,
				MongoIndex.createCompoundIndex(JOURNEY_DESTINATION + "." + MongoConstants.ID, JOURNEY_FROM_DATE, JOURNEY_TO_DATE),
				MongoIndex.createIndex(JOURNEY_TENANT, MongoConstants.ID));
	}

	@Override
	public Journey getJourneyByName(String journeyName) {
		return findOneByKeyValue(JOURNEY_NAME, journeyName);
	}

	@Override
	public Journey add(Journey journey) {
		if (journey.getId() == 0) {
			journey.setId(journey.hashCode());
		}

		return super.add(journey);
	}

	@Override
	public Collection<Journey> findJourneys(String destination, Date fromDate, Date toDate, boolean normalize) {
		BasicDBObject query = new BasicDBObject(JOURNEY_DESTINATION + "." + MongoConstants.ID, destination);
		query.put(JOURNEY_FROM_DATE, buildQuery(QueryOperators.GTE).value(fromDate).create());
		query.put(JOURNEY_TO_DATE, buildQuery(QueryOperators.LTE).value(toDate).create());
		return find(query);
	}

	@Override
	public Journey getJourneyById(Integer id) {
		return findOneByKeyValue(MongoConstants.ID, id);
	}

	@Override
	public Collection<Journey> getJourneysByTenant(String tenantName) {
		return find(buildQuery(JOURNEY_TENANT, MongoConstants.ID).value(tenantName).create());
	}

	@Override
	public Collection<Journey> getJourneysByTenant(String tenantName, int fromIdx, int count) {
		return find(buildQuery(JOURNEY_TENANT, MongoConstants.ID).value(tenantName).create(), fromIdx, count);
	}

	@Override
	public int getJourneyCountByTenant(String tenantName) {
		return getSelectiveCount(buildQuery(JOURNEY_TENANT, MongoConstants.ID).value(tenantName).create());
	}

	@Override
	public int getJourneyIndexByName(String tenantName, String journeyName) {
		BasicDBObject query = new BasicDBObject(JOURNEY_TENANT + "." + MongoConstants.ID, tenantName);
		query.put(JOURNEY_NAME, journeyName);


		Journey foundJourney = findOne(query);
		return foundJourney == null ? 0 : foundJourney.getId();
	}

	@Override
	public Collection<Integer> getAllJourneyIds() {
		DBCursor find = getCollection().find(NO_CONDITION, buildQuery(MongoConstants.ID).value(1).create()); // we only need the
// id field
		List<Integer> ids = Lists.newArrayListWithCapacity(find.size());
		while (find.hasNext()) {
			BasicDBObject next = (BasicDBObject) find.next();
			ids.add(next.getInt(MongoConstants.ID));
		}
		return ids;
	}

	@Override
	public boolean isJourneyDestination(String name) {
		return findOneByKeyValue(JOURNEY_DESTINATION + "." + MongoConstants.ID, name) != null;
	}

	@Override
	public boolean isJourneyStart(String locationName) {
		return findOneByKeyValue(JOURNEY_START + "." + MongoConstants.ID, locationName) != null;
	}

	@Override
	public Collection<Location> getMatchingJourneyDestinations(String locationNamePart, boolean normalize) {
		return findUsing(JOURNEY_DESTINATION, locationNamePart);
	}

	private Collection<Location> findUsing(String journeyStr, String locationNamePart) {
		DBObject patternQuery = createPatternForLocationName(journeyStr, locationNamePart);

		BasicDBObject fields = new BasicDBObject();
		fields.put(journeyStr + "." + MongoConstants.ID, 1);
		fields.put(MongoConstants.ID, 0);

		Collection<DBObject> locations = find(patternQuery, fields);

		ArrayList<Location> results = Lists.newArrayListWithExpectedSize(locations.size());
		for (DBObject dbObject : locations) {
			LocationMongoMarshaller marshaller = (LocationMongoMarshaller) dbObject.get(journeyStr);
			results.add(marshaller.unmarshal());
		}
		return results;
	}

	private DBObject createPatternForLocationName(String key, String locationNamePart) {
		Pattern pattern = Pattern.compile(".*" + locationNamePart + ".*", Pattern.CASE_INSENSITIVE);
		return buildQuery(key, MongoConstants.ID).value(pattern).create();
	}

	@Override
	public void removeJourneyById(int id) {
		Collection<Journey> allJourney = getAll();
		for (Journey journey : allJourney) {
			if (journey.hashCode() == id) {
				delete(buildQuery(JOURNEY_NAME).value(journey.getName()).create());
				return;
			}
		}
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
