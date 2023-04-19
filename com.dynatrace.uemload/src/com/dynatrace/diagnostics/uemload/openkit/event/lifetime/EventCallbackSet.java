package com.dynatrace.diagnostics.uemload.openkit.event.lifetime;

import com.dynatrace.diagnostics.uemload.openkit.event.EventCallback;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class EventCallbackSet {
	public final Set<Consumer<EventCallback>> callbacks = new HashSet<>();

	protected EventCallbackSet(Consumer<EventCallback> callback) {
		this.callbacks.add(callback);
	}

	public EventCallbackSet() { }

	public EventCallbackSet and(Consumer<EventCallback> beginCallback) {
		callbacks.add(beginCallback);
		return this;
	}
}
