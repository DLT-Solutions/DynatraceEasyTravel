package com.dynatrace.diagnostics.uemload.openkit.action.definition.report.failure;

import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionParent;
import com.dynatrace.diagnostics.uemload.openkit.event.lifetime.EventCallbackSet;
import com.dynatrace.openkit.api.Action;

import java.util.function.Consumer;

public class FailureReportBuilder {
	private final Consumer<Action> failureReport;
	private final ActionParent actionParent;

	FailureReportBuilder(Consumer<Action> failureReport, ActionParent actionParent) {
		this.failureReport = failureReport;
		this.actionParent = actionParent;
	}

	public FailureReportDefinition begin(EventCallbackSet eventCallbacks) {
		return new FailureReportDefinition(actionParent, eventCallbacks, failureReport);
	}
}
