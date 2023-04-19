package com.dynatrace.diagnostics.uemload.openkit.action;

import com.dynatrace.diagnostics.uemload.openkit.Device;

/**
 * A set of actions performed on a device
 */
public abstract class ActionSet<D extends Device> {
	protected final D device;

	public ActionSet(D device) {
		this.device = device;
	}

	protected abstract ActionDefinitionSet build();

	public void run() {
		build().run(device.getActiveSession());
	}
}
