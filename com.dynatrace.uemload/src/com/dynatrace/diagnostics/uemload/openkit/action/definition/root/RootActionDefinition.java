package com.dynatrace.diagnostics.uemload.openkit.action.definition.root;

import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionDefinition;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionRoot;
import com.dynatrace.diagnostics.uemload.openkit.event.ControlledEvent;
import com.dynatrace.diagnostics.uemload.openkit.event.lifetime.EventCallbackSet;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.api.Session;

import java.util.Collections;
import java.util.function.Consumer;

/**
 * Represents an OpenKit {@link RootAction}. Can Be started directly by calling {@link #start(Session)}.
 * Before returning it waits for all tasks (including itself) to finish.
 *
 * @see ControlledEvent for a detailed state timeline ({@link #beginAfter(Consumer callback)} is disabled)
 */
public class RootActionDefinition extends ActionDefinition<RootActionDefinition, RootAction> implements ActionRoot {
	RootActionDefinition(String name) {
		super(name);
	}

	@Override
	protected void run() {
		super.run();
		action.leaveAction();
	}

	protected RootActionDefinition addWaitForCallbacks(EventCallbackSet eventCallbacks) {
		return addCallbacks(eventCallbacks, this::waitFor);
	}

	@Override
	public RootAction getAction() {
		return action;
	}

	@Override
	public void start(Session session) {
		action = session.enterAction(name);
		startExecution();
		waitForTasksCompletion(Collections.singleton(() -> task));
	}

	@Override
	protected RootActionDefinition getThis() {
		return this;
	}
}
