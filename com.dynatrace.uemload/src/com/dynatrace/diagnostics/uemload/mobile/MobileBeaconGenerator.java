package com.dynatrace.diagnostics.uemload.mobile;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.ArrayList;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * this is cloned from ruxit repo com.compuware.apm.pattern.mobile.helper.MobileBeaconGenerator - keep in sync!
 * @author patrick.haruksteiner
 */
public class MobileBeaconGenerator {

	private static final Logger LOGGER = LoggerFactory.make();

	public static final String EASY_TRAVEL_RUXIT_APPLICATION_ID = "752c288d-5973-4c79-b7d1-3a49d4d42ea0";
	public static final String EASY_TRAVEL_APPLICATION_NAME = "easyTravel mobile";
	public static final String MOBILE_AGENT_HEADER = "x-dynatrace";
	public static final String TAG_PREFIX = "MT";
	public static final int CURRENT_PROTOCOL_VERSION = 3;

	private static final String ENCODING = "UTF-8";
	private static final int OMITTED = -1;

	public enum EventType {
		UNKNOWN,
		ACTION_MANUAL,
		ACTION_AUTO,
		VALUE_STRING,
		VALUE_INT,
		VALUE_DOUBLE,
		NAMED_EVENT,
		SESSION_END,
		APP_START,
		DISPLAY,
		REDISPLAY,
		WEBREQUEST,
		ERROR_CODE,
		EXCEPTION,
		NS_ERROR,
		CRASH,
		SESSION_START,
		IDENTIFY_USER;

		public static EventType enumFromProtocolValue (short protocolValue) {
			switch (protocolValue) {
				case 1: return ACTION_MANUAL;
				case 6: return ACTION_AUTO;
				case 11: return VALUE_STRING;
				case 12: return VALUE_INT;
				case 13: return VALUE_DOUBLE;
				case 10: return NAMED_EVENT;
				case 18: return SESSION_START;
				case 19: return SESSION_END;
				case 20: return APP_START;
				case 21: return DISPLAY;
				case 22: return REDISPLAY;
				case 30: return WEBREQUEST;
				case 40: return ERROR_CODE;
				case 42: return EXCEPTION;
				case 43: return NS_ERROR;
				case 50: return CRASH;
				case 60: return IDENTIFY_USER;
				default: return UNKNOWN;
			}
		}

		public short protocolValue() {
			switch (this) {
				case ACTION_MANUAL: return 1;
				case ACTION_AUTO: return 6;
				case VALUE_STRING: return 11;
				case VALUE_INT: return 12;
				case VALUE_DOUBLE: return 13;
				case NAMED_EVENT: return 10;
				case SESSION_START: return 18;
				case SESSION_END: return 19;
				case APP_START: return 20;
				case DISPLAY: return 21;
				case REDISPLAY: return 22;
				case WEBREQUEST: return 30;
				case ERROR_CODE: return 40;
				case EXCEPTION: return 42;
				case NS_ERROR: return 43;
				case CRASH: return 50;
				case IDENTIFY_USER: return 60;
				default: return -1;
			}
		}
	}

	private static final String PREFIX_LC_IOS = "ios";

	private class Location {

		private float longitude;
		private float latitude;

		public Location(float longitude, float latitude) {
			this.longitude = longitude;
			this.latitude = latitude;
		}

		@Override
		public String toString() {
			return String.format("%.3fx%.3f", longitude, latitude);
		}
	}

	public enum SoftwareState {UNKNOWN ("-"), GENUINE ("Genuine"), JAILBROKEN ("Jailbroken"), ROOTED ("Rooted");

	private final String name;

	private SoftwareState(String name) {
		this.name = name;
	}

	public static SoftwareState enumFromProtocolValue (char protocolValue, String operatingSystem) {
		switch (protocolValue) {
			case 'g': return GENUINE;
			case 'r': return operatingSystem.toLowerCase().startsWith(PREFIX_LC_IOS)
					? JAILBROKEN : ROOTED;
			default: return UNKNOWN;
		}
	}

	public char protocolValue() {
		switch (this) {
			case GENUINE: return 'g';
			case JAILBROKEN:
			case ROOTED: return 'r';
			default: return '-';
		}
	}

