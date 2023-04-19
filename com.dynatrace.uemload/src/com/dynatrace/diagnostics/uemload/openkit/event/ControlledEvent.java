package com.dynatrace.diagnostics.uemload.openkit.event;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionDefinition;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;

/**
 * <p>
 * An event whose execution can be controlled. It finishes its work after either a minimum duration or receiving a callback. Extra work time can be added.
 * After performing {@link ElapsingEvent} end routines ({@link ElapsingEvent#run()}) it can wait for additional end delay before returning.
 * </p>
 * <h4>State timeline:</h4>
 * <p>
 * +>   Event thread created {@link #beginAfter(Consumer callback)}
 * |
 * |  Start delay period {@link #withStartDelay(int min, int max)}
 * |
 * +>   Event started, internal {@link #start()} and listeners {@link #started(EventCallback callback)} called
 * |
 * |  Callback waiting period {@link #waitFor(Consumer callback)}
 * |  Minimum duration period {@link #withMinimumDuration(int min, int max)} (if not elapsed yet)
 * |  Extra duration period {@link #withExtraDuration(int min, int max)}
 * |
 * +>   Event ended, listeners called {@link #ended(EventCallback callback)}
 * |
 * |  Sub tasks waiting period {@link #addSubTask(Supplier subTask)} - handled internally
 * |  Finish delay period {@link #withFinishDelay(int min, int max)}
 * |
 * +>   Event thread finished ({@link ActionDefinition} action finished)
 * </p>
 * All periods are optional and disabled by default - set them with listed chained methods
 */
public abstract class ControlledEvent<E extends ControlledEvent<E>> extends ElapsingEvent<E> {
	private static final long MAX_WAIT_FOR_END_CALLBACK_TIME = 1L * 60L * 1000L; //1m.
	
	private volatile long finishDelay;
	private volatile long minimumDuration;
	private volatile long extraDuration;

	private volatile boolean waitForCallback;
	private volatile boolean waitCallbackFired;

	private final AtomicInteger waitForCallbackCount = new AtomicInteger();
	private final Object lockObject = new Object();

	public E waitFor(Consumer<EventCallback> callback) {
		waitForCallbackCount.incrementAndGet();
		callback.accept(this::waitForCallbackCalled);
		waitForCallback = true;
		return getThis();
	}

	public EventCallback registerWaitForCallback() {
		waitForCallbackCount.incrementAndGet();
		waitForCallback = true;
		return this::waitForCallbackCalled;
	}

	private void waitForCallbackCalled() {
		if(waitForCallbackCount.decrementAndGet() > 0)
			return;
		synchronized (lockObject) {
			waitCallbackFired = true;
			lockObject.notifyAll();
		}
	}

	protected E withFinishDelay(int minEndDelay, int maxEndDelay) {
		this.finishDelay = UemLoadUtils.randomInt(minEndDelay, maxEndDelay);
		return getThis();
	}

	protected E withMinimumDuration(int minAddedDuration, int maxAddedDuration) {
		this.minimumDuration = UemLoadUtils.randomInt(minAddedDuration, maxAddedDuration);
		return getThis();
	}

	protected E withExtraDuration(int minExtraDuration, int maxExtraDuration) {
		this.extraDuration = UemLoadUtils.randomInt(minExtraDuration, maxExtraDuration);
		return getThis();
	}

	private void waitForCallback() {
		try {
			long callbackWaitStartTime = System.currentTimeMillis();
			synchronized (lockObject) {
				if (!waitCallbackFired) {
					lockObject.wait(MAX_WAIT_FOR_END_CALLBACK_TIME);
				}
			}
			if (System.currentTimeMillis() - callbackWaitStartTime >= MAX_WAIT_FOR_END_CALLBACK_TIME)
				logger.warning("Callback wait time exceeded");
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Event thread interrupted while waiting for end callback", e);
			Thread.currentThread().interrupt();
		}
	}

	protected void midDuration() {}

	@Override
	protected void run() {
		if (waitForCallback)
			waitForCallback();
		timeService.waitForDuration(calcRemainingWaitTime(minimumDuration));
		midDuration();
		timeService.waitForDuration(extraDuration);
		super.run();
		timeService.waitForDuration(finishDelay);
	}
}
