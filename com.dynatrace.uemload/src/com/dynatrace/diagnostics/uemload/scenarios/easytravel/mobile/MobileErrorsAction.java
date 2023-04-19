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
import com.dynatrace.diagnostics.uemload.mobile.MobileDeviceType;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;


/**
 *
 * @author cwat-pharukst
 */
public class MobileErrorsAction extends MobileEasyTravelAction {

	private static final Logger LOGGER = Logger.getLogger(MobileErrorsAction.class.getName());

	/**
	 *
	 * @param session
	 * @author cwat-pharukst
	 */
	public MobileErrorsAction(MobileSession session) {
		super(session);
	}

	@Override
	protected void runOnDevice(final MobileDevice device, final UEMLoadCallback continuation) throws Exception {
		if (PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.MOBILE_ERRORS) && UemLoadUtils
						.randomInt(100) < 98) {
			device.error(getSession());
		}

		try {
			cont(continuation);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Couldn't finish mobile error action.", e);
		}
	}
}
