package com.dynatrace.diagnostics.uemload;

import com.dynatrace.diagnostics.uemload.mobile.MobileDeviceType;
import com.dynatrace.easytravel.misc.LoyaltyStatus;

/**
 * Extended traffic parameters for demo users.
 *
 * @author Michal.Bakula
 *
 */

public final class ExtendedDemoUser {




	private static final DemoUserData HAINER_DEMOUSER = DemoUserData.HAINER_USER;
	private static final RandomSet<BrowserType> HAINER_BROWSERS = getBrowsers(HAINER_DEMOUSER);
	private static final VisitorInfo HAINER_VISITOR_INFO = new VisitorInfo("1554736525761OP9PRTQQDGRBAQQ6AM0PBTTJT9PTPB41");

	private static final DemoUserData MONICA_DEMOUSER = DemoUserData.MONICA_USER;
	private static final RandomSet<BrowserType> MONICA_BROWSERS = getBrowsers(MONICA_DEMOUSER);
	private static final VisitorInfo MONICA_VISITOR_INFO = new VisitorInfo("1487753868791R6P0K9F0FEUTG6EQ54U3WNVL6A2KNE9N");

	private static final DemoUserData MARIA_DEMOUSER = DemoUserData.MARIA_USER;
	private static final RandomSet<BrowserType> MARIA_BROWSERS = getBrowsers(MARIA_DEMOUSER);
	private static final VisitorInfo MARIA_VISITOR_INFO = new VisitorInfo("1487753868791FXEQ0XERF7ZE9YVK48I2FFKURFUKWPNK");

	private static final Location DEMOUSER_LOCATION = new Location("North America", "United States", "204.97.208.18", -7);
	private static final RandomSet<BrowserType> DEMOUSER_BROWSERS = getBrowsers(BrowserType.CHROME_40);
	private static final VisitorInfo DEMOUSER_VISITOR_INFO = new VisitorInfo("14877538687910QDTC5ATUAGHRWXL42FIWL1EAPQ9HAZU");

	private static final Location DEMOUSER2_LOCATION = new Location("Europe", "United Kingdom", "78.32.12.144", 0);
	private static final RandomSet<BrowserType> DEMOUSER2_BROWSERS = getBrowsers(BrowserType.FF_360);
	private static final VisitorInfo DEMOUSER2_VISITOR_INFO = new VisitorInfo("148775386879278N2OQPBGK82DDQHC7G3PIE5IKW6UB0D");

	private static final DemoUserData GEORGE_DEMOUSER = DemoUserData.GEORGE_USER;
	private static final RandomSet<BrowserType> GEORGE_BROWSERS = getBrowsers(GEORGE_DEMOUSER);
	private static final VisitorInfo GEORGE_VISITOR_INFO = new VisitorInfo("1554736525761OP9PRTQQDGRBAQQ6AM0PBTTJT9PTPB41");

	private static final int DEMOUSER_WEIGHT = 1000;

	private ExtendedDemoUser() {
		throw new IllegalAccessError("Utility class");
	}

	public static final ExtendedCommonUser HAINER_USER = new ExtendedCommonUser.ExtendedCommonUserBuilder(HAINER_DEMOUSER.getName(), HAINER_DEMOUSER.getFullName(), LoyaltyStatus.NONE, HAINER_DEMOUSER.getName(), DEMOUSER_WEIGHT)
			.setLocation(HAINER_DEMOUSER.getLocation())
			.setDesktopBrowsers(HAINER_BROWSERS)
			.setMobileDevice(HAINER_DEMOUSER.getMobileDevice())
			.setMobileBrowser(BrowserType.MOBILE_SAFARI_10)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(HAINER_DEMOUSER.getBrowserWindowSize())
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(HAINER_VISITOR_INFO)
			.setMobileDeviceId(128)
			.build();

	public static final ExtendedCommonUser MONICA_USER = new ExtendedCommonUser.ExtendedCommonUserBuilder(MONICA_DEMOUSER.getName(), MONICA_DEMOUSER.getFullName(), LoyaltyStatus.NONE, MONICA_DEMOUSER.getName(), DEMOUSER_WEIGHT)
			.setLocation(MONICA_DEMOUSER.getLocation())
			.setDesktopBrowsers(MONICA_BROWSERS)
			.setMobileDevice(MONICA_DEMOUSER.getMobileDevice())
			.setMobileBrowser(BrowserType.MOBILE_SAFARI_10)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(MONICA_DEMOUSER.getBrowserWindowSize())
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(MONICA_VISITOR_INFO)
			.setMobileDeviceId(129)
			.build();

