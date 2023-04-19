/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: MobileDevice.java
 * @date: 20.01.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload.mobile;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Random;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import ch.qos.logback.classic.Logger;

import com.dynatrace.diagnostics.uemload.ActionExecutor;
import com.dynatrace.diagnostics.uemload.Bandwidth;
import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.NavigationTiming;
import com.dynatrace.diagnostics.uemload.VisitorId;
import com.dynatrace.diagnostics.uemload.http.base.HostAvailability;
import com.dynatrace.diagnostics.uemload.http.base.HttpRequest.Type;
import com.dynatrace.diagnostics.uemload.http.base.HttpResponse;
import com.dynatrace.diagnostics.uemload.http.callback.ErrorHandlingHttpResponseCallback;
import com.dynatrace.diagnostics.uemload.http.callback.HttpResponseCallback;
import com.dynatrace.diagnostics.uemload.http.exception.PageNotAvailableException;
import com.dynatrace.diagnostics.uemload.mobile.MobileBeaconGenerator.EventType;
import com.dynatrace.diagnostics.uemload.mobile.MobileBeaconGenerator.Orientation;
import com.dynatrace.diagnostics.uemload.mobile.MobileBeaconGenerator.SoftwareState;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.MobileSession;
import com.dynatrace.diagnostics.uemload.utils.UemLoadHttpUtils;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.DtHeader;
import com.dynatrace.easytravel.constants.BaseConstants.Uem;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.util.DynatraceUrlUtils;
import com.dynatrace.easytravel.util.ResourceFileReader;
import com.dynatrace.easytravel.util.TextUtils;
import com.dynatrace.oneagent.sdk.OneAgentSDKFactory;
import com.dynatrace.oneagent.sdk.api.IncomingWebRequestTracer;
import com.dynatrace.oneagent.sdk.api.OneAgentSDK;
import com.dynatrace.oneagent.sdk.api.infos.WebApplicationInfo;

/**
 * class to uem page actions usually performed by applications run on
 * mobile devices like Android cell phones, tablets, iPhones and iPads.
 *
 * @author peter.lang
 */
@SuppressWarnings("nls")
public class MobileDevice extends ActionExecutor {
	private static final int MAX_RANDOM_ACTIONDELAY = 50;

	private static final Logger LOG = LoggerFactory.make();
	
	/** To match a line, e.g. "Version:         2.0.24 (2.0.024)" in order to replace the version name and build */
	private static final String VERSION_LINE_PATTERN = "^" + // start of line
			"(\\bVersion\\b:\\s*)" + // starting with "Version:" followed by 0..n spaces
			"([^\n]*)"; // any char except newline
	private static final Pattern APPLE_CRASH_VERSION_PATTERN = Pattern.compile(VERSION_LINE_PATTERN);

	private static final Random rnd = new Random(System.currentTimeMillis());
	private static final SecureRandom secureRnd = new SecureRandom();
	
	private final MobileDeviceType deviceType;
	private final String connectionType;
	private final MobileBeaconGenerator bg;

	private Stack<MobileAction> actionStack = new Stack<MobileAction>();

	private boolean isAgentActive = true;

	private static final int protocolVersion = 3;

	private static boolean IS_RUXIT;
	private static String ruxitBeaconUrl;
	private static boolean ruxitConfigOK = false;
	private static int ruxitInvalidResponseCount = 0;
	private OneAgentSDK oneAgent = OneAgentSDKFactory.createInstance();
	private final static String[] calledServiceNames = {
			"EasyTravelWebServer-ET-WS-1",
			"EasyTravelWebServer-ET-WS-2",
			"EasyTravelWebServer-ET-WS-3",
			"EasyTravelWebServer-ET-WS-4",
			"EasyTravelWebServer-ET-WS-5",
			"EasyTravelWebServer-ET-WS-6",
			"EasyTravelWebServer-ET-WS-7",
			"EasyTravelWebServer-ET-WS-8",
			"EasyTravelWebServer-ET-WS-9"
	};
	private final static String[] httpErrorCodes = {"404", "500"};