	@Override
	public String toString(){
		return name;
	}
	}

	public enum Orientation {UNKNOWN ("-"), PORTRAIT ("Portrait"), LANDSCAPE ("Landscape");

	private final String name;

	private Orientation(String name) {
		this.name = name;
	}

	public static Orientation enumFromProtocolValue (char protocolValue) {
		switch (protocolValue) {
			case 'p': return PORTRAIT;
			case 'l': return LANDSCAPE;
			default: return UNKNOWN;
		}
	}

	public char protocolValue() {
		switch (this) {
			case PORTRAIT: return 'p';
			case LANDSCAPE: return 'l';
			default: return '-';
		}
	}

	@Override
	public String toString(){
		return name;
	}
	}

	public enum ConnectionType { UNKNOWN ("-"), OFFLINE ("Offline"), MOBILE ("Mobile"), WIFI ("WiFi"), LAN ("LAN");

	private final String name;

	private ConnectionType(String name) {
		this.name = name;
	}

	public static ConnectionType enumFromProtocolValue (char protocolValue) {
		switch (protocolValue) {
		case 'm': return MOBILE;
		case 'w': return WIFI;
		case 'o': return OFFLINE;
		case 'l': return LAN;
		default: return UNKNOWN;
		}
	}

	public char protocolValue() {
		switch (this) {
		case MOBILE: return 'm';
		case WIFI: return 'w';
		case OFFLINE: return 'o';
		case LAN: return 'l';
		default: return '-';
		}
	}

	@Override
	public String toString(){
		return name;
	}
	}

	private String query;
	private int tagId = 0;
	private int sequenceNumber = 0;
	private long lastEndTime = -1;

	//basic data
	public short protocolVersion = CURRENT_PROTOCOL_VERSION;
	public int serverId = 1;
	public String agentVersion = "7.2.5678";
	public String appId = EASY_TRAVEL_RUXIT_APPLICATION_ID;	//easyTravel app id
	public String appName = "Test App";
	public String appPackage = "com.dynatrace.easytravel";
	public String appVersionName = "1.0";
	public String appVersionBuild = "1.0.123";
	public long sessionStartTime = System.currentTimeMillis();
	public String visitorId = "123456789";
	public int sessionNumber = 42;
	public int memory = 2048;
	public String cpu = "ARM_64";
	public String operatingSystem = "iOS 12.1";
	public String manufacturer = "Apple";
	public String modelId = "iPhone10,3";
	public SoftwareState softwareState = SoftwareState.GENUINE;
	public int screenWidth = 750;
	public int screenHeight = 1334;
	public int screenDensity = 2;
	public Orientation orientation = Orientation.PORTRAIT;
	public byte batteryLevel = 99;
	public int freeMemory = 512;
	public String carrier = "AT&T";
	public ConnectionType connectionType = ConnectionType.WIFI;
	public String networkTechnology = "LTE";
	public Location location = new Location(42.332f, -83.046f);
	public String userLanguage = "en_US";
	public boolean isNewUser = false;
	public String remoteIP = "52.5.109.165";	//ruxit.com

	private long drift = 0;

	private ArrayList<String> eventDataEvents = new ArrayList<String>();
	private ArrayList<String> eventDataActions = new ArrayList<String>();

	/**
	 * create mobile beacon, pre-filled with demo basic data, but no events<br>
	 * demo basic data can be overwritten public accessible variables if needed
	 *
	 * @param appId
	 * @param protocolVersion
	 */
	public MobileBeaconGenerator (String appId, int protocolVersion) {
		this.protocolVersion = (short) protocolVersion;
		this.appId = appId;
		try {
			query = "?type=m&srvid=1&app=" + URLEncoder.encode(appId, ENCODING);
		} catch (UnsupportedEncodingException uee) {
			LOGGER.error("Exception while creating mobile beacon generator: ", uee);
		}
		this.sessionStartTime = System.currentTimeMillis();
	}
    
