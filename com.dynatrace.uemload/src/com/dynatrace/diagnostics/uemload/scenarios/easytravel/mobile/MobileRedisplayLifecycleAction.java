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
public class MobileRedisplayLifecycleAction extends MobileEasyTravelAction {

	private static final Logger LOGGER = Logger.getLogger(MobileRedisplayLifecycleAction.class.getName());
	
	private String viewName;

	/**
	 *
	 * @param session
	 * @author cwat-pharukst
	 */
	public MobileRedisplayLifecycleAction(MobileSession session, String viewName) {
		super(session);
		this.viewName = viewName;
	}

	@Override
	protected void runOnDevice(final MobileDevice device, final UEMLoadCallback continuation) throws Exception {
		device.redisplayLifecycleAction(getSession(), viewName);
		try {
			cont(continuation);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Couldn't finish mobile storeBooking action.", e);
		}
	}

}
