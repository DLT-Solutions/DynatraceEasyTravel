package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.ExtendedDemoUser;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileActionType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.parameters.MobileOpenKitParams;
import com.dynatrace.diagnostics.uemload.mobileopenkit.visit.BookingJourneyVisit;
import com.dynatrace.diagnostics.uemload.openkit.visit.OpenKitVisit;

public class HainerOpenKitMobileAppScenario extends OpenKitMobileAppScenario {
	
	@Override
	public OpenKitVisit<MobileActionType> getRandomVisit(String apiUrl, MobileOpenKitParams params, ExtendedCommonUser user) {
		return new BookingJourneyVisit(params, user, apiUrl);
	}
		
	@Override
	public ExtendedCommonUser getRandomMobileUser(String country) {
		return ExtendedDemoUser.HAINER_USER; 
	}
}