    public String getAgentTechnologyType() {
        if (operatingSystem.toLowerCase().startsWith("ios")) {
            return "maios";
        } else if (operatingSystem.toLowerCase().startsWith("android")) {
            return "maandroid";
        }
        return "okjava"; //default to OpenKit Java if OS is unknown
    }

	public int nextSequenceNumber() {
		return ++sequenceNumber;
	}

	public int nextTagId() {
		return ++tagId;
	}


	public long getTimeSinceSessionStart() {
		return System.currentTimeMillis() - sessionStartTime;
	}

	public void setDrift(long drift) {
		this.drift = drift;
	}

	public String getMobileRequestTag(int parentActionId, int requestId, int threadId){
		final StringBuilder tagBuilder = new StringBuilder();
		//MT_version_serverID_visitorID_sessionNr_appID_actionID_threadID_requestID;
		tagBuilder.append(TAG_PREFIX);
		tagBuilder.append(BaseConstants.UNDERSCORE);
		//basicInfoAttachment is null for non-root events
		tagBuilder.append(protocolVersion);
		tagBuilder.append(BaseConstants.UNDERSCORE);
		tagBuilder.append(serverId);
		tagBuilder.append(BaseConstants.UNDERSCORE);
		tagBuilder.append(visitorId);
		tagBuilder.append(BaseConstants.UNDERSCORE);
		tagBuilder.append(sessionNumber);
		tagBuilder.append(BaseConstants.UNDERSCORE);
		tagBuilder.append(appId);
		tagBuilder.append(BaseConstants.UNDERSCORE);
		tagBuilder.append(parentActionId);
		tagBuilder.append(BaseConstants.UNDERSCORE);
		tagBuilder.append(threadId);
		tagBuilder.append(BaseConstants.UNDERSCORE);
		tagBuilder.append(requestId);
		return tagBuilder.toString();
	}

	public void newUser() {
		visitorId = Integer.toString((int)(Math.random() * 100000000));
	}

	public void newVisit() {
		sessionNumber = (int)(Math.random() * 1000);
		visitorId = Integer.toString((int)(Math.random() * 100000000));
		sessionStartTime = System.currentTimeMillis();
		tagId = 0;
		sequenceNumber = 0;
	}

	public void setPlatform(String operatingSystem, String manufacturer, String modelId, int screenWidth, int screenHeight, int screenDensity) {
		this.operatingSystem = operatingSystem;
		this.manufacturer = manufacturer;
		this.modelId = modelId;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.screenDensity = screenDensity;
	}

	private void addProcessingTime(){
		//as System.getTimeMillis() is used we need to insert some time between calls to get more realistic results
		long processingTime = (long) Math.random() * 1000;
		try {
			Thread.sleep(processingTime);
		} catch (InterruptedException e) {
			LOGGER.warn("Thread exception: ", e);
		}
	}

	public void addEvent(EventType eventType,
						 String name,
						 int threadId,
						 int currentActionId,
						 int parentActionId,
						 int sequenceNumber0_start,
						 long time0_start,
						 int sequenceNumber1,
						 long time1,
						 int sequenceNumber2,
						 long time2,
						 int sequenceNumber3,
						 long time3,
						 String viewAtStart,
						 String viewAtEnd) {	//only sent if different than viewAtStart
		addEventExtended(eventType, name, threadId, currentActionId, parentActionId, sequenceNumber0_start, time0_start, sequenceNumber1, time1, sequenceNumber2, time2, sequenceNumber3, time3,
				viewAtStart, viewAtEnd,
				null, null, -1, -1, null, null, null, -1, null);
	}

