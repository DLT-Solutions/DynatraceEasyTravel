package com.dynatrace.diagnostics.uemload.dtheader;


public interface DynaTraceHeader extends HeaderEntry {

	DynaTraceHeader setPageContext(String pageContext);

	DynaTraceHeader setTestName(String testName);

	DynaTraceHeader setScriptName(String scriptName);

	DynaTraceHeader setVirtualUserId(int virtualUserId);

	DynaTraceHeader setVirtualUserId(String virtualUserId);

	DynaTraceHeader setMetaData(DynaTraceTagMetaData metaData);

	DynaTraceHeader setTimerName(String timerName);

	DynaTraceHeader setGeographicRegion(String countryAndContinent);

	DynaTraceHeader setApplicationId(String appId);
	DynaTraceHeader setMonitorId(String monitorId);
	DynaTraceHeader setStepId(String stepId);				

}