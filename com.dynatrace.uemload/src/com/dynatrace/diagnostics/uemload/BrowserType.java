package com.dynatrace.diagnostics.uemload;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public final class BrowserType {
	/* see table in http://en.wikipedia.org/wiki/Windows_NT */
	private static final String WINDOWS_XP = "Windows NT 5.1";
	private static final String WINDOWS_VISTA = "Windows NT 6.0";
	private static final String WINDOWS_7 = "Windows NT 6.1";
	private static final String WINDOWS_8 = "Windows NT 6.2";
	private static final String WINDOWS_8_1 = "Windows NT 6.3";
	private static final String WINDOWS_10 = "Windows NT 10.0";

	private static final String MAC_OSX_10_6 = "Mac OS X 10_6";
	private static final String MAC_OSX_10_7 = "Mac OS X 10_7";
	private static final String MAC_OSX_10_8 = "Mac OS X 10_8";
	private static final String MAC_OSX_10_9 = "Mac OS X 10_9";
	private static final String MAC_OSX_10_10 = "Mac OS X 10_10";
	private static final String MAC_OSX_10_11 = "Mac OS X 10_11";
	private static final String MAC_OSX_10_12 = "Mac OS X 10_12";

	public enum BrowserFamily {
		IE, Firefox, Chrome, Safari, Opera, Robot, Mobile, Unknown, Other;
	}

	public static final BrowserType RUXIT_SYNTHETIC = createRuxitSyntheticBrowser();	
	
	/*
	 * Internet Explorer & EDGE
	 */
	public static final BrowserType IE_6 = createInternetExplorer(6, WINDOWS_XP, 0.5, false, false);
	public static final BrowserType IE_7 = createInternetExplorer(7, WINDOWS_VISTA, 0.6, false, false);
	public static final BrowserType IE_8 = createInternetExplorer8(WINDOWS_7, 0.8);
	public static final BrowserType IE_9 = createInternetExplorer(9, WINDOWS_7, 0.9, false, false);
	public static final BrowserType IE_10 = createInternetExplorer(10, WINDOWS_8, 1.0, true, false);
	public static final BrowserType IE_11 = new BrowserType(
			"Mozilla/5.0 (Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko", true, 1.2, BrowserFamily.IE, true, false);
	public static final BrowserType EDGE_12 = createEdge(12, WINDOWS_10, "92.0", 1.6, true, false);
	public static final BrowserType EDGE_13 = createEdge(13, WINDOWS_10, "97.0", 1.7, true, false);
	public static final BrowserType EDGE_14 = createEdge(14, WINDOWS_10, "102.0", 1.8, true, false);			
	
	/*
	 * Modzilla Firefox
	 */
	public static final BrowserType FF_360 = createFirefox("36.0", WINDOWS_8, 1.9, true, true);
	public static final BrowserType FF_380 = createFirefox("38.0", WINDOWS_XP, 1.7, true, true);
	public static final BrowserType FF_390 = createFirefox("39.0", WINDOWS_XP, 1.7, true, true);
	public static final BrowserType FF_400 = createFirefox("40.0", WINDOWS_VISTA, 1.7, true, true);
	public static final BrowserType FF_410 = createFirefox("41.0", WINDOWS_7, 1.8, true, true);
	public static final BrowserType FF_420 = createFirefox("42.0", WINDOWS_7, 1.8, true, true);
	public static final BrowserType FF_430 = createFirefox("43.0", WINDOWS_7, 1.8, true, true);
	public static final BrowserType FF_440 = createFirefox("44.0", WINDOWS_7, 1.8, true, true);
	public static final BrowserType FF_450 = createFirefox("45.0", WINDOWS_7, 1.9, true, true);
	public static final BrowserType FF_460 = createFirefox("46.0", WINDOWS_7, 1.9, true, true);
	public static final BrowserType FF_470 = createFirefox("47.0", WINDOWS_7, 1.9, true, true);
	public static final BrowserType FF_480 = createFirefox("48.0", WINDOWS_7, 1.9, true, true);
	public static final BrowserType FF_490 = createFirefox("49.0", WINDOWS_8, 1.9, true, true);
	public static final BrowserType FF_500 = createFirefox("50.0", WINDOWS_8_1, 1.9, true, true);
	public static final BrowserType FF_510 = createFirefox("51.0", WINDOWS_8_1, 1.9, true, true);
	public static final BrowserType FF_520 = createFirefox("52.0", WINDOWS_10, 2, true, true);
	public static final BrowserType FF_530 = createFirefox("53.0", WINDOWS_10, 2, true, true);

	/*
	 * Opera
	 */
	public static final BrowserType OPERA_35 = createOpera("35.0", "95.0", WINDOWS_VISTA, 1.6, true, true);
	public static final BrowserType OPERA_36 = createOpera("36.0", "96.0", WINDOWS_7, 1.7, true, true);
	public static final BrowserType OPERA_37 = createOpera("37.0", "97.0", WINDOWS_7, 1.8, true, true);
	public static final BrowserType OPERA_38 = createOpera("38.0", "98.0", WINDOWS_7, 1.8, true, true);
	public static final BrowserType OPERA_39 = createOpera("39.0", "99.0", WINDOWS_7, 1.8, true, true);
	public static final BrowserType OPERA_40 = createOpera("40.0", "100.0", WINDOWS_8, 1.8, true, true);
	public static final BrowserType OPERA_41 = createOpera("41.0", "101.0", WINDOWS_8_1, 1.9, true, true);
	public static final BrowserType OPERA_42 = createOpera("42.0", "102.0", WINDOWS_10, 1.9, true, true);
	
	/*
	 * Chrome
	 */
	public static final BrowserType CHROME_36 = createChrome("81.0", WINDOWS_XP, 1.8, true, true);
	public static final BrowserType CHROME_37 = createChrome("82.0", WINDOWS_XP, 1.8, true, true);
	public static final BrowserType CHROME_38 = createChrome("83.0", WINDOWS_VISTA, 1.8, true, true);
	public static final BrowserType CHROME_39 = createChrome("84.0", WINDOWS_7, 2, true, true);
	public static final BrowserType CHROME_40 = createChrome("85.0", WINDOWS_7, 2, true, true);
	public static final BrowserType CHROME_41 = createChrome("86.0", WINDOWS_7, 2, true, true);
	public static final BrowserType CHROME_42 = createChrome("87.0", WINDOWS_7, 2, true, true);
	public static final BrowserType CHROME_43 = createChrome("88.0", WINDOWS_7, 2, true, true);
	public static final BrowserType CHROME_44 = createChrome("89.0", WINDOWS_7, 2, true, true);
	public static final BrowserType CHROME_45 = createChrome("90.0", WINDOWS_7, 2, true, true);
	public static final BrowserType CHROME_46 = createChrome("91.0", WINDOWS_7, 2, true, true);
	public static final BrowserType CHROME_47 = createChrome("92.0", WINDOWS_7, 2, true, true);
	public static final BrowserType CHROME_48 = createChrome("93.0", WINDOWS_7, 2, true, true);
	public static final BrowserType CHROME_49 = createChrome("94.0", WINDOWS_7, 2, true, true);
	public static final BrowserType CHROME_50 = createChrome("95.0", WINDOWS_7, 2, true, true);
	public static final BrowserType CHROME_51 = createChrome("96.0", WINDOWS_8, 2, true, true);
	public static final BrowserType CHROME_52 = createChrome("97.0", WINDOWS_8, 2, true, true);
	public static final BrowserType CHROME_53 = createChrome("98.0", WINDOWS_8_1, 2, true, true);
	public static final BrowserType CHROME_54 = createChrome("99.0", WINDOWS_8_1, 2, true, true);
	public static final BrowserType CHROME_55 = createChrome("100.0", WINDOWS_10, 2.2, true, true);
	public static final BrowserType CHROME_56 = createChrome("101.0", WINDOWS_10, 2.2, true, true);
	public static final BrowserType CHROME_57 = createChrome("102.0", WINDOWS_10, 2.2, true, true);
	
	/*
	 * Safari
	 */
	public static final BrowserType SAFARI_4 = createSafari("4.0", MAC_OSX_10_6, 1.8, false, false);
	public static final BrowserType SAFARI_5 = createSafari("5.0", MAC_OSX_10_7, 1.8, false, false);
	public static final BrowserType SAFARI_6 = createSafari("6.0", MAC_OSX_10_8, 1.8, false, false);
	public static final BrowserType SAFARI_7 = createSafari("7.0", MAC_OSX_10_9, 1.9, false, false);
	public static final BrowserType SAFARI_8 = createSafari("8.0", MAC_OSX_10_10, 1.9, false, false);
	public static final BrowserType SAFARI_9 = createSafari("9.0", MAC_OSX_10_11, 2, false, false);
	public static final BrowserType SAFARI_10 = createSafari("10.0", MAC_OSX_10_12, 2, false, false);
	
	/*
	 * Mobile Chrome
	 */
	public static final BrowserType MOBILE_CHROME_36 = createMobileChrome("92.0", "3.2.6", 0.7);
	public static final BrowserType MOBILE_CHROME_48 = createMobileChrome("93.0", "3.2.6", 0.7);
	public static final BrowserType MOBILE_CHROME_49 = createMobileChrome("94.0", "4.0.4", 0.7);
	public static final BrowserType MOBILE_CHROME_50 = createMobileChrome("95.0", "4.2.7", 0.7);
	public static final BrowserType MOBILE_CHROME_51 = createMobileChrome("96.0", "4.3.0", 0.7);
	public static final BrowserType MOBILE_CHROME_52 = createMobileChrome("97.0", "4.4.4", 0.7);
	public static final BrowserType MOBILE_CHROME_53 = createMobileChrome("98.0", "5.0.1", 0.7);
	public static final BrowserType MOBILE_CHROME_54 = createMobileChrome("99.0", "5.1.1", 0.7);
	public static final BrowserType MOBILE_CHROME_55 = createMobileChrome("100.0", "6.0.0", 0.7);
	public static final BrowserType MOBILE_CHROME_56 = createMobileChrome("101.0", "6.0.1", 0.7);
	public static final BrowserType MOBILE_CHROME_57 = createMobileChrome("102.0", "7.1.1", 0.7);
	public static final BrowserType TABLET_CHROME_54 = createTabletChrome("99.0", "5.1.1", 0.7);
	public static final BrowserType TABLET_CHROME_55 = createTabletChrome("100.0", "6.0.0", 0.7);
	public static final BrowserType TABLET_CHROME_56 = createTabletChrome("101.0", "6.0.1", 0.7);
	public static final BrowserType TABLET_CHROME_57 = createTabletChrome("102.0", "7.1.1", 0.7);
	
	/*
	 * Mobile Safari
	 */
	public static final BrowserType MOBILE_SAFARI_4 = createMobileSafari("4.0", "4_0", 0.6);
	public static final BrowserType MOBILE_SAFARI_5 = createMobileSafari("5.0", "4_3_3", 0.7);
	public static final BrowserType MOBILE_SAFARI_6 = createMobileSafari("6.0", "6_0", 0.8);
	public static final BrowserType MOBILE_SAFARI_7 = createMobileSafari("7.0", "7_0_2", 1);
	public static final BrowserType MOBILE_SAFARI_8 = createMobileSafari("8.0", "8_0_2", 1.2);
	public static final BrowserType MOBILE_SAFARI_9 = createMobileSafari("9.0", "9_0_2", 1.2);
	public static final BrowserType MOBILE_SAFARI_10 = createMobileSafari("10.0", "10_0_2", 1.3);
	public static final BrowserType MOBILE_SAFARI_6_IPAD = createIpadSafari("6.0", "6_0_2", 0.8);
	public static final BrowserType MOBILE_SAFARI_7_IPAD = createIpadSafari("7.0", "7_0_2", 1);
	public static final BrowserType MOBILE_SAFARI_8_IPAD = createIpadSafari("8.0", "8_0_2", 1.1);
	public static final BrowserType MOBILE_SAFARI_9_IPAD = createIpadSafari("9.0", "9_0_2", 1.2);
	public static final BrowserType MOBILE_SAFARI_10_IPAD = createIpadSafari("10.0", "10_0_2", 1.3);
	
	/*
	 * Mobile UC
	 */
	public static final BrowserType MOBILE_UC_11 = createMobileUC("11.2", "7.1.1", 0.8);
	public static final BrowserType MOBILE_UC_10 = createMobileUC("10.10", "5.1.1", 0.7);
	public static final BrowserType MOBILE_UC_9 = createMobileUC("9.6", "4.4.4", 0.6);
	public static final BrowserType TABLET_UC_11 = createTabletUC("11.2", "10_0_2", 0.8);
	public static final BrowserType TABLET_UC_10 = createTabletUC("10.10", "9_0_2", 0.7);
	public static final BrowserType TABLET_UC_9 = createTabletUC("9.6", "8_0_2", 0.6);
	
	/*
	 * Mobile Opera
	 */
	public static final BrowserType MOBILE_OPERA_42 = createMobileOpera("42", "7.1.1", 0.7);
	public static final BrowserType MOBILE_OPERA_41 = createMobileOpera("41", "6.0.1", 0.7);
	public static final BrowserType MOBILE_OPERA_40 = createMobileOpera("40", "5.1.1", 0.7);
	public static final BrowserType MOBILE_OPERA_39 = createMobileOpera("39", "4.4.4", 0.7);
	
	/*
	 * Mobile Android
	 */
	public static final BrowserType ANDROID_2_4 = createMobileAndroid("2.0", "2.4.1", 0.6);
	public static final BrowserType ANDROID_4_1 = createMobileAndroid("4.0", "4.1.1", 0.6);
	public static final BrowserType ANDROID_4_2 = createMobileAndroid("4.0", "4.2.1", 0.6);
	public static final BrowserType ANDROID_4_3 = createMobileAndroid("4.0", "4.3.1", 0.6);
	public static final BrowserType ANDROID_4_4 = createMobileAndroid("4.0", "4.4.4", 0.7);
	public static final BrowserType ANDROID_5_0 = createMobileAndroid("5.0", "5.0.1", 0.7);
	public static final BrowserType ANDROID_5_1 = createMobileAndroid("5.0", "5.1.1", 0.7);
	public static final BrowserType ANDROID_6_0 = createMobileAndroid("6.0", "6.0.1", 0.7);
	public static final BrowserType ANDROID_7_0 = createMobileAndroid("7.0", "7.0.1", 0.8);
	public static final BrowserType ANDROID_7_1 = createMobileAndroid("7.0", "7.1.1", 0.8);
	
	/*
	 * Samsung Internet
	 */
	public static final BrowserType MOBILE_SAMSUNG_3 = createMobileSamsung("3.0", "4.4.4", 0.6);
	public static final BrowserType MOBILE_SAMSUNG_4 = createMobileSamsung("4.0", "5.0.2", 0.7);
	public static final BrowserType MOBILE_SAMSUNG_5 = createMobileSamsung("5.0", "7.1.1", 0.8);
	public static final BrowserType TABLET_SAMSUNG_3 = createTabletSamsung("3.0", "4.4.4", 0.6);
	public static final BrowserType TABLET_SAMSUNG_4 = createTabletSamsung("4.0", "5.0.2", 0.7);
	public static final BrowserType TABLET_SAMSUNG_5 = createTabletSamsung("5.0", "7.1.1", 0.8);
	
	/*
	 * Mobile IE 
	 */
	public static final BrowserType MOBILE_IE_7 = createMobileIE("7.0", "7.0", 0.6);
	public static final BrowserType MOBILE_IE_8 = createMobileIE("8.0", "7.0", 0.6);
	public static final BrowserType MOBILE_IE_9 = createMobileIE("9.0", "7.0", 0.6);
	public static final BrowserType MOBILE_IE_10 = createMobileIE("10.0", "8.0", 0.7);
	public static final BrowserType MOBILE_IE_11 = createMobileIE("11.0", "8.1", 0.7);
	public static final BrowserType MOBILE_IE_11_1 = createMobileIE("11.1", "8.1", 0.8);
	
	/*
	 * Mobile EDGE
	 */
	public static final BrowserType MOBILE_EDGE_12 = createMobileEdge("12.0", "Windows Phone 10.0", 0.9);
	public static final BrowserType MOBILE_EDGE_13 = createMobileEdge("13.0", "Android 6.0.1", 0.9);
	public static final BrowserType MOBILE_EDGE_14 = createMobileEdge("14.0", "Windows Phone 10.0", 0.9);
	
	/*
	 * Specific browsers for mobile devices
	 */
	public static final BrowserType SAMSUNG_GALAXY_TAB_A = new BrowserType("Mozilla/5.0 (Linux; Android 5.0.2; SAMSUNG SM-T550 Build/LRX22G) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/3.3 Chrome/102.0.5005.102 Safari/537.36", true, 1.2, BrowserFamily.Mobile);
	public static final BrowserType SAMSUNG_GALAXY_TAB_S = new BrowserType("Mozilla/5.0 (Linux; Android 5.0.2; SAMSUNG SM-T805 Build/LRX22G) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.111 Safari/537.36", true, 1.2, BrowserFamily.Mobile);
	public static final BrowserType SAMSUNG_GALAXY_S5 = new BrowserType("Mozilla/5.0 (Linux; Android 4.4.2; en-us; SAMSUNG SM-G900T Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Version/1.6 Chrome/100.0.4896.94 Mobile Safari/537.36", true, 1.2, BrowserFamily.Mobile);
	public static final BrowserType SAMSUNG_GALAXY_S6 = new BrowserType("Mozilla/5.0 (Linux; Android 6.0.1; SM-G920V Build/MMB29K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.98 Mobile Safari/537.36", true, 1.2, BrowserFamily.Mobile);
	public static final BrowserType SAMSUNG_GALAXY_S6_EDGE = new BrowserType("Mozilla/5.0 (Linux; Android 5.1.1; SM-G925F Build/LMY47X) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.94 Mobile Safari/537.36", true, 1.2, BrowserFamily.Mobile);
	public static final BrowserType SAMSUNG_GALAXY_S6_EDGE_PLUS = new BrowserType("Mozilla/5.0 (Linux; Android 5.1.1; SM-G928X Build/LMY47X) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.83 Mobile Safari/537.36", true, 1.2, BrowserFamily.Mobile);
	public static final BrowserType SAMSUNG_GALAXY_S7 = new BrowserType("Mozilla/5.0 (Linux; Android 6.0; SAMSUNG SM-G930F Build/MMB29K) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/4.0 Chrome/96.0.4664.133 Mobile Safari/537.36", true, 1.2, BrowserFamily.Mobile);
	
	public static final BrowserType NOKIA_LUMIA_830 = new BrowserType("Mozilla/5.0 (Windows Phone 8.1; NOKIA; Lumia 830) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.79 Mobile Safari/537.36 IEMobile/10.0", true, 1.2, BrowserFamily.Mobile);
	public static final BrowserType NOKIA_LUMIA_930 = new BrowserType("Mozilla/5.0 (Windows Phone 10.0; NOKIA; Lumia 930) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.79 Mobile Safari/537.36 Edge/14.14393", true, 1.2, BrowserFamily.Mobile);
	public static final BrowserType NOKIA_LUMIA_950 = new BrowserType("Mozilla/5.0 (Windows Phone 10.0; Android 4.2.1; Microsoft; Lumia 950) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.0 Mobile Safari/537.36 Edge/13.10586", true, 1.2, BrowserFamily.Mobile);
	
	public static final BrowserType HUAWEI_P8_LITE = new BrowserType("Mozilla/5.0 (Linux; Android 5.0; ALE-L21 Build/HuaweiALE-L21) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/95.0.4638.0 Mobile Safari/537.36", true, 1.2, BrowserFamily.Mobile);
	public static final BrowserType HUAWEI_P9 = new BrowserType("Mozilla/5.0 (Linux; Android 6.0; VIE-L09 Build/HUAWEIVIE-L09) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.105 Mobile Safari/537.36", true, 1.2, BrowserFamily.Mobile);
	public static final BrowserType HUAWEI_P10 = new BrowserType("Mozilla/5.0 (Linux; Android 7.0; VTR-L29 Build/HUAWEIVTR-L29) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.105 Mobile Safari/537.36", true, 1.2, BrowserFamily.Mobile);
	public static final BrowserType HUAWEI_NEXUS_6P = new BrowserType("Mozilla/5.0 (Linux; Android 6.0.1; Nexus 6P Build/MMB29P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.83 Mobile Safari/537.36", true, 1.2, BrowserFamily.Mobile);
	
	public static final BrowserType LG_PHOENIX_3 = new BrowserType("Mozilla/5.0 (Linux; Android 6.0.1; LG-M150 Build/M150) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/102.0.5005.102 Mobile Safari/537.36", true, 1.2, BrowserFamily.Mobile);
	public static final BrowserType LG_FORTUNE = new BrowserType("Mozilla/5.0 (Linux; Android 6.0.1; LG-M153 Build/M153) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/102.0.5005.102 Mobile Safari/537.36", true, 1.2, BrowserFamily.Mobile);
	public static final BrowserType LG_STYLO_3 = new BrowserType("Mozilla/5.0 (Linux; Android 7.0.1; LG-LS777 Build/LS777) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/102.0.5005.102 Mobile Safari/537.36", true, 1.2, BrowserFamily.Mobile);
	
	public static final BrowserType HTC_DESIRE_650 = new BrowserType("Mozilla/5.0 (Linux; Android 6.0; HTC Desire 650 Build/MMB29M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.124 Mobile Safari/537.36", true, 1.2, BrowserFamily.Mobile);
	public static final BrowserType HTC_U_PLAY = new BrowserType("Mozilla/5.0 (Linux; Android 6.0; HTC U Play Build/MRA58K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.91 Mobile Safari/537.3", true, 1.2, BrowserFamily.Mobile);
	public static final BrowserType HTC_ONE_M9 = new BrowserType("Mozilla/5.0 (Linux; Android 6.0; HTC One M9 Build/MRA58K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.98 Mobile Safari/537.36", true, 1.2, BrowserFamily.Mobile);
	
	public static final BrowserType XIAOMI_MI_5 = new BrowserType("Mozilla/5.0 (Linux; Android 6.0; MI 5 Build/MRA58K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.76 Mobile Safari/537.36", true, 1.2, BrowserFamily.Mobile);
	public static final BrowserType XIAOMI_NOTE_4 = new BrowserType("Mozilla/5.0 (Linux; Android 6.0; Redmi Note 4 Build/MRA58K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.85 Mobile Safari/537.36", true, 1.2, BrowserFamily.Mobile);
	
	public static final BrowserType SAFARI_IPAD = new BrowserType("Mozilla/5.0 (iPad; U; CPU OS 4_2_1 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8C148 Safari/6533.18.5", true, 0.8, BrowserFamily.Mobile);
	public static final BrowserType IPHONE_40 = new BrowserType("Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7", true, 0.6, BrowserFamily.Mobile);
	public static final BrowserType IPHONE_4S = new BrowserType("Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3", true, 0.6, BrowserFamily.Mobile);

	public static final BrowserType ANDROID_22 = new BrowserType("Mozilla/5.0 (Linux; U; Android 2.2.1; fr-fr; Desire HD Build/FRG83D) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1", true, 0.6, BrowserFamily.Mobile);
	public static final BrowserType ANDROID_24 = new BrowserType("Mozilla/5.0 (Linux; U; Android 2.4; en-us; Nexus One Build/GRI06B) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1", true, 0.7, BrowserFamily.Mobile);
	public static final BrowserType ANDROID_403 = new BrowserType("Mozilla/5.0 (Linux; U; Android 4.0.3; de-at; HTC Sensation Build/IML74K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30", true, 0.7, BrowserFamily.Mobile);

	public static final BrowserType WINDOWS_PHONE_7 = new BrowserType("Mozilla/5.0 (compatible; MSIE 9.0; Windows Phone OS 7.5; Trident/5.0; IEMobile/9.0; SAMSUNG; GT-I8350)", true, 0.7, BrowserFamily.Mobile);
	public static final BrowserType WINDOWS_PHONE_8 = new BrowserType("Mozilla/5.0 (compatible; MSIE 10.0; Windows Phone 8.0; Trident/6.0; ARM; Touch; IEMobile/10.0; SAMSUNG; GT-I8350)", true, 0.7, BrowserFamily.Mobile);
	public static final BrowserType WINDOWS_PHONE_81 = new BrowserType("Mozilla/5.0 (compatible; MSIE 11.0; Windows Phone 8.1; Trident/7.0; ARM; Touch; IEMobile/11.0; SAMSUNG; GT-I8350)", true, 0.8, BrowserFamily.Mobile);

	public static final BrowserType GOOGLE_PIXEL_C = new BrowserType("Mozilla/5.0 (Linux; Android 7.0; Pixel C Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/99.0.4844.98 Safari/537.36", true, 1, BrowserFamily.Mobile);
	public static final BrowserType SONY_XPERIA_Z4_TAB = new BrowserType("Mozilla/5.0 (Linux; Android 6.0.1; SGP771 Build/32.2.A.0.253; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/99.0.4844.98 Safari/537.36", true, 1, BrowserFamily.Mobile);
	public static final BrowserType SONY_XPERIA_Z5 = new BrowserType("Mozilla/5.0 (Linux; Android 6.0.1; E6653 Build/32.2.A.0.253) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.98 Mobile Safari/537.36", true, 1.1, BrowserFamily.Mobile);
	public static final BrowserType NVIDIA_SHIELD = new BrowserType("Mozilla/5.0 (Linux; Android 5.1.1; SHIELD Tablet Build/LMY48C) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.98 Safari/537.36", true, 1, BrowserFamily.Mobile);
	public static final BrowserType AMAZON_KINDLE_FIRE_HDX_7 = new BrowserType("Mozilla/5.0 (Linux; Android 4.4.3; KFTHWI Build/KTU84M) AppleWebKit/537.36 (KHTML, like Gecko) Silk/47.1.79 like Chrome/97.0.4692.80 Safari/537.36", true, 1, BrowserFamily.Mobile);
	public static final BrowserType LG_G_PAD_7 = new BrowserType("Mozilla/5.0 (Linux; Android 5.0.2; LG-V410/V41020c Build/LRX22G) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/90.0.4430.118 Safari/537.36", true, 1, BrowserFamily.Mobile);
	
	public static final BrowserType NINTENDO_WII_U = new BrowserType("Mozilla/5.0 (Nintendo WiiU) AppleWebKit/536.30 (KHTML, like Gecko) NX/3.0.4.2.12 NintendoBrowser/4.3.1.11264.US", true, 1, BrowserFamily.Other);
	public static final BrowserType NINTENDO_3DS = new BrowserType("Mozilla/5.0 (Nintendo 3DS; U; ; en) Version/1.7412.EU", true, 0.9, BrowserFamily.Other);
	public static final BrowserType PS_VITA = new BrowserType("Mozilla/5.0 (PlayStation Vita 3.61) AppleWebKit/537.73 (KHTML, like Gecko) Silk/3.2", true, 0.8, BrowserFamily.Other);
	public static final BrowserType PS_4 = new BrowserType("Mozilla/5.0 (PlayStation 4 3.11) AppleWebKit/537.73 (KHTML, like Gecko)", true, 1, BrowserFamily.Other);
	public static final BrowserType X_BOX_ONE = new BrowserType("Mozilla/5.0 (Windows Phone 10.0; Android 4.2.1; Xbox; Xbox One) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.0 Mobile Safari/537.36 Edge/13.10586", true, 1, BrowserFamily.Other);
	
	public static final BrowserType AMAZON_KINDLE_3 = new BrowserType("Mozilla/5.0 (Linux; U; en-US) AppleWebKit/528.5+ (KHTML, like Gecko, Safari/528.5+) Version/4.0 Kindle/3.0 (screen 600x800; rotate)", true, 0.8, BrowserFamily.Other);
	public static final BrowserType AMAZON_KINDLE_4 = new BrowserType("Mozilla/5.0 (X11; U; Linux armv7l like Android; en-us) AppleWebKit/531.2+ (KHTML, like Gecko) Version/5.0 Safari/533.2+ Kindle/3.0+", true, 0.8, BrowserFamily.Other);	
	
	/*
	 * Robot browsers
	 */
	public static final BrowserType GOOGLEBOT = createGoogleBot();
	public static final BrowserType GOMEZ_AGENT = createGomezAgent();
	public static final BrowserType BAIDU_SPIDER = createBaiduSpider();
	public static final BrowserType BING_BOT = createBingBot();
	public static final BrowserType UPTIME_ROBOT = createUptimeRobot();
	public static final BrowserType YANDEX_BOT = createYandexBot();
	public static final BrowserType YAHOO_SLURP = createYahooSlurp();
	public static final BrowserType KEYNOTE_KTXN = createKeynoteKTXN();
	public static final BrowserType KEYNOTE_KHTE = createKeynoteKHTE();

	public static final BrowserType NONE = new BrowserType("NONE", false, 1);

	private final String userAgent;
	private final boolean isJavaScriptSupported;
	private final boolean isColumnNumberInOnErrorAvailable;
	private final boolean isErrorObjectInOnErrorAvailable;
	private final double speed;
	private final BrowserFamily browserFamily;
	
	private static Map<BrowserType,String> browserNames = null;

	private BrowserType(String userAgent, boolean isJavaScriptSupported, double speed, BrowserFamily family,
			boolean isColumnNumberInOnErrorAvailable, boolean isErrorObjectInOnErrorAvailable) {
		this.userAgent = userAgent;
		this.isJavaScriptSupported = isJavaScriptSupported;
		this.speed = speed;
		this.browserFamily = family;
		this.isErrorObjectInOnErrorAvailable = isErrorObjectInOnErrorAvailable;
		this.isColumnNumberInOnErrorAvailable = isColumnNumberInOnErrorAvailable;
	}

	private BrowserType(String userAgent, boolean isJavaScriptSupported, double speed, BrowserFamily family) {
		this(userAgent, isJavaScriptSupported, speed, family, false, false);
	}

	private BrowserType(String userAgent, boolean isJavaScriptSupported, double speed) {
		this(userAgent, isJavaScriptSupported, speed, BrowserFamily.Unknown, false, false);
	}

	public String getUserAgent() {
		return userAgent;
	}

	public boolean isJavaScriptSupported() {
		return isJavaScriptSupported;
	}


	public boolean isColumnNumberInOnErrorAvailable() {
		return isColumnNumberInOnErrorAvailable;
	}

	public boolean isErrorObjectInOnErrorAvailable() {
		return isErrorObjectInOnErrorAvailable;
	}

	public double getSpeed() {
		return speed;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((browserFamily == null) ? 0 : browserFamily.hashCode());
		result = prime * result + (isColumnNumberInOnErrorAvailable ? 1231 : 1237);
		result = prime * result + (isErrorObjectInOnErrorAvailable ? 1231 : 1237);
		result = prime * result + (isJavaScriptSupported ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(speed);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((userAgent == null) ? 0 : userAgent.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BrowserType other = (BrowserType) obj;
		if (browserFamily != other.browserFamily)
			return false;
		if (isColumnNumberInOnErrorAvailable != other.isColumnNumberInOnErrorAvailable)
			return false;
		if (isErrorObjectInOnErrorAvailable != other.isErrorObjectInOnErrorAvailable)
			return false;
		if (isJavaScriptSupported != other.isJavaScriptSupported)
			return false;
		if (Double.doubleToLongBits(speed) != Double.doubleToLongBits(other.speed))
			return false;
		if (userAgent == null) {
			if (other.userAgent != null)
				return false;
		} else if (!userAgent.equals(other.userAgent))
			return false;
		return true;
	}

	public BrowserFamily getBrowserFamily() {
		return browserFamily;
	}
	
	public boolean isRuxitSynthetic() {
		return RUXIT_SYNTHETIC == this;
	}
	
	public static String getBrowserFieldName(BrowserType t) throws IllegalArgumentException, IllegalAccessException{
		if(browserNames == null){
			Map<BrowserType, String> bNames = new HashMap<>();
			for(Field field : BrowserType.class.getDeclaredFields()){
				if((field.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) != 0 && BrowserType.class == field.getType()){
					bNames.put((BrowserType)field.get(null), field.getName());
				}
			}
			browserNames=bNames;
		}		
		return browserNames.get(t);
	}
	
	public static BrowserType getFieldByName(String name) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		Field field = BrowserType.class.getField(name);
		return (BrowserType)field.get(null);
	}

	private static BrowserType createRuxitSyntheticBrowser() {
		String userAgent = "Mozilla/5.0 (Windows NT 6.3;WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.124 Safari/537.36 RuxitSynthetic/1.0";
		return new BrowserType(userAgent, true, 1, BrowserFamily.Unknown, false, false);
	}

	private static BrowserType createInternetExplorer8(String os, double speed) {
		String userAgent = String.format("Mozilla/4.0 (compatible; MSIE 8.0; %s; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.3; .NET4.0C; .NET4.0E; .NET CLR 1.1.4322)", os);
		return new BrowserType(userAgent, true, speed, BrowserFamily.IE, false, false);
	}

	private static BrowserType createInternetExplorer(int version, String os, double speed, boolean supportsColumnNumber, boolean supportsStackTrace) {
		String userAgent = String.format("Mozilla/4.0 (compatible; MSIE %d.0; %s; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.0.04506; InfoPath.2)", version, os);
		return new BrowserType(userAgent, true, speed, BrowserFamily.IE, supportsColumnNumber, supportsStackTrace);
	}

	private static BrowserType createEdge(int version, String os, String chromiumVersion, double speed, boolean supportsColumnNumber, boolean supportsStackTrace){
		String userAgent = String.format("Mozilla/5.0 (%s) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/%s.2311.135 Safari/537.36 Edge/%d.10136", os, chromiumVersion, version);
		return new BrowserType(userAgent, true, speed, BrowserFamily.IE, supportsColumnNumber, supportsStackTrace);
	}
	private static BrowserType createFirefox(String version, String os, double speed, boolean supportsColumnNumber, boolean supportsStackTrace) {
		String userAgent = String.format("Mozilla/5.0 (%s; WOW64; rv:%s) Gecko/20110303 Firefox/%s", os, version, version);
		return new BrowserType(userAgent, true, speed, BrowserFamily.Firefox, supportsColumnNumber, supportsStackTrace);
	}

	private static BrowserType createChrome(String version, String os, double speed, boolean supportsColumnNumber, boolean supportsStackTrace) {
		String userAgent = String.format("Mozilla/5.0 (%s) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/%s Safari/537.36", os, version);
		return new BrowserType(userAgent, true, speed, BrowserFamily.Chrome, supportsColumnNumber, supportsStackTrace);
	}
	
	private static BrowserType createMobileChrome(String version, String androidVersion, double speed) {
		String userAgent = String.format("Mozilla/5.0 (Linux; Android %s; Galaxy Nexus Build/IMM76B) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/%s.1025.133 Mobile Safari/535.19", androidVersion, version);
		return new BrowserType(userAgent, true, speed, BrowserFamily.Mobile, true, true);
	}
	
	private static BrowserType createTabletChrome(String version, String androidVersion, double speed) {
		String userAgent = String.format("Mozilla/5.0 (Linux; Android %s; Pixel C Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/%s.0.2743.98 Safari/537.36", androidVersion, version);
		return new BrowserType(userAgent, true, speed, BrowserFamily.Mobile, true, true);
	}

	private static BrowserType createSafari(String version, String os, double speed, boolean supportsColumnNumber, boolean supportsStackTrace) {
		String userAgent = String.format("Mozilla/5.0 (Macintosh; Intel %s) AppleWebKit/537.71 (KHTML, like Gecko) Version/%s Safari/537.71", os, version);
		return new BrowserType(userAgent, true, speed, BrowserFamily.Safari, supportsColumnNumber, supportsStackTrace);
	}

	private static BrowserType createOpera(String version, String chromiumVersion, String os, double speed, boolean supportsColumnNumber, boolean supportsStackTrace) {
		String userAgent = String.format("Mozilla/5.0 (%s; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/%s.1500.52 Safari/537.36 OPR/%s.1147.100", os, chromiumVersion, version);
		return new BrowserType(userAgent, true, speed, BrowserFamily.Opera, supportsColumnNumber, supportsStackTrace);
	}
	
	private static BrowserType createMobileIE(String version, String phoneVersion, double speed){
		String userAgent = String.format("Mozilla/5.0 (compatible; MSIE %s; Windows Phone %s; Trident/7.0; ARM; Touch; IEMobile/%s)", version, phoneVersion, version);
		return new BrowserType(userAgent, true, speed, BrowserFamily.Mobile, true, true);
	}
	
	private static BrowserType createMobileSamsung(String version, String androidVersion, double speed){
		String userAgent = String.format("Mozilla/5.0 (Linux; Android %s) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/%s Chrome/96.0.4664.133 Mobile Safari/537.36", androidVersion, version);
		return new BrowserType(userAgent, true, speed, BrowserFamily.Mobile, true, true);
	}
	
	private static BrowserType createTabletSamsung(String version, String androidVersion, double speed){
		String userAgent = String.format("Mozilla/5.0 (Linux; Android %s; SAMSUNG SM-T550 Build/LRX22G) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/%s.3 Chrome/102.0.5005.102 Safari/537.36", androidVersion, version);
		return new BrowserType(userAgent, true, speed, BrowserFamily.Mobile, true, true);
	}
	
	private static BrowserType createMobileAndroid(String version, String androidVersion, double speed){
		String userAgent = String.format("Mozilla/5.0 (Linux; U; Android %s; en-us) AppleWebKit/533.1 (KHTML, like Gecko) Version/%s Mobile Safari/533.1", androidVersion, version);
		return new BrowserType(userAgent, true, speed, BrowserFamily.Mobile, true, true);
	}
	
	private static BrowserType createMobileOpera(String version, String androidVersion, double speed){
		String userAgent = String.format("Mozilla/5.0 (Linux; Android %s; SM-G930V Build/MMB29M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.94 Mobile Safari/537.36 OPR/%s.0.2192.105088", androidVersion, version);
		return new BrowserType(userAgent, true, speed, BrowserFamily.Mobile, true, true);
	}
	
	private static BrowserType createMobileUC(String version, String androidVersion, double speed){
		String userAgent = String.format("UCWEB/2.0 (MIDP-2.0; U; Adr %s; en-US; XT1022) U2/1.0.0 UCBrowser/%s.0.706 U2/1.0.0 Mobile", androidVersion, version);
		return new BrowserType(userAgent, true, speed, BrowserFamily.Mobile, true, true);
	}
	
	private static BrowserType createTabletUC(String version, String androidVersion, double speed){
		String userAgent = String.format("Mozilla/5.0 (iPad; U; CPU OS %s like Mac OS X; ru; iPad3,6) AppleWebKit/534.46 (KHTML, like Gecko) UCBrowser/%s.4.0.367 U3/1 Safari/7543.48.3", androidVersion, version);
		return new BrowserType(userAgent, true, speed, BrowserFamily.Mobile, true, true);
	}
	
	private static BrowserType createMobileSafari(String version, String iosVersion, double speed){
		String userAgent = String.format("Mozilla/5.0 (iPhone; CPU iPhone OS %s like Mac OS X) AppleWebKit/600.1.4 (KHTML, like Gecko) Version/%s Safari/600.1.4", iosVersion, version);
		return new BrowserType(userAgent, true, speed, BrowserFamily.Mobile, true, true);
	}
	
	private static BrowserType createIpadSafari(String version, String iosVersion, double speed){
		String userAgent = String.format("Mozilla/5.0 (iPad; U; CPU OS %s like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/%s.0.2 Mobile/8C148 Safari/6533.18.5", iosVersion, version);
		return new BrowserType(userAgent, true, speed, BrowserFamily.Mobile, true, true);
	}
	
	private static BrowserType createMobileEdge(String version, String iosVersion, double speed){
		String userAgent = String.format("Mozilla/5.0 (%s) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.71 Mobile Safari/537.36 Edge/%s", iosVersion, version);
		return new BrowserType(userAgent, true, speed, BrowserFamily.Mobile, true, true);
	}

	private static BrowserType createGoogleBot() {
		return new BrowserType("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)", true, 1, BrowserFamily.Robot, false, false);
	}

	private static BrowserType createGomezAgent() {
		return new BrowserType("Mozilla/5.0 (compatible; GomezAgent/1.0; +http://www.gomez.com/)", true, 1, BrowserFamily.Robot, false, false);
	}

	private static BrowserType createYandexBot() {
		return new BrowserType("Mozilla/5.0 (compatible; YandexBot/3.0; +http://yandex.com/bots)", true, 1, BrowserFamily.Robot, false, false);
	}

	private static BrowserType createUptimeRobot() {
		return new BrowserType("Mozilla/5.0+(compatible; UptimeRobot/2.0; http://www.uptimerobot.com/)", true, 1, BrowserFamily.Robot, false, false);
	}

	private static BrowserType createBingBot() {
		return new BrowserType("Mozilla/5.0 (compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm)", true, 1, BrowserFamily.Robot, false, false);
	}

	private static BrowserType createBaiduSpider() {
		return new BrowserType("Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)", true, 1, BrowserFamily.Robot, false, false);
	}

	private static BrowserType createYahooSlurp() {
		return new BrowserType("Mozilla/5.0 (compatible; Yahoo! Slurp; http://help.yahoo.com/help/us/ysearch/slurp)", true, 1, BrowserFamily.Robot, false, false);
	}

	/** KTXN = Keynote Transaction Perspective (real-browser monitoring) */
	private static BrowserType createKeynoteKTXN() {
		return new BrowserType("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; KTXN B494599541A47056T1231321)", true, 1, BrowserFamily.Robot, false, false);
	}

	/** KHTE = Application Perspective (emulated browser) */
	private static BrowserType createKeynoteKHTE() {
		return new BrowserType("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Keynote) KHTE", true, 1, BrowserFamily.Robot, false, false);
	}
}
