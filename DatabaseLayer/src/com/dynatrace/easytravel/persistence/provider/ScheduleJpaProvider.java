/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: ScheduleJpaProvider.java
 * @date: 18.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence.provider;

import com.dynatrace.easytravel.jpa.business.Schedule;
import com.dynatrace.easytravel.persistence.JpaDatabaseController;


/**
 *
 * @author stefan.moschinski
 */
public class ScheduleJpaProvider extends JpaProvider<Schedule> implements ScheduleProvider {

	/**
	 * 
	 * @param controller
	 * @param cls
	 * @author stefan.moschinski
	 */
	public ScheduleJpaProvider(JpaDatabaseController controller) {
		super(controller, Schedule.class);
	}

	@Override
	public Schedule getScheduleByName(String name) {
		return find(name);
	}

}
