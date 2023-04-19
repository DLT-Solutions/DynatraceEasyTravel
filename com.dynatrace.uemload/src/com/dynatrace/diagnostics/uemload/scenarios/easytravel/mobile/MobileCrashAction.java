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
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;


/**
 *
 * @author cwat-pharukst
 */
public class MobileCrashAction extends MobileEasyTravelAction {

	private static final Logger LOGGER = Logger.getLogger(MobileCrashAction.class.getName());

	/**
	 *
	 * @param session
	 * @author cwat-pharukst
	 */
	public MobileCrashAction(MobileSession session) {
		super(session);
	}

	@Override
	protected void runOnDevice(final MobileDevice device, final UEMLoadCallback continuation) throws Exception {
		if (PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.MOBILE_CRASHES_PEAK) ||
				(device.isTablet() && PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.TABLET_CRASHES) && UemLoadUtils.randomInt(100) < 2)) {
			device.crash(getSession());
			// no continuation after a crash
		} else {
			try {
				cont(continuation);
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, "Couldn't finish mobile crash action.", e);
			}
		}
	}

}
