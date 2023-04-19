package com.dynatrace.diagnostics.uemload.openkit.action.definition.report.failure;

import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionParent;
import com.dynatrace.openkit.api.ErrorReport;

public class ReportError {
	private ReportError() {}

	public static ErrorReportBuilder from(ActionParent actionParent) {
		return new ErrorReportBuilder(actionParent);
	}

	public static class ErrorReportBuilder {
		private ActionParent actionParent;

		private ErrorReportBuilder(ActionParent actionParent) {
			this.actionParent = actionParent;
		}

		public FailureReportBuilder with(ErrorReport report) {
			return new FailureReportBuilder(action -> action.reportError(report), actionParent);
		}
	}
}
