package com.dynatrace.diagnostics.uemload.headless;

import java.util.ArrayList;
import java.util.List;

import com.dynatrace.diagnostics.uemload.Action;
import static com.dynatrace.diagnostics.uemload.HeadlessBySelectors.*;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;
import com.dynatrace.easytravel.misc.CommonUser;

public class HeadlessCustomerPageWandererVisit extends HeadlessVisit {

	private boolean isShort;
	private boolean convert;
	
	public HeadlessCustomerPageWandererVisit(String host, boolean isShort, boolean convert) {
		super(host);
		this.isShort = isShort;
		this.convert = convert;
	}

	@Override
	public Action[] getActions(CommonUser user, Location location) {
		List<Action> actions = new ArrayList<>();
		actions.add(new HeadlessGetAction(host));
		actions.addAll(getLoginActions(user));
		int nrOfSearches;
		if(isShort) {
			nrOfSearches = 2;
		} else {
			nrOfSearches = 4 + UemLoadUtils.randomInt(4);
		}
		for(int i = 0; i < nrOfSearches; i++) {
			actions.addAll(getSearchActions(null, null));
			actions.add(new HeadlessClickAction(CustomerBookNowButton.get(), true));
			if(Math.random() < 0.3) {
				actions.add(new HeadlessClickAction(CustomerReviewNextButton.get()));
				actions.add(new HeadlessClickAction(CustomerPaymentBackButton.get()));
			}
			actions.add(new HeadlessClickAction(CustomerReviewNewSearchButton.get()));
			actions.add(new HeadlessClickAction(CustomerClearLink.get()));
		}
		if(convert) {
			actions.addAll(getSearchActions(null, null));
			actions.add(new HeadlessClickAction(CustomerBookNowButton.get(), true));
			actions.add(new HeadlessClickAction(CustomerReviewNextButton.get()));
			actions.addAll(getCreditCardActions(user));
		}
		actions.add(new HeadlessClickAction(CustomerLoginFormLogoutLink.get(), true));
		return actions.toArray(new Action[actions.size()]);
	}

	@Override
	public String getVisitName() {
		return VisitNames.HEADLESS_PAGE_WANDERER_VISIT;
	}
}
