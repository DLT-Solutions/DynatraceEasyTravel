package com.dynatrace.diagnostics.uemload;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.dynatrace.diagnostics.uemload.headless.HeadlessAngularConvertedVisit;
import com.dynatrace.diagnostics.uemload.mobile.MobileDeviceType;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.AbstractUEMLoadSession;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

public abstract class UEMLoadScenario {

	protected static final String AFRICA = "Africa";
	protected static final String ASIA = "Asia";
	protected static final String EUROPE = "Europe";
	protected static final String NORTH_AMERICA = "North America";
	protected static final String OCEANIA = "Oceania";
	protected static final String SOUTH_AMERICA = "South America";

	private IterableSet<Visit> visits;
	private Map<String, IterableSet<Visit>> visitsByContinent;
	private Map<String, IterableSet<Visit>> visitsByCountry;
	private IterableSet<Visit> rushHourVisits;
	private IterableSet<Visit> anonymousVisits;

	private RandomLocation locations;

	private RandomSet<BrowserType> desktopBrowsers;
	private Map<String, RandomSet<BrowserType>> desktopBrowsersByContinent;
	private Map<String, RandomSet<BrowserType>> desktopBrowsersByCountry;

	private RandomSet<BrowserType> mobileBrowsers;
	private Map<String, RandomSet<BrowserType>> mobileBrowsersByContinent;
	private Map<String, RandomSet<BrowserType>> mobileBrowsersByCountry;

	private RandomSet<BrowserType> syntheticBrowsers;
	private RandomSet<BrowserType> robotBrowsers;

	private RandomSet<MobileDeviceType> mobileDevices;
	private RandomSet<String> connectionType;

	private RandomSet<Bandwidth> bandwidth;
	private Map<String, RandomSet<Bandwidth>> bandwidthByContinent;
	private Map<String, RandomSet<Bandwidth>> bandwidthByCountry;

	private Map<String, Integer> dnsSlowdownFactorByContinent;
	private Map<String, Integer> dnsSlowdownFactorByCountry;

	private RandomSet<BrowserWindowSize> browserwindowsize;
	private RandomSet<BrowserWindowSize> mobilebrowserwindowsize;

	public void init() {
		visits = createVisits();
		visitsByContinent = createVisitsByContinent();
		visitsByCountry = createVisitsByCountry();
		rushHourVisits = createRushHourVisits();
		anonymousVisits = createAnonymousVisits();

		locations = createRandomLocations();

		desktopBrowsers = createBrowsers(false);
		desktopBrowsersByContinent = createBrowsersByContinent(false);
		desktopBrowsersByCountry = createBrowsersByCountry(false);

		mobileBrowsers = createBrowsers(true);
		mobileBrowsersByContinent = createBrowsersByContinent(true);
		mobileBrowsersByCountry = createBrowsersByCountry(true);

		syntheticBrowsers = createSyntheticBrowsers();
		robotBrowsers = createRobotBrowsers();

		bandwidth = createBandwidth();
		bandwidthByCountry = createBandwidthByCountry();
		bandwidthByContinent = createBandwidthByContinent();

		mobileDevices = createMobileDevices();
		connectionType = createConnectionTypes();

		browserwindowsize = createBrowserWindowSize();
		mobilebrowserwindowsize = createMobileBrowserWindowSize();

		dnsSlowdownFactorByContinent = createDnsSlowdownFactorByContinent();
		dnsSlowdownFactorByCountry = createDnsSlowdownFactorByCountry();
	}

	/**
	 *
	 *
	 * @return
	 * @author peter.lang
	 */
	public abstract Simulator createSimulator();

	protected abstract IterableSet<Visit> createVisits();

	protected IterableSet<Visit> createRushHourVisits() {
		return createVisits();
	}

	protected IterableSet<Visit> createAnonymousVisits() {
		return new RandomSet<>();
	}

	protected Map<String, IterableSet<Visit>> createVisitsByContinent() {
		return Collections.emptyMap();
	}

