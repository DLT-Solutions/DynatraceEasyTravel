package com.dynatrace.diagnostics.uemload.headless;

import com.dynatrace.diagnostics.uemload.HeadlessBySelectors;
import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class HeadlessAngularOverloadVisit extends HeadlessOverloadBase {

	public HeadlessAngularOverloadVisit(String host) {
		super(host);

		// set up list of actions to perform - menu items
		actionIds.add(HeadlessBySelectors.AngularSpecialOffers.get() );
		actionIds.add(HeadlessBySelectors.AngularContact.get() );
		actionIds.add(HeadlessBySelectors.AngularLogin.get() );
		actionIds.add(HeadlessBySelectors.AngularSignUp.get() );
		actionIds.add(HeadlessBySelectors.AngularContactFooter.get() );
		actionIds.add(HeadlessBySelectors.AngularTermsFooter.get() );
		actionIds.add(HeadlessBySelectors.AngularPolicyFooter.get() );
	}

	@Override
	public String getVisitName() {
		return VisitNames.ANGULAR_OVERLOAD_VISIT;
	}

}
