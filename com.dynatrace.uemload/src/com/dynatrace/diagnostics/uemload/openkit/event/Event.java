package com.dynatrace.diagnostics.uemload.openkit.event;

import com.dynatrace.diagnostics.uemload.openkit.event.lifetime.EventCallbackSet;
import com.dynatrace.diagnostics.uemload.openkit.time.RealTimeService;
import com.dynatrace.diagnostics.uemload.openkit.time.TimeService;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Base class representing an event in time without duration. Usually triggered by a callback. Provides a way to notify listeners when it's running.
 * </p>
 * <h4>State timeline:</h4>
 * <p>
 * +>   Event started {@link #beginAfter(Consumer callback)}, internal {@link #start()} and listeners {@link #started(EventCallback callback)} called
 * |
 * +>   Event thread finished
 * </p>
 * Start delay period is optional and disabled by default - add it with listed chained method
 *
 * @param <E> Self-referential generic parameter introduced to enable easy Method chaining without casts
 */
public abstract class Event<E extends Event<E>> {
	protected static final Logger logger = Logger.getLogger(Event.class.getName());

	protected volatile long startTime;
	private final Set<EventCallback> startListeners = new HashSet<>();

	private final AtomicInteger beginCallbackCount = new AtomicInteger();

	protected TimeService timeService = timeServiceSupplier.get();
	public static Supplier<TimeService> timeServiceSupplier = RealTimeService::new; // Used to speed up unit tests

	protected E beginAfter(Consumer<EventCallback> callback) {
		beginCallbackCount.incrementAndGet();
		callback.accept(this::beginCallback);

		return getThis();
	}

	protected E addCallbacks(EventCallbackSet eventCallbacks, Consumer<Consumer<EventCallback>> callbackAction) {
		eventCallbacks.callbacks.forEach(callbackAction);
		return getThis();
	}

	protected final E addBeginCallbacks(EventCallbackSet eventCallbacks) {
		return addCallbacks(eventCallbacks, this::beginAfter);
	}

	public EventCallback registerBeginCallback() {
		beginCallbackCount.incrementAndGet();
		return this::beginCallback;
	}

	private void beginCallback() {
		if(beginCallbackCount.decrementAndGet() == 0)
			startExecution();
	}

	protected abstract void run();
	protected void start() {}

	protected void startExecution() {
		try {
			start();
			callListeners(startListeners);
			startTime = System.currentTimeMillis();
			run();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception thrown while executing event", e);
		}
	}

	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}

	public TimeService getTimeService() {
		return timeService;
	}

	protected abstract E getThis();

	protected void callListeners(Set<EventCallback> listeners) {
		listeners.forEach(EventCallback::call);
	}

	public void started(EventCallback callback) {
		startListeners.add(callback);
	}
}
