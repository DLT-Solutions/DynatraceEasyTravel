/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: MobileNativeAction.java
 * @date: 23.01.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload;

import com.dynatrace.diagnostics.uemload.mobile.MobileDevice;


/**
 *
 * @author peter.lang
 */
public abstract class MobileNativeAction extends Action {

	/*
	 * (non-Javadoc)
	 *
	 * @see com.dynatrace.diagnostics.uemload.Action#run(com.dynatrace.diagnostics.uemload.ActionExecutor,
	 * com.dynatrace.diagnostics.uemload.UEMLoadCallback)
	 */
	@Override
	public final void run(ActionExecutor device, UEMLoadCallback continuation) throws Exception {
		if (device instanceof MobileDevice) {
			/*
			 * The current time is set in order to calculate the view duration.
			 */
			runOnDevice((MobileDevice)device, continuation);
		} else {
			throw new UnsupportedOperationException("Provided ActionExecutor not supported: class=" + device.getClass().getName());
		}

	}

	protected abstract void runOnDevice(MobileDevice device, UEMLoadCallback continuation) throws Exception;

}
