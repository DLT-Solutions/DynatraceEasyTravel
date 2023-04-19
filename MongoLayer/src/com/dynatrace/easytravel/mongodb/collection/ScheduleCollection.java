/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: ScheduleCollection.java
 * @date: 20.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb.collection;

import static com.dynatrace.easytravel.jpa.business.Schedule.SCHEDULE_NAME;

import com.dynatrace.easytravel.jpa.business.Schedule;
import com.dynatrace.easytravel.persistence.provider.ScheduleProvider;
import com.mongodb.DB;
/**
 *
 * @author stefan.moschinski
 */
public class ScheduleCollection extends MongoDbCollection<Schedule> implements ScheduleProvider {

	private static final String SCHEDULE_COLLECTION_NAME = "ScheduleCollection";

	/**
	 * 
	 * @param database
	 * @param collectionName
	 * @param marshaller
	 * @author stefan.moschinski
	 */
	public ScheduleCollection(DB database) {
		super(database, SCHEDULE_COLLECTION_NAME);
	}

	@Override
	public Schedule getScheduleByName(String scheduleName) {
		return findOneByKeyValue(SCHEDULE_NAME, scheduleName);
	}

}
