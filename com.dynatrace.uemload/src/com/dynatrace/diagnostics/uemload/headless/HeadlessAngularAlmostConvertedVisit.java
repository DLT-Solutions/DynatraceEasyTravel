package com.dynatrace.diagnostics.uemload.headless;

import java.util.ArrayList;
import java.util.List;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.HeadlessBySelectors;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.headless.HeadlessVisit.HeadlessClickAction;
import com.dynatrace.diagnostics.uemload.headless.actions.HeadlessSelectDropdownValueAction;
import com.dynatrace.diagnostics.uemload.utils.AngularSearchParameters;
import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;
import com.dynatrace.easytravel.misc.CommonUser;

/**
 *
 * @author Michal.Bakula
 *
 */
public class HeadlessAngularAlmostConvertedVisit extends HeadlessVisit {

	public HeadlessAngularAlmostConvertedVisit(String host) {
		super(host);
	}

	@Override
	public Action[] getActions(CommonUser user, Location location) {
		List<Action> actions = new ArrayList<>();
		actions.add(new HeadlessGetAction(host));
		actions.add(new HeadlessClickAction(HeadlessBySelectors.AngularSlideshowArrowContainerNext.get(), true));
		actions.add(new HeadlessAngularSearchWithRetry(new AngularSearchParameters(), true));
		actions.add(new HeadlessSelectDropdownValueAction(HeadlessBySelectors.AngularTravelersDropdown.get()));
		actions.add(new HeadlessClickAction(HeadlessBySelectors.AngularBookNowButton.get(), true));
		actions.addAll(getAngularLoginActions(user));
		actions.addAll(getUsabilityClickPayButtonActions());
		actions.add(new HeadlessClickMobileAction(HeadlessBySelectors.AngularLogout.get(), true));
		return actions.toArray(new Action[actions.size()]);
	}

	@Override
	public String getVisitName() {
		return VisitNames.ANGULAR_ALMOST_CONVERTED_VISIT;
	}

}