	static {
		initRuxitBeaconUrl();
	}
	
	static void initRuxitBeaconUrl() {
		EasyTravelConfig config = EasyTravelConfig.read();
		IS_RUXIT = DtVersionDetector.isAPM();
		ruxitBeaconUrl = null;
		
		if (IS_RUXIT) {
			ruxitConfigOK = config.apmTenant != null && !config.apmTenant.isEmpty();
			if (!ruxitConfigOK) {
				LOG.error("'apmTenant' missing from config - will not send mobile events");
			} else {
				ruxitBeaconUrl = DynatraceUrlUtils.getDynatraceBeaconUrl();
				LOG.info("Setting mobile agent beacon url to " + ruxitBeaconUrl);
			}
		} 
	}
			
	//method needed for tests only
	static String getRuxitBeaconUrl() {
		return ruxitBeaconUrl;
	}

	/**
	 *
	 * @param deviceType
	 * @param location
	 * @param latency
	 * @param bandwidth
	 * @author peter.lang
	 */
	public MobileDevice(MobileDeviceType deviceType, Location location, int latency, Bandwidth bandwidth, BrowserType browserType, String connectionType, VisitorId visitorId) {
		super(location, latency, bandwidth, browserType, deviceType.getUserAgent(), visitorId);
		this.deviceType = deviceType;
		this.connectionType = connectionType;
		
		final String appId = IS_RUXIT ? MobileBeaconGenerator.EASY_TRAVEL_RUXIT_APPLICATION_ID : MobileBeaconGenerator.EASY_TRAVEL_APPLICATION_NAME;
		bg = new MobileBeaconGenerator(appId, protocolVersion);
		
		configureMobileBeaconGeneratorData();
	}
	
	/**
	 * add some random stuff for better looking demo data
	 * random value generated by beaconGenerator can safely be used for more simulated data diversity in Dynatrace (except for stuff like protocol version)
	 */
	private void configureMobileBeaconGeneratorData() {
		bg.newVisit();

		//apply DeviceType properties to BeaconGenerator her to get correct random values (e.g. matching to OS)
		bg.manufacturer = deviceType.getManufacturer();
		bg.operatingSystem = deviceType.getOs();
		bg.screenWidth = deviceType.getScreenWidth();
		bg.screenHeight = deviceType.getScreenHeight();
		bg.cpu = deviceType.getCpu();
		bg.modelId = deviceType.getModelId();

		//bg.randomPlatform();	//TODO PH: currently do not use, as it overwrites deviceType info and might lead to mixed iOS/Android configuration
								//TODO PH: maybe after desupporting the old Dynatrace agent protocol we can switch it completely. But for now it needs to be kept in sync with MobileDeviceType because of patterns like TabletCrashes etc.
		bg.randomAppVersion();
		// FIXME PH: need different users vs. sessions count
		
		bg.userLanguage = "en_US";
		bg.agentVersion = "7.2.1234";
		// bg.appVersionName - set by random
		// bg.appVersionBuild - set by random
		bg.carrier = "T-Mobile";
		if (deviceType.getOs().startsWith("Android")) {
		  bg.carrier = "AT&T";
		}
		if (deviceType.getOs().endsWith(".3")) {
		  bg.carrier = "Orange";
		}
		
		bg.networkTechnology = connectionType.equals(Uem.CONNECTION_TYPE_WIFI) ? "802.11x" : "EDGE";

		if (deviceType.getOs().startsWith("Android")) {
		  bg.screenDensity = 320;
		} else {
		  bg.screenDensity = 2;
		}
		bg.orientation = Math.random() >= 0.5d ? Orientation.PORTRAIT : Orientation.LANDSCAPE;
		bg.freeMemory = (int) (Math.random() * 100);
		bg.batteryLevel = (byte) (Math.random() * 100);
		bg.memory = 512 + (int) (Math.random() * 512);
	}
	