	/**
	 * use -1/null for values that do not apply or where the default values should be used
	 */
	public void addEventExtended(EventType eventType,
								 String name,
								 int threadId,
								 int currentActionId,
								 int parentActionId,
								 int sequenceNumber0_start,
								 long time0_start,
								 int sequenceNumber1,
								 long time1,
								 int sequenceNumber2,
								 long time2,
								 int sequenceNumber3,
								 long time3,
								 String viewAtStart,
								 String viewAtEnd,	//only sent if different than viewAtStart
								 String value,
								 String response,
								 long bytesSent,
								 long bytesReceived,
								 String reason,
								 String errorValue,
								 String errorDescription,
								 int signalNumber,
								 String stacktrace) {
		if (protocolVersion >= CURRENT_PROTOCOL_VERSION) {
			StringBuilder eventBuilder = new StringBuilder();
			eventBuilder.append("&et=");
			eventBuilder.append(eventType.protocolValue());
			try {
				if (name != null){
					eventBuilder.append("&na=");
					eventBuilder.append(URLEncoder.encode(name, ENCODING));
				}
				if (threadId != -1) {
					eventBuilder.append("&it=");
					eventBuilder.append(threadId);
				}
				if (currentActionId != -1) {
					eventBuilder.append("&ca=");
					eventBuilder.append(currentActionId);
				}
				if (parentActionId != -1) {
					eventBuilder.append("&pa=");
					eventBuilder.append(parentActionId);
				}
				if (sequenceNumber0_start != -1) {
					eventBuilder.append("&s0=");
					eventBuilder.append(sequenceNumber0_start);
				}
				if (time0_start != -1) {
					eventBuilder.append("&t0=");
					eventBuilder.append(time0_start);
				}
				if (sequenceNumber1 != -1) {
					eventBuilder.append("&s1=");
					eventBuilder.append(sequenceNumber1);
				}
				if (time1 != -1) {
					eventBuilder.append("&t1=");
					eventBuilder.append(time1);
				}
				if (sequenceNumber2 != -1) {
					eventBuilder.append("&s2=");
					eventBuilder.append(sequenceNumber2);
				}
				if (time2 != -1) {
					eventBuilder.append("&t2=");
					eventBuilder.append(time2);
				}
				if (sequenceNumber3 != -1) {
					eventBuilder.append("&s3=");
					eventBuilder.append(sequenceNumber3);
				}
				if (time3 != -1) {
					eventBuilder.append("&t3=");
					eventBuilder.append(time3);
				}
				if (viewAtStart!= null){
					eventBuilder.append("&vs=");
					eventBuilder.append(URLEncoder.encode(viewAtStart, ENCODING));
				}
				if (viewAtEnd!= null && !viewAtEnd.equals(viewAtStart)){
					eventBuilder.append("&ve=");
					eventBuilder.append(URLEncoder.encode(viewAtEnd, ENCODING));
					//duration is in t2
				}
				//append extended data on attachment
				if (value != null && (eventType == EventType.VALUE_STRING ||
						eventType == EventType.VALUE_DOUBLE ||
						eventType == EventType.VALUE_INT)) {
					eventBuilder.append("&vl=");
					eventBuilder.append(value);
				}
				if (response != null && eventType == EventType.WEBREQUEST) {
					eventBuilder.append("&rc=");
					eventBuilder.append(response);
				}
				if (bytesSent != 1 && eventType == EventType.WEBREQUEST) {
					eventBuilder.append("&bs=");
					eventBuilder.append(bytesSent);
				}
				if (bytesReceived != -1 && eventType == EventType.WEBREQUEST) {
					eventBuilder.append("&br=");
					eventBuilder.append(bytesReceived);
				}
				if (reason != null && (eventType == EventType.ERROR_CODE ||
						eventType == EventType.EXCEPTION ||
						eventType == EventType.NS_ERROR ||
						eventType == EventType.CRASH)) {
					eventBuilder.append("&rs=");
					eventBuilder.append(URLEncoder.encode(reason, ENCODING));
				}
				if (errorValue != null && (eventType == EventType.ERROR_CODE ||
						eventType == EventType.EXCEPTION ||
						eventType == EventType.NS_ERROR)) {
					eventBuilder.append("&ev=");
					eventBuilder.append(errorValue);
				}
				if (errorDescription != null && eventType == EventType.NS_ERROR) {
					eventBuilder.append("&ed=");
					eventBuilder.append(errorDescription);
				}
				if (signalNumber != -1 && (eventType == EventType.CRASH)) {
					eventBuilder.append("&sg=");
					eventBuilder.append(signalNumber);
				}
				if (stacktrace != null && (eventType == EventType.ERROR_CODE ||
						eventType == EventType.EXCEPTION ||
						eventType == EventType.NS_ERROR ||
						eventType == EventType.CRASH)) {
					eventBuilder.append("&st=");
					eventBuilder.append(URLEncoder.encode(stacktrace, ENCODING));
				}
			} catch (UnsupportedEncodingException uee) {
				LOGGER.warn("Error while creating mobile event: ", uee);
			}
			if (eventType == EventType.ACTION_AUTO ||
					eventType == EventType.ACTION_MANUAL ||
					eventType == EventType.APP_START ||
					eventType == EventType.DISPLAY ||
					eventType == EventType.REDISPLAY) {
				eventDataActions.add(eventBuilder.toString());
			} else {
				eventDataEvents.add(eventBuilder.toString());
			}

			switch (eventType) {
			case ACTION_MANUAL:
			case ACTION_AUTO:
			case APP_START:
			case WEBREQUEST:
				lastEndTime = Math.max(lastEndTime, time0_start + time1);
				break;
			case UNKNOWN:
			case VALUE_DOUBLE:
			case VALUE_INT:
			case VALUE_STRING:
			case NAMED_EVENT:
			case SESSION_END:
			case SESSION_START:
			case CRASH:
			case ERROR_CODE:
			case EXCEPTION:
			case NS_ERROR:
			case IDENTIFY_USER:
				lastEndTime = Math.max(lastEndTime, time0_start);
				break;
			case DISPLAY:
			case REDISPLAY:
				lastEndTime = Math.max(lastEndTime, time0_start + time3);
				break;
			default:
				LOGGER.error("unhandled event type: " + eventType);
				break;
			}

			addProcessingTime();
		} else {
			LOGGER.error("unsupported protocol version " +protocolVersion);
		}
	}

