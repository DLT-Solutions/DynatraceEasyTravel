package com.dynatrace.diagnostics.uemload.openkit.action.definition.report.value;

import com.dynatrace.openkit.api.Action;

import java.util.function.Consumer;

public class ValueReportConfig {
	public final Consumer<Action> valueReport;

	private ValueReportConfig( Consumer<Action> valueReport) {
		this.valueReport = valueReport;
	}

	public static ValueReportConfigBuilder textValue(String reportedText) {
		return new ValueReportConfigBuilder<>(reportedText);
	}

	public static ValueReportConfigBuilder doubleValue(double reportedNumber) {
		return new ValueReportConfigBuilder<>(reportedNumber);
	}

	public static ValueReportConfigBuilder intValue(int reportedNumber) {
		return new ValueReportConfigBuilder<>(reportedNumber);
	}

	public static class ValueReportConfigBuilder<V> {
		public final V value;

		private ValueReportConfigBuilder(V reportedValue) {
			this.value = reportedValue;
		}

		public ValueReportConfig named(String reportedName) {
			if (value instanceof String)
				return new ValueReportConfig(action -> action.reportValue(reportedName, (String) value));
			else if (value instanceof Double)
				return new ValueReportConfig(action -> action.reportValue(reportedName, (Double) value));
			else
				return new ValueReportConfig(action -> action.reportValue(reportedName, (Integer) value));
		}
	}
}
