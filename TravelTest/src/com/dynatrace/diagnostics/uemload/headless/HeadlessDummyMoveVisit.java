/**
 *
 */
package com.dynatrace.diagnostics.uemload.headless;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.HeadlessBySelectors;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.headless.actions.HeadlessSelectDropdownValueAction;
import com.dynatrace.easytravel.misc.CommonUser;

/**
 * @author tomasz.wieremjewicz
 * @date 27 lis 2018
 *
 */
public class HeadlessDummyMoveVisit extends HeadlessVisit {
	public HeadlessDummyMoveVisit(String host) {
		super(host);
	}

	@Override
	public Action[] getActions(CommonUser user, Location location) {
		List<Action> actions = new ArrayList<>();
		actions.add(new HeadlessGetAction(host));
		actions.add(new HeadlessClickAction(HeadlessBySelectors.AngularLogin.get(), true));
		actions.addAll(getAngularLoginActions(user));
		actions.add(new HeadlessWaitAction(3000));
		actions.add(new HeadlessGetAction(host));
		actions.add(new HeadlessClickAction(HeadlessBySelectors.AngularSpecialOffersResult.get(), true));
		actions.add(new HeadlessSelectDropdownValueAction(HeadlessBySelectors.AngularTravelersDropdown.get()));
		actions.add(new HeadlessClickAction(HeadlessBySelectors.AngularBookNowButton.get(), true));
		actions.addAll(getUsabilityClickPayButtonActions());
		actions.add(new HeadlessClickAction(HeadlessBySelectors.AngularContact.get()));
		actions.add(new HeadlessClickAction(HeadlessBySelectors.AngularLogout.get(), true));
		actions.add(new HeadlessWaitAction(3000));
		return actions.toArray(new Action[actions.size()]);
	}

	@Override
	protected List<Action> getUsabilityClickPayButtonActions() {
		List<Action> actions = new LinkedList<>();
		actions.add(new HeadlessWaitAction(2000));
		actions.add(new HeadlessCustomPageDownPageUpAction(3, 400));
		actions.add(new HeadlessMoveAroundAndClickAction(
				HeadlessBySelectors.AngularPay2Button.get(),
				HeadlessBySelectors.AngularDateParagraph.get(),
				HeadlessBySelectors.AngularLogout.get()));
		return actions;
	}

	@Override
	public String getVisitName() {
		return "HeadlessDummyMoveVisit";
	}
}