	private void configureMobileBeaconGeneratorSessionData(MobileSession session) {
		if (session.isRooted()) {
			if (deviceType.getOs().startsWith("Android")) {
				bg.softwareState = SoftwareState.ROOTED;
			} else {
				bg.softwareState = SoftwareState.JAILBROKEN;
			}
		} else {
			bg.softwareState = SoftwareState.GENUINE;
		}

		bg.sessionStartTime = session.getStartTime();
		bg.visitorId = session.getVisitId(); // deviceType.getDeviceId() ??
		bg.sessionNumber = Integer.parseInt(session.getSessionId());
		bg.appName = session.getApplicationName();
	}

	public MobileDeviceType getDeviceType() {
		return deviceType;
	}

	private static class MobileAction {
		private final int tagId;
		private final int parentTagId;
		private final String name;
		private final long startTime;
		private final int sequenceNumber0;

		public MobileAction(int tagId, String name, long startTime, int parentTagId, int sequenceNumber0) {
			this.tagId = tagId;
			this.parentTagId = parentTagId;
			this.name = name;
			this.startTime = startTime;
			this.sequenceNumber0 = sequenceNumber0;
		}
	}

	/**
	 * Start a new interval
	 *
	 * @param name
	 * @author clemens.fuchs
	 */
	public void startAction(MobileSession session, String name) {
		configureMobileBeaconGeneratorSessionData(session);
		
		final int tagId = bg.nextTagId();
		final int parentTagId = getTagIdOfLatestAction();
		final long startTime = bg.getTimeSinceSessionStart();
		final int sequenceNumber0 = bg.nextSequenceNumber();
				
		actionStack.push(new MobileAction(tagId, name, startTime, parentTagId, sequenceNumber0));

		delayRandomDeviceLatency();
	}
	
	/**
	 * @return The tagId of the latest stacked action or 0 if none is stacked.
	 */
	private int getTagIdOfLatestAction() {
		int tagId = 0;
		
		if(!actionStack.isEmpty()) {
			tagId = actionStack.peek().tagId;
		}
		
		return tagId;
	}

	/**
	 * Ends currently active interval
	 *
	 * @author clemens.fuchs
	 */
	public void leaveAction(MobileSession session) {
		final MobileAction action = actionStack.pop();
		final long duration = bg.getTimeSinceSessionStart() - action.startTime;
		final int sequenceNumber1 = bg.nextSequenceNumber();
				
		bg.manualAction(action.name, 0, action.tagId, action.parentTagId, action.sequenceNumber0, action.startTime, sequenceNumber1, duration);

		if(actionStack.isEmpty()) {
			sendBeaconSignal(session.getHost(), bg);
		}
	}

	/**
	 * Ends all active intervals.
	 *
	 * @param end
	 * @param reverseOrder used for testing reverse
	 * @author clemens.fuchs
	 */
	public void leaveAllActions(MobileSession session) {
		while (!actionStack.isEmpty()) {
			leaveAction(session);
		}
	}

	public void startAutoUserAction(MobileSession session, String name) {
		configureMobileBeaconGeneratorSessionData(session);
		
		final int tagId = bg.nextTagId();
		final int parentTagId = getTagIdOfLatestAction();
		final long startTime = bg.getTimeSinceSessionStart();
		final int sequenceNumber0 = bg.nextSequenceNumber();
		
		actionStack.push(new MobileAction(tagId, name, startTime, parentTagId, sequenceNumber0));
		
		simulateProcessingOnDevice(300, 600);
	}

	public void leaveAutoUserAction(MobileSession session) {
		leaveAction(session);
	}

	public void loadingAutoUserAction(MobileSession session, String viewName) {
		if (isIOS()) {
			startAutoUserAction(session, "Loading easyTravel");
		}
		appStartLifecycleAction(session);
		displayLifecycleAction(session, viewName);
		if (isIOS()) {
			leaveAutoUserAction(session);
		}
	}

