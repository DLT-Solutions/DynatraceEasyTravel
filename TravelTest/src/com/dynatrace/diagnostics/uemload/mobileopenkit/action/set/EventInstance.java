package com.dynatrace.diagnostics.uemload.mobileopenkit.action.set;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.mobile.DisplayAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract class EventInstance {
	protected final String value;
	public Runnable eventVerification;

	public EventInstance(String value) {
		this.value = value;
	}

	public void setVerification(Runnable verification) {
		this.eventVerification = verification;
	}

	static class EventReportInstance extends EventInstance {
		public EventReportInstance(String name) {
			super(name);
		}

		public String getName() {
			return value;
		}
	}

	static class LifeCycleInstance extends EventInstance implements EventRoot<DisplayAction> {
		public DisplayAction action;

		public LifeCycleInstance(String name) {
			super(name);
		}

		public String getName() {
			return value;
		}

		@Override
		public DisplayAction getAction() {
			return action;
		}
	}

	static class ValueReportInstance extends EventReportInstance {
		private Object value;

		public ValueReportInstance(String name, int value) {
			super(name);
			this.value = value;
		}
		public ValueReportInstance(String name, String value) {
			super(name);
			this.value = value;
		}
		public ValueReportInstance(String name, double value) {
			super(name);
			this.value = value;
		}

		public Object getValue() {
			return value;
		}
	}

	static class ErrorReportInstance extends EventReportInstance {
		public final int errorCode;

		public ErrorReportInstance(String name, int errorCode) {
			super(name);
			this.errorCode = errorCode;
		}
	}

	static class WebRequestInstance extends EventInstance {
		public final boolean matchUrlEqual;

		public WebRequestInstance(String url, boolean matchUrlEqual) {
			super(url);
			this.matchUrlEqual = matchUrlEqual;
		}

		public String getUrl() {
			return value;
		}
	}

	static class ActionInstance extends EventInstance implements EventRoot<Action> {
		public final List<EventInstance> subActions = new ArrayList<>();
		public Action action;

		public ActionInstance(String name) {
			super(name);
		}

		public String getName() {
			return value;
		}

		public void add(EventInstance subAction) {
			subActions.add(subAction);
		}

		public void addAll(EventInstance... actions) {
			subActions.addAll(Arrays.asList(actions));
		}

		@Override
		public Action getAction() {
			return action;
		}
	}

}
