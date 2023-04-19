package com.dynatrace.diagnostics.uemload.scenarios;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dynatrace.diagnostics.uemload.*;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.*;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages.*;
import com.dynatrace.diagnostics.uemload.utils.UemLoadCalendarUtils;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.misc.CommonUser;
import com.dynatrace.easytravel.util.PHPEnablementCheck;
import com.google.common.collect.Lists;

import ch.qos.logback.classic.Logger;


public abstract class EasyTravel extends BrowserScenario implements EasyTravelLauncherScenario {
	
	public enum VisitLength {
		SHORT, LONG
	}

	public static boolean isUemCorrelationTestingMode = Boolean.parseBoolean(System.getProperty("com.dynatrace.easyTravel.debugSendXHR", "false"));

	protected final boolean highLoadFromAsia;

	private final EasyTravelHostManager hosts;

	private static AtomicBoolean TAGGED_WEB_REQUEST = new AtomicBoolean();

	private static final Logger LOGGER = LoggerFactory.make();

	public EasyTravel(String customerFrontendHost, String b2bFrontendHost, boolean highLoadFromAsia) {
		LOGGER.info("Adding customer frontend host: "  + customerFrontendHost + ", hosts are also added whenever procedures are started.");
		LOGGER.info("Adding B2B frontend host: " + b2bFrontendHost + ", hosts are also added whenever procedures are started.");

		hosts = new EasyTravelHostManager();

		if (customerFrontendHost != null) {
			hosts.setCustomerFrontendHost(customerFrontendHost);
		}
		if (b2bFrontendHost != null) {
			hosts.setB2BFrontendHost(b2bFrontendHost);
		}
		
		
		this.highLoadFromAsia = highLoadFromAsia;
	}


	@Override
	protected IterableSet<Visit> createVisits() {
		if(isUemCorrelationTestingMode) {
			VisitsModel visits = new VisitsModel.VisitsBuilder().setConvert(100).build();
			return createVisits(visits);
		} else {
			VisitsModel visits = new VisitsModel.VisitsBuilder().setSearch(100).build();
			return createVisits(visits);
		}
	}

	@Override
	protected Map<String, IterableSet<Visit>> createVisitsByContinent() {
		if(isUemCorrelationTestingMode) {
			return super.createVisitsByContinent();
		} else {
			Map<String, IterableSet<Visit>> res = new HashMap<String, IterableSet<Visit>>();
			VisitsModel visits = new VisitsModel.VisitsBuilder().setDefaults()
					.setBounce(60)
					.setSearch(60)
					.setAlmost(9)
					.setConvert(1)
					.setB2b(5)
					.build();
			res.put(EUROPE, createVisits(visits));
			return res;
		}
	}

	protected IterableSet<Visit> createVisits(VisitsModel visits) {			
		RandomSet<Visit> res = new RandomSet<Visit>();
		int weightModifier = hosts.getB2bFrontendHostCount() + 1;
		for (String customerFrontendHost : hosts.getCustomerFrontendHosts()) {
    		res.add(new Bounce(customerFrontendHost), visits.getBounce() * weightModifier);
            if (EasyTravelHostManager.isApacheWebserver(customerFrontendHost)) {
                // Used only when ApacheWebserver is running
                // The Seo Visit was created to demonstrate Apache mod_rewrite module
                res.add(new Seo(customerFrontendHost), visits.getSeo() * weightModifier);
            }
    		res.add(new Search(customerFrontendHost), visits.getSearch() * weightModifier);
    		res.add(new AlmostConvert(customerFrontendHost), visits.getAlmost() * weightModifier);
    		res.add(new Convert(customerFrontendHost, useRandomFirstPage()), visits.getConvert() * weightModifier);
			res.add(new SpecialOffers(customerFrontendHost), visits.getSpecialOffers() * weightModifier);
			res.add(new PageWanderer(customerFrontendHost, useRandomFirstPage(), VisitLength.SHORT), visits.getWandererShort() * weightModifier);
			res.add(new PageWanderer(customerFrontendHost, useRandomFirstPage(), VisitLength.LONG), visits.getWandererLong() * weightModifier);
			res.add(new PageWandererConvert(customerFrontendHost, useRandomFirstPage(), VisitLength.SHORT), visits.getWandererShort() * weightModifier);
			res.add(new PageWandererConvert(customerFrontendHost, useRandomFirstPage(), VisitLength.LONG), visits.getWandererLong() * weightModifier);

			if(PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.MAGENTO_SHOP)) {
				res.add(new WordPressShopVisit(customerFrontendHost, VisitLength.SHORT), visits.getMagentoShort() * weightModifier);
				res.add(new WordPressShopVisit(customerFrontendHost, VisitLength.LONG), visits.getMagentoLong() * weightModifier);
			}
			
			if(EasyTravelAMP.isConfigSet()) {
				res.add(new EasyTravelAMPVisit(customerFrontendHost), visits.getAmp() * weightModifier);
			} else {
				LOGGER.warn("AMP visits will not be generated. Set configs properly to enable AMP traffic generation.");
			}
		}
		weightModifier = hosts.getCustomerFrontendHostCount() + 1;
		for (String b2bFrontendHost : hosts.getB2bFrontendHosts()) {
			res.add(new B2B(b2bFrontendHost), visits.getB2b() * weightModifier);
		}

