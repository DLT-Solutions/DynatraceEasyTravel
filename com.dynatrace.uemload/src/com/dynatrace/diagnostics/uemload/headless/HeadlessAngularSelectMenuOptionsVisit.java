package com.dynatrace.diagnostics.uemload.headless;

import java.util.ArrayList;
import java.util.List;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.HeadlessBySelectors;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;
import com.dynatrace.easytravel.misc.CommonUser;

/**
 *
 * @author Michal.Bakula
 *
 */
public class HeadlessAngularSelectMenuOptionsVisit extends HeadlessVisit {

	public HeadlessAngularSelectMenuOptionsVisit(String host) {
		super(host);
	}

	@Override
	public Action[] getActions(CommonUser user, Location location) {
		boolean clickByJS = true;

		List<Action> actions = new ArrayList<>();
		actions.add(new HeadlessGetAction(host));
		actions.add(new HeadlessClickAction(HeadlessBySelectors.AngularSlideshowArrowContainerNext.get(), true));
		actions.add(new HeadlessClickMobileAction(HeadlessBySelectors.AngularSpecialOffers.get(), clickByJS));
		actions.add(new HeadlessClickMobileAction(HeadlessBySelectors.AngularContact.get(), clickByJS));
		actions.add(new HeadlessClickMobileAction(HeadlessBySelectors.AngularLogin.get(), clickByJS));
		actions.add(new HeadlessClickMobileAction(HeadlessBySelectors.AngularSignUp.get(), clickByJS));
		actions.add(new HeadlessClickAction(HeadlessBySelectors.AngularContactFooter.get(), clickByJS));
		actions.add(new HeadlessClickAction(HeadlessBySelectors.AngularTermsFooter.get(), clickByJS));
		actions.add(new HeadlessClickAction(HeadlessBySelectors.AngularPolicyFooter.get(), clickByJS));
		return actions.toArray(new Action[actions.size()]);
	}

	@Override
	public String getVisitName() {
		return VisitNames.ANGULAR_SELECT_MENU_OPTIONS_VISIT;
	}

}
