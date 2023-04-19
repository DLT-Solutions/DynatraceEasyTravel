package com.dynatrace.diagnostics.uemload.openkit.action.definition.report.failure;

import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionParent;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.report.ReportActionDefinition;
import com.dynatrace.diagnostics.uemload.openkit.event.Event;
import com.dynatrace.diagnostics.uemload.openkit.event.lifetime.EventCallbackSet;
import com.dynatrace.openkit.api.Action;

import java.util.function.Consumer;

/**
 * Represents an error or crash reported by an OpenKit {@link Action}
 *
 * @see Event for a detailed state timeline
 */
public class FailureReportDefinition extends ReportActionDefinition<FailureReportDefinition> {
	private final Consumer<Action> failureReport;

	FailureReportDefinition(ActionParent actionParent, EventCallbackSet eventCallbacks, Consumer<Action> failureReport) {
		super(actionParent, eventCallbacks);
		this.failureReport = failureReport;
	}

	@Override
	protected void run() {
		super.run();
		failureReport.accept(action);
	}

	@Override
	protected FailureReportDefinition getThis() {
		return this;
	}
}
