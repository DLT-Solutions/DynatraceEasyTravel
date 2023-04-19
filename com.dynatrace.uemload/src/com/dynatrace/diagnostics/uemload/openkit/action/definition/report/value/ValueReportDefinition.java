package com.dynatrace.diagnostics.uemload.openkit.action.definition.report.value;

import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionParent;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.report.ReportActionDefinition;
import com.dynatrace.diagnostics.uemload.openkit.event.Event;
import com.dynatrace.diagnostics.uemload.openkit.event.lifetime.EventCallbackSet;
import com.dynatrace.openkit.api.Action;

import java.util.function.Consumer;

/**
 * Represents an value reported by an OpenKit {@link Action}
 *
 * @see Event for a detailed state timeline
 */
public class ValueReportDefinition extends ReportActionDefinition<ValueReportDefinition> {
	private final Consumer<Action> valueReport;

	@Override
	protected void run() {
		super.run();
		valueReport.accept(action);
	}

	ValueReportDefinition(ActionParent parentAction, EventCallbackSet startCallbacks, Consumer<Action> valueReport) {
		super(parentAction, startCallbacks);
		this.valueReport = valueReport;
	}

	@Override
	protected ValueReportDefinition getThis() {
		return this;
	}
}
