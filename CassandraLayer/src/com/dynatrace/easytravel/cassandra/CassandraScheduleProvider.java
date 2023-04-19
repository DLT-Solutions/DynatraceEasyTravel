package com.dynatrace.easytravel.cassandra;

import java.util.Collection;

import com.dynatrace.easytravel.cassandra.tables.objectmapper.ScheduleTable;
import com.dynatrace.easytravel.jpa.business.Schedule;
import com.dynatrace.easytravel.persistence.provider.ScheduleProvider;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class CassandraScheduleProvider implements ScheduleProvider {
	
	private final ScheduleTable scheduleTable;
	
	public CassandraScheduleProvider(ScheduleTable scheduleTable) {
		this.scheduleTable = scheduleTable;
	}

	@Override
	public Schedule add(Schedule value) {
		scheduleTable.addModel(value);
		return value;
	}

	@Override
	public Schedule update(Schedule value) {
		scheduleTable.updateModel(value);
		return value;
	}

	@Override
	public Collection<Schedule> getAll() {
		return scheduleTable.getAllModels();
	}

	@Override
	public Collection<Schedule> getWithLimit(int limit) {
		return scheduleTable.getModelsWithLimit(limit);
	}

	@Override
	public int getCount() {
		return scheduleTable.getCount();
	}

	@Override
	public void reset() {
		scheduleTable.reset();		
	}

	@Override
	public Schedule getScheduleByName(String name) {
		return scheduleTable.getByName(name);
	}

}
