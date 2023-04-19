package com.dynatrace.diagnostics.uemload.openkit.event.lifetime;

import com.dynatrace.diagnostics.uemload.openkit.event.EventCallback;

import java.util.function.Consumer;

public class BeginCallbackSet extends EventCallbackSet {
	protected BeginCallbackSet(Consumer<EventCallback> callback) {
		super(callback);
	}

	public static BeginCallbackSet after(Consumer<EventCallback> endCallback) {
		return new BeginCallbackSet(endCallback);
	}
}
