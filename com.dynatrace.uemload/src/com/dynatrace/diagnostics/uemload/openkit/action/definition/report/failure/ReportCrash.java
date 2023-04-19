package com.dynatrace.diagnostics.uemload.openkit.action.definition.report.failure;

import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionParent;
import com.dynatrace.openkit.api.CrashReport;

public class ReportCrash {
	private ReportCrash() {}

	public static CrashReportBuilder from(ActionParent actionParent) {
		return new CrashReportBuilder(actionParent);
	}

	public static class CrashReportBuilder {
		private ActionParent actionParent;

		private CrashReportBuilder(ActionParent actionParent) {
			this.actionParent = actionParent;
		}

		public FailureReportBuilder containing(CrashReport report) {
			return new FailureReportBuilder(action -> action.reportCrash(report), actionParent);
		}
	}
}
