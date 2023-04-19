package com.dynatrace.diagnostics.uemload.mobileopenkit.action.set;

import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileDevice;
import com.dynatrace.diagnostics.uemload.openkit.action.ActionDefinitionSet;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.report.failure.ReportError;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.root.RootAction;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.root.RootActionDefinition;
import com.dynatrace.openkit.api.ErrorBuilder;
import com.dynatrace.openkit.api.ErrorReport;

import static com.dynatrace.diagnostics.uemload.openkit.event.lifetime.BeginCallbackSet.after;
import static com.dynatrace.diagnostics.uemload.openkit.event.lifetime.LiveCallbackSet.forDuration;

public class UseGPSWithErrorActionSet extends MobileActionSet {
	private static final String GPS_ERROR_REASON = "EasyTravelGPSStatus: Geo Location failed";

	public UseGPSWithErrorActionSet(MobileDevice device) {
		super(device);
	}

	@Override
	protected ActionDefinitionSet buildAndroid() {
		return buildCommon();
	}

	@Override
	protected ActionDefinitionSet buildIOS() {
		return buildCommon();
	}

	private ActionDefinitionSet buildCommon() {
		RootActionDefinition rootAction = RootAction.named("EasyTravelGPSStatus").live(forDuration(40, 60));
		ErrorReport errorReport = new ErrorBuilder.ExceptionEvent("EasyTravelGPSStatus", "Exception").withMessage(GPS_ERROR_REASON).build();
		ReportError.from(rootAction).with(errorReport).begin(after(rootAction::started));
		return new ActionDefinitionSet(rootAction);
	}
}
