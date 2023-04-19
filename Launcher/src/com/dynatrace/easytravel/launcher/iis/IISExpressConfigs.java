package com.dynatrace.easytravel.launcher.iis;

public enum IISExpressConfigs {
	TEMPLATE("iisexpresstemplate.config"),
	B2B_FRONTEND_CONFIG("b2bfrontend.config", "easyTravel B2B Site", "B2BFrontend"),
	PAYMENT_BACKEND_CONFIG("paymentbackend.config", "PaymentService Web Service", "PaymentBackend")
	;
	
	private final String name;
	private final String siteName;
	private final String logFileNamePattern;
	private final String traceLogFileNamePattern;
	
	private IISExpressConfigs(String name) {
		this.name = name;
		this.siteName = null;
		this.logFileNamePattern = null;
		this.traceLogFileNamePattern = null;
	}
	
	private IISExpressConfigs(String name, String siteName, String logFileName) {
		this.name = name;
		this.siteName = siteName;
		this.logFileNamePattern = buildLogNamePattern(logFileName, false);
		this.traceLogFileNamePattern = buildLogNamePattern(logFileName, true);
	}
	
	public String getName() {
		return name;
	}
	
	public String getSiteName() {
		return siteName;
	}
	
	public String getLogFileNamePattern() {
		return logFileNamePattern;
	}
	
	public String getTraceLogFileNamePattern() {
		return traceLogFileNamePattern;
	}
	
	private String buildLogNamePattern(String name, boolean isTrace) {
		StringBuilder sb = new StringBuilder(name);
		sb.append("_IISExpress");
		if(isTrace)
			sb.append("_TraceLog");
		sb.append("_{0}.log");
		return sb.toString();
	}
}
