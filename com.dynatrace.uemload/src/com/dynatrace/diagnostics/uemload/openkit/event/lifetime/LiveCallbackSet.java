package com.dynatrace.diagnostics.uemload.openkit.event.lifetime;

import com.dynatrace.diagnostics.uemload.openkit.event.EventCallback;

import java.util.function.Consumer;

public class LiveCallbackSet extends EventCallbackSet {
	public final int minDuration;
	public final int maxDuration;

	protected LiveCallbackSet(Consumer<EventCallback> callback) {
		super(callback);
		minDuration = 0;
		maxDuration = 0;
	}

	public LiveCallbackSet(int minDuration, int maxDuration) {
		this.minDuration = minDuration;
		this.maxDuration = maxDuration;
	}

	public static LiveCallbackSet forDuration(int minDuration, int maxDuration) {
		return new LiveCallbackSet(minDuration, maxDuration);
	}

	public static LiveCallbackSet until(Consumer<EventCallback> endCallback) {
		return new LiveCallbackSet(endCallback);
	}
}