	public void appStartLifecycleAction(MobileSession session) {
		configureMobileBeaconGeneratorSessionData(session);
		
		final int tagId = bg.nextTagId();
		final int parentTagId = getTagIdOfLatestAction();
		final long startTime = bg.getTimeSinceSessionStart();
		final int sequenceNumber0 = bg.nextSequenceNumber();
		final int sequenceNumber1 = bg.nextSequenceNumber();	
		final String viewName = isIOS() ? "DTNavigationController" : "easyTravel"; // check for OS
		final int delay = simulateProcessingOnDevice(15, 30);	// Added a delay in order to ensure that this action comes before the following

		bg.isNewUser = Math.random() > .6; 	//only send once during AppStart
		bg.appStart(viewName, 0, tagId, parentTagId, sequenceNumber0, startTime, sequenceNumber1, delay);
		
		if(IS_RUXIT) {
			bg.identifyUser(session.getUser().getName(), 0 /*should always be root event*/, startTime);
		}

		if(actionStack.isEmpty()) {
			sendBeaconSignal(session.getHost(), bg);
		}
	}

	public void displayLifecycleAction(MobileSession session, String viewName) {
		configureMobileBeaconGeneratorSessionData(session);
		
		final int tagId = bg.nextTagId();
		final int parentTagId = getTagIdOfLatestAction();
		final long eventTime = bg.getTimeSinceSessionStart();		
		final int sequenceNumber0 = bg.nextSequenceNumber();
		final long startTime = eventTime - 10;
		final int sequenceNumber1 = bg.nextSequenceNumber();
		final long time1 = bg.randomize(5, 10);
		final int sequenceNumber2 = bg.nextSequenceNumber();
		final long time2 = bg.randomize(15, 20);
		final int sequenceNumber3 = bg.nextSequenceNumber();
		final long endTime = bg.randomize(25, 30);
		
		bg.display(viewName, 0, tagId, parentTagId, sequenceNumber0, startTime, sequenceNumber1, time1, sequenceNumber2, time2, sequenceNumber3, endTime);
		
		if(actionStack.isEmpty()) {
			sendBeaconSignal(session.getHost(), bg);
		}
	}

	public void redisplayLifecycleAction(MobileSession session, String viewName) {
		configureMobileBeaconGeneratorSessionData(session);
		
		final int tagId = bg.nextTagId();
		final int parentTagId = getTagIdOfLatestAction();
		final long eventTime = bg.getTimeSinceSessionStart();		
		final int sequenceNumber0 = bg.nextSequenceNumber();
		final long startTime = eventTime - 10;
		final int sequenceNumber1 = bg.nextSequenceNumber();
		final long time1 = bg.randomize(5, 10);
		final int sequenceNumber2 = bg.nextSequenceNumber();
		final long time2 = bg.randomize(15, 20);
		final int sequenceNumber3 = bg.nextSequenceNumber();
		final long endTime = bg.randomize(25, 30);
		
		bg.redisplay(viewName, 0, tagId, parentTagId, sequenceNumber0, startTime, sequenceNumber1, time1, sequenceNumber2, time2, sequenceNumber3, endTime);
		
		if(actionStack.isEmpty()) {
			sendBeaconSignal(session.getHost(), bg);
		}
	}

