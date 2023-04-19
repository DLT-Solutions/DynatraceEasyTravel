package com.dynatrace.easytravel;

import com.dynatrace.easytravel.jpa.QueryNames;
import com.dynatrace.easytravel.jpa.QueryOverride;
import com.dynatrace.easytravel.spring.AbstractPlugin;

/**
 * Overrides findJourneys for the Sql Server.
 *
 * @author philipp.grasboeck
 */
public class FindJourneysSqlServerQueryOverride extends AbstractPlugin implements QueryOverride {

	// the SQLServer Stored Procedure
	private static final String SP_FIND_JOURNEYS = "exec sp_findJourneys @destination=:destination, @fromDate=:fromDate, @toDate=:toDate";

	@Override
	public String getQueryName() {
		return QueryNames.JOURNEY_FIND;
	}

	@Override
	public boolean isNative() {
		return true;
	}

	@Override
	public String getQueryText() {
		return SP_FIND_JOURNEYS;
	}
}
