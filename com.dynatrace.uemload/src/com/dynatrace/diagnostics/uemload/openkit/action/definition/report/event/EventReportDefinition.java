package com.dynatrace.diagnostics.uemload.openkit.action.definition.report.event;

import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionParent;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.report.ReportActionDefinition;
import com.dynatrace.diagnostics.uemload.openkit.event.Event;
import com.dynatrace.diagnostics.uemload.openkit.event.lifetime.EventCallbackSet;
import com.dynatrace.openkit.api.Action;

/**
 * Represents an event reported by an OpenKit {@link Action}
 *
 * @see Event for a detailed state timeline
 */
public class EventReportDefinition extends ReportActionDefinition<EventReportDefinition> {
	EventReportDefinition(ActionParent parentAction, EventCallbackSet startCallbacks, String name) {
		super(parentAction, startCallbacks);
		this.name = name;
	}

	@Override
	protected void run() {
		super.run();
		action.reportEvent(name);
	}

	@Override
	protected EventReportDefinition getThis() {
		return this;
	}
}