	public void appStart(String firstViewName, int threadId, int currentActionId, int parentActionId, int sequenceNumber0, long startTime, int sequenceNumber1, long endTime) {
		addEvent(EventType.APP_START, firstViewName, threadId, currentActionId, parentActionId, sequenceNumber0, startTime, sequenceNumber1, endTime, OMITTED, OMITTED, OMITTED, OMITTED, null, null);
	}

	public void identifyUser(String userId, int parentActionId, long startTime) {
		addEvent(EventType.IDENTIFY_USER, userId, parentActionId, nextTagId(), parentActionId, nextSequenceNumber(), startTime, OMITTED, OMITTED, OMITTED, OMITTED, OMITTED, OMITTED, null, null);
	}

	public void display(String viewName, int threadId, int currentActionId, int parentActionId, int sequenceNumber0, long startTime, int sequenceNumber1, long time1, int sequenceNumber2, long time2, int sequenceNumber3, long time3) {
		addEvent(EventType.DISPLAY, viewName, threadId, currentActionId, parentActionId, sequenceNumber0, startTime, sequenceNumber1, time1, sequenceNumber2, time2, sequenceNumber3, time3, null, null);
	}

	public void redisplay(String viewName, int threadId, int currentActionId, int parentActionId, int sequenceNumber0, long startTime, int sequenceNumber1, long time1, int sequenceNumber2, long time2, int sequenceNumber3, long time3) {
		addEvent(EventType.REDISPLAY, viewName, threadId, currentActionId, parentActionId, sequenceNumber0, startTime, sequenceNumber1, time1, sequenceNumber2, time2, sequenceNumber3, time3, null, null);
	}
	
	public void namedEvent(String name, int threadId, int parentActionId, int sequenceNumber0, long startTime) {
		addEvent(EventType.NAMED_EVENT, name, threadId, OMITTED, parentActionId, sequenceNumber0, startTime, 
				 OMITTED, OMITTED, OMITTED, OMITTED, OMITTED, OMITTED, null, null);
	}
	
	public void manualAction(String name, int threadId, int currentActionId, int parentActionId, int sequenceNumber0, long startTime, int sequenceNumber1, long duration) {
		addEvent(EventType.ACTION_MANUAL, name, threadId, currentActionId, parentActionId, sequenceNumber0, startTime, sequenceNumber1, duration, OMITTED, OMITTED, OMITTED, OMITTED, null, null);
	}

