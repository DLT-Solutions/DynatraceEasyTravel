package com.dynatrace.diagnostics.uemload.mobileopenkit.action.set.search;

import static com.dynatrace.diagnostics.uemload.openkit.event.lifetime.BeginCallbackSet.after;
import static com.dynatrace.diagnostics.uemload.openkit.event.lifetime.LiveCallbackSet.forDuration;

import com.dynatrace.diagnostics.uemload.mobile.Error;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.set.MobileActionSet;
import com.dynatrace.diagnostics.uemload.mobileopenkit.crash.Crash;
import com.dynatrace.diagnostics.uemload.mobileopenkit.crash.CrashLoader;
import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileDevice;
import com.dynatrace.diagnostics.uemload.openkit.action.ActionDefinitionSet;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.report.failure.ReportError;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.root.RootAction;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.openkit.api.CrashBuilder;
import com.dynatrace.openkit.api.CrashReport;
import com.dynatrace.openkit.api.ErrorBuilder;
import com.dynatrace.openkit.api.ErrorReport;

public class MobileSearchTouchCrashActionSet extends MobileActionSet {
	private static final int TABLET_CRASHES_THRESHOLD_DEFAULT = 2;
	private static final int MOBILE_ERRORS_THRESHOLD_DEFAULT = 5;
	//not final because may be modified in tests
	@TestOnly
	private static volatile boolean GENERATE_MOBILE_OR_TABLET_CRASH_ALWAYS = false;
	@TestOnly
	private static volatile boolean GENERATE_MOBILE_ERRORS_ALWAYS = false;
	
	public MobileSearchTouchCrashActionSet(MobileDevice device) {
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
		Error[] errors = Error.values(); //TODO iOS?
		Error error = errors[randomGenerator.nextInt(errors.length)];

		if (generateMobileOrTabletCrash()) {
			CrashReport crashReport = new CrashBuilder(error.getName()).withReason(error.getReason()).withStackTrace(error.getStacktrace()).build();			
			device.setCrashReport(crashReport);
		}
		else if (generateMobileError()) {
			RootAction.RootActionBuilder rootAction = RootAction.named("Touch on Search");
			
			if (!device.isIOS()) {	
				ErrorReport errorReport = new ErrorBuilder.ExceptionEvent(error.getName(), error.getValue())
						.withMessage(error.getReason()).withStackTrace(error.getStacktrace()).build();
				ReportError.from(rootAction).with(errorReport).begin(after(rootAction::started));
			}

			Crash crash = CrashLoader.loadCrashReport(device);
			CrashReport crashReport = new CrashBuilder(crash.name).withReason(crash.reason).withStackTrace(crash.getStackTrace()).withSignalNumber(crash.signalNumber).build();
			device.setCrashReport(crashReport);			
			return new ActionDefinitionSet(rootAction.live(forDuration(100, 300)));
		}
		return new ActionDefinitionSet();
	}
	
	private boolean generateMobileOrTabletCrash() {
		return !device.isIOS() && 
				(
						GENERATE_MOBILE_OR_TABLET_CRASH_ALWAYS || 
						PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.MOBILE_CRASHES_PEAK) || 
						(
								device.getUser().getMobileDevice().isTablet() &&
								PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.TABLET_CRASHES) && randomGenerator.nextInt(100) < TABLET_CRASHES_THRESHOLD_DEFAULT
						)
				);
	}
	
	private boolean generateMobileError() {
		return GENERATE_MOBILE_ERRORS_ALWAYS
				|| (PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.MOBILE_ERRORS) && randomGenerator.nextInt(100) < MOBILE_ERRORS_THRESHOLD_DEFAULT);
	}
	
	@TestOnly
	public static void eachVisitShoudGenerateMobileOrTabletCrash() {
		GENERATE_MOBILE_OR_TABLET_CRASH_ALWAYS = true;
	}
	
	@TestOnly
	public static void eachVisitShoudGenerateMobileError() {
		GENERATE_MOBILE_ERRORS_ALWAYS = true;
	}	
}
