/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: BrowserAction.java
 * @date: 23.01.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload;


/**
 *
 * @author peter.lang
 */
public abstract class BrowserAction extends Action {

	/* (non-Javadoc)
	 * @see com.dynatrace.diagnostics.uemload.Action#run(com.dynatrace.diagnostics.uemload.ActionExecutor, com.dynatrace.diagnostics.uemload.UEMLoadCallback)
	 */
	@Override
	public final void run(ActionExecutor actionExecutor, UEMLoadCallback continuation) throws Exception {
		if (actionExecutor instanceof Browser) {
			/*
			 * The current time is set in order to calculate the view duration.
			 */
			Browser browser = ((Browser) actionExecutor);
			browser.updateViewDuration();
			runInBrowser(browser, continuation);
		} else {
			throw new UnsupportedOperationException("Provided ActionExecutor not supported: class=" + actionExecutor.getClass().getName());
		}
	}

	protected abstract void runInBrowser(Browser browser, UEMLoadCallback continuation) throws Exception;


}