		if(res.isEmpty()) {
			LOGGER.warn("No visits created. Number of B2B frontend hosts: " + hosts.getB2bFrontendHosts().size() + " Number of frontend hosts: " + hosts.getCustomerFrontendHosts().size() );
		}
		return res;
	}

	/**
	 * Overwrite this method to define which page will be read at the beginning int the Convert visit
	 * true: first page will be generated automatically
	 * false: 'contact' page will be used
	 * @return
	 */
	protected boolean useRandomFirstPage() {
		return !isUemCorrelationTestingMode;
	}

	@Override
	protected RandomLocation createRandomLocations() {
		SampledRandomLocation location = new SampledRandomLocation(highLoadFromAsia);
		AbstractUEMLoadSession.setCustomerLocations(location);
		return location;
	}

	@Override
	protected Map<String, Double> createHardwareSpeedByContinent() {
		Map<String, Double> res = new HashMap<String, Double>();
		res.put(ASIA, 0.5);
		res.put(AFRICA, 0.3);
		return res;
	}

	@Override
	protected Map<String, Double> createHardwareSpeedByCountry() {
		Map<String, Double> res = new HashMap<String, Double>();
		res.put("China", 0.4);
		return res;
	}

	protected static class Bounce implements Visit {

		private final String host;

		public Bounce(String host) {
			this.host = host;
		}

		@Override
		public Action[] getActions(CommonUser user, Location location) {
			CustomerSession session = createCustomerSession(host, user, location);

			RandomSet<Action> actions = new RandomSet<Action>();
			actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.INIT), 6);
			actions.add(createSpecialOffersPage(session), 8);
			actions.add(createContactPage(session), 11);
			actions.add(createLegalPage(session), 10);
			actions.add(createPrivacyPage(session), 12);
			actions.add(createAboutPage(session), 11);

			return createActionArray(location.isRuxitSynthetic(), actions.getRandom());
		}

		@Override
		public String getVisitName() {
			return VisitNames.EASYTRAVEL_BOUNCE;
		}
	}

	/**
	 * This class creates a visits that starts on the special offers page and converts.
	 *
	 * @author cwat-bfellner
	 */
	protected static class SpecialOffers implements Visit, VisitWithExcludedBrowser {

		private final String host;

		public SpecialOffers(String host) {
			this.host = host;
		}

		@Override
		public Action[] getActions(CommonUser user, Location location) {
			CustomerSession session = createCustomerSession(host, user, location);

			List<Action> actions = Lists.newArrayList();
			actions.add(createSpecialOffersPage(session));
			actions.add (new EasytravelStartPage(session, EasytravelStartPage.State.INIT));
			actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.LOGIN));
			actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.SEARCH));
			if (PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.PHP_ENABLEMENT_PLUGIN)) {
				actions.add(new EasyTravelTripDetailsPage(session));
			}
			actions.add(new EasyTravelReviewPage(session));
			actions.add(new EasyTravelPaymentPage(session));
			actions.add(new EasyTravelFinishPage(session));
			actions.add(createLogoutPage(session));
			if(location.isRuxitSynthetic()) {
				actions.add(new SyntheticEndVisitAction());
			}

			return actions.toArray(new Action[actions.size()]);
		}

		@Override
		public BrowserType getExcludedBrowser() {
			return BrowserType.IE_10;
		}

		@Override
		public String getVisitName() {
			return VisitNames.EASYTRAVEL_SPECIAL_OFFERS;
		}
	}

	protected static class Search implements Visit {

		private final String host;

		public Search(String host) {
			this.host = host;
		}

		@Override
		public Action[] getActions(CommonUser user, Location location) {
			CustomerSession session = createCustomerSession(host, user, location);
			List<Action> actions = Lists.newArrayList();
			actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.INIT));
			UemLoadCalendarUtils calendar = new UemLoadCalendarUtils();
			if(!calendar.isTripThisYear()) {
				actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.CALENDAR_YEAR, calendar));
			}
			if(!calendar.isTripThisMonth()) {
				actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.CALENDAR_MONTH, calendar));
			}
			actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.CALENDAR_DAY, calendar));
			actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.SEARCH));
			actions.add(createContactPage(session));
			if(location.isRuxitSynthetic()) {
				actions.add(new SyntheticEndVisitAction());
			}

			return actions.toArray(new Action[actions.size()]);
		}

		@Override
		public String getVisitName() {
			return "Search";
		}
	}

    protected static class Seo implements Visit {

        private final String host;

        public Seo(String host) {
            this.host = host;
        }

        @Override
        public Action[] getActions(CommonUser user, Location location) {
            CustomerSession session = createCustomerSession(host, user, location);
            return createActionArray(location.isRuxitSynthetic(),
                    new EasytravelStartPage(session, EasytravelStartPage.State.INIT),
                    new EasyTravelSeoPage(session),
                    new EasyTravelSeoAboutPage(session),
                    new EasyTravelSeoContactPage(session)
            );
        }

		@Override
		public String getVisitName() {
			return "Seo";
		}
    }

	static CustomerSession createCustomerSession(String host, CommonUser user, Location location) {
		CustomerSession session = new CustomerSession(host, user, location, TAGGED_WEB_REQUEST.get());
		return session;
	}

	private static B2BSession createB2BSession(String host, CommonUser user, Location location) {
		return new B2BSession(host, user, location, TAGGED_WEB_REQUEST.get());
	}

	protected static class AlmostConvert implements Visit {

		private final String host;

		public AlmostConvert(String host) {
			this.host = host;
		}

		@Override
		public Action[] getActions(CommonUser user, Location location) {
			CustomerSession session = createCustomerSession(host, user, location);
			RandomSet<Action> startActions = new RandomSet<Action>();
			startActions.add(new EasytravelStartPage(session, EasytravelStartPage.State.INIT), 4);
			startActions.add(createSpecialOffersPage(session), 10); // note that these would now be followed by start page - see below
				// thus we get the start page always and regardless

			Action randomAction = startActions.getRandom();

			return createActionArray(location.isRuxitSynthetic(),
				randomAction,
				((EasyTravelPage) randomAction).getPage() ==  EtPageType.SPECIAL_OFFERS ? new EasytravelStartPage(session, EasytravelStartPage.State.INIT) : null,
				new EasytravelStartPage(session, EasytravelStartPage.State.LOGIN),
				new EasytravelStartPage(session, EasytravelStartPage.State.SEARCH),
				new EasyTravelReviewPage(session),
				new EasyTravelPaymentPage(session),
				PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.FAILED_XHRs) ? new EasyTravelFailingXHRPage(session) : null,
				createLegalPage(session),
				createPrivacyPage(session),
				createLogoutPage(session)
			);
		}

		@Override
		public String getVisitName() {
			return VisitNames.EASYTRAVEL_ALMOST_CONVERT;
		}

	}

	protected static class Convert implements Visit {

		private final String host;
		//if set to true first loaded page will be chosen randomly
		//if set to false contact page will be loaded - used with EasyTravelFixedCustomer traffic scenario or when isUemCorrelationTestingMode is set
		private final boolean useRandomPage;

		public Convert(String host, boolean useRandomPage) {
			this.host = host;
			this.useRandomPage = useRandomPage;
		}

		@Override
		public Action[] getActions(CommonUser user, Location location) {
			CustomerSession session = createCustomerSession(host, user, location);
			List<Action> actions = Lists.newArrayList();
			actions.add(
					useRandomPage ? createRandomPage(session) : new EasytravelStartPage(session, EasytravelStartPage.State.INIT));
			if(isPHPEnabled()){
				actions.add(createBlogDetailsPage(session));
			}
			actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.INIT));
			actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.LOGIN));
			actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.SEARCH));
			if (PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.PHP_ENABLEMENT_PLUGIN)){
				actions.add(new EasyTravelTripDetailsPage(session));
			}
			actions.add(new EasyTravelReviewPage(session));
			actions.add(new EasyTravelPaymentPage(session));
			actions.add(new EasyTravelFinishPage(session));
			actions.add(createLogoutPage(session));
			if(location.isRuxitSynthetic()) {
				actions.add(new SyntheticEndVisitAction());
			}

			return actions.toArray(new Action[actions.size()]);
		}

		@Override
		public String getVisitName() {
			return VisitNames.EASYTRAVEL_CONVERT;
		}

	}

	public static boolean isPHPEnabled() {
		return !EasyTravelConfig.read().PHPEnabledForAngularOnly && PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.PHP_ENABLEMENT_PLUGIN);
	}

	protected static class B2B implements Visit {

		private final String host;

		public B2B(String host) {
			this.host = host;
		}

		@Override
		public Action[] getActions(CommonUser user, Location location) {
			B2BSession session = createB2BSession(host, user, location);
			return createActionArray(location.isRuxitSynthetic(),
				createB2bHome(session),
				createB2bLogin(session),
				createB2bJourney(session),
				createB2bReport(session),
				createB2bBooking(session),
				createB2bLogout(session)
			);
		}

		@Override
		public String getVisitName() {
			return VisitNames.EASYTRAVEL_B2B;
		}
	}

	/**
	 * This method creates a random page for a converted visit. It should start
	 * mostly on the home page and not on the special offers page.
	 * The other pages such as contact, about, privacy and legal should not get high
	 * conversion rates.
	 *
	 * @param session The customer session.
	 * @return The random page action.
	 */
	static Action createRandomPage(CustomerSession session) {

		RandomSet<Action> actions = new RandomSet<Action>();
		actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.INIT), 35);
		actions.add(createContactPage(session), 1);
		actions.add(createLegalPage(session), 1);
		actions.add(createPrivacyPage(session), 1);
		actions.add(createAboutPage(session), 1);

		return actions.getRandom();
	}

	private static Action createSpecialOffersPage(CustomerSession session) {
		return new EasyTravelSimplePage(EtPageType.SPECIAL_OFFERS, session);
	}

	private static Action createAboutPage(CustomerSession session) {
		return new EasyTravelSimplePage(
				EtPageType.ABOUT,
				session
		);
	}

	static Action createLogoutPage(CustomerSession session) {
		return new EasyTravelSimplePage(
				EtPageType.LOGOUT,
				session
		);
	}

	private static Action createContactPage(CustomerSession session) {
		return new EasyTravelContactPage(
				EtPageType.CONTACT,
				session
		);
	}

	private static Action createLegalPage(CustomerSession session) {
		return new EasyTravelSimplePage(
				EtPageType.TERMS,
				session
		);
	}

	private static Action createPrivacyPage(CustomerSession session) {
		return new EasyTravelSimplePage(
				EtPageType.PRIVACY,
				session
		);
	}

	static Action createBlogDetailsPage(CustomerSession session){
		return new EasyTravelSimplePage(
				EtPageType.BLOGDETAILS,
				session
		);
	}

	private static Action createB2bHome(B2BSession session) {
		return new B2BSimplePage(
				EtPageType.B2B_HOME,
				session
		);
	}

	private static Action createB2bLogin(B2BSession session) {
		return new B2BAuthenticatedPage(
				EtPageType.B2B_LOGIN,
				session
		);
	}

	private static Action createB2bJourney(B2BSession session) {
		return new B2BSimplePage (
				EtPageType.B2B_JOURNEY,
				session
		);
	}

	private static Action createB2bReport(B2BSession session) {
		return new B2BSimplePage (
				EtPageType.B2B_REPORT,
				session
		);
	}

	private static Action createB2bBooking(B2BSession session) {
		return new B2BSimplePage (
				EtPageType.B2B_BOOKING,
				session
		);
	}

	private static Action createB2bLogout(B2BSession session) {
		return new B2BSimplePage (
				EtPageType.B2B_LOGOUT,
				session
		);
	}

	@Override
	public void init(boolean taggedWebRequest) {
		TAGGED_WEB_REQUEST.set(taggedWebRequest);
		super.init();
	}

	public static void setTaggedWebRequest(boolean taggedWebRequest) {
		TAGGED_WEB_REQUEST.set(taggedWebRequest);
	}

	@Override
	public EasyTravelHostManager getHostsManager() {
		return hosts;
	}

	private static Action[] createActionArray(final boolean isRuxitSynthetic, Action... actions) {
		ArrayList<Action> actionList = new ArrayList<Action>();
		for (Action action: actions) {
			if (action != null) {
				actionList.add(action);
			}
		}
		if(isRuxitSynthetic) {
			actionList.add(new SyntheticEndVisitAction());
		}

		return actionList.toArray(new Action[actionList.size()]);
	}

}