package com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.mobile.MobileAppScenario;
import com.dynatrace.diagnostics.uemload.mobile.MobileBeaconGenerator;
import com.dynatrace.diagnostics.uemload.mobile.MobileDeviceType;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelHostManager;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelLauncherScenario;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.android.AndroidBookJourneyAction;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.android.AndroidDoLoginAction;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.android.AndroidDoLoginFailedAction;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.android.AndroidDoSearchAction;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.ios.IOSBookJourneyAction;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.ios.IOSPerformLoginAction;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.ios.IOSPerformLoginFailedAction;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.ios.IOSPerformSearchAction;
import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.misc.CommonUser;
import com.dynatrace.easytravel.util.DtVersionDetector;

import ch.qos.logback.classic.Logger;

/**
 * Scenario to simulate visits from mobile native devices.
 *
 * @author peter.lang
 */

@SuppressWarnings("nls")
public class EasyTravelMobileAppScenario extends MobileAppScenario implements EasyTravelLauncherScenario {

	private static final Logger LOGGER = LoggerFactory.make();

	private final EasyTravelHostManager hosts;

	public EasyTravelMobileAppScenario() {
		this(null);
	}

	/**
	 *
	 * @param string
	 * @author peter.lang
	 */
	public EasyTravelMobileAppScenario(String customerFrontendUrl) {
		super();
		LOGGER.info("Adding customer frontend host: "  + customerFrontendUrl + ", hosts are also added whenever procedures are started.");

		this.hosts = new EasyTravelHostManager();
		if (customerFrontendUrl!=null) {
			hosts.addCustomerFrontendHost(customerFrontendUrl);
		}
	}

	@Override
	protected RandomSet<Visit> createVisits() {
		RandomSet<Visit> res = new RandomSet<Visit>();

		if (hasHosts()) {
			String dtAgentUrl = hosts.getCustomerFrontendHosts().get(0);
			res.add(new Bounce(dtAgentUrl), 1);
			res.add(new SearchJourney(dtAgentUrl), 5);
			res.add(new BookingJourneyAndCrashIfPatternEnabled(dtAgentUrl), 5);	//same as BookingJourney, but crashes for tablets if TabletCrashes Plugin is enabled
			res.add(new SpecialOffers(dtAgentUrl), 3);
			res.add(new ManyActionsSession(dtAgentUrl), 10);
		}

		return res;
	}

	/* (non-Javadoc)
	 * @see com.dynatrace.diagnostics.uemload.Scenario#getName()
	 */
	@Override
	protected String getName() {
		return "EasyTravelMobileApp";
	}

	private static MobileSession createMobileAppSession(String host, CommonUser user, Location location) {
		boolean rooted = Math.random() > 0.7d;	//30% should be rooted
		String appId = DtVersionDetector.isAPM() ? MobileBeaconGenerator.EASY_TRAVEL_RUXIT_APPLICATION_ID : MobileBeaconGenerator.EASY_TRAVEL_APPLICATION_NAME;
		MobileSession session = new MobileSession(host, user, location, appId, MobileBeaconGenerator.EASY_TRAVEL_APPLICATION_NAME, System.currentTimeMillis(), location.getCoordinates(), rooted);
		return session;
	}
	
	private static class Bounce implements MobileVisit {

		private final String agentHost;

		public Bounce(String agentHost) {
			this.agentHost = agentHost;
		}

		@Override
		public Action[] getActions(MobileDeviceType device, CommonUser user, Location location) {
			MobileSession session = createMobileAppSession(agentHost, user, location);

			RandomSet<Action> actions = new RandomSet<Action>();
			if(device.isIOS()){
				actions.add(new IOSPerformSearchAction(session), 2);
				actions.add(new IOSPerformLoginAction(session), 2);
				actions.add(new IOSPerformLoginFailedAction(session), 2);
				actions.add(new MobileGPSErrorAction(session), 1);
			}else{
				actions.add(new AndroidDoSearchAction(session), 2);
				actions.add(new AndroidDoLoginAction(session), 2);
				actions.add(new AndroidDoLoginFailedAction(session), 2);
				actions.add(new MobileGPSErrorAction(session), 1);
			}
			return new Action[] {
					actions.getRandom()
			};
		}

		@Override
		public Action[] getActions(CommonUser user, Location location) {
			return getActions(MobileDeviceType.APPLE_IPHONE_7_PLUS, user, location);
		}

		@Override
		public String getVisitName() {
			return VisitNames.MOBILE_BOUNCE;
		}
	}

	private static class SearchJourney implements MobileVisit {

		private final String agentHost;

		public SearchJourney(String agentHost) {
			this.agentHost = agentHost;
		}

