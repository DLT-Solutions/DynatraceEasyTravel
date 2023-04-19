/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: NullScheduleProvider.java
 * @date: 16.01.2013
 * @author: stmo
 */
package com.dynatrace.easytravel.persistence.provider.util;

import java.util.Collection;
import java.util.Collections;

import com.dynatrace.easytravel.jpa.business.Schedule;
import com.dynatrace.easytravel.persistence.provider.ScheduleProvider;


/**
 *
 * @author stmo
 */
public class NullScheduleProvider implements ScheduleProvider {

	@Override
	public Schedule add(Schedule value) {
		return value;
	}

	@Override
	public Schedule update(Schedule value) {
		return value;
	}

	@Override
	public Collection<Schedule> getAll() {
		return Collections.emptyList();
	}

	@Override
	public Collection<Schedule> getWithLimit(int limit) {
		return Collections.emptyList();
	}

	@Override
	public int getCount() {
		return 0;
	}

	@Override
	public Schedule getScheduleByName(String name) {
		return null;
	}

	@Override
	public void reset() {
	}

}
