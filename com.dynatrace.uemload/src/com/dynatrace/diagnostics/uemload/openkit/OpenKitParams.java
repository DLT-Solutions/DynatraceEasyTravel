package com.dynatrace.diagnostics.uemload.openkit;

import com.dynatrace.openkit.AgentTechnologyType;
import com.dynatrace.openkit.CrashReportingLevel;
import com.dynatrace.openkit.DataCollectionLevel;

/**
 * @author Michal.Bakula
 */
public class OpenKitParams {		
	private String country;
	private String ip;
	private String manufacturer;
	private String model;
	private String os;
	private String appVersion;
	private long deviceId;
	private DataCollectionLevel dataCollectionLevel;
	private CrashReportingLevel crashReportingLevel;
	private AgentTechnologyType agentType;
	private String appVersionBuild;
	private String applicationPackage;

	public OpenKitParams() {}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(long deviceId) {
		this.deviceId = deviceId;
	}

	public DataCollectionLevel getDataCollectionLevel() {
		return dataCollectionLevel;
	}

	public void setDataCollectionLevel(DataCollectionLevel dataCollectionLevel) {
		this.dataCollectionLevel = dataCollectionLevel;
	}

	public CrashReportingLevel getCrashReportingLevel() {
		return crashReportingLevel;
	}

	public void setCrashReportingLevel(CrashReportingLevel crashReportingLevel) {
		this.crashReportingLevel = crashReportingLevel;
	}

	public AgentTechnologyType getAgentType() {
		return agentType;
	}

	public void setAgentType(AgentTechnologyType agentType) {
		this.agentType = agentType;
	}

	public String getAppVersionBuild() {
		return appVersionBuild;
	}

	public void setAppVersionBuild(String appVersionBuild) {
		this.appVersionBuild = appVersionBuild;
	}

	public String getApplicationPackage() {
		return applicationPackage;
	}

	public void setApplicationPackage(String applicationPackage) {
		this.applicationPackage = applicationPackage;
	}
}
