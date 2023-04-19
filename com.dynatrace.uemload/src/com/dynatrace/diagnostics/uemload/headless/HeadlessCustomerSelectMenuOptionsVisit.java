package com.dynatrace.diagnostics.uemload.headless;

import java.util.ArrayList;
import java.util.List;

import com.dynatrace.diagnostics.uemload.Action;
import static com.dynatrace.diagnostics.uemload.HeadlessBySelectors.*;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;
import com.dynatrace.easytravel.misc.CommonUser;

public class HeadlessCustomerSelectMenuOptionsVisit extends HeadlessVisit {

	public HeadlessCustomerSelectMenuOptionsVisit(String host) {
		super(host);
	}

	@Override
	public Action[] getActions(CommonUser user, Location location) {

		StringBuilder sb = new StringBuilder();
		String slash = host.charAt(host.length()-1) == '/' ? "" : "/";
		sb.append(host).append(slash).append("");	//special-offers.jsp

		boolean clickByJS = true;

		List<Action> actions = new ArrayList<>();
		actions.add(new HeadlessGetAction(sb.toString()));
		actions.add(new HeadlessClickAction(CustomerAboutLink.get(), clickByJS));				// about-oraange.jsf    _t19
		actions.add(new HeadlessClickAction(CustomerPrivacyPolicyLink.get(), clickByJS));		// privacy-orange.jsf    _t28
		actions.add(new HeadlessClickAction(CustomerTermOfUseLink.get(), clickByJS));		// legal-orange.jsf    _t25
		actions.add(new HeadlessClickAction(CustomerAboutLink.get(), clickByJS));				// about-oraange.jsf    _t19
		actions.add(new HeadlessClickAction(CustomerPrivacyPolicyLink.get(), clickByJS));		// privacy-orange.jsf    _t28
		actions.add(new HeadlessClickAction(CustomerTermOfUseLink.get(), clickByJS));		// legal-orange.jsf    _t25
		return actions.toArray(new Action[actions.size()]);
	}

	@Override
	public String getVisitName() {
		return VisitNames.HEADLESS_SELECT_MENU_OPTIONS_VISIT;
	}

}
