package com.dynatrace.diagnostics.uemload.openkit.action.definition;

import com.dynatrace.diagnostics.uemload.openkit.event.ControlledEvent;
import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.RootAction;

/**
 * A base Action (wrapping either OpenKit {@link RootAction} or {@link Action}) with a type and name.
 *
 * @see ControlledEvent for a detailed state timeline
 */
public abstract class ActionDefinition<E extends ActionDefinition<E, A>, A extends Action> extends ControlledEvent<E> implements ActionParent<A> {
	protected A action;
	protected final String name;

	protected ActionDefinition(String name) {
		this.name = name;
	}

	@Override
	public E withFinishDelay(int minEndDelay, int maxEndDelay) {
		return super.withFinishDelay(minEndDelay, maxEndDelay);
	}

	@Override
	public E withMinimumDuration(int minAddedDuration, int maxAddedDuration) {
		return super.withMinimumDuration(minAddedDuration, maxAddedDuration);
	}

	@Override
	public E withExtraDuration(int minExtraDuration, int maxExtraDuration) {
		return super.withExtraDuration(minExtraDuration, maxExtraDuration);
	}
}