	public void autoAction(String name, int threadId, int currentActionId, int parentActionId, int sequenceNumber0, long startTime, int sequenceNumber1, long duration) {
		addEvent(EventType.ACTION_AUTO, name, threadId, currentActionId, parentActionId, sequenceNumber0, startTime, sequenceNumber1, duration, OMITTED, OMITTED, OMITTED, OMITTED, null, null);
	}

	public void touchOn(String name, int threadId, int currentActionId, int parentActionId, int sequenceNumberStart, long startTime, int sequenceNumberEnd, long duration) {
		addEvent(EventType.ACTION_AUTO, "Touch on " +name, threadId, currentActionId, parentActionId, sequenceNumberStart, startTime, sequenceNumberEnd, duration, OMITTED, OMITTED, OMITTED, OMITTED, null, null);
	}

	public void webRequest(String url, int threadId, int parentActionId, int sequenceNumber0, long startTime, int sequenceNumber1, long duration, String response, long bytesSent, long bytesReceived) {
		addEventExtended(EventType.WEBREQUEST, url, threadId, OMITTED, parentActionId, sequenceNumber0, startTime, sequenceNumber1, duration, OMITTED, OMITTED, OMITTED, OMITTED,
						null, null, null, response, bytesSent, bytesReceived, null, null, null, OMITTED, null);
	}
	
	public void webRequestOnAction(String url, int threadId, int parentActionId, int sequenceNumber0, long startTime, int sequenceNumber1, long duration, String response, long bytesSent, long bytesReceived){
		int currentActionId = nextTagId();
		addEvent(EventType.ACTION_AUTO, url, threadId, currentActionId, parentActionId, sequenceNumber0, startTime, sequenceNumber1, duration, OMITTED, OMITTED, OMITTED, OMITTED,
				null, null);
		webRequest(url, threadId, currentActionId, sequenceNumber0, startTime, sequenceNumber1, duration, response, bytesSent, bytesReceived);
	}

	public void crash(String name, int threadId, int parentActionId, int sequenceNumber0, long startTime, String reason, int signalNumber, String crashReport) {
		addEventExtended(EventType.CRASH, name, threadId, OMITTED, parentActionId, sequenceNumber0, startTime, OMITTED, OMITTED, OMITTED, OMITTED, OMITTED,
				OMITTED, null, null, null, null, OMITTED, OMITTED, reason, null, reason, signalNumber, crashReport);
	}
	public void error(int threadId, int parentActionId, int sequenceNumber, long errorTime, String name, String errorReason, String errorValue, String errorStacktrace) {
		addEventExtended(EventType.EXCEPTION, name, threadId, OMITTED, parentActionId, sequenceNumber, errorTime, OMITTED, OMITTED, OMITTED, OMITTED, OMITTED, OMITTED,
				null, null, null, null, OMITTED, OMITTED, errorReason, errorValue, null, OMITTED, errorStacktrace);
	}

	public void touchOnWithError(String name, int threadId, int parentActionId, int sequenceNumberStart, long startTime, int sequenceNumberEnd, long duration,
			int sequenceNumberError, long errorTime, String errorName, String errorReason, String errorValue, String errorStacktrace) {
		int currentActionId = nextTagId();
		addEvent(EventType.ACTION_AUTO, "Touch on " +name, threadId, currentActionId, parentActionId, sequenceNumberStart, startTime, sequenceNumberEnd, duration, OMITTED, OMITTED, OMITTED, OMITTED, null, null);
		error(threadId, currentActionId, sequenceNumberError, errorTime, errorName, errorReason, errorValue, errorStacktrace);
	}

	public void value(EventType et, String name, int threadId, int parentActionId, int sequenceNumber0, long startTime, Object value) {		
		addEventExtended(et, name, threadId, OMITTED, parentActionId, sequenceNumber0, startTime,
						 OMITTED, OMITTED, OMITTED, OMITTED, OMITTED, OMITTED, null, null,
						 value.toString(), null, OMITTED, OMITTED, null, null, null, OMITTED, null);
	}
	
