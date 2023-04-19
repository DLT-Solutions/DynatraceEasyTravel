/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: ScheduleProvider.java
 * @date: 18.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence.provider;

import com.dynatrace.easytravel.jpa.business.Schedule;


/**
 *
 * @author stefan.moschinski
 */
public interface ScheduleProvider extends EasyTravelPersistenceProvider<Schedule> {

	/**
	 * 
	 * @param name
	 * @return
	 * @author stefan.moschinski
	 */
	Schedule getScheduleByName(String name);

}
