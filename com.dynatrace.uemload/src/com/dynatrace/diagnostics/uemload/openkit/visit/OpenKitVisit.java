package com.dynatrace.diagnostics.uemload.openkit.visit;

import com.dynatrace.diagnostics.uemload.openkit.CommandFactory;
import com.dynatrace.diagnostics.uemload.openkit.Device;
import com.dynatrace.diagnostics.uemload.openkit.action.EventType;
import com.dynatrace.openkit.api.CrashReport;

public abstract class OpenKitVisit<A extends EventType> {
	protected CommandFactory commandFactory;
	protected Device device; 
	

	public abstract A[] getActions();

	public void executeAction(A action) {
		commandFactory.executeCommand(action);
	}

	public void finishVisit() {
		device.endActiveSession();
	}
	
	public Device getDevice() {
		return device;
	}
}