	public void error(EventType et, String name, int threadId, int parentActionId, int sequenceNumber0, long startTime, String reason, String errorValue, String stackTrace, String errorDescription) {
		addEventExtended(et, name, threadId, OMITTED, parentActionId, sequenceNumber0, startTime, OMITTED, OMITTED, OMITTED,
							OMITTED, OMITTED, OMITTED, null, null, null, null, OMITTED, OMITTED, reason, errorValue,
							errorDescription, OMITTED, stackTrace);
	}
	
	public String getBeaconQuery() {
		return query  + "&va=" + agentVersion + "&tt=" + getAgentTechnologyType();
	}

	public void clearEvents() {
		eventDataEvents.clear();
		eventDataActions.clear();
	}

	public String getBeaconPostBody() {

		StringBuilder bodyBuilder = new StringBuilder();
		try {
			//basic data
			bodyBuilder.append("vv=");
			bodyBuilder.append(protocolVersion);
			bodyBuilder.append("&va=");
			bodyBuilder.append(URLEncoder.encode(agentVersion, ENCODING));
			bodyBuilder.append("&ap=");
			bodyBuilder.append(URLEncoder.encode(appId, ENCODING));
			bodyBuilder.append("&an=");
			bodyBuilder.append(URLEncoder.encode(appName, ENCODING));
			bodyBuilder.append("&ai=");
			bodyBuilder.append(URLEncoder.encode(appPackage, ENCODING));
			bodyBuilder.append("&vn=");
			bodyBuilder.append(URLEncoder.encode(appVersionName, ENCODING));
			bodyBuilder.append("&vb=");
			bodyBuilder.append(URLEncoder.encode(appVersionBuild, ENCODING));
			bodyBuilder.append("&tv=");
			bodyBuilder.append(sessionStartTime + drift);
            bodyBuilder.append("&tx=");    //assume current time as request should be sent immediately
			bodyBuilder.append(System.currentTimeMillis());
			bodyBuilder.append("&vi=");
			bodyBuilder.append(URLEncoder.encode(visitorId, ENCODING));
			bodyBuilder.append("&sn=");
			bodyBuilder.append(sessionNumber);
			bodyBuilder.append("&rm=");
			bodyBuilder.append(memory);
			bodyBuilder.append("&cp=");
			bodyBuilder.append(URLEncoder.encode(cpu, ENCODING));
			bodyBuilder.append("&os=");
			bodyBuilder.append(URLEncoder.encode(operatingSystem, ENCODING));
			bodyBuilder.append("&mf=");
			bodyBuilder.append(URLEncoder.encode(manufacturer, ENCODING));
			bodyBuilder.append("&md=");
			bodyBuilder.append(URLEncoder.encode(modelId, ENCODING));
			if(softwareState.protocolValue() != '-') {
				bodyBuilder.append("&rj=");
				bodyBuilder.append(softwareState.protocolValue());
			}
			bodyBuilder.append("&ul=");
			bodyBuilder.append(URLEncoder.encode(userLanguage, ENCODING));
			bodyBuilder.append("&sw=");
			bodyBuilder.append(screenWidth);
			bodyBuilder.append("&sh=");
			bodyBuilder.append(screenHeight);
			bodyBuilder.append("&sd=");
			bodyBuilder.append(screenDensity);
			if(orientation.protocolValue() != '-') {
				bodyBuilder.append("&so=");
				bodyBuilder.append(orientation.protocolValue());
			}
			bodyBuilder.append("&bl=");
			bodyBuilder.append(batteryLevel);
			bodyBuilder.append("&fm=");
			bodyBuilder.append(freeMemory);
			bodyBuilder.append("&cr=");
			bodyBuilder.append(URLEncoder.encode(carrier, ENCODING));
			if(connectionType.protocolValue() != '-') {
				bodyBuilder.append("&ct=");
				bodyBuilder.append(connectionType.protocolValue());
			}
			bodyBuilder.append("&np=");
			bodyBuilder.append(URLEncoder.encode(networkTechnology, ENCODING));
			bodyBuilder.append("&lx=");
			bodyBuilder.append(URLEncoder.encode(location.toString(), ENCODING));
			if(isNewUser) {
				bodyBuilder.append("&nu=1");
			}
            bodyBuilder.append("&tt=");
            bodyBuilder.append(URLEncoder.encode(getAgentTechnologyType(), ENCODING));

			//event data
			for (String data : eventDataEvents) {	//events need to be sent before actions to be linked correctly
				bodyBuilder.append(data);
			}
			for (String data : eventDataActions) {
				bodyBuilder.append(data);
			}
		} catch (UnsupportedEncodingException uee) {
			LOGGER.error("Error while creating POST body for Mobile Beacon: ", uee);
		}
		return bodyBuilder.toString();
	}

