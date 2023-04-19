package com.dynatrace.diagnostics.uemload.headless;

import java.util.ArrayList;
import java.util.List;
import static com.dynatrace.diagnostics.uemload.HeadlessBySelectors.*;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.easytravel.constants.BaseConstants.B2BAccount;
import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;
import com.dynatrace.easytravel.misc.CommonUser;

/**
 * Visit that will log into one of available accounts, go through all menu options and log out.
 * @author krzysztof.sajko
 * @Date 2021.10.05
 */
public class HeadlessB2BAllMenuOptionsVisit extends HeadlessB2BVisit {
	
	private final B2BAccount account;
	
	public HeadlessB2BAllMenuOptionsVisit(String host, B2BAccount account) {
		super(host);
		this.account = account;
	}

	@Override
	public Action[] getActions(CommonUser user, Location location) {
		List<Action> actions = new ArrayList<>();
		actions.add(new HeadlessGetAction(host));
		actions.addAll(getLoginActions(account));
		actions.add(new HeadlessClickAction(B2BMenuJourneys.get()));
		actions.add(new HeadlessWaitAction(2000));
		actions.add(new HeadlessClickAction(B2BMenuLocations.get()));
		actions.add(new HeadlessWaitAction(2000));
		actions.add(new HeadlessClickAction(B2BMenuBookings.get()));
		actions.add(new HeadlessWaitAction(2000));
		actions.add(new HeadlessClickAction(B2BMenuReports.get()));
		actions.add(new HeadlessWaitAction(2000));
		actions.add(new HeadlessClickAction(B2BMenuLogout.get()));
		return actions.toArray(new Action[actions.size()]);
	}

	@Override
	public String getVisitName() {
		return VisitNames.HEADLESS_B2B_ALL_MENU_OPTIONS_VISIT;
	}

}
