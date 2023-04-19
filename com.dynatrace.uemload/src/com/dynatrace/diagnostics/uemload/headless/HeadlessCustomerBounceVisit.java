package com.dynatrace.diagnostics.uemload.headless;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;
import com.dynatrace.easytravel.misc.CommonUser;

public class HeadlessCustomerBounceVisit extends HeadlessVisit {
	public HeadlessCustomerBounceVisit(String host) {
		super(host);
	}

	private static RandomSet<Action> actions = new RandomSet<>();

	@Override
	public Action[] getActions(CommonUser user, Location location) {
		synchronized(actions) {
			if(actions.isEmpty()) {
				fill();
			}
		}
		return new Action[] { actions.getRandom() };
	}

	private void fill() {
		actions.add(new HeadlessGetAction(host), 6);
		actions.add(new HeadlessGetAction(getBounceUrl("special-offers.jsp")), 8);
		actions.add(new HeadlessGetAction(getBounceUrl("legal-orange.jsf")), 10);
		actions.add(new HeadlessGetAction(getBounceUrl("privacy-orange.jsf")), 12);
		actions.add(new HeadlessGetAction(getBounceUrl("about-orange.jsf")), 11);
	}

	@Override
	public String getVisitName() {
		return VisitNames.HEADLESS_BOUNCE_VISIT;
	}


}
