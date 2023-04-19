package com.dynatrace.diagnostics.uemload.headless;

import java.util.ArrayList;
import java.util.List;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.HeadlessBySelectors;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.headless.HeadlessVisit.HeadlessClickAction;
import com.dynatrace.diagnostics.uemload.headless.actions.HeadlessSelectDropdownValueAction;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.diagnostics.uemload.utils.AngularSearchParameters;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;
import com.dynatrace.easytravel.misc.CommonUser;

/**
 *
 * @author Michal.Bakula
 *
 */
public class HeadlessAngularSpecialOffersConvertVisit extends HeadlessVisit {

	public HeadlessAngularSpecialOffersConvertVisit(String host) {
		super(host);
	}

	@Override
	public Action[] getActions(CommonUser user, Location location) {
		List<Action> actions = new ArrayList<>();
		actions.add(new HeadlessGetAction(host));
		actions.add(new HeadlessClickAction(HeadlessBySelectors.AngularSlideshowArrowContainerNext.get(), true));
		actions.add(new HeadlessClickAction(HeadlessBySelectors.AngularSpecialOffersResult.get(), true));
		actions.add(new HeadlessSelectDropdownValueAction(HeadlessBySelectors.AngularTravelersDropdown.get()));
		actions.add(new HeadlessClickAction(HeadlessBySelectors.AngularBookNowButton.get(), true));
		actions.addAll(getAngularLoginActions(user));
		actions.addAll(getUsabilityClickPayButtonActions());
		
		boolean stopBookingProcess = UemLoadUtils.randomInt(10) < BOOK_FAILURE_RATE;
		actions.addAll(getAngularCreditCardActions(new AngularSearchParameters(), stopBookingProcess, user));
		if (PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.ANGULAR_BIZ_EVENTS_PLUGIN) == false || 
				stopBookingProcess == false) {
			actions.addAll(getBookingSummaryButtonActions());
			actions.add(new HeadlessClickMobileAction(HeadlessBySelectors.AngularLogout.get(), true));
			actions.add(new HeadlessWaitAction(1000));
		}
		return actions.toArray(new Action[actions.size()]);
	}

	@Override
	public String getVisitName() {
		return VisitNames.ANGULAR_CONVERTED_VISIT;
	}

}
