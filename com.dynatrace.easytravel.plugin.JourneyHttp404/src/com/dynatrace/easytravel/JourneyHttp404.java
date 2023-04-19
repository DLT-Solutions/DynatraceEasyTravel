package com.dynatrace.easytravel;

import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

public class JourneyHttp404 extends AbstractGenericPlugin {

    @Override
	public Object doExecute(String location, Object... context) {
		// creates a http 404 - Not Found error while searching for journeys
		JourneyDO[] journeys = (JourneyDO[]) context[0];
		if (journeys != null && journeys.length > 0) {
			journeys[0].setPicture(null);
		}
		return null;
	}
}
