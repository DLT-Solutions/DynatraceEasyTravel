/**
 *
 * @author: cwat-pharukst
 */
package com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile;


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.mobile.MobileDevice;

/**
 *
 * @author cwat-pharukst
 */
public class MobileAppStartLifecycleAction extends MobileEasyTravelAction {

	private static final Logger LOGGER = Logger.getLogger(MobileAppStartLifecycleAction.class.getName());

	/**
	 *
	 * @param session
	 * @author cwat-pharukst
	 */
	public MobileAppStartLifecycleAction(MobileSession session) {
		super(session);
	}

	@Override
	protected void runOnDevice(final MobileDevice device, final UEMLoadCallback continuation) throws Exception {
		device.loadingAutoUserAction(getSession(), device.isIOS() ? "DTMasterViewController" : "SearchJourneyActivity");
		try {
			cont(continuation);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Couldn't finish mobile storeBooking action.", e);
		}
	}

}
