package com.dynatrace.diagnostics.uemload.mobileopenkit.action.set;

import com.dynatrace.diagnostics.uemload.mobileopenkit.action.EventNameMapper;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.MobileActionSetPatterns;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileActionType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileOS;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.set.EventInstance.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.Matchers.endsWith;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GoThroughCitiesActionSetTest extends ActionSetTestBase {
	@Override
	protected MobileActionType getActionType() {
		return MobileActionType.GO_THROUGH_CITIES;
	}

	@Override
	protected List<EventRoot> buildActionTrees(MobileOS platform, EventNameMapper nameMapper) {
		return Arrays.stream(GoThroughCitiesActionSet.CITIES).map(city -> touchWidgetAction(city, nameMapper)).collect(Collectors.toList());
	}

	private ActionInstance touchWidgetAction(String actionTarget, EventNameMapper nameMapper) {
		ActionInstance rootAction = new ActionInstance(MobileActionSetPatterns.TOUCH_ON_ACTION_PREFIX + " " + actionTarget);
		rootAction.add(new LifeCycleInstance(nameMapper.get(MobileActionType.SEARCH)));
		return rootAction;
	}

	@Override
	protected void verifyActionSetExecution(List<EventRoot> actionTrees) {
		Map<String, Long> actionMap = Arrays.stream(GoThroughCitiesActionSet.CITIES).collect(Collectors.groupingBy(name -> name, Collectors.counting()));
		actionMap.forEach((entry, count) -> verify(session, times(count.intValue())).enterAction(endsWith(MobileActionSetPatterns.TOUCH_ON_ACTION_PREFIX + " " + entry)));
	}
}
