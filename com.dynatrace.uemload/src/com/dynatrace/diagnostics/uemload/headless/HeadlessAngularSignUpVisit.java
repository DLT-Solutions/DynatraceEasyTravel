package com.dynatrace.diagnostics.uemload.headless;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.HeadlessBySelectors;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;
import com.dynatrace.easytravel.misc.CommonUser;

/**
 *
 * @author Michal.Bakula
 *
 */
public class HeadlessAngularSignUpVisit extends HeadlessVisit {
	
	private static final int SIGNUP_ERROR_TYPES = 6;
	private static final String SIGNUP_EMAIL = "john.doe@missing.server.com";
	private static final String SIGNUP_WRONG_EMAIL = "john.doe@missing.server.commm";
	private static final String SIGNUP_PASSWORD = "JohnDoe123:)";
	private static final String SIGNUP_WRONG_PASSWORD = "JohnDoe123:((((";

	public HeadlessAngularSignUpVisit(String host) {
		super(host);
	}

	@Override
	public Action[] getActions(CommonUser user, Location location) {
		boolean clickByJS = true;

		List<Action> actions = new ArrayList<>();
		actions.add(new HeadlessGetAction(host));
		actions.add(new HeadlessClickMobileAction(HeadlessBySelectors.AngularSignUp.get(), clickByJS));
		actions.addAll(tryToSignUpActions());
		return actions.toArray(new Action[actions.size()]);
	}
	
	private List<Action> tryToSignUpActions() {
		List<Action> subactions = new LinkedList<>();
		SignUpErrorType errorType = SignUpErrorType.valueOfNumber(UemLoadUtils.randomInt(SIGNUP_ERROR_TYPES));
		
		if (SignUpErrorType.MISSING_FIRSTNAME != errorType) {
			subactions.add(new HeadlessSendKeysAction(HeadlessBySelectors.AngularSignUpFirstName.get(), "John"));
		}
		
		if (SignUpErrorType.MISSING_LASTNAME != errorType) {
			subactions.add(new HeadlessSendKeysAction(HeadlessBySelectors.AngularSignUpLastName.get(), "Doe"));
		}
		
		if (SignUpErrorType.MISSING_EMAIL != errorType) {
			subactions.add(new HeadlessSendKeysAction(HeadlessBySelectors.AngularSignUpEmail.get(), SIGNUP_EMAIL));
		}
		if (SignUpErrorType.WRONG_CONFIRM_EMAIL != errorType) {
			subactions.add(new HeadlessSendKeysAction(HeadlessBySelectors.AngularSignUpConfirmEmail.get(), SIGNUP_EMAIL));
		}
		else {
			subactions.add(new HeadlessSendKeysAction(HeadlessBySelectors.AngularSignUpConfirmEmail.get(), SIGNUP_WRONG_EMAIL));
		}
		
		if (SignUpErrorType.MISSING_PASSWORD != errorType) {
			subactions.add(new HeadlessSendKeysAction(HeadlessBySelectors.AngularSignUpPassword.get(), SIGNUP_PASSWORD));
		}
		if (SignUpErrorType.WRONG_CONFIRM_PASSWORD != errorType) {
			subactions.add(new HeadlessSendKeysAction(HeadlessBySelectors.AngularSignUpConfirmPassword.get(), SIGNUP_PASSWORD));
		}
		else {
			subactions.add(new HeadlessSendKeysAction(HeadlessBySelectors.AngularSignUpConfirmPassword.get(), SIGNUP_WRONG_PASSWORD));
		}
		
		subactions.add(new HeadlessClickAction(HeadlessBySelectors.AngularSignUpButton.get(), true));
		subactions.add(new HeadlessWaitAction(2000));
		
		return subactions;
	}

	@Override
	public String getVisitName() {
		return VisitNames.ANGULAR_SELECT_MENU_OPTIONS_VISIT;
	}
}