		@Override
		public Action[] getActions(MobileDeviceType device, CommonUser user, Location location) {
			MobileSession session = createMobileAppSession(agentHost, user, location);
			if(device.isIOS()){
				return new Action[] {
						new MobileDisplayLifecycleAction(session, "DTSearchViewController"), //$NON-NLS-1$
						new IOSPerformSearchAction(session),
						new MobileDisplayLifecycleAction(session, "DTOfflineWebViewController"), //$NON-NLS-1$
						new MobileAppWebpageViewAction(session),
						new MobileTouchOnAction(session, "SearchJourneyActivity", "Search"), //$NON-NLS-1$
						new IOSPerformSearchAction(session),
						new MobileDisplayLifecycleAction(session, "DTOfflineWebViewController"), //$NON-NLS-1$
						new MobileAppWebpageViewAction(session)
				};
			}
			return new Action[] {
					new MobileDisplayLifecycleAction(session, "SearchJourneyActivity"), //$NON-NLS-1$
					new AndroidDoSearchAction(session),
					new MobileDisplayLifecycleAction(session, "DTOfflineWebViewController"), //$NON-NLS-1$
					new MobileAppWebpageViewAction(session),
					new MobileTouchOnAction(session, "SearchJourneyActivity", "Search"), //$NON-NLS-1$
					new AndroidDoSearchAction(session),
					new MobileDisplayLifecycleAction(session, "DTOfflineWebViewController"), //$NON-NLS-1$
					new MobileAppWebpageViewAction(session)
			};
		}

		@Override
		public Action[] getActions(CommonUser user, Location location) {
			return getActions(MobileDeviceType.APPLE_IPHONE_7_PLUS, user, location);
		}

		@Override
		public String getVisitName() {
			return VisitNames.MOBILE_SEARCH;
		}
	}

	private static class BookingJourneyAndCrashIfPatternEnabled implements MobileVisit {

		private final String agentHost;

		public BookingJourneyAndCrashIfPatternEnabled(String agentHost) {
			this.agentHost = agentHost;
		}

		@Override
		public Action[] getActions(MobileDeviceType device, CommonUser user, Location location) {
			MobileSession session = createMobileAppSession(agentHost, user, location);

			if(device.isIOS()){
				return new Action[] {
						new MobileAppStartLifecycleAction(session),
						new MobileDisplayLifecycleAction(session, "DTSearchViewController"), //$NON-NLS-1$
						new IOSPerformSearchAction(session),
						new MobileTouchOnAction(session, "DTLoginViewCrontroller", "User Account"), //$NON-NLS-1$
						new IOSPerformLoginAction(session),
						new MobileTouchOnAction(session, "DTSearchViewController", "Search"), //$NON-NLS-1$
						new IOSBookJourneyAction(session),
						new MobileDisplayLifecycleAction(session, "DTOfflineWebViewController"), //$NON-NLS-1$
						new MobileAppWebpageViewAction(session),
						new MobileErrorsAction(session),
						new MobileCrashAction(session)
				};
			}
			return new Action[] {
					new MobileAppStartLifecycleAction(session),
					new MobileDisplayLifecycleAction(session, "SearchJourneyActivity"), //$NON-NLS-1$
					new AndroidDoSearchAction(session),
					new MobileTouchOnAction(session, "LoginActivity", "User Account"), //$NON-NLS-1$
					new AndroidDoLoginAction(session),
					new MobileTouchOnAction(session, "SearchJourneyActivity", "Search"), //$NON-NLS-1$
					new AndroidBookJourneyAction(session),
					new MobileDisplayLifecycleAction(session, "DTOfflineWebViewController"), //$NON-NLS-1$
					new MobileAppWebpageViewAction(session),
					new MobileErrorsAction(session),
					new MobileCrashAction(session)
			};

		}

		@Override
		public Action[] getActions(CommonUser user, Location location) {
			return getActions(MobileDeviceType.APPLE_IPHONE_7_PLUS, user, location);
		}

		@Override
		public String getVisitName() {
			return VisitNames.MOBILE_BOOKING_AND_CRASH;
		}
	}

	private static class ManyActionsSession implements MobileVisit {

		private final String agentHost;

		public ManyActionsSession(String agentHost) {
			this.agentHost = agentHost;
		}

