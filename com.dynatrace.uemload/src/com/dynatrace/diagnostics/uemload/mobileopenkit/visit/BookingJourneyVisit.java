package com.dynatrace.diagnostics.uemload.mobileopenkit.visit;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileActionType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.parameters.MobileOpenKitParams;

import java.util.List;

public class BookingJourneyVisit extends MobileOpenKitVisit {

	public BookingJourneyVisit(MobileOpenKitParams params, ExtendedCommonUser user, String apiUrl) {
		super(params, user, apiUrl);
	}

	@Override
	public List<MobileActionType> addActions(List<MobileActionType> list) {
		list.add(MobileActionType.LOAD_APP);
		list.add(MobileActionType.IDENTIFY_USER);
		list.add(MobileActionType.LOAD_SEARCH);
		list.add(MobileActionType.SEARCH);
		list.add(MobileActionType.SELECT_RANDOM_JOURNEY);
		list.add(MobileActionType.TOUCH_LOGIN);
		list.add(MobileActionType.LOGIN);
		list.add(MobileActionType.BOOK_JOURNEY);
		list.add(MobileActionType.LOAD_WEBVIEW);
		list.add(MobileActionType.TOUCH_SEARCH_WITH_CRASH);
		return list;
	}
}
