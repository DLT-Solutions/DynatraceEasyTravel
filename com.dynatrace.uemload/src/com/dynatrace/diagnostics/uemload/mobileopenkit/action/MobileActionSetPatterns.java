package com.dynatrace.diagnostics.uemload.mobileopenkit.action;

import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionParent;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.lifecycyle.LifeCycleAction;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.lifecycyle.LifeCycleActionDefinition;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.root.RootAction;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.root.RootActionDefinition;

import static com.dynatrace.diagnostics.uemload.openkit.action.definition.lifecycyle.LifeCycleConfig.createOrDidLoadAfter;
import static com.dynatrace.diagnostics.uemload.openkit.event.lifetime.BeginCallbackSet.after;
import static com.dynatrace.diagnostics.uemload.openkit.event.lifetime.LiveCallbackSet.until;

public class MobileActionSetPatterns {
	public static final String TOUCH_ON_ACTION_PREFIX = "Touch on";

	private MobileActionSetPatterns() { }

	public static RootActionDefinition touchWidgetAction(String actionTarget, String displayView) {
		return simpleAction(TOUCH_ON_ACTION_PREFIX, actionTarget, displayView);
	}

	public static RootActionDefinition simpleDisplayViewAction(String displayView) {
		return simpleAction("Loading", displayView, displayView);
	}

	private static RootActionDefinition simpleAction(String actionName, String actionTarget, String displayView) {
		RootAction.RootActionBuilder rootAction = RootAction.named(actionName + " " + actionTarget);
		LifeCycleActionDefinition displayAction = getDisplayAction(displayView, rootAction);
		return rootAction.live(until(displayAction::ended));
	}

	private static LifeCycleActionDefinition getDisplayAction(String viewName, ActionParent<com.dynatrace.openkit.api.RootAction> rootAction) {
		return LifeCycleAction.of(rootAction).named(viewName).begin(after(rootAction::started))
				.report(createOrDidLoadAfter(50, 80).startOrWillAppearAfter(50, 80).resumeOrDidAppearAfter(30, 60)).withStartDelay(150, 200);
	}
}
