package com.dynatrace.diagnostics.uemload.headless;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.HeadlessBySelectors;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;
import com.dynatrace.easytravel.misc.CommonUser;

public class HeadlessCustomerSearchVisit extends HeadlessVisit {

	public HeadlessCustomerSearchVisit(String host) {
		super(host);
	}

	@Override
	public Action[] getActions(CommonUser user, Location location) {
		List<Action> actions = new ArrayList<>();
		actions.add(new HeadlessGetAction(host));
		actions.addAll(getSearchActions(new SimpleDateFormat("MMM dd, yyyy").format(new Date()), null));
		actions.add(new HeadlessClickAction(HeadlessBySelectors.CustomerAboutLink.get()));
		return actions.toArray(new Action[actions.size()]);
	}

	@Override
	public String getVisitName() {
		return VisitNames.HEADLESS_SEARCH_VISIT;
	}
}