		@Override
		public Action[] getActions(MobileDeviceType device, CommonUser user, Location location) {
			MobileSession session = createMobileAppSession(agentHost, user, location);

			if(device.isIOS()){
				return new Action[] {
						new MobileAppStartLifecycleAction(session),
						new MobileDisplayLifecycleAction(session, "DTSearchViewController"), //$NON-NLS-1$
						new IOSPerformSearchAction(session),
						new MobileTouchOnAction(session, "DTLoginViewCrontroller", "User Account"), //$NON-NLS-1$
						new IOSPerformLoginAction(session),
						new MobileTouchOnAction(session, "DTSearchViewController", "Search"), //$NON-NLS-1$
						new MobileTouchOnAction(session, "DTSearchViewController", "Paris"), //$NON-NLS-1$
						new MobileTouchOnAction(session, "DTSearchViewController", "New York"), //$NON-NLS-1$
						new MobileTouchOnAction(session, "DTSearchViewController", "Search"), //$NON-NLS-1$
						new MobileTouchOnAction(session, "DTSearchViewController", "Berlin"), //$NON-NLS-1$
						new MobileTouchOnAction(session, "DTSearchViewController", "Amsterdam"), //$NON-NLS-1$
						new MobileTouchOnAction(session, "DTSearchViewController", "Vienna"), //$NON-NLS-1$
						new MobileTouchOnAction(session, "DTSearchViewController", "Search"), //$NON-NLS-1$
						new MobileTouchOnAction(session, "DTSearchViewController", "Rome"), //$NON-NLS-1$
						new IOSBookJourneyAction(session),
						new MobileDisplayLifecycleAction(session, "DTOfflineWebViewController"), //$NON-NLS-1$
						new MobileAppWebpageViewAction(session),
						new MobileErrorsAction(session),
						new MobileCrashAction(session)
				};
			}
			return new Action[] {
					new MobileAppStartLifecycleAction(session),
					new MobileDisplayLifecycleAction(session, "SearchJourneyActivity"), //$NON-NLS-1$
					new AndroidDoSearchAction(session),
					new MobileTouchOnAction(session, "LoginActivity", "User Account"), //$NON-NLS-1$
					new AndroidDoLoginAction(session),
					new MobileTouchOnAction(session, "SearchJourneyActivity", "Search"), //$NON-NLS-1$
					new MobileTouchOnAction(session, "SearchJourneyActivity", "Paris"), //$NON-NLS-1$
					new MobileTouchOnAction(session, "SearchJourneyActivity", "New York"), //$NON-NLS-1$
					new MobileTouchOnAction(session, "SearchJourneyActivity", "Search"), //$NON-NLS-1$
					new MobileTouchOnAction(session, "SearchJourneyActivity", "Berlin"), //$NON-NLS-1$
					new MobileTouchOnAction(session, "SearchJourneyActivity", "Amsterdam"), //$NON-NLS-1$
					new MobileTouchOnAction(session, "SearchJourneyActivity", "Vienna"), //$NON-NLS-1$
					new MobileTouchOnAction(session, "SearchJourneyActivity", "Search"), //$NON-NLS-1$
					new MobileTouchOnAction(session, "SearchJourneyActivity", "Rome"), //$NON-NLS-1$
					new AndroidBookJourneyAction(session),
					new MobileDisplayLifecycleAction(session, "DTOfflineWebViewController"), //$NON-NLS-1$
					new MobileAppWebpageViewAction(session),
					new MobileErrorsAction(session),
					new MobileCrashAction(session)
			};

		}

		@Override
		public Action[] getActions(CommonUser user, Location location) {
			return getActions(MobileDeviceType.APPLE_IPHONE_7_PLUS, user, location);
		}

		@Override
		public String getVisitName() {
			return "Mobile App - Many Action Session";
		}
	}

	private static class SpecialOffers implements MobileVisit {

		private final String agentHost;

		public SpecialOffers(String agentHost) {
			this.agentHost = agentHost;
		}

		@Override
		public Action[] getActions(MobileDeviceType device, CommonUser user, Location location) {
			MobileSession session = createMobileAppSession(agentHost, user, location);

			if(device.isIOS()){
				return new Action[] {
						new MobileAppStartLifecycleAction(session),
						new MobileDisplayLifecycleAction(session, "DTNavigationViewController"), //$NON-NLS-1$
						new MobileDisplayLifecycleAction(session, "DTMasterViewController"), //$NON-NLS-1$
						new MobileTouchOnAction(session, null, "Special Offers"), //$NON-NLS-1$
						new MobileDisplayLifecycleAction(session, "DTOfflineWebViewController"), //$NON-NLS-1$
				};
			}
			return new Action[] {
					new MobileAppStartLifecycleAction(session),
					new MobileDisplayLifecycleAction(session, "SearchJourneyActivity"), //$NON-NLS-1$
					new MobileTouchOnAction(session, null, "Special Offers"), //$NON-NLS-1$
					new MobileDisplayLifecycleAction(session, "WebViewActivity"), //$NON-NLS-1$
			};

		}

		@Override
		public Action[] getActions(CommonUser user, Location location) {
			return getActions(MobileDeviceType.APPLE_IPHONE_7_PLUS, user, location);
		}

		@Override
		public String getVisitName() {
			return VisitNames.MOBILE_MANY_ACTION_SESSION;
		}
	}

	/* (non-Javadoc)
	 * @see com.dynatrace.diagnostics.uemload.scenarios.EasyTravelLauncherScenario#init(boolean)
	 */
	@Override
	public void init(boolean taggedWebRequest) {
		super.init();
	}

	/* (non-Javadoc)
	 * @see com.dynatrace.diagnostics.uemload.scenarios.EasyTravelLauncherScenario#getHostsManager()
	 */
	@Override
	public EasyTravelHostManager getHostsManager() {
		return hosts;
	}

	/* (non-Javadoc)
	 * @see com.dynatrace.diagnostics.uemload.scenarios.EasyTravelLauncherScenario#hasHosts()
	 */
	@Override
	public boolean hasHosts() {
		return hosts.getCustomerFrontendHostCount() > 0;
	}

}
