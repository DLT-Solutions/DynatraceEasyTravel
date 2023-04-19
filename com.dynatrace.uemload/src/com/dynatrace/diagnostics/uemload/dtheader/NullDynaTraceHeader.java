package com.dynatrace.diagnostics.uemload.dtheader;


class NullDynaTraceHeader implements DynaTraceHeader {

	@Override
	public DynaTraceHeader setPageContext(String pageContext) {
		return this;
	}

	@Override
	public DynaTraceHeader setVirtualUserId(int virtualUserId) {
		return this;
	}

	@Override
	public DynaTraceHeader setVirtualUserId(String virtualUserId) {
		return this;
	}

	@Override
	public DynaTraceHeader setTimerName(String timerName) {
		return this;
	}

	@Override
	public DynaTraceHeader addHeaderEntry(String key, String value) {
		return this;
	}

	@Override
	public String getHeaderName() {
		return null;
	}

	@Override
	public String getHeaderValue() {
		return null;
	}

	@Override
	public boolean hasValue() {
		return false;
	}

	@Override
	public DynaTraceHeader setTestName(String testName) {
		return this;
	}

	@Override
	public DynaTraceHeader setScriptName(String scriptName) {
		return this;
	}

	@Override
	public DynaTraceHeader setGeographicRegion(String countryAndContinent) {
		return this;
	}

	@Override
	public DynaTraceHeader setMetaData(DynaTraceTagMetaData metaData) {
		return this;
	}

	@Override
	public DynaTraceHeader setStepId(String string) {
		return this;		
	}
	
	@Override
	public DynaTraceHeader setApplicationId(String appId) {
		return this;
	}
	
	@Override
	public DynaTraceHeader setMonitorId(String monitorId) {
		return this;
	}
		

}
