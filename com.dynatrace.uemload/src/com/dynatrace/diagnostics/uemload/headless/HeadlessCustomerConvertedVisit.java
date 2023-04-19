package com.dynatrace.diagnostics.uemload.headless;

import static com.dynatrace.diagnostics.uemload.HeadlessBySelectors.CustomerBookNowButton;
import static com.dynatrace.diagnostics.uemload.HeadlessBySelectors.CustomerReviewNextButton;

import java.util.ArrayList;
import java.util.List;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;
import com.dynatrace.easytravel.misc.CommonUser;

public class HeadlessCustomerConvertedVisit extends HeadlessVisit {

	public HeadlessCustomerConvertedVisit(String host) {
		super(host);
	}

	@Override
	public Action[] getActions(CommonUser user, Location location) {
		List<Action> actions = new ArrayList<>();
		actions.add(new HeadlessGetAction(host));
		actions.addAll(getLoginActions(user));
		actions.addAll(getSearchActions(null, null));
		actions.add(new HeadlessClickAction(CustomerBookNowButton.get(), true));
		actions.add(new HeadlessClickAction(CustomerReviewNextButton.get()));
		actions.addAll(getCreditCardActions(user));
		actions.add(new HeadlessClickAction(CustomerReviewNextButton.get(), true));
		return actions.toArray(new Action[actions.size()]);
	}

	@Override
	public String getVisitName() {
		return VisitNames.HEADLESS_CONVERTED_VISIT;
	}

}
