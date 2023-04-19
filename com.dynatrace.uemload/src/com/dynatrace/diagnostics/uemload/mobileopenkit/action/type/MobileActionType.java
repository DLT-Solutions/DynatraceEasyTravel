package com.dynatrace.diagnostics.uemload.mobileopenkit.action.type;

import com.dynatrace.diagnostics.uemload.mobileopenkit.action.set.*;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.set.login.MobileLoginActionSet;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.set.login.MobileLoginFailedActionSet;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.set.search.MobileSearchActionSet;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.set.search.MobileSearchTouchCrashActionSet;
import com.dynatrace.diagnostics.uemload.openkit.action.EventType;

public enum MobileActionType implements EventType {
	LOGIN(MobileLoginActionSet.class),
	LOGIN_FAILED(MobileLoginFailedActionSet.class),
	LOGIN_SUB_ACTION,

	SEARCH(MobileSearchActionSet.class),
	SEARCH_DESTINATION_PARENT,
	SEARCH_WHILE_TYPING,
	SEARCH_FOR_DESTINATION,

	LOAD_APP(MobileAppLoadActionSet.class),
	APP_START,
	IDENTIFY_USER,
	SELECT_RANDOM_JOURNEY,

	LOAD_SEARCH,
	LOAD_WEBVIEW,

	TOUCH_LOGIN,
	TOUCH_SEARCH,
	TOUCH_SPECIAL_OFFERS,
	TOUCH_SEARCH_WITH_CRASH(MobileSearchTouchCrashActionSet.class),

	GO_THROUGH_CITIES(GoThroughCitiesActionSet.class),
	BOOK_JOURNEY(BookJourneyActionSet.class),
	DISPLAY,

	USE_GPS_WITH_ERROR(UseGPSWithErrorActionSet.class);

	private Class<? extends MobileActionSet> implementation;

	MobileActionType() {
	}

	MobileActionType(Class<? extends MobileActionSet> bindedImplementation) {
		this.implementation = bindedImplementation;
	}

	public Class<? extends MobileActionSet> getImplementation() {
		return implementation;
	}
}
