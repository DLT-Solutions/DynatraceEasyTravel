package com.dynatrace.diagnostics.uemload.headless;

import static com.dynatrace.diagnostics.uemload.HeadlessBySelectors.*;

import java.util.ArrayList;
import java.util.List;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.headless.HeadlessVisit.HeadlessWaitAction;
import com.dynatrace.diagnostics.uemload.headless.actions.HeadlessAngularScroll;
import com.dynatrace.diagnostics.uemload.headless.actions.HeadlessSelectDropdownValueAction;
import com.dynatrace.diagnostics.uemload.utils.AngularSearchParameters;
import com.dynatrace.easytravel.misc.CommonUser;

public class HeadlessMouseMoveVisit extends HeadlessVisit {

	public HeadlessMouseMoveVisit(String host) {
		super(host);
	}
		
	@Override
	public Action[] getActions(CommonUser user, Location location) {
		List<Action> actions = new ArrayList<>();
		actions.add(new HeadlessGetAction(host));		
		actions.add(new HeadlessAngularScroll(2000, 10));
		actions.add(new HeadlessWaitAction(2000));
		actions.add(new HeadlessAngularScroll(2000, -10));
		actions.add(new HeadlessWaitAction(2000));
		actions.add(new HeadlessMouseMoveAction("mouse_moves/easytravel-search.log", AngularLogo.get()));
		AngularSearchParameters params = new AngularSearchParameters("Paris");
		actions.add(new HeadlessAngularSearchWithRetry(params, true));		
		actions.add(new HeadlessMouseMoveAction("mouse_moves/easytravel-gototravelers.log"));
		actions.add(new HeadlessClickAction(AngularTravelersDropdown.get(), false));
		actions.add(new HeadlessSelectDropdownValueAction(AngularTravelersDropdown.get()));
		actions.add(new HeadlessMouseMoveAction("mouse_moves/easytravel-book.log", AngularTravelersDropdown.get()));
		actions.add(new HeadlessClickAction(AngularBookNowButton.get(), true));
		actions.add(new HeadlessWaitForElement(AngularLoginFormUsername.get(), 2));
		actions.add(new HeadlessMouseMoveAction("mouse_moves/easytravel-login.log"));
		actions.addAll(getAngularLoginActions(user));
		actions.add(new HeadlessMouseMoveAction("mouse_moves/easytravel-pay.log"));
		actions.add(new HeadlessWaitAction(2000));
		actions.add(new HeadlessClickAction(AngularPayButton.get()));
		actions.add(new HeadlessMouseMoveAction("mouse_moves/easytravel-creditcard.log"));
		actions.addAll(getAngularCreditCardActions(params, false, user));
		actions.add(new HeadlessWaitAction(5000));
		actions.add(new HeadlessMouseMoveAction("mouse_moves/easytravel-summary.log"));
		actions.addAll(getBookingSummaryButtonActions());
		actions.add(new HeadlessWaitAction(1000));
		actions.add(new HeadlessMouseMoveAction("mouse_moves/easytravel-logout.log"));
		actions.add(new HeadlessClickMobileAction(AngularLogout.get(), true));
		actions.add(new HeadlessWaitAction(5000));
		return actions.toArray(new Action[actions.size()]);
	}

	@Override
	public String getVisitName() {
		return "MouseMoveVisit";
	}

}
