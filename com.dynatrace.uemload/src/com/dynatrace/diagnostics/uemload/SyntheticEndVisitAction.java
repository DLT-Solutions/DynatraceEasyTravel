package com.dynatrace.diagnostics.uemload;

public class SyntheticEndVisitAction extends Action {

	@Override
	public void run(ActionExecutor browser, UEMLoadCallback continuation) throws Exception {
		if (browser instanceof Browser) {
			((Browser)browser).sendSyntheticEndVisit();
		} else {
			throw new UnsupportedOperationException("Provided ActionExecutor not supported: class=" + browser.getClass().getName());
		}
	}
}
