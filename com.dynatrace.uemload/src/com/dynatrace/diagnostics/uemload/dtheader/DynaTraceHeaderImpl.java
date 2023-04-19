package com.dynatrace.diagnostics.uemload.dtheader;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.RandomStringUtils;

import com.dynatrace.easytravel.util.TextUtils;

import static com.dynatrace.easytravel.constants.BaseConstants.DtHeader.*;

class DynaTraceHeaderImpl extends AbstractHeaderEntry implements DynaTraceHeader {


	private static final String testName;
	private static final String agentName;
	private static final String STEP_ID = "PI";
	private static final String APPLICATION_ID = "AI";
	private static final String MONITOR_ID = "MI";

	static {
		Calendar instance = GregorianCalendar.getInstance();
		testName =  TextUtils.merge("easyTravel_Test_{0}_{1}_{2}", String.valueOf(instance.get(Calendar.YEAR)), instance.get(Calendar.MONTH), instance.get(Calendar.DAY_OF_MONTH));
		agentName =  "eT_Agent_" + RandomStringUtils.randomAlphanumeric(5);
	}

	DynaTraceHeaderImpl() {
		super(X_DYNATRACE);
		addHeaderEntry(TEST_NAME, testName);
		addHeaderEntry(SOURCE_ID, Source.WEB_LOAD_TESTING);
		addHeaderEntry(AGENT_NAME, agentName);
	}

	DynaTraceHeaderImpl(int virtualUserId) {
		this();
		setVirtualUserId(virtualUserId);
	}
	
	@Override
	public DynaTraceHeader setPageContext(String pageContext) {
		addHeaderEntry(PAGE_CONTEXT, pageContext);
		return this;
	}

	@Override
	public DynaTraceHeader setTestName(String testName) {
		addHeaderEntry(TEST_NAME, testName);
		return this;
	}

	@Override
	public DynaTraceHeaderImpl setScriptName(String scriptName) {
		addHeaderEntry(SCRIPT_NAME, scriptName);
		return this;
	}

	@Override
	public DynaTraceHeader setVirtualUserId(int virtualUserId) {
		setVirtualUserId(String.valueOf(virtualUserId));
		return this;
	}

	@Override
	public DynaTraceHeader setVirtualUserId(String virtualUserId) {
		addHeaderEntry(VIRTUAL_USER_ID, virtualUserId);
		return this;
	}

	@Override
	public DynaTraceHeader setStepId(String stepid) {
		addHeaderEntry(STEP_ID, stepid);
		return this;
	}
	
	@Override
	public DynaTraceHeader setMonitorId(String monitorId) {
		addHeaderEntry(MONITOR_ID, monitorId);
		return this;
	}
	
	@Override
	public DynaTraceHeader setApplicationId(String appId) {
		addHeaderEntry(APPLICATION_ID, appId);
		return this;
	}
	
	@Override
	public DynaTraceHeader setTimerName(String timerName) {
		addHeaderEntry(TIMER_NAME, timerName);
		return this;
	}


	@Override
	public DynaTraceHeader setMetaData(DynaTraceTagMetaData metaData) {
		return setPageContext(metaData.getPageContext())
				.setTimerName(metaData.getTimerName())
				.setScriptName(metaData.getScriptName());
	}

	@Override
	public DynaTraceHeaderImpl setGeographicRegion(String countryAndContinent) {
		addHeaderEntry(GEOGRAPHIC_REGION, countryAndContinent);
		return this;
	}
}