	@SuppressWarnings("boxing")
	public void crash(MobileSession session) {
		configureMobileBeaconGeneratorSessionData(session);
		
		final int parentTagId = getTagIdOfLatestAction();
		final int sequenceNumber0 = bg.nextSequenceNumber();
		final long startTime = bg.getTimeSinceSessionStart();
		String name = null;
		String crashReport = null;
		int signalNumber = -1;
		String reason = null;
		final int version = Character.getNumericValue(bg.appVersionName.charAt(0));

		if(bg.operatingSystem.startsWith("iOS")) {
			//iOS Crash
			reason = "iOS Crash Reason";

			if (version > 6) {
				signalNumber = 6;    //SIGABRT
				name = "NSRangeException";
				crashReport = loadAppleCrashReport("easyTravel_iOS.crash", bg.appVersionName, bg.appVersionBuild);
			} else if (version > 3) {
				signalNumber = 5;    //SIGTRAP
				name = "NSDemoException";
				crashReport  = loadAppleCrashReport("easyTravel_iOS2.crash", bg.appVersionName, bg.appVersionBuild);
			} else {
				signalNumber = 6;    //SIGABRT
				name = "NSRangeException";
				crashReport = loadAppleCrashReport("easyTravel_iOS3.crash", bg.appVersionName, bg.appVersionBuild);
			}
		} else if (bg.operatingSystem.startsWith("Android")) {
			//Android Crash
			reason = "Android Crash Reason";
			if (version > 6) {
				name = "java.lang.IndexOutOfBoundsException";
				crashReport = loadCrashReport("easyTravel_Android.txt");
			} else if (version > 3) {
				name = "java.lang.RuntimeException";
				crashReport = loadCrashReport("easyTravel_Android2.txt");
			} else {
				name = "java.lang.NullPointerException";
				crashReport = loadCrashReport("easyTravel_Android3.txt");
			}
		}
				
		bg.crash(name, 0, parentTagId, sequenceNumber0, startTime, reason, signalNumber, crashReport);
		
		if(actionStack.isEmpty()) {
			sendBeaconSignal(session.getHost(), bg);
		}
	}

	public void error(MobileSession session) {
		configureMobileBeaconGeneratorSessionData(session);

		final int parentTagId = getTagIdOfLatestAction();
		final int sequenceNumber0 = bg.nextSequenceNumber();
		final long startTime = bg.getTimeSinceSessionStart();
				
		Error[] errors = Error.values();
		int rand = secureRnd.nextInt(errors.length);
		Error error = errors[rand];

		final int sequenceNumberError = bg.nextSequenceNumber();
		final int sequenceNumberEnd = bg.nextSequenceNumber();
		bg.touchOnWithError(" Search (with error)", 0, parentTagId, sequenceNumber0, startTime,
				sequenceNumberEnd, 100, sequenceNumberError, startTime, error.getName(), error.getReason(), error.getValue(), error.getStacktrace());
		bg.error(0, parentTagId, sequenceNumber0, startTime, error.getName(), error.getReason(), error.getValue(), error.getStacktrace());

		if(actionStack.isEmpty()) {
			sendBeaconSignal(session.getHost(), bg);
		}
	}

	/**
	 * To load an iOS crash report from file resource. This method also replaces the line containing the version name and build number.
	 *
	 * @param fileName where to load the crashReport from
	 * @param appVersionName the version name to be replaced
	 * @param appVersionBuild the build number to be replaced
	 * @return the crash report or an empty string if not found
	 */
	private String loadAppleCrashReport(String fileName, String appVersionName, String appVersionBuild) {
		final String crashReport = loadCrashReport(fileName);
		final String replacement = "Version:         " + appVersionName + " (" + appVersionBuild + ")";
		Matcher m = APPLE_CRASH_VERSION_PATTERN.matcher(crashReport);
		return m.replaceFirst(replacement);
	}

	/**
	 * To load the file resource to a {@code String}
	 * @param filename which resource to load
	 * @return the resource content or an empty string if not found
	 */
	public static String loadCrashReport(String filename) {
		try {
			InputStream is = ResourceFileReader.getInputStream(filename);
			if (is == null) {
				LOG.warn("Could not find requested file: " + filename + " in resources.");
				return "";
			}
			try {
				byte[] buffer = new byte[10000];
				StringBuilder sb = new StringBuilder();
				int length = 0;
				while ((length = is.read(buffer)) != -1) {
					sb.append(new String(buffer, 0, length));
				}
				return sb.toString();
			} finally {
				is.close();
			}
		} catch (Exception e) {
			LOG.warn("Failed to load crash report '" + filename + "' from classpath-folder 'resources'", e);
			return "";
		}
	}

