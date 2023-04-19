package com.dynatrace.diagnostics.uemload.mobileopenkit.visit;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.mobileopenkit.MobileCommandFactory;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileActionType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileDevice;
import com.dynatrace.diagnostics.uemload.mobileopenkit.parameters.MobileOpenKitParams;
import com.dynatrace.diagnostics.uemload.openkit.visit.OpenKitVisit;

import java.util.ArrayList;
import java.util.List;

public abstract class MobileOpenKitVisit extends OpenKitVisit<MobileActionType> {
	
	public MobileOpenKitVisit(MobileOpenKitParams params, ExtendedCommonUser user, String apiUrl) {
		this.device = new MobileDevice(params, user, apiUrl);
		this.commandFactory = MobileCommandFactory.init((MobileDevice) device);
	}

	protected abstract List<MobileActionType> addActions(List<MobileActionType> list);

	@Override
	public MobileActionType[] getActions() {
		return addActions(new ArrayList<>()).toArray(new MobileActionType[0]);
	}
}
