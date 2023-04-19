package com.dynatrace.diagnostics.uemload.headless;

import com.dynatrace.diagnostics.uemload.HeadlessBySelectors;
import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;

public class HeadlessCustomerOverloadVisit extends HeadlessOverloadBase {

	public HeadlessCustomerOverloadVisit(String host) {
		super(host);

		// set up list of actions to perform - menu items
		actionIds.add(HeadlessBySelectors.CustomerAboutLink.get() );
		actionIds.add(HeadlessBySelectors.CustomerPrivacyPolicyLink.get() );
		actionIds.add(HeadlessBySelectors.CustomerTermOfUseLink.get() );
	}

	@Override
	public String getVisitName() {
		return VisitNames.HEADLESS_OVERLOAD_VISIT;
	}

}
