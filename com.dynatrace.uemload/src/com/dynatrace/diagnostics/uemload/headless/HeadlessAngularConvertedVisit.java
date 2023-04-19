package com.dynatrace.diagnostics.uemload.headless;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.HeadlessBySelectors;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.headless.actions.HeadlessSelectDropdownValueAction;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.diagnostics.uemload.utils.AngularSearchParameters;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;
import com.dynatrace.easytravel.misc.CommonUser;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michal.Bakula
 *
 */
public class HeadlessAngularConvertedVisit extends HeadlessVisit {
	private static final double BLOG_VISIT_CHANCE = 0.4;

	public HeadlessAngularConvertedVisit(String host) {
		super(host);
	}

	@Override
	public Action[] getActions(CommonUser user, Location location) {
		List<Action> actions = new ArrayList<>();
		if(Math.random() < BLOG_VISIT_CHANCE)
			actions.addAll(getBlogVisitActions(host));
		actions.add(new HeadlessGetAction(host));
		actions.add(new HeadlessClickAction(HeadlessBySelectors.AngularSlideshowArrowContainerNext.get(), true));
		AngularSearchParameters params = new AngularSearchParameters();
		actions.add(new HeadlessAngularSearchWithRetry(params, true));
		actions.add(new HeadlessSelectDropdownValueAction(HeadlessBySelectors.AngularTravelersDropdown.get()));
		actions.add(new HeadlessClickAction(HeadlessBySelectors.AngularBookNowButton.get(), true));
		actions.addAll(getAngularLoginActions(user));
		actions.addAll(getUsabilityClickPayButtonActions());
		
		boolean stopBookingProcess = UemLoadUtils.randomInt(10) < BOOK_FAILURE_RATE;
		actions.addAll(getAngularCreditCardActions(params, stopBookingProcess, user));
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