	/**
	 * Triggers named event.
	 *
	 * @param name
	 * @author clemens.fuchs
	 */
	public void reportEvent(MobileSession session, String name) {
		configureMobileBeaconGeneratorSessionData(session);
		
		final int parentTagId = getTagIdOfLatestAction();
		final int sequenceNumber0 = bg.nextSequenceNumber();
		final long startTime = bg.getTimeSinceSessionStart();
		
		bg.namedEvent(name, 0, parentTagId, sequenceNumber0, startTime);
		
		if(actionStack.isEmpty()) {
			sendBeaconSignal(session.getHost(), bg);
		}
	}

	@SuppressWarnings("boxing")
	public void reportValue(MobileSession session, String name, int value) {
		configureMobileBeaconGeneratorSessionData(session);
		
		final int parentTagId = getTagIdOfLatestAction();
		final int sequenceNumber0 = bg.nextSequenceNumber();
		final long startTime = bg.getTimeSinceSessionStart();
		
		bg.value(EventType.VALUE_INT, name, 0, parentTagId, sequenceNumber0, startTime, value);
		
		if(actionStack.isEmpty()) {
			sendBeaconSignal(session.getHost(), bg);
		}
	}

	public void reportValue(MobileSession session, String name, double value) {
		configureMobileBeaconGeneratorSessionData(session);
		
		final int parentTagId = getTagIdOfLatestAction();
		final int sequenceNumber0 = bg.nextSequenceNumber();
		final long startTime = bg.getTimeSinceSessionStart();
		
		bg.value(EventType.VALUE_DOUBLE, name, 0, parentTagId, sequenceNumber0, startTime, value);
		
		if(actionStack.isEmpty()) {
			sendBeaconSignal(session.getHost(), bg);
		}
	}

	public void reportValue(MobileSession session, String name, String value) {
		configureMobileBeaconGeneratorSessionData(session);
		
		final int parentTagId = getTagIdOfLatestAction();
		final int sequenceNumber0 = bg.nextSequenceNumber();
		final long startTime = bg.getTimeSinceSessionStart();
		
		bg.value(EventType.VALUE_STRING, name, 0, parentTagId, sequenceNumber0, startTime, value);
		
		if(actionStack.isEmpty()) {
			sendBeaconSignal(session.getHost(), bg);
		}
	}
	
	public void reportErrorCode(MobileSession session, String errorName, int errorValue) {
		configureMobileBeaconGeneratorSessionData(session);
		
		final int parentTagId = getTagIdOfLatestAction();
		final int sequenceNumber0 = bg.nextSequenceNumber();
		final long startTime = bg.getTimeSinceSessionStart();
		
		bg.error(EventType.ERROR_CODE, errorName, 0, parentTagId, sequenceNumber0, startTime, null, String.valueOf(errorValue), null, null);
		
		if(actionStack.isEmpty()) {
			sendBeaconSignal(session.getHost(), bg);
		}
	}
	
	public void reportException(MobileSession session, String errorName, String reason, String errorValue, String stackTrace) {
		configureMobileBeaconGeneratorSessionData(session);
		
		final int parentTagId = getTagIdOfLatestAction();
		final int sequenceNumber0 = bg.nextSequenceNumber();
		final long startTime = bg.getTimeSinceSessionStart();
		
		bg.error(EventType.EXCEPTION, errorName, 0, parentTagId, sequenceNumber0, startTime, reason, errorValue, stackTrace, null);
		
		if(actionStack.isEmpty()) {
			sendBeaconSignal(session.getHost(), bg);
		}
	}

	public void reportNSError(MobileSession session, String errorName, String reason, String errorValue, String stackTrace, String errorDescription) {
		configureMobileBeaconGeneratorSessionData(session);
		
		final int parentTagId = getTagIdOfLatestAction();
		final int sequenceNumber0 = bg.nextSequenceNumber();
		final long startTime = bg.getTimeSinceSessionStart();
		
		bg.error(EventType.NS_ERROR, errorName, 0, parentTagId, sequenceNumber0, startTime, reason, errorValue, stackTrace, errorDescription);
		
		if(actionStack.isEmpty()) {
			sendBeaconSignal(session.getHost(), bg);
		}
	}

