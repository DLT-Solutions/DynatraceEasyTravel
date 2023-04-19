package com.dynatrace.diagnostics.uemload.openkit.action.definition.sub;

import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionParent;
import com.dynatrace.diagnostics.uemload.openkit.event.EventCallback;
import com.dynatrace.diagnostics.uemload.openkit.event.lifetime.EventCallbackSet;
import com.dynatrace.openkit.api.RootAction;

import java.util.function.Consumer;

public class TestSubAction extends SubActionDefinition {
	public TestSubAction(ActionParent<RootAction> parentAction, String name) {
		super(parentAction, new EventCallbackSet(), name);
	}

	public void beginAfterCallback(Consumer<EventCallback> callback) {
		super.beginAfter(callback);
	}
}