	protected Map<String, IterableSet<Visit>> createVisitsByCountry() {
		return Collections.emptyMap();
	}

	protected RandomLocation createRandomLocations() {
		return new SampledRandomLocation();
	}

	protected Map<String, Double> createHardwareSpeedByContinent() {
		return Collections.emptyMap();
	}

	protected Map<String, Double> createHardwareSpeedByCountry() {
		return Collections.emptyMap();
	}

	protected RandomSet<BrowserType> createBrowsers(boolean onlyMobile) {
		return BrowserDistribution.createDefaultBrowserDistribution(onlyMobile);
	}

	protected Map<String, RandomSet<BrowserType>> createBrowsersByContinent(boolean onlyMobile) {
		Map<String, RandomSet<BrowserType>> res = new HashMap<String, RandomSet<BrowserType>>();
		res.put(AFRICA, BrowserDistribution.createDefaultBrowserDistributionAfrica(onlyMobile));
		res.put(EUROPE, BrowserDistribution.createDefaultBrowserDistributionEurope(onlyMobile));
		res.put(NORTH_AMERICA, BrowserDistribution.createDefaultBrowserDistributionNorthAmerica(onlyMobile));
		res.put(OCEANIA, BrowserDistribution.createDefaultBrowserDistributionOceanic(onlyMobile));
		res.put(SOUTH_AMERICA, BrowserDistribution.createDefaultBrowserDistributionSouthAmerica(onlyMobile));
		res.put(ASIA, BrowserDistribution.createDefaultBrowserDistributionAsia(onlyMobile));
		return res;
	}

	protected Map<String, RandomSet<BrowserType>> createBrowsersByCountry(boolean onlyMobile) {
		return Collections.emptyMap();
	}

	protected RandomSet<BrowserType> createSyntheticBrowsers(){
		return BrowserDistribution.createDefaultSyntheticBrowserDistribution();
	}

	protected RandomSet<BrowserType> createRobotBrowsers(){
		return BrowserDistribution.createDefaultRobotBrowserDistribution();
	}

	protected RandomSet<Bandwidth> createBandwidth() {
		return BandwidthDistribution.createDefaultBandwidthDistribution();
	}

	protected Map<String, RandomSet<Bandwidth>> createBandwidthByContinent() {
		Map<String, RandomSet<Bandwidth>> res = new HashMap<String, RandomSet<Bandwidth>>();
		res.put(AFRICA, BandwidthDistribution.createDefaultBandwidthDistributionAfrica());
		res.put(ASIA, BandwidthDistribution.createDefaultBandwidthDistributionAsia());
		res.put(EUROPE, BandwidthDistribution.createDefaultBandwidthDistributionEurope());
		res.put(NORTH_AMERICA, BandwidthDistribution.createDefaultBandwidthDistributionNorthAmerica());
		res.put(OCEANIA, BandwidthDistribution.createDefaultBandwidthDistributionOceanic());
		res.put(SOUTH_AMERICA, BandwidthDistribution.createDefaultBandwidthDistributionSouthAmerica());
		return res;
	}

	/**
	 *
	 * Returns a RandomSet for the given country if there is one defined for it.
	 * Used in conjunction with worldMapRegionFails problem pattern.
	 *
	 * @author cwat-hgrining
	 * @return
	 */
	protected Map<String, RandomSet<Bandwidth>> createBandwidthByCountry() {
		Map<String, RandomSet<Bandwidth>> res = new HashMap<String, RandomSet<Bandwidth>>();
		AbstractUEMLoadSession.addCountriesBandwidth(res);
		return res;
	}

	/**
	 *
	 * @return
	 * @author peter.lang
	 */
	protected RandomSet<MobileDeviceType> createMobileDevices() {
		return MobileDeviceDistribution.createDefaultMobileDeviceDistribution();
	}

	protected RandomSet<String> createConnectionTypes() {
		return ConnectionTypeDistribution.createDefaultConnectionTypeDistribution();
	}

