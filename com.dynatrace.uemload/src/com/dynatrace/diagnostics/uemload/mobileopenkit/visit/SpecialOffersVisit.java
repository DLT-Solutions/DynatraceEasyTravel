package com.dynatrace.diagnostics.uemload.mobileopenkit.visit;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileActionType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.parameters.MobileOpenKitParams;

import java.util.List;

public class SpecialOffersVisit extends MobileOpenKitVisit {
	public SpecialOffersVisit(MobileOpenKitParams params, ExtendedCommonUser user, String apiUrl) {
		super(params, user, apiUrl);
	}

	@Override
	protected List<MobileActionType> addActions(List<MobileActionType> list) {
		list.add(MobileActionType.SEARCH);
		list.add(MobileActionType.TOUCH_SPECIAL_OFFERS);
		list.add(MobileActionType.LOAD_WEBVIEW);
		return list;
	}
}
