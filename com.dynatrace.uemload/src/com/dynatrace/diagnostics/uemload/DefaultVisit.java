package com.dynatrace.diagnostics.uemload;

import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;
import com.dynatrace.easytravel.misc.CommonUser;

public class DefaultVisit implements Visit {

	private final Action[] actions;

	public DefaultVisit(Action... actions) {
		this.actions = actions;
	}

	@Override
	public Action[] getActions(CommonUser user, Location location) {
		return actions;
	}

	@Override
	public String getVisitName() {
		return VisitNames.DEFAULT;
	}

}