	public void randomPlatform() {
		double random = Math.random();
		if (random > 0.90) {
			setPlatform("Android 5.1", "Samsung", "Galaxy S6", 1440, 2560, 560);
		} else if (random > 0.75){
			setPlatform("iOS 10.2.1", "Apple", "iPhone9,1", 750, 1334, 2);
		} else if (random > 0.5){
			setPlatform("iOS 9.3.5", "Apple", "iPad3,4", 1536, 2048, 2);
		} else if (random > 0.375){
			setPlatform("Android 7.1", "HTC", "Pixel", 1080, 1920, 480);
		} else if (random > 0.25){
			setPlatform("iOS 8.4", "Apple", "iPhone7,2", 750, 1334, 1);
		} else if (random > 0.125){
			setPlatform("Android 5.0.2", "LGE", "G Pad 7.0", 800, 1280, 213);
		} else {
			setPlatform("Android 4.4.2", "HTC", "One X", 720, 1280, 320);
		}
	}

	public void randomClientIp() {
		String randomIp;
		//IP adresses from GeolocationTest, Dynatrace GeoInfoTest
		double random = Math.random();
		if (random > 0.9) {
			randomIp =  "5.10.191.3";	//Germany
		} else if (random > 0.8){
			randomIp = "90.146.74.58";	//Austria
		} else if (random > 0.7){
			randomIp = "217.172.84.118";	//Faroer Islands - CITY
		} else if (random > 0.6){
			randomIp = "94.100.48.1";	//Kosovo
		} else if (random > 0.5){
			randomIp = "24.24.24.24";	//US
		} else if (random > 0.4){
			randomIp = "80.24.24.80";	//Spain
		} else if (random > 0.3){
			randomIp = "200.24.24.40";	//Colombia
		} else if (random > 0.2){
			randomIp = "68.24.24.46";	//Norway
		} else if (random > 0.1){
			double anotherRandom = Math.random();
			if (anotherRandom > 0.75) {
				randomIp = "213.52.50.8";	// no idea
			} else if (anotherRandom > 0.5){
				randomIp = "13.2.23.234";	// no idea
			} else if (anotherRandom > 0.25){
				randomIp = "64.4.4.4";	// no idea
			}else {
				randomIp = "90.146.74.58";	// no idea
			}
		} else {
			randomIp = "52.5.109.165";	//ruxit.com
		}
		remoteIP = randomIp;
	}

	public void randomAppVersion() {
		int major = 1;
		double random = Math.random();
		if (random > 0.5) {
			major = 7;	//most
		} else if (random > 0.4) {
			major = 6;	//often
		} else if (random > 0.25) {
			major = 5;	//medium
		} else if (random > 0.23) {
			major = 4;	//quite rare
		} else if (random > 0.08) {
			major = 3;	//medium
		} else if (random > 0.01) {
			major = 2;	//medium rare
		} else {
			major = 1;	//really rare
		}
		int minor = major +2;
		int revision = major +1;
		int build = major * 1000 + minor * 100 + revision * 10 +3;

		appVersionName = major +"." +minor +"." +revision;

		if (operatingSystem.startsWith("iOS")) {
			appVersionBuild = major +"." +minor +"." +build;
		} else {
			appVersionBuild = Integer.toString(build);
		}
	}

	/**
	 * @param max
	 * @return random value between max/2 and max
	 */
	public int randomize(int max) {
		return (int) (max / 2 * (1 + Math.random()));
	}

	/**
	 * @param min
	 * @param max
	 * @return random value between min and max
	 */
	public int randomize(int min, int max) {
		return (int) (min + (max - min) * Math.random());
	}
}
