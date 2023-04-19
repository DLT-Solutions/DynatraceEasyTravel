package com.dynatrace.diagnostics.uemload.openkit.action.definition.sub;

import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionDefinition;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionParent;
import com.dynatrace.diagnostics.uemload.openkit.event.lifetime.EventCallbackSet;
import com.dynatrace.diagnostics.uemload.openkit.event.ControlledEvent;
import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.RootAction;

import java.util.function.Supplier;

/**
 * Represents an OpenKit {@link Action}
 *
 * @see ControlledEvent for a detailed state timeline
 */
public class SubActionDefinition extends ActionDefinition<SubActionDefinition, Action> {
	private final Supplier<RootAction> rootActionSupplier;

	SubActionDefinition(ActionParent<RootAction> parentAction, EventCallbackSet startCallbacks, String name) {
		super(name);
		this.rootActionSupplier = parentAction::getAction;

		withParentAction(parentAction);
		addBeginCallbacks(startCallbacks);
	}

	@Override
	protected void run() {
		super.run();
		action.leaveAction();
	}

	@Override
	protected void start() {
		action = rootActionSupplier.get().enterAction(name);
	}

	@Override
	public Action getAction() {
		return action;
	}

	@Override
	public SubActionDefinition withStartDelay(int minStartDelay, int maxStartDelay) {
		return super.withStartDelay(minStartDelay, maxStartDelay);
	}

	@Override
	protected SubActionDefinition getThis() {
		return this;
	}
}