	/**
	 * Performs a web request to server defined via {@link #startApplication(String)}.
	 *
	 * @param path
	 * @param tagId
	 * @param hopCount
	 * @throws IOException
	 * @author clemens.fuchs
	 */
	public void performWebRequest(final MobileSession session, final String path, int hopCount,
			final ErrorHandlingHttpResponseCallback callback) throws IOException {
		final int parentTagId = getTagIdOfLatestAction();
		final int sequenceNumber0 = bg.nextSequenceNumber();
		final int sequenceNumber1 = sequenceNumber0 + 1; //necessary to not break webrequest linking!

		final String appId = IS_RUXIT ? MobileBeaconGenerator.EASY_TRAVEL_RUXIT_APPLICATION_ID : MobileBeaconGenerator.EASY_TRAVEL_APPLICATION_NAME;
		final String header = constructMobileTagHeaderValue(1, session.getVisitId(), session.getSessionId(), appId,
															parentTagId, hopCount, sequenceNumber0);

		Collection<Header> headers = UemLoadHttpUtils.getHeaderSingleton(DtHeader.X_DYNATRACE_MOBILE_ONEAGENT, header);
		try {
			final long startTime = System.currentTimeMillis();
			http.request(path, headers, NavigationTiming.NONE, new HttpResponseCallback() {

				@Override
				public void readDone(HttpResponse response) throws IOException {
					// send the network timing event back
					final long endTime = System.currentTimeMillis();
					final long duration = (long)((endTime - startTime) * 1.1);
					final long t0 = endTime - session.getStartTime();
					final long t1 = bg.randomize((int)(duration));
					String responseValue = response.getStatusCode() > 0 ? String.valueOf(response.getStatusCode())
																				: "could not connect to host";
					final long bytesSent = bg.randomize(111, 333);
					final long bytesReceived = bg.randomize(666, 999);
					
					double errorProbability = Math.random();
					if (errorProbability > 0.97) {
						responseValue = generateHttpError();
					}
					
					bg.webRequest(path, 0, parentTagId, sequenceNumber0, t0, sequenceNumber1, t1, responseValue, bytesSent, bytesReceived);

					final int probabilityWoUserActions = bg.randomize(1,11);
					if(probabilityWoUserActions > 7) {					
						bg.webRequestOnAction(path, 0, parentTagId, sequenceNumber0, t0, sequenceNumber1, t1, responseValue, bytesSent, bytesReceived);
					}
					
					if(actionStack.isEmpty()) {
						sendBeaconSignal(session.getHost(), bg);
					}
					createServiceCall(path, header, Integer.parseInt(responseValue));

					if (callback != null) {
						callback.readDone(response);
					}
				}
			});
		} catch (PageNotAvailableException e) {
			if (callback != null) {
				callback.handleRequestError(e);
			}
		}

		if (HostAvailability.INSTANCE.isHostUnavailable(path)) {
			if (callback != null) {
				callback.handleRequestError(new PageNotAvailableException(path, -1));
			}
		}
	}
	
	private void createServiceCall(String path, String mobileTag, int responseStatus) {
		
		WebApplicationInfo webApplicationInfo = oneAgent.createWebApplicationInfo("EasyTravelWebserver",
				generateCalledServiceName(), "/");
		IncomingWebRequestTracer tracer = oneAgent.traceIncomingWebRequest(webApplicationInfo, path,
				"POST");

		tracer.addRequestHeader("x-dynatrace", mobileTag);
		tracer.setRemoteAddress(bg.remoteIP);
		tracer.start();
		
		try {
			tracer.setStatusCode(responseStatus);
		} catch (Exception e) {
			tracer.setStatusCode(500); // we expect that the container sends HTTP 500 status code in case request processing throws an exception
			tracer.error(e);
			throw e;
		} finally {
			tracer.end();
		}
	}
	
	private String generateCalledServiceName() {
		int rand = secureRnd.nextInt(calledServiceNames.length); 
		return calledServiceNames[rand];
	}
	
