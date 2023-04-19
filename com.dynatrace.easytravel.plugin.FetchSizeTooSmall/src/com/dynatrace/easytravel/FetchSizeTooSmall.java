package com.dynatrace.easytravel;

import javax.persistence.Query;

import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

public class FetchSizeTooSmall extends AbstractGenericPlugin {

	@Override
	public Object doExecute(String location, Object... context) {
		Query query = (Query) context[0];
		query.setHint("org.hibernate.fetchSize", 1);
		return null;
	}
}
