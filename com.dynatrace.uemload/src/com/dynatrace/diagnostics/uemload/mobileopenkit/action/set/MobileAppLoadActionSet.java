package com.dynatrace.diagnostics.uemload.mobileopenkit.action.set;

import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileActionType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileDevice;
import com.dynatrace.diagnostics.uemload.openkit.action.ActionDefinitionSet;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.lifecycyle.LifeCycleAction;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.lifecycyle.LifeCycleActionDefinition;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.root.RootAction;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.root.RootActionDefinition;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.sub.SubAction;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.sub.SubActionDefinition;

import static com.dynatrace.diagnostics.uemload.openkit.action.definition.lifecycyle.LifeCycleConfig.createOrDidLoadAfter;
import static com.dynatrace.diagnostics.uemload.openkit.event.lifetime.BeginCallbackSet.after;
import static com.dynatrace.diagnostics.uemload.openkit.event.lifetime.LiveCallbackSet.forDuration;
import static com.dynatrace.diagnostics.uemload.openkit.event.lifetime.LiveCallbackSet.until;

public class MobileAppLoadActionSet extends MobileActionSet {
	public static final String LOADING_ACTION_NAME = "Loading easyTravel";

	public MobileAppLoadActionSet(MobileDevice device) {
		super(device);
		eventNameMapper.register(MobileActionType.APP_START, "AppStart ", "(easyTravel)", "(DTNavigationController)");
		eventNameMapper.register(MobileActionType.DISPLAY, "SearchJourneyActivity", "DTMasterViewController");
	}

	@Override
	protected ActionDefinitionSet buildAndroid() {
		RootActionDefinition appStart = RootAction.named(eventNameMapper.get(MobileActionType.APP_START)).live(forDuration(20, 50));
		LifeCycleActionDefinition displayView = LifeCycleAction.named(eventNameMapper.get(MobileActionType.DISPLAY))
				.report(createOrDidLoadAfter(20, 30).startOrWillAppearAfter(20, 40).resumeOrDidAppearAfter(20, 30));
		return new ActionDefinitionSet(appStart, displayView);
	}

	@Override
	protected ActionDefinitionSet buildIOS() {
		RootAction.RootActionBuilder rootAction = RootAction.named(LOADING_ACTION_NAME);

		SubActionDefinition appStart = SubAction.of(rootAction).named(eventNameMapper.get(MobileActionType.APP_START))
				.begin(after(rootAction::started)).withStartDelay(200, 400).withMinimumDuration(20, 50);

		LifeCycleActionDefinition displayView = LifeCycleAction.of(rootAction).named(eventNameMapper.get(MobileActionType.DISPLAY)).begin(after(appStart::started))
				.report(createOrDidLoadAfter(30, 40).startOrWillAppearAfter(50, 80).resumeOrDidAppearAfter(30, 60)).withStartDelay(10, 30);

		RootActionDefinition rootActionDefinition = rootAction.live(until(displayView::ended)).withFinishDelay(10, 20);
		return new ActionDefinitionSet(rootActionDefinition);
	}
}
