/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: ScheduleMongoMarshaller.java
 * @date: 20.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb.dbobject;

import static com.dynatrace.easytravel.jpa.business.Schedule.SCHEDULE_LAST_EXECUTION;
import static com.dynatrace.easytravel.jpa.business.Schedule.SCHEDULE_PERIOD;

import com.dynatrace.easytravel.jpa.business.Schedule;
import com.dynatrace.easytravel.mongodb.MongoConstants;
import com.mongodb.DBObject;

/**
 *
 * @author stefan.moschinski
 */
public class ScheduleMongoMarshaller extends MongoObjectMarshaller<Schedule> {

	private static final long serialVersionUID = 1L;

	@Override
	protected DBObject marshalTypeSpecific(Schedule value, boolean withId) {
		if (withId) {
			put(MongoConstants.ID, value.getName());
		}
		put(SCHEDULE_LAST_EXECUTION, value.getLastExecution());
		put(SCHEDULE_PERIOD, value.getPeriod());
		return this;
	}

	@Override
	protected Schedule unmarshalTypeSpecific() {
		Schedule schedule = new Schedule();
		schedule.setName(getString(MongoConstants.ID));
		schedule.setLastExecution(getDate(SCHEDULE_LAST_EXECUTION));
		schedule.setPeriod(getLong(SCHEDULE_PERIOD));
		return schedule;
	}

	@Override
	public MongoObjectMarshaller<Schedule> newInstance() {
		return new ScheduleMongoMarshaller();
	}

}
