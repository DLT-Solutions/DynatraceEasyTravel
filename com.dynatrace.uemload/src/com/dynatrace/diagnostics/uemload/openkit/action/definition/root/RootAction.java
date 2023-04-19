package com.dynatrace.diagnostics.uemload.openkit.action.definition.root;

import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionParent;
import com.dynatrace.diagnostics.uemload.openkit.event.EventCallback;
import com.dynatrace.diagnostics.uemload.openkit.event.lifetime.LiveCallbackSet;
import com.dynatrace.diagnostics.uemload.openkit.time.TimeService;

import java.util.concurrent.Future;
import java.util.function.Supplier;

public class RootAction {
	private RootAction() {}

	public static RootActionBuilder named(String name) {
		return new RootActionBuilder(name);
	}

	public static class RootActionBuilder implements ActionParent<com.dynatrace.openkit.api.RootAction> {
		private final RootActionDefinition action;

		private RootActionBuilder(String name) {
			action = new RootActionDefinition(name);
		}

		public RootActionDefinition live(LiveCallbackSet liveCallbacks) {
			return action.addWaitForCallbacks(liveCallbacks).withMinimumDuration(liveCallbacks.minDuration, liveCallbacks.maxDuration);
		}

		// Functions used by SubActionBuilder - needed before the actual action definition can be returned

		@Override
		public void addSubTask(Supplier<Future> task) {
			action.addSubTask(task);
		}

		@Override
		public com.dynatrace.openkit.api.RootAction getAction() {
			return action.getAction();
		}

		@Override
		public void ended(EventCallback callback) {
			action.ended(callback);
		}

		@Override
		public void started(EventCallback callback) {
			action.started(callback);
		}

		@Override
		public void setTimeService(TimeService timeService) {
			action.setTimeService(timeService);
		}
	}
}