	private String generateHttpError() {
		int rand = secureRnd.nextInt(httpErrorCodes.length);
		return httpErrorCodes[rand];
	}

	private void sendBeaconSignal(String dtAgentHost, MobileBeaconGenerator beaconGenerator) {		
		String url = null;
		
		if(IS_RUXIT) {
			if(isAgentActive && ruxitConfigOK) {
				url = ruxitBeaconUrl;
			}
		}
		else {
			if(isAgentActive) {
				url = dynatraceBeaconUrlFromHost(dtAgentHost);
			}
		}
		
		if(url != null) {
			try {
				sendBeaconSignalToUrl(url, beaconGenerator);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
				throw new MobileBeaconGenerationException(e);
			}
		}
		
		beaconGenerator.clearEvents();
	}
	
	private String dynatraceBeaconUrlFromHost(String dtAgentHost) {
		StringBuilder sb = new StringBuilder();
		sb.append(TextUtils.appendTrailingSlash(dtAgentHost)).append(Uem.DT_MONITOR);
		
		return sb.toString();
	}

	private void sendBeaconSignalToUrl(String beaconUrl, MobileBeaconGenerator bg) throws IOException {
		String body = bg.getBeaconPostBody();
		String query = bg.getBeaconQuery();
		final String signalUrl = beaconUrl + query;
		if (!HostAvailability.INSTANCE.isHostUnavailable(signalUrl)) {
			try {
				http.request(Type.POST, signalUrl, new HttpResponseCallback() {

					@Override
					public void readDone(HttpResponse response) throws IOException {
						if (response.getStatusCode() != 200) {
							ruxitInvalidResponseCount++;
							LOG.info("Invalid response (" + response.getStatusCode() + "): " + response.toString());
							if(ruxitInvalidResponseCount > 9) {
								LOG.warn("Could not send mobile beacon signal " + ruxitInvalidResponseCount + " times in a row - disabling it.");
								isAgentActive = false;
							}
						} else {
							ruxitInvalidResponseCount = 0;
						}
					}
				}, body.getBytes());
			} catch (PageNotAvailableException e) { // JLT-53049 disable signal sending on PageNotAvailableExcpetion
				LOG.info("Could not send mobile beacon signal: " + signalUrl, e);
				isAgentActive = false;
			}
		}
	}

	public boolean isIOS() {
		return deviceType.isIOS();
	}

	private void delayRandomDeviceLatency() {
		int randomDelay = rnd.nextInt(MAX_RANDOM_ACTIONDELAY);
		simulateProcessingOnDevice(10, 10 + randomDelay);
	}

	/**
	 * Simulates processing time on device by calling Thread.sleep(). The simulated processing time
	 * is randomly distributed between [minDelay, maxDelay].
	 *
	 * Note: minDelay<maxDelay will not be checked.
	 *
	 * @param minDelay minduration of processing time
	 * @param maxDelay max duration of processing time.
	 * @author peter.lang
	 * @return the used delay
	 */
	public int simulateProcessingOnDevice(int minDelay, int maxDelay) {
		int delay = minDelay + (int) ((maxDelay - minDelay) * rnd.nextFloat());
		try {
//			if (LOG.isLoggable(Level.FINEST)) {
//				LOG.finest("now sleeping: " + delay + " [ms]");
//			}
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			LOG.warn("Interrupted Exception caught: " + e.getMessage(), e);
		}
		return delay;
	}

	/**
	 *
	 * @return x-dynatrace header mobile tag
	 */
	@SuppressWarnings("boxing")
	private static String constructMobileTagHeaderValue(int serverId, String visitorId, String sessionId, String appId, int tagId, int threadId, int requestId) {
		// MT_version_serverID_visitorID_sessionNr_appID_actionID_threadID_requestID
		return StringUtils.join(new Object[] { "MT_"+protocolVersion, serverId, visitorId, sessionId, appId, tagId, threadId, requestId}, BaseConstants.UNDERSCORE);
	}

	public boolean isTablet() {
		return deviceType.isTablet();
	}
}