	protected RandomSet<BrowserWindowSize> createBrowserWindowSize(){
		return BrowserWindowSizeDistribution.createDefaulBrowserWindowSize();
	}

	protected RandomSet<BrowserWindowSize> createMobileBrowserWindowSize(){
		return BrowserWindowSizeDistribution.createDefaulMobileBrowserWindowSize();
	}

	private Map<String, Integer> createDnsSlowdownFactorByContinent() {
		Map<String, Integer> res = new HashMap<String, Integer>();
		AbstractUEMLoadSession.addDNSSlowdownByContinent(res);
		return res;
	}

	private Map<String, Integer> createDnsSlowdownFactorByCountry() {
		Map<String, Integer> res = new HashMap<String, Integer>();
		AbstractUEMLoadSession.addDNSSlowdownByCountry(res);
		return res;
	}

	protected static String[] getResources(String host, String[] resources, int count) {
		String[] res = new String[count];
		for(int i = 0; i < count; i++) {
			res[i] = host + resources[i % resources.length];
		}
		return res;
	}

	protected static String[] getResources(String[] resources, int count) {
		String[] res = new String[count];
		for(int i = 0; i < count; i++) {
			res[i] = resources[i % resources.length];
		}
		return res;
	}

	public Visit getRandomVisit(Location location) {
		if(location.isRushHourNow()) {
			return rushHourVisits.getNext();
		}

		if(location.getCountry() != null) {
			IterableSet<Visit> set = visitsByCountry.get(location.getCountry());
			if(set != null) {
				return set.getNext();
			}
		}
		if(location.getContinent() != null) {
			IterableSet<Visit> set = visitsByContinent.get(location.getContinent());
			if(set != null) {
				return set.getNext();
			}
		}
		return visits.getNext();
	}

	public Visit getRandomAnonymousVisit(){
		if(anonymousVisits == null){
			anonymousVisits = createAnonymousVisits();
		}
		return anonymousVisits.getNext();
	}

	public Location getRandomLocation(){
		return locations.get();
	}

	public Bandwidth getRandomBandwidth(Location location) {
		if(location.getCountry() != null && bandwidthByCountry != null && !bandwidthByCountry.isEmpty()) {
			RandomSet<Bandwidth> set = bandwidthByCountry.get(location.getCountry());
			if(set != null) {
				return set.getRandom();
			}
		}
		if(location.getContinent() != null) {
			RandomSet<Bandwidth> set = bandwidthByContinent.get(location.getContinent());
			if(set != null) {
				return set.getRandom();
			}
		}
		return bandwidth.getRandom();
	}

	public ExtendedCommonUser getRandomUser(String country) {
		return getRandomUser(country, false, false);
	}

	public ExtendedCommonUser getRandomUser(String country, boolean isRuxit){
		return getRandomUser(country, isRuxit, false);
	}
	
	public ExtendedCommonUser getRandomUser(String country, boolean isRuxit, boolean canBeSpecialUser){
		ExtendedCommonUser user = null;
		int i = 0;
		
		do {
			i++;
			user = basicGetRandomUser(country, isRuxit);
		}
		while(!user.isUserGood(canBeSpecialUser) && i <10 );
		
		if(!user.isUserGood(canBeSpecialUser)) {
			user = ExtendedDemoUser.MARIA_USER;
		}
		
		return user;
	}
	
	private ExtendedCommonUser basicGetRandomUser(String country, boolean isRuxit){
		ExtendedCommonUser user = null;

		if(country != null && UserDistribution.getUsers().get(country) != null) {
			user = UserDistribution.getUsers().get(country).getNext();

			if(!isRuxit && !user.getMobileDevice().isDeviceAppmonSupported()) {
				user = UserDistribution.getDemoUsers().getNext();
			}

			return user;
		}

		return UserDistribution.getDemoUsers().getNext();
	}

	public Location getLocation(ExtendedCommonUser user){
		Location location = user.getLocation();
		if(location != null){
			return location;
		}
		return getRandomLocation();
	}

