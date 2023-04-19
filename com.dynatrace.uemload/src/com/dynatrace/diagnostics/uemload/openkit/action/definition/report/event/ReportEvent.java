package com.dynatrace.diagnostics.uemload.openkit.action.definition.report.event;

import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionParent;
import com.dynatrace.diagnostics.uemload.openkit.event.lifetime.EventCallbackSet;

public class ReportEvent {
	private ReportEvent() {}

	public static EventReportBuilder from(ActionParent actionParent) {
		return new EventReportBuilder(actionParent);
	}

	public static class EventReportBuilder {
		private final ActionParent actionParent;

		private EventReportBuilder(ActionParent actionParent) {
			this.actionParent = actionParent;
		}

		public NamedEventReportBuilder named(String name) {
			return new NamedEventReportBuilder(name);
		}

		public class NamedEventReportBuilder {
			private final String name;

			private NamedEventReportBuilder(String name) {
				this.name = name;
			}

			public EventReportDefinition begin(EventCallbackSet eventCallbacks) {
				return new EventReportDefinition(actionParent, eventCallbacks, name);
			}
		}
	}
}
