package com.dynatrace.diagnostics.uemload.headless;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;
import com.dynatrace.easytravel.misc.CommonUser;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class HeadlessAngularBounceVisit extends HeadlessVisit {
	
	private static RandomSet<Action> actions = new RandomSet<>();

	public HeadlessAngularBounceVisit(String host) {
		super(host);
	}

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
		actions.add(new HeadlessGetAction(getBounceUrl("easytravel/home")), 8);
		actions.add(new HeadlessGetAction(getBounceUrl("easytravel/contact")), 11);
		actions.add(new HeadlessGetAction(getBounceUrl("easytravel/login")), 4);
		actions.add(new HeadlessGetAction(getBounceUrl("easytravel/signup")), 6);
	}

	@Override
	public String getVisitName() {
		return VisitNames.ANGULAR_BOUNCE_VISIT;
	}

}