	public BrowserType getBrowser(ExtendedCommonUser user){
		BrowserType bt = user.getRandomDesktopBrowser();
		if(bt != null){
			return user.getRandomDesktopBrowser();
		}
		return getRandomDesktopBrowser();
	}

	public BrowserWindowSize getBrowserWindowsSize(ExtendedCommonUser user){
		BrowserWindowSize bws = user.getDesktopBrowserWindowSize();
		if(bws != null){
			return bws;
		}
		return getRandomBrowserWindowSize();
	}

	public Bandwidth getBandwidth(ExtendedCommonUser user){
		Bandwidth bandWidth = user.getBandwidth();
		if(bandWidth != null){
			return bandWidth;
		}
		return getRandomBandwidth();
	}

	public VisitorInfo getVisitorInfo(ExtendedCommonUser user){
		VisitorInfo visitorInfo = user.getVisitorInfo();
		if(visitorInfo != null){
			visitorInfo.decreaseNumberOfReturns();
			return visitorInfo;
		}
		return new VisitorInfo(false);
	}

	public Integer getDNSSlowdownFactor(ExtendedCommonUser user){
		Integer slowdown = user.getDNSSlowdown();
		if(slowdown != null){
			return slowdown;
		}
		return 1;
	}

	public MobileDeviceType getMobileDevice(ExtendedCommonUser user){
		MobileDeviceType mobileDeviceType = user.getMobileDevice();
		if(mobileDeviceType != null){
			return mobileDeviceType;
		}
		return getRandomMobileDevice();
	}

	public BrowserType getRandomDesktopBrowser(){
		return desktopBrowsers.getRandom();
	}

	public BrowserWindowSize getRandomBrowserWindowSize() {
		return this.browserwindowsize.getRandom();
	}

	public Bandwidth getRandomBandwidth() {
		return bandwidth.getRandom();
	}

	public MobileDeviceType getRandomMobileDevice() {
		return mobileDevices.getRandom();
	}

	public BrowserType getRandomSyntheticBrowser(){
		return syntheticBrowsers.getRandom();
	}

	public BrowserType getRandomRobotBrowser(){
		return robotBrowsers.getRandom();
	}

	public BrowserType getRandomDesktopBrowser(Location location) {
		if(location.getCountry() != null) {
			RandomSet<BrowserType> set = desktopBrowsersByCountry.get(location.getCountry());
			if(set != null) {
				return set.getRandom();
			}
		}
		if(location.getContinent() != null) {
			RandomSet<BrowserType> set = desktopBrowsersByContinent.get(location.getContinent());
			if(set != null) {
				return set.getRandom();
			}
		}
		return desktopBrowsers.getRandom();
	}

	public BrowserType getRandomMobileBrowser(Location location) {
		if (location.getCountry() != null) {
			RandomSet<BrowserType> set = mobileBrowsersByCountry.get(location.getCountry());
			if (set != null) {
				return set.getRandom();
			}
		}
		if (location.getContinent() != null) {
			RandomSet<BrowserType> set = mobileBrowsersByContinent.get(location.getContinent());
			if (set != null) {
				return set.getRandom();
			}
		}
		return mobileBrowsers.getRandom();
	}

	public String getRandomConnectionType() {
		return connectionType.getRandom();
	}

	public int getDNSSlowdownFactor(Location location) {
		if(location.getCountry() != null) {
			Integer factor = dnsSlowdownFactorByCountry.get(location.getCountry());
			if(factor != null) {
				return factor;
			}
		}
		if(location.getContinent() != null) {
			Integer factor = dnsSlowdownFactorByContinent.get(location.getContinent());
			if(factor != null) {
				return factor;
			}
		}
		return 1; // 1 is the default
	}

	public BrowserWindowSize getRandomMobileBrowserWindowSize() {
		return this.mobilebrowserwindowsize.getRandom();
	}

	protected String getName() {
		return "{no name assigned}:";
	}

	public void setLoad(int load) {}
	public int getLoad() {
		return 0;
	}
}