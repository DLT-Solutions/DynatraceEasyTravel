package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.ExtendedDemoUser;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.iot.IotDevice;
import com.dynatrace.diagnostics.uemload.mobile.MobileDeviceType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.MobileSimulator;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileActionType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileDevice;
import com.dynatrace.diagnostics.uemload.mobileopenkit.parameters.MobileOpenKitParams;
import com.dynatrace.diagnostics.uemload.mobileopenkit.visit.BookingJourneyVisit;
import com.dynatrace.diagnostics.uemload.mobileopenkit.visit.BounceVisit;
import com.dynatrace.diagnostics.uemload.mobileopenkit.visit.ManyActionsVisit;
import com.dynatrace.diagnostics.uemload.mobileopenkit.visit.MobileVisits;
import com.dynatrace.diagnostics.uemload.mobileopenkit.visit.SearchJourneyVisit;
import com.dynatrace.diagnostics.uemload.mobileopenkit.visit.SpecialOffersVisit;
import com.dynatrace.diagnostics.uemload.openkit.visit.OpenKitVisit;
import com.dynatrace.diagnostics.uemload.scenarios.OpenKitScenario;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.openkit.AgentTechnologyType;
import com.dynatrace.openkit.CrashReportingLevel;
import com.dynatrace.openkit.DataCollectionLevel;

import ch.qos.logback.classic.Logger;

public class OpenKitMobileAppScenario extends OpenKitScenario<MobileVisits, OpenKitVisit<MobileActionType>, MobileOpenKitParams> {
	protected static final MobileDevice.AppVersion HAINER_APP_VERSION = new MobileDevice.AppVersion("7.9.8", "7.9.8.7983");
	private static final Random random = new Random();
	
	private final static AtomicInteger visitCounter = new AtomicInteger(0);
	
	private final String APP_PACKAGE_NAME = "com.dynatrace.easytravel";

	@Override
	protected RandomSet<MobileVisits> createVisitSet() {
		RandomSet<MobileVisits> visits = new RandomSet<>();
		visits.add(MobileVisits.MANY_ACTIONS_VISIT, 1);
		visits.add(MobileVisits.BOOKING_JOURNEY_VISIT, 10);
		visits.add(MobileVisits.SEARCH_JOURNEY_VISIT, 1);
		visits.add(MobileVisits.BOUNCE_VISIT, 1);
		visits.add(MobileVisits.SPECIAL_OFFERS_VISIT, 1);
		return visits;
	}

	@Override
	public OpenKitVisit<MobileActionType> getRandomVisit(String apiUrl, MobileOpenKitParams params, ExtendedCommonUser user) {

		MobileVisits visitType = getRandomVisitType(user);
		switch (visitType) {
			case MANY_ACTIONS_VISIT:
				return new ManyActionsVisit(params, user, apiUrl);
			case BOOKING_JOURNEY_VISIT:
				return new BookingJourneyVisit(params, user, apiUrl);
			case SEARCH_JOURNEY_VISIT:
				return new SearchJourneyVisit(params, user, apiUrl);
			case BOUNCE_VISIT:
				return new BounceVisit(params, user, apiUrl);
			case SPECIAL_OFFERS_VISIT:
				return new SpecialOffersVisit(params, user, apiUrl);
			default:
				return new BookingJourneyVisit(params, user, apiUrl);
		}
	}

	public MobileVisits getRandomVisitType(ExtendedCommonUser user) {
		return (user == ExtendedDemoUser.HAINER_USER) ? MobileVisits.BOOKING_JOURNEY_VISIT : visits.getNext();
	}

	@Override
	protected String getName() {
		return "OpenKit ET Mobile Scenario";
	}

	@Override
	public Simulator createSimulator() {
		return new MobileSimulator(this);
	}

	@Override
	public boolean hasHosts() {
		return getHostsManager().hasAngularFrontendHost();
	}

	public MobileOpenKitParams getRandomParams(ExtendedCommonUser user) {
		int value = visitCounter.incrementAndGet();
		
		Location location = user.getLocation();
		MobileDeviceType mobileDevice = user.getMobileDevice();
		AgentTechnologyType agentType = mobileDevice.getOsType() != null ? mobileDevice.getOsType().agentType : AgentTechnologyType.JAVA;
		MobileDevice.AppVersion appVersion = getAppVersion(user);
		
		MobileOpenKitParams params = new MobileOpenKitParams();
		params.setCountry(location.getCountry());
		params.setIp(location.getIp());
		params.setManufacturer(mobileDevice.getManufacturer());
		params.setModel(mobileDevice.getModelId());
		params.setOs(mobileDevice.getOs());
		params.setAppVersion(appVersion.version);
		params.setDeviceId(user.getMobileDeviceId());
		params.setDataCollectionLevel(getRandomDataCollectionLevel(value));
		params.setCrashReportingLevel(getRandomCrashReportingLevel(value));
		params.setAgentType(agentType);
		params.setAppVersionBuild(appVersion.build);
		params.setApplicationPackage(APP_PACKAGE_NAME);
		
		return params;
	}

	private MobileDevice.AppVersion getAppVersion(ExtendedCommonUser user) {
		return (user == ExtendedDemoUser.HAINER_USER ? HAINER_APP_VERSION : MobileDevice.getRandomAppVersion(user.getMobileDevice().getOsType()));
	}


	private boolean isPluginEnabled() {
		return PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.ANGULAR_BOOKING_ERROR_500);
	}

	/*
	 * Wrapper method for getRandomUser() method.
	 * If Angular booking error plugin is enabled,
	 * Hainer user is added to mobile scenario visits
	 * with probability of 0.5.
	 *
	 * If ANGULAR_BOOKING_ERROR_500 problem pattern is disabled,
	 * original getRandomUser() method is called.
	 *
	 * @param country
	 * @author maria.rolbiecka
	 */
	public ExtendedCommonUser getRandomMobileUser(String country) {
		if(isPluginEnabled()) {
			int r = random.nextInt(10);
			if (r > 4) {
				return ExtendedDemoUser.HAINER_USER;
			}
		}
		return getRandomUser(country, DtVersionDetector.isAPM());
	}
	
	private DataCollectionLevel getRandomDataCollectionLevel(int value) {
		if (value % 10 == 0) {
			return DataCollectionLevel.OFF;
		}
		else if (value % 10 < 3) {
			return DataCollectionLevel.PERFORMANCE;
		}
		
		return DataCollectionLevel.USER_BEHAVIOR;
	}
	
	private CrashReportingLevel getRandomCrashReportingLevel(int value) {
		if (value % 10 == 1) {
			return CrashReportingLevel.OPT_OUT_CRASHES;
		}
		
		return CrashReportingLevel.OPT_IN_CRASHES;
	}
}
