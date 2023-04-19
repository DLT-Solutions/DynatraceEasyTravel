package com.dynatrace.diagnostics.uemload.scenarios;

import java.util.List;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages.EasyTravelAMP;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;
import com.dynatrace.easytravel.misc.CommonUser;
import com.google.common.collect.Lists;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class EasyTravelAMPVisit implements Visit {
	
	private final String host;

	public EasyTravelAMPVisit(String host) {
		this.host = host;
	}

	@Override
	public Action[] getActions(CommonUser user, Location location) {
		CustomerSession session = EasyTravel.createCustomerSession(host, user, location);
		List<Action> actions = Lists.newArrayList();
		actions.add(new EasyTravelAMP(session, EasyTravelAMP.State.Init));
		for(int i=0;i<UemLoadUtils.randomInt(2, 9);i++) {
			actions.add(new EasyTravelAMP(session, EasyTravelAMP.State.Random));
		}
		actions.add(new EasyTravelAMP(session, EasyTravelAMP.State.Finish));
		return actions.toArray(new Action[actions.size()]);
	}

	@Override
	public String getVisitName() {
		return VisitNames.AMP;
	}

}
