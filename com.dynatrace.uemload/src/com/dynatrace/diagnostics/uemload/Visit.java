package com.dynatrace.diagnostics.uemload;

import com.dynatrace.easytravel.misc.CommonUser;


public interface Visit {

	public Action[] getActions(CommonUser user, Location location);
	
	public String getVisitName();

}
