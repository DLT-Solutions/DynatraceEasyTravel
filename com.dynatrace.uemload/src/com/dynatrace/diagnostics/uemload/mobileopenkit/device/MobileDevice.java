package com.dynatrace.diagnostics.uemload.mobileopenkit.device;

import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.http.base.UemLoadHttpClient;
import com.dynatrace.diagnostics.uemload.mobile.MobileDeviceType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.parameters.MobileOpenKitParams;
import com.dynatrace.diagnostics.uemload.openkit.Device;
import com.dynatrace.diagnostics.uemload.openkit.EtOpenKitBuilder;
import com.dynatrace.diagnostics.uemload.openkit.OpenKitLoggerProxy;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PseudoRandomJourneyDestination;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants.OpenkitLogs;
import com.dynatrace.easytravel.frontend.rest.data.JourneyDTO;
import com.dynatrace.easytravel.logging.BasicLoggerConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.openkit.PlatformType;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.mobile.RootOrJailbreakInfo;
import com.dynatrace.openkit.api.mobile.ScreenOrientation;

import ch.qos.logback.classic.Logger;

public class MobileDevice extends Device {
	private final UemLoadHttpClient httpClient;
	private final String apiUrl;

	private JourneyDTO[] cachedJourneys;
	public final MobileOS os;
	public final String appVersionBuild;

	private final Random random = new Random();
	
	private JourneyDTO selectedJourney;
	
	private static final Logger logger = LoggerFactory.make();

	private static final String BEACON = StringUtils.trim(EasyTravelConfig.read().mobileBeaconUrl);
	private static final String APP_ID = StringUtils.trim(EasyTravelConfig.read().etMobileAppId);
	private static final OpenKit OPEN_KIT = new EtOpenKitBuilder(BEACON, APP_ID, 0).withInstanceName("MobileDevice")
			.withPlatformType(PlatformType.MOBILE)
			.withLogger(new OpenKitLoggerProxy(logger, new BasicLoggerConfig(OpenkitLogs.FILE_PREFIX, MobileDevice.class.getSimpleName()))).build(); // device ID set on a session level

	public MobileDevice(MobileOpenKitParams params, ExtendedCommonUser user, String apiUrl) {
		super(OPEN_KIT, params, user);
		appVersionBuild = params.getAppVersionBuild();
		this.apiUrl = apiUrl;
		os = user.getMobileDevice().getOsType();

		BrowserType browser = user.getMobileBrowser();
		httpClient = new UemLoadHttpClient(user.getBandwidth(), browser);
		httpClient.setUserAgent(browser.getUserAgent());
		String ip = user.getLocation().getIp();
		if (ip != null) {
			this.httpClient.setClientIP(ip);
		}
		reportMobileSessionParams(params);
	}

	private void reportMobileSessionParams(MobileOpenKitParams params) {
		MobileDeviceType device = user.getMobileDevice();
		activeSession.reportScreenResolution(device.getScreenWidth(), device.getScreenHeight());
		activeSession.reportCpu(device.getCpu());
		activeSession.reportUserLanguage("en_US");
		String carrier = os == MobileOS.ANDROID ? "AT&T" : "T-Mobile";
		if (device.getOs().endsWith(".3"))
			carrier = "Orange";
		activeSession.reportCarrier(carrier);
		activeSession.reportNetworkTechnology(params.getRandomNetworkTechnologyType().value);
		activeSession.reportScreenOrientation(Math.random() >= 0.5d ? ScreenOrientation.LANDSCAPE : ScreenOrientation.PORTRAIT);
		activeSession.reportFreeMemory(random.nextInt(100));
		activeSession.reportTotalMemory(512 + random.nextInt(512));
		activeSession.reportBatteryLevel((byte) (Math.random() * 100));
		if (Math.random() > 0.7d)
			activeSession.reportRootedOrJailbroken(os == MobileOS.ANDROID ? RootOrJailbreakInfo.ROOTED : RootOrJailbreakInfo.JAILBROKEN);
		else
			activeSession.reportRootedOrJailbroken(RootOrJailbreakInfo.GENUINE);
		activeSession.reportLocation(getRandomLocationParam(90) + "x" + getRandomLocationParam(180));
		activeSession.reportConnectionType(params.getRandomConnectionType());
	}

	private String getRandomLocationParam(int max) {
		return String.format("%.3f", (Math.random() * 2 - 1) * max);
	}

	public static void shutdownOpenKit() {
		OPEN_KIT.shutdown();
	}

	public JourneyDTO getRandomJourney() {
		if (cachedJourneys == null || cachedJourneys.length == 0)
			return null;
		return cachedJourneys[new Random().nextInt(cachedJourneys.length)];
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public UemLoadHttpClient getHttpClient() {
		return httpClient;
	}

	public boolean isIOS() {
		return os == MobileOS.IOS;
	}

	public String getRandomJourneyDestination() {
		return PseudoRandomJourneyDestination.get();
	}

	public void setCachedJourneys(JourneyDTO[] cachedJourneys) {
		this.cachedJourneys = cachedJourneys;
	}

	public static AppVersion getRandomAppVersion(MobileOS os) {
		int major = 1;
		double random = Math.random();
		if (random > 0.5) {
			major = 7;    //most
		} else if (random > 0.4) {
			major = 6;    //often
		} else if (random > 0.25) {
			major = 5;    //medium
		} else if (random > 0.23) {
			major = 4;    //quite rare
		} else if (random > 0.08) {
			major = 3;    //medium
		} else if (random > 0.01) {
			major = 2;    //medium rare
		} else {
			major = 1;    //really rare
		}
		int minor = major + 2;
		int revision = major + 1;
		int build = major * 1000 + minor * 100 + revision * 10 + 3;

		String appVersion = major + "." + minor + "." + revision;
		String appVersionBuild;
		if (os != MobileOS.ANDROID) {
			appVersionBuild = major + "." + minor + "." + build;
		} else {
			appVersionBuild = Integer.toString(build);
		}
		return new AppVersion(appVersion, appVersionBuild);
	}

	public static class AppVersion {
		public final String version;
		public final String build;

		public AppVersion(String version, String build) {
			this.version = version;
			this.build = build;
		}
	}

	public JourneyDTO getSelectedJourney() {
		return selectedJourney;
	}

	public void setSelectedJourney(JourneyDTO selectedJourney) {
		this.selectedJourney = selectedJourney;
	}
	
	public void selectRandomJourney() {
		this.selectedJourney = getRandomJourney();
	}
}
