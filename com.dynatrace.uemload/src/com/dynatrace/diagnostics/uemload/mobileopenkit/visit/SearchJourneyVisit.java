package com.dynatrace.diagnostics.uemload.mobileopenkit.visit;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileActionType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.parameters.MobileOpenKitParams;

import java.util.List;

public class SearchJourneyVisit extends MobileOpenKitVisit {

	public SearchJourneyVisit(MobileOpenKitParams params, ExtendedCommonUser user, String apiUrl) {
		super(params, user, apiUrl);
	}

	@Override
	public List<MobileActionType> addActions(List<MobileActionType> list) {
		list.add(MobileActionType.LOAD_SEARCH);
		list.add(MobileActionType.SEARCH);
		list.add(MobileActionType.LOAD_WEBVIEW);
		list.add(MobileActionType.TOUCH_SEARCH);
		list.add(MobileActionType.SEARCH);
		list.add(MobileActionType.LOAD_WEBVIEW);
		list.add(MobileActionType.TOUCH_SEARCH_WITH_CRASH);
		return list;
	}
}
