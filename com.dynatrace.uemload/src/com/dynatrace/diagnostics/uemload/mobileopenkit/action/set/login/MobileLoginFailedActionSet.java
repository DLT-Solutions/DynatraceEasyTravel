package com.dynatrace.diagnostics.uemload.mobileopenkit.action.set.login;

import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileDevice;

public class MobileLoginFailedActionSet extends MobileLoginActionSet {
	public MobileLoginFailedActionSet(MobileDevice device) {
		super(device);
	}

	@Override
	protected String getUserPassword() {
		return super.getUserPassword() + "_";
	}
}
