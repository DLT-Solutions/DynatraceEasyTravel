package com.dynatrace.diagnostics.uemload.headless;

import java.util.ArrayList;
import java.util.List;

import com.dynatrace.diagnostics.uemload.Action;
import static com.dynatrace.diagnostics.uemload.HeadlessBySelectors.*;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;
import com.dynatrace.easytravel.misc.CommonUser;

public class HeadlessCustomerAlmostConvertedVisit extends HeadlessVisit {

	public HeadlessCustomerAlmostConvertedVisit(String host) {
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
		if (PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.FAILED_XHRs)) {
			actions.add(new HeadlessXhrErrorAction(host));
		}
		actions.add(new HeadlessClickAction(CustomerTermOfUseLink.get()));
		actions.add(new HeadlessClickAction(CustomerPrivacyPolicyLink.get()));
		actions.add(new HeadlessClickAction(CustomerLoginFormLogoutLink.get(), true));
		return actions.toArray(new Action[actions.size()]);
	}

	@Override
	public String getVisitName() {
		return VisitNames.HEADLESS_ALMOST_CONVERTED_VISIT;
	}

}
