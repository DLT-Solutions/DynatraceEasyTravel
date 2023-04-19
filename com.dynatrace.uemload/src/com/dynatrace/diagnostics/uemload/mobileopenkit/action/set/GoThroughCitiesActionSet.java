package com.dynatrace.diagnostics.uemload.mobileopenkit.action.set;

import com.dynatrace.diagnostics.uemload.mobileopenkit.action.MobileActionSetPatterns;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileActionType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileDevice;
import com.dynatrace.diagnostics.uemload.openkit.action.ActionDefinitionSet;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GoThroughCitiesActionSet extends MobileActionSet {
	private static final String SEARCH_KEY = "Search";
	static final String[] CITIES = new String[]{SEARCH_KEY, "Paris", "New York", SEARCH_KEY, "Berlin", "Amsterdam", "Vienna", SEARCH_KEY, "Rome"};

	public GoThroughCitiesActionSet(MobileDevice device) {
		super(device);
		eventNameMapper.register(MobileActionType.SEARCH, "SearchJourneyActivity", "DTSearchViewController");
	}

	@Override
	protected ActionDefinitionSet buildAndroid() {
		return buildCommon();
	}

	@Override
	protected ActionDefinitionSet buildIOS() {
		return buildCommon();
	}

	private ActionDefinitionSet buildCommon() {
		return new ActionDefinitionSet(Arrays.stream(CITIES).map(city ->
				MobileActionSetPatterns.touchWidgetAction(city, eventNameMapper.get(MobileActionType.SEARCH))).collect(Collectors.toList()));
	}
}
