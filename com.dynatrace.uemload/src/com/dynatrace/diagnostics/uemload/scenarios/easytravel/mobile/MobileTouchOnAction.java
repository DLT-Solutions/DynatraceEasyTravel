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
public class MobileTouchOnAction extends MobileEasyTravelAction {

	private static final Logger LOGGER = Logger.getLogger(MobileTouchOnAction.class.getName());

	private String viewName;
	private String widgetName;

	/**
	 *
	 * @param session
	 * @author cwat-pharukst
	 */
	public MobileTouchOnAction(MobileSession session, String viewName, String widgetName) {
		super(session);
		this.viewName = viewName;
		this.widgetName = widgetName;
	}

	@Override
	protected void runOnDevice(final MobileDevice device, final UEMLoadCallback continuation) throws Exception {
		device.startAutoUserAction(getSession(), "Touch on " +widgetName);
		if(viewName != null){	//don't do a display if there is no view
			device.displayLifecycleAction(getSession(), viewName);
		}
		device.leaveAutoUserAction(getSession());
		try {
			cont(continuation);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Couldn't finish mobile touchOn action.", e);
		}
	}

}
