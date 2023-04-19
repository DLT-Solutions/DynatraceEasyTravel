package com.dynatrace.diagnostics.uemload.openkit;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.openkit.SessionSurrogateDataBuilder;
import com.dynatrace.openkit.api.CrashReport;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.Session;
import com.google.common.base.Strings;

public abstract class Device {	
	protected final ExtendedCommonUser user;
	
	protected Session activeSession; 
	public final String appVersion;

	protected CrashReport crashReport;
	private final boolean disableCrashReports = EasyTravelConfig.read().openKitDisableCrashReports;
	
	public Device(OpenKit openKit, OpenKitParams params, ExtendedCommonUser user) {
		this.user = user;
		this.appVersion = params.getAppVersion();
		openKit.waitForInitCompletion(60000);
		
		SessionSurrogateDataBuilder builder = new SessionSurrogateDataBuilder()
			.withAgentTechnologyType(params.getAgentType())
			.withManufacturer(params.getManufacturer())
			.withApplicationVersion(params.getAppVersion())
			.withModelId(params.getModel())
			.withOperatingSystem(params.getOs())
			.withDeviceId(params.getDeviceId())
			.withDataCollectionLevel(params.getDataCollectionLevel())
			.withCrashReportingLevel(params.getCrashReportingLevel());
		
		if (!Strings.isNullOrEmpty(params.getAppVersionBuild())) {
			builder = builder.withApplicationVersionBuild(params.getAppVersionBuild());
		}
		
		if (!Strings.isNullOrEmpty(params.getApplicationPackage())) {
			builder = builder.withApplicationPackage(params.getApplicationPackage());
		}
		
		activeSession = openKit.createSession(params.getIp(), builder.build());
	}

	public void endActiveSession() {
		activeSession.end();
		activeSession = null;
	}

	public Session getActiveSession() {
		return activeSession;
	}

	public ExtendedCommonUser getUser() {
		return user;
	}
	
	public boolean isCrashed() {
		return crashReport != null;
	}
	
	public void sendCrash() {
		if (!disableCrashReports && crashReport != null) {
			activeSession.reportCrash(crashReport);
		}
	}
	
	public void setCrashReport(CrashReport crashReport) {
		this.crashReport = crashReport;
	}	
	
	public void identifyUser() {
		activeSession.identifyUser(user.getFullName());
	}
}
