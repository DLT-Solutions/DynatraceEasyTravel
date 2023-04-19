package com.dynatrace.easytravel;

import java.util.Date;

import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

/**
 * Plugin to throw an Exception when the date range is invalid, i.e.
 * to-date after from-date.
 *
 */
public class JourneySearchRequestValidator extends AbstractGenericPlugin  {

	@Override
	public Object doExecute(String location, Object... context) {
		if (illegalArguments(context)) {
			return null;
		}
		Long fromDate = (Long) context[1];
		Long toDate = (Long) context[2];
		if (fromDate != null && toDate != null && fromDate > toDate) {
			throw new IllegalArgumentException("toDate is before fromDate: " + new Date(fromDate) + " > " + new Date(toDate));
		}

		return null;
	}

	private boolean illegalArguments(Object... context) {
		return context == null || context.length < 3 || !(context[1] instanceof Long) || !(context[2] instanceof Long);
	}
}