	public static final ExtendedCommonUser MARIA_USER = new ExtendedCommonUser.ExtendedCommonUserBuilder(MARIA_DEMOUSER.getName(), MARIA_DEMOUSER.getFullName(), LoyaltyStatus.NONE, MARIA_DEMOUSER.getName(), DEMOUSER_WEIGHT)
			.setLocation(MARIA_DEMOUSER.getLocation())
			.setDesktopBrowsers(MARIA_BROWSERS)
			.setMobileDevice(MARIA_DEMOUSER.getMobileDevice())
			.setMobileBrowser(BrowserType.SAMSUNG_GALAXY_S7)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(MARIA_DEMOUSER.getBrowserWindowSize())
			.setMobileBrowserWindowSize(BrowserWindowSize._m2560x1440)
			.setDnsSlowdow(1)
			.setVisitorInfo(MARIA_VISITOR_INFO)
			.setMobileDeviceId(130)
			.build();

	public static final ExtendedCommonUser DEMOUSER = new ExtendedCommonUser.ExtendedCommonUserBuilder("demouser", "demouser", LoyaltyStatus.GOLD, "demopass", DEMOUSER_WEIGHT)
			.setLocation(DEMOUSER_LOCATION)
			.setDesktopBrowsers(DEMOUSER_BROWSERS)
			.setMobileDevice(MobileDeviceType.HUAWEI_P10)
			.setMobileBrowser(BrowserType.HUAWEI_P10)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1920x1080)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(DEMOUSER_VISITOR_INFO)
			.setMobileDeviceId(131)
			.build();

	public static final ExtendedCommonUser DEMOUSER2 = new ExtendedCommonUser.ExtendedCommonUserBuilder("demouser2", "demouser2", LoyaltyStatus.PLATINUM, "demouser2", DEMOUSER_WEIGHT)
			.setLocation(DEMOUSER2_LOCATION)
			.setDesktopBrowsers(DEMOUSER2_BROWSERS)
			.setMobileDevice(MobileDeviceType.HTC_DESIRE_650)
			.setMobileBrowser(BrowserType.SAFARI_10)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1920x1200)
			.setMobileBrowserWindowSize(BrowserWindowSize._m2560x1440)
			.setDnsSlowdow(1)
			.setVisitorInfo(DEMOUSER2_VISITOR_INFO)
			.setMobileDeviceId(132)
			.build();

	public static ExtendedCommonUser getUserWithGivenMobileDevice(MobileDeviceType mobileDeviceType) {
		return new ExtendedCommonUser.ExtendedCommonUserBuilder("demouser", "demouser", LoyaltyStatus.GOLD, "demopass", DEMOUSER_WEIGHT)
			.setLocation(DEMOUSER_LOCATION)
			.setDesktopBrowsers(DEMOUSER_BROWSERS)
			.setMobileDevice(mobileDeviceType)
			.setMobileBrowser(BrowserType.HUAWEI_P10)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1920x1080)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(DEMOUSER_VISITOR_INFO)
			.setMobileDeviceId(133)
			.build();
	}

	public static final ExtendedCommonUser GEORGE_USER = new ExtendedCommonUser.ExtendedCommonUserBuilder(GEORGE_DEMOUSER.getName(), GEORGE_DEMOUSER.getFullName(), LoyaltyStatus.NONE, GEORGE_DEMOUSER.getName(), DEMOUSER_WEIGHT)
			.setLocation(GEORGE_DEMOUSER.getLocation())
			.setDesktopBrowsers(GEORGE_BROWSERS)
			.setMobileDevice(GEORGE_DEMOUSER.getMobileDevice())
			.setMobileBrowser(BrowserType.MOBILE_SAFARI_10)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(GEORGE_DEMOUSER.getBrowserWindowSize())
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(GEORGE_VISITOR_INFO)
			.setMobileDeviceId(134)
			.build();

	public static final ExtendedCommonUser WEEKLY_USER_1 = new ExtendedCommonUser.ExtendedCommonUserBuilder(
			"afrodyta", "Afrodyta Sachowich", LoyaltyStatus.PLATINUM, "afrodyta", DEMOUSER_WEIGHT)
			.setLocation(new Location("Oceania", "Australia", "118.67.60.29", 1))
			.setDesktopBrowsers(getBrowsers(BrowserType.SAFARI_10))
			.setMobileDevice(MobileDeviceType.IPHONE_XR)
			.setMobileBrowser(BrowserType.MOBILE_SAFARI_10)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1024x768)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(new VisitorInfo("16239300974845XVIWUQUMYXJCEEQX0AES2P6T8A69O8K"))
			.setMobileDeviceId(151)
			.build();
	
	public static final ExtendedCommonUser WEEKLY_USER_2 = new ExtendedCommonUser.ExtendedCommonUserBuilder(
			"agni", "Agni Weiho", LoyaltyStatus.GOLD, "agni", DEMOUSER_WEIGHT)
			.setLocation(new Location("Europe", "United Kingdom", "86.16.167.7", 1))
			.setDesktopBrowsers(getBrowsers(BrowserType.CHROME_56))
			.setMobileDevice(MobileDeviceType.SAMSUNG_GALAXY_S5)
			.setMobileBrowser(BrowserType.MOBILE_CHROME_56)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1024x768)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(new VisitorInfo("1623930097485VIANVI3055NKO9PG7RIC5FOCOPPRQJMZ"))
			.setMobileDeviceId(151)
			.build();
	
	public static final ExtendedCommonUser WEEKLY_USER_3 = new ExtendedCommonUser.ExtendedCommonUserBuilder(
			"qiao", "Qiao Kuchoo", LoyaltyStatus.SILVER, "qiao", DEMOUSER_WEIGHT)
			.setLocation(new Location("Europe", "Italy", "87.19.3.163", 1))
			.setDesktopBrowsers(getBrowsers(BrowserType.SAFARI_4))
			.setMobileDevice(MobileDeviceType.IPHONE_6)
			.setMobileBrowser(BrowserType.MOBILE_SAFARI_4)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1024x768)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(new VisitorInfo("16239300974862OCA2VO5O9VICDTT0TV6HOAN8IPORB1P"))
			.setMobileDeviceId(151)
			.build();
	
	public static final ExtendedCommonUser WEEKLY_USER_4 = new ExtendedCommonUser.ExtendedCommonUserBuilder(
			"eliott", "Eliott Rashi", LoyaltyStatus.NONE, "eliott", DEMOUSER_WEIGHT)
			.setLocation(new Location("Europe", "Sweden", "137.60.124.181", 1))
			.setDesktopBrowsers(getBrowsers(BrowserType.CHROME_39))
			.setMobileDevice(MobileDeviceType.SAMSUNG_GALAXY_S7)
			.setMobileBrowser(BrowserType.MOBILE_CHROME_36)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1024x768)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(new VisitorInfo("16239300974878TAZEP9STA0RUP2YBDF6GNXNHQ8X6XJW"))
			.setMobileDeviceId(151)
			.build();
	
	public static final ExtendedCommonUser WEEKLY_USER_5 = new ExtendedCommonUser.ExtendedCommonUserBuilder(
			"rashiado", "Rashiado Okono", LoyaltyStatus.PLATINUM, "rashiado", DEMOUSER_WEIGHT)
			.setLocation(new Location("Asia", "China", "180.223.188.197", 1))
			.setDesktopBrowsers(getBrowsers(BrowserType.SAFARI_8))
			.setMobileDevice(MobileDeviceType.IPHONE_8)
			.setMobileBrowser(BrowserType.MOBILE_SAFARI_8)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1024x768)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(new VisitorInfo("1623930097488JU25HNUTZSRWJ8XPLD6D77SS6KPT9914"))
			.setMobileDeviceId(151)
			.build();
	
	public static final ExtendedCommonUser WEEKLY_USER_6 = new ExtendedCommonUser.ExtendedCommonUserBuilder(
			"anna", "Anna Jinji", LoyaltyStatus.GOLD, "anna", DEMOUSER_WEIGHT)
			.setLocation(new Location("Asia", "Taiwan", "111.241.211.90", 1))
			.setDesktopBrowsers(getBrowsers(BrowserType.CHROME_42))
			.setMobileDevice(MobileDeviceType.HTC_U12_LIFE)
			.setMobileBrowser(BrowserType.CHROME_42)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1024x768)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(new VisitorInfo("16239300974897YNHFZN1SQ7GNIQTBDSVXJ3K8M0ABXH8"))
			.setMobileDeviceId(151)
			.build();
	
	public static final ExtendedCommonUser WEEKLY_USER_7 = new ExtendedCommonUser.ExtendedCommonUserBuilder(
			"annelia", "Annelia Wagelia", LoyaltyStatus.SILVER, "annelia", DEMOUSER_WEIGHT)
			.setLocation(new Location("North America", "Canada", "71.19.36.234", 1))
			.setDesktopBrowsers(getBrowsers(BrowserType.SAFARI_8))
			.setMobileDevice(MobileDeviceType.IPHONE_XR)
			.setMobileBrowser(BrowserType.MOBILE_SAFARI_8)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1024x768)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(new VisitorInfo("16239300974903P4WYGGJ86NNHPBRUHV3IH9WFL39EPXF"))
			.setMobileDeviceId(151)
			.build();
	
	public static final ExtendedCommonUser WEEKLY_USER_8 = new ExtendedCommonUser.ExtendedCommonUserBuilder(
			"oidi", "Oidi Mando", LoyaltyStatus.NONE, "oidi", DEMOUSER_WEIGHT)
			.setLocation(new Location("North America", "United States", "44.151.3.43", 1))
			.setDesktopBrowsers(getBrowsers(BrowserType.CHROME_53))
			.setMobileDevice(MobileDeviceType.SAMSUNG_GALAXY_S6)
			.setMobileBrowser(BrowserType.MOBILE_CHROME_53)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1024x768)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(new VisitorInfo("1623930097491NLJ2TZNXQTGLG9G57M183MFWMK5BLKUX"))
			.setMobileDeviceId(151)
			.build();
	
	public static final ExtendedCommonUser WEEKLY_USER_9 = new ExtendedCommonUser.ExtendedCommonUserBuilder(
			"armi", "Armi Cruzo", LoyaltyStatus.PLATINUM, "armi", DEMOUSER_WEIGHT)
			.setLocation(new Location("South America", "Brazil", "201.95.76.167", 1))
			.setDesktopBrowsers(getBrowsers(BrowserType.SAFARI_10))
			.setMobileDevice(MobileDeviceType.IPHONE_X)
			.setMobileBrowser(BrowserType.MOBILE_SAFARI_10)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1024x768)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(new VisitorInfo("1623930097492JR0557AFZGOM1NA4T407A49PGDZL0ADP"))
			.setMobileDeviceId(151)
			.build();
	
	public static final ExtendedCommonUser WEEKLY_USER_10 = new ExtendedCommonUser.ExtendedCommonUserBuilder(
			"arriva", "Arriva Kalejaja", LoyaltyStatus.GOLD, "arriva", DEMOUSER_WEIGHT)
			.setLocation(new Location("Africa", "South Africa", "41.122.75.67", 1))
			.setDesktopBrowsers(getBrowsers(BrowserType.CHROME_48))
			.setMobileDevice(MobileDeviceType.SONY_XPERIA_Z5)
			.setMobileBrowser(BrowserType.MOBILE_CHROME_48)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1024x768)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(new VisitorInfo("1623930097493FH2MACXU9UZDYMVBP9VW2YRNVUX53G1Z"))
			.setMobileDeviceId(151)
			.build();
	
	public static final ExtendedCommonUser WEEKLY_USER_11 = new ExtendedCommonUser.ExtendedCommonUserBuilder(
			"aso", "Aso Erdya", LoyaltyStatus.SILVER, "aso", DEMOUSER_WEIGHT)
			.setLocation(new Location("South America", "Colombia", "201.228.220.5", 1))
			.setDesktopBrowsers(getBrowsers(BrowserType.SAFARI_8))
			.setMobileDevice(MobileDeviceType.IPHONE_7_PLUS)
			.setMobileBrowser(BrowserType.MOBILE_SAFARI_8)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1024x768)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(new VisitorInfo("16239300974945C01K0JA4U8HSEQBRVEUQRORZO4MRFEN"))
			.setMobileDeviceId(151)
			.build();
	
	public static final ExtendedCommonUser MONTHLY_USER_1 = new ExtendedCommonUser.ExtendedCommonUserBuilder(
			"rofiq", "Rofiq Guagg", LoyaltyStatus.PLATINUM, "rofiq", DEMOUSER_WEIGHT)
			.setLocation(new Location("Oceania", "New Zealand", "60.234.208.73", 1))
			.setDesktopBrowsers(getBrowsers(BrowserType.CHROME_45))
			.setMobileDevice(MobileDeviceType.SAMSUNG_GALAXY_S8)
			.setMobileBrowser(BrowserType.MOBILE_CHROME_55)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1024x768)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(new VisitorInfo("1623930098495NUYUH0OMHZ8E75Z7O0QOIG18BDPXT3JZ"))
			.setMobileDeviceId(150)
			.build();
	
	public static final ExtendedCommonUser MONTHLY_USER_2 = new ExtendedCommonUser.ExtendedCommonUserBuilder(
			"baisa", "Baisa Giesow", LoyaltyStatus.GOLD, "baisa", DEMOUSER_WEIGHT)
			.setLocation(new Location("Europe", "Germany", "85.183.222.158", 1))
			.setDesktopBrowsers(getBrowsers(BrowserType.SAFARI_5))
			.setMobileDevice(MobileDeviceType.IPHONE_XR)
			.setMobileBrowser(BrowserType.MOBILE_SAFARI_5)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1024x768)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(new VisitorInfo("1623930098496DQEQ2ALPJAVE364DM75EU3IOUU8FMDMM"))
			.setMobileDeviceId(150)
			.build();
	
	public static final ExtendedCommonUser MONTHLY_USER_3 = new ExtendedCommonUser.ExtendedCommonUserBuilder(
			"odill", "Odill Rui", LoyaltyStatus.SILVER, "odill", DEMOUSER_WEIGHT)
			.setLocation(new Location("Europe", "Netherlands", "145.66.150.124", 1))
			.setDesktopBrowsers(getBrowsers(BrowserType.CHROME_42))
			.setMobileDevice(MobileDeviceType.SAMSUNG_GALAXY_S6_EDGE_PLUS)
			.setMobileBrowser(BrowserType.MOBILE_CHROME_48)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1024x768)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(new VisitorInfo("1623930098497S97E3I5XJXA1S7U35368OG9L9ENM1RYB"))
			.setMobileDeviceId(150)
			.build();
	
	public static final ExtendedCommonUser MONTHLY_USER_4 = new ExtendedCommonUser.ExtendedCommonUserBuilder(
			"roosevi", "Roosevi Keiwo", LoyaltyStatus.NONE, "roosevi", DEMOUSER_WEIGHT)
			.setLocation(new Location("Europe", "Poland", "31.175.115.131", 1))
			.setDesktopBrowsers(getBrowsers(BrowserType.SAFARI_9))
			.setMobileDevice(MobileDeviceType.IPHONE_8)
			.setMobileBrowser(BrowserType.MOBILE_SAFARI_9)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1024x768)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(new VisitorInfo("16239300984984Z1MO1NLWQJ4X2GLQAXWI20GXWZN622T"))
			.setMobileDeviceId(150)
			.build();
	
	public static final ExtendedCommonUser MONTHLY_USER_5 = new ExtendedCommonUser.ExtendedCommonUserBuilder(
			"barbra", "Barbra Peox", LoyaltyStatus.PLATINUM, "barbra", DEMOUSER_WEIGHT)
			.setLocation(new Location("Asia", "Japan", "59.85.108.121", 1))
			.setDesktopBrowsers(getBrowsers(BrowserType.CHROME_54))
			.setMobileDevice(MobileDeviceType.HUAWEI_P8_LITE)
			.setMobileBrowser(BrowserType.MOBILE_CHROME_54)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1024x768)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(new VisitorInfo("1623930098499OB32S2SV28F075I20AQ3YQX3CP6L8P3Z"))
			.setMobileDeviceId(150)
			.build();
	
	public static final ExtendedCommonUser MONTHLY_USER_6 = new ExtendedCommonUser.ExtendedCommonUserBuilder(
			"gag", "Gag Jia", LoyaltyStatus.GOLD, "gag", DEMOUSER_WEIGHT)
			.setLocation(new Location("Asia", "India", "59.94.24.101", 1))
			.setDesktopBrowsers(getBrowsers(BrowserType.SAFARI_10))
			.setMobileDevice(MobileDeviceType.IPHONE_XR)
			.setMobileBrowser(BrowserType.MOBILE_SAFARI_10)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1024x768)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(new VisitorInfo("1623930098500EILU7PAMKX7EH4L88U98KWG5JMS2FJUG"))
			.setMobileDeviceId(150)
			.build();
	
	public static final ExtendedCommonUser MONTHLY_USER_7 = new ExtendedCommonUser.ExtendedCommonUserBuilder(
			"mir", "Mir Hayaka", LoyaltyStatus.SILVER, "mir", DEMOUSER_WEIGHT)
			.setLocation(new Location("North America", "Mexico", "187.208.111.83", 1))
			.setDesktopBrowsers(getBrowsers(BrowserType.CHROME_37))
			.setMobileDevice(MobileDeviceType.SAMSUNG_GALAXY_S5)
			.setMobileBrowser(BrowserType.MOBILE_CHROME_36)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1024x768)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(new VisitorInfo("1623930098501PV2TXNKLK5TOAM856EPDJPJQEHBKGLM4"))
			.setMobileDeviceId(150)
			.build();
	
	public static final ExtendedCommonUser MONTHLY_USER_8 = new ExtendedCommonUser.ExtendedCommonUserBuilder(
			"biatao", "Biatao Shinji", LoyaltyStatus.NONE, "biatao", DEMOUSER_WEIGHT)
			.setLocation(new Location("North America", "United States", "208.88.71.104", 1))
			.setDesktopBrowsers(getBrowsers(BrowserType.SAFARI_7))
			.setMobileDevice(MobileDeviceType.IPHONE_7)
			.setMobileBrowser(BrowserType.MOBILE_SAFARI_7)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1024x768)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(new VisitorInfo("1623930098502CICDQ44MWNZCCKY6IGX3W0O6ELU9KJ1Y"))
			.setMobileDeviceId(150)
			.build();
	
	public static final ExtendedCommonUser MONTHLY_USER_9 = new ExtendedCommonUser.ExtendedCommonUserBuilder(
			"carlos", "Carlos Duto", LoyaltyStatus.PLATINUM, "carlos", DEMOUSER_WEIGHT)
			.setLocation(new Location("South America", "Argentina", "190.173.188.194", 1))
			.setDesktopBrowsers(getBrowsers(BrowserType.CHROME_56))
			.setMobileDevice(MobileDeviceType.XIAOMI_MI_5)
			.setMobileBrowser(BrowserType.MOBILE_CHROME_56)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1024x768)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(new VisitorInfo("1623930098503ZGRVQ0F7P7JH97LAHQP13JKM4JMHAFLZ"))
			.setMobileDeviceId(150)
			.build();
	
	public static final ExtendedCommonUser MONTHLY_USER_10 = new ExtendedCommonUser.ExtendedCommonUserBuilder(
			"komami", "Komami Coriano", LoyaltyStatus.GOLD, "komami", DEMOUSER_WEIGHT)
			.setLocation(new Location("South America", "Peru", "190.40.238.189", 1))
			.setDesktopBrowsers(getBrowsers(BrowserType.SAFARI_5))
			.setMobileDevice(MobileDeviceType.IPHONE_XR)
			.setMobileBrowser(BrowserType.MOBILE_SAFARI_5)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1024x768)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(new VisitorInfo("16239300985044H9PAXGIRBSI5UYV6ZPPXD718PZEXMWW"))
			.setMobileDeviceId(150)
			.build();
	
	public static final ExtendedCommonUser MONTHLY_USER_11 = new ExtendedCommonUser.ExtendedCommonUserBuilder(
			"ussu", "Ussu Hirodai", LoyaltyStatus.SILVER, "ussu", DEMOUSER_WEIGHT)
			.setLocation(new Location("Africa", "Egypt", "45.104.213.1", 1))
			.setDesktopBrowsers(getBrowsers(BrowserType.CHROME_57))
			.setMobileDevice(MobileDeviceType.SAMSUNG_GALAXY_S6_EDGE_PLUS)
			.setMobileBrowser(BrowserType.MOBILE_CHROME_57)
			.setBandwidth(Bandwidth.BROADBAND)
			.setDesktopBrowserWindowSize(BrowserWindowSize._1024x768)
			.setMobileBrowserWindowSize(BrowserWindowSize._m1920x1080)
			.setDnsSlowdow(1)
			.setVisitorInfo(new VisitorInfo("16239300985052G6OGC8Z7DVBQCRAZP4JIODCUI01DO8D"))
			.setMobileDeviceId(150)
			.build();
	
	private static RandomSet<BrowserType> getBrowsers(DemoUserData user){
		RandomSet<BrowserType> rsb = new RandomSet<>();
		rsb.add(user.getBrowser(), 1);
		return rsb;
	}

	private static RandomSet<BrowserType> getBrowsers(BrowserType browser){
		RandomSet<BrowserType> rsb = new RandomSet<>();
		rsb.add(browser, 1);
		return rsb;
	}
}
