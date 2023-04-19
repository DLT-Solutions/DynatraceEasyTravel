package com.dynatrace.diagnostics.uemload.openkit.action;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.ErrorBuilder;
import com.dynatrace.openkit.api.Session;

import java.util.logging.Logger;

/**
 * Common action set patterns
 */
public class ActionSetPatterns {

	protected final Logger logger = Logger.getLogger(getClass().getName());
	protected final Session activeSession;

	public ActionSetPatterns(Session session) {
		this.activeSession = session;
	}

	public void simpleAction(String name, long actionDurationMs) {
		Action action = activeSession.enterAction(name);
		simulateProcessing(action, actionDurationMs, DurationUnit.MS);
		action.leaveAction();
	}

	protected void simpleRealtimeAction(String name, int actionDurationNs) {
		Action action = activeSession.enterAction(name);
		simulateProcessing(action, actionDurationNs, DurationUnit.NS);
		action.leaveAction();
	}

	public void actionWithEvent(String name, long actionDurationMs, String eventName) {
		Action action = activeSession.enterAction(name);
		action.reportEvent(eventName);
		simulateProcessing(action, actionDurationMs, DurationUnit.MS);
		action.leaveAction();
	}

	protected void actionWithValue(String name, long actionDurationMs, String key, String value) {
		Action action = activeSession.enterAction(name);
		action.reportValue(key, value);
		simulateProcessing(action, actionDurationMs, DurationUnit.MS);
		action.leaveAction();
	}

	protected void realtimeActionWithValue(String name, int actionDurationNs, String key, String value) {
		Action action = activeSession.enterAction(name);
		action.reportValue(key, value);
		simulateProcessing(action, actionDurationNs, DurationUnit.NS);
		action.leaveAction();
	}

	public void actionWithError(String name, long actionDurationMs, String errorName, int errorCode) {
		Action action = activeSession.enterAction(name);
		action.reportError(new ErrorBuilder.ErrorCodeEvent(errorName, errorCode).build());
		simulateProcessing(action, actionDurationMs, DurationUnit.MS);
		action.leaveAction();
	}

	protected void realtimeActionWithError(String name, int actionDurationNs, String errorName, int errorCode) {
		Action action = activeSession.enterAction(name);
		action.reportError(new ErrorBuilder.ErrorCodeEvent(errorName, errorCode).build());
		simulateProcessing(action, actionDurationNs, DurationUnit.NS);
		action.leaveAction();
	}

	protected void simulateProcessing(Action action, long duration, DurationUnit unit) {
		if (duration <= 0) return;
		try {
			if (unit == DurationUnit.MS)
				Thread.sleep(duration);
			else
				Thread.sleep(0, (int) duration);
		} catch (InterruptedException e) {
			action.reportError(new ErrorBuilder.ErrorCodeEvent("Internal Error", -1).build());
			Thread.currentThread().interrupt();
		}
	}

	protected enum DurationUnit {
		MS, NS
	}

}
