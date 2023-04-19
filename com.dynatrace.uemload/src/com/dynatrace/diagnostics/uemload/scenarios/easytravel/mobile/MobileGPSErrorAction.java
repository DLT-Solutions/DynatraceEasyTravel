package com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile;

import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.mobile.MobileDevice;

public class MobileGPSErrorAction extends MobileEasyTravelAction {

	public MobileGPSErrorAction(MobileSession session) {
		super(session);
	}

	@Override
	protected void runOnDevice(final MobileDevice device, final UEMLoadCallback continuation) throws Exception {
		device.reportException(getSession(), "EasyTravelGPSStatus", "Exception", "EasyTravelGPSStatus: Geo Location failed", null);	//GPS error
	}

}
