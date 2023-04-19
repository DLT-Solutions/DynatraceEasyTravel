package com.dynatrace.diagnostics.uemload.openkit.action.definition.report.value;

import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionParent;
import com.dynatrace.diagnostics.uemload.openkit.event.lifetime.EventCallbackSet;

public class ReportValue {
	private ReportValue() {}

	public static ValueReportBuilder from(ActionParent actionParent) {
		return new ValueReportBuilder(actionParent);
	}

	public static class ValueReportBuilder {
		private final ActionParent actionParent;

		private ValueReportBuilder(ActionParent actionParent) {
			this.actionParent = actionParent;
		}

		public ConfiguredValueReportBuilder with(ValueReportConfig config) {
			return new ConfiguredValueReportBuilder(config);
		}

		public class ConfiguredValueReportBuilder {
			private final ValueReportConfig config;

			private ConfiguredValueReportBuilder(ValueReportConfig config) {
				this.config = config;
			}

			public ValueReportDefinition begin(EventCallbackSet eventCallbacks) {
				return new ValueReportDefinition(actionParent, eventCallbacks, config.valueReport);
			}
		}
	}
}
