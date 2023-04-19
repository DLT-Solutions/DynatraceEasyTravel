package com.dynatrace.diagnostics.uemload.scenarios;

import java.util.List;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravel.VisitLength;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.easytravel.misc.CommonUser;
import com.google.common.collect.Lists;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class PageWandererConvert extends PageWanderer {

	public PageWandererConvert(String host, boolean useRandomPage, VisitLength length) {
		super(host, useRandomPage, length);
	}

	@Override
	public Action[] getActions(CommonUser user, Location location) {
		
		CustomerSession session = EasyTravel.createCustomerSession(host, user, location);
		List<Action> actions = Lists.newArrayList();
		
		addSetupActions(session, actions);
		addMultipleSearch(session, actions);
		addSearchWithConversion(session, actions);
		addEndOfSession(session, actions, location);
		
		return actions.toArray(new Action[actions.size()]);
	}
	
	@Override
	public String getVisitName() {
		return "Page Wanderer Convert";
	}
}
