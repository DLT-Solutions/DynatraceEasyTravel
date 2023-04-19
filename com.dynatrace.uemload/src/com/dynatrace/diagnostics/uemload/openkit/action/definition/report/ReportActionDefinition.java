package com.dynatrace.diagnostics.uemload.openkit.action.definition.report;

import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionParent;
import com.dynatrace.diagnostics.uemload.openkit.event.Event;
import com.dynatrace.diagnostics.uemload.openkit.event.lifetime.EventCallbackSet;
import com.dynatrace.openkit.api.Action;

import java.util.function.Supplier;

/**
 * Represents an entity reported by an OpenKit {@link Action}
 *
 * @see Event for a detailed state timeline
 */
public abstract class ReportActionDefinition<E extends ReportActionDefinition<E>> extends Event<E> {
	protected String name;
	protected Action action;
	private final Supplier<Action> actionSupplier;

	public ReportActionDefinition(ActionParent parentAction, EventCallbackSet startCallbacks) {
		this.actionSupplier = parentAction::getAction;
		addBeginCallbacks(startCallbacks);
	}

	@Override
	protected void run() {
		action = actionSupplier.get();
	}

	public E setName(String name) {
		this.name = name;
		return getThis();
	}
}
