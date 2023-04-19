/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: MobileDeviceType.java
 * @date: 20.01.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload.mobile;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileOS;

/**
 * DeviceTypes known for mobile native load simulation.
 *
 * @author peter.lang
 */
public class MobileDeviceType {	
	public static final String IOS = "ios"; //$NON-NLS-1$

	/**
	 * Pixel ratios found either on the sites listed or approximated via ppi number - 150 ppi equals to pixel ratio of 1.0. Sites:
	 * https://material.io/tools/devices/
	 * https://www.mydevice.io/#compare-devices
	 * http://screensiz.es/phone
	 * https://www.h3xed.com/web-and-internet/mobile-device-resolution-and-pixel-ratio-list
	 */

	//Android Devices
	public static final MobileDeviceType SAMSUNG_GALAXY_TAB = new MobileDeviceType("Galaxy Tab", "Samsung", "product:SM-T580N androidId:9694f3907f48f3db getDeviceId:null", "Android 6.0", MobileOS.ANDROID, 1200, 1920, "ksoap2-android/2.6.0+", "armv7", "SM-T580N", BrowserType.ANDROID_22, true, 1.0);
	public static final MobileDeviceType SAMSUNG_GALAXY_S9 = new MobileDeviceType("Galaxy S9", "Samsung", "product:SM-G960F androidId:e53fd356329ad792 getDeviceId:358001045669450", "Android 8.0 Oreo", MobileOS.ANDROID, 480, 800, "ksoap2-android/2.6.0+", "armv7", "SM-G960F", BrowserType.ANDROID_22, false, 1.5);
	public static final MobileDeviceType SAMSUNG_GALAXY_S8 = new MobileDeviceType("Galaxy S8", "Samsung", "product:SM-G950FZKADBT androidId:cf8ea35205cb060c getDeviceId:356440045901814", "Android 8.0 Oreo", MobileOS.ANDROID, 480, 800, "ksoap2-android/2.6.0+", "armv7", "SM-G950FZKADBT", BrowserType.MOBILE_CHROME_36, false, 1.5);

	public static final MobileDeviceType SAMSUNG_GALAXY_TAB_A = new MobileDeviceType("Galaxy Tab A", "Samsung", "002ebf12-a125-5ddf-a739-67c3c5d20164", "Android 6.0.1", MobileOS.ANDROID, 1920, 1200, "Mozilla/5.0 (Linux; Android 5.0.2; SAMSUNG SM-T550 Build/LRX22G) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/3.3 Chrome/102.0.5005.102 Safari/537.36", "1.6 GHz Octa-Core ARM Cortex-A53", "SM-T550", BrowserType.SAMSUNG_GALAXY_TAB_A, true, 1.0);
	public static final MobileDeviceType SAMSUNG_GALAXY_TAB_S = new MobileDeviceType("Galaxy Tab S", "Samsung", "002ebf12-a125-5ddf-a739-67c3c5d20165", "Android 5.0.2", MobileOS.ANDROID, 2650, 1600, "Mozilla/5.0 (Linux; Android 5.0.2; SAMSUNG SM-T805 Build/LRX22G) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.111 Safari/537.36", "Snapdragon 800 2.3 GHz Quadcore", "SM-T805", BrowserType.SAMSUNG_GALAXY_TAB_S, true, 1.0);
	public static final MobileDeviceType SAMSUNG_GALAXY_S5 = new MobileDeviceType("Galaxy S5", "Samsung", "002ebf12-a125-5ddf-a739-67c3c5d20166", "Android 4.4.2", MobileOS.ANDROID, 1080, 1920, "Mozilla/5.0 (Linux; Android 4.4.2; en-us; SAMSUNG SM-G900T Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Version/1.6 Chrome/100.0.4896.94 Mobile Safari/537.36", "2.5 GHz quad-core Krait 400", "SM-G900F", BrowserType.SAMSUNG_GALAXY_S5, false, 1.0);
	public static final MobileDeviceType SAMSUNG_GALAXY_S6 = new MobileDeviceType("Galaxy S6", "Samsung", "002ebf12-a125-5ddf-a739-67c3c5d20131", "Android 6.0.1", MobileOS.ANDROID, 1440, 2560, "Mozilla/5.0 (Linux; Android 6.0.1; SM-G920V Build/MMB29K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.98 Mobile Safari/537.36", "ARM Cortex-A57", "SM-G920V", BrowserType.SAMSUNG_GALAXY_S6, false, 1.0);
	public static final MobileDeviceType SAMSUNG_GALAXY_S6_EDGE = new MobileDeviceType("Galaxy S6 EDGE", "Samsung", "002ebf12-a125-5ddf-a739-67c3c5d20167", "Android 5.1.1", MobileOS.ANDROID, 1440, 2560, "Mozilla/5.0 (Linux; Android 5.1.1; SM-G925F Build/LMY47X) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.94 Mobile Safari/537.36", "ARM Cortex-A57", "SM-G925F", BrowserType.SAMSUNG_GALAXY_S6_EDGE, false, 1.0);
	public static final MobileDeviceType SAMSUNG_GALAXY_S6_EDGE_PLUS = new MobileDeviceType("Galaxy S6 EDGE Plus", "Samsung", "002ebf12-a125-5ddf-a739-67c3c5d20132", "Android 5.1.1", MobileOS.ANDROID, 1440, 2560, "Mozilla/5.0 (Linux; Android 5.1.1; SM-G928X Build/LMY47X) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.83 Mobile Safari/537.36", "ARM Cortex-A57", "SM-G925F", BrowserType.SAMSUNG_GALAXY_S6_EDGE_PLUS, false, 1.0);
	public static final MobileDeviceType SAMSUNG_GALAXY_S7 = new MobileDeviceType("Galaxy S7", "Samsung", "002ebf12-a125-5ddf-a739-67c3c5d20168", "Android 6.0.1", MobileOS.ANDROID, 1440, 2560, "Mozilla/5.0 (Linux; Android 6.0; SAMSUNG SM-G930F Build/MMB29K) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/4.0 Chrome/96.0.4664.133 Mobile Safari/537.36", "Dual-core 2.15 GHz Kryo", "SM-G930F", BrowserType.SAMSUNG_GALAXY_S7, false, 1.0);

	public static final MobileDeviceType HUAWEI_P8_LITE = new MobileDeviceType("Huawei P8 Lite", "Huawei", "002ebf12-a125-5ddf-a739-67c3c5d20157", "Android 5.0.1", MobileOS.ANDROID, 720, 1280, "Mozilla/5.0 (Linux; Android 5.0; ALE-L21 Build/HuaweiALE-L21) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/95.0.4638.0 Mobile Safari/537.36", "HiSilicon Kirin 620", "ALE-L21", BrowserType.HUAWEI_P8_LITE, false, 1.0);
	public static final MobileDeviceType HUAWEI_P9 = new MobileDeviceType("Huawei P9", "Huawei", "002ebf12-a125-5ddf-a739-67c3c5d20158", "Android 6.0.1", MobileOS.ANDROID, 1080, 1920, "Mozilla/5.0 (Linux; Android 6.0; VIE-L09 Build/HUAWEIVIE-L09) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.105 Mobile Safari/537.36", "HiSilicon Kirin 955", "VIE-L09", BrowserType.HUAWEI_P9, false, 1.0);
	public static final MobileDeviceType HUAWEI_P10 = new MobileDeviceType("Huawei P10", "Huawei", "002ebf12-a125-5ddf-a739-67c3c5d20159", "Android 7.0.1", MobileOS.ANDROID, 1080, 1920, "Mozilla/5.0 (Linux; Android 7.0; VTR-L29 Build/HUAWEIVTR-L29) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.105 Mobile Safari/537.36", "HiSilicon Kirin 960", "VTR-L29", BrowserType.HUAWEI_P10, false, 1.0);
	public static final MobileDeviceType HUAWEI_NEXUS_6P = new MobileDeviceType("Huawei NEXUS 6P", "Huawei", "002ebf12-a125-5ddf-a739-67c3c5d20129", "Android 6.0.1", MobileOS.ANDROID, 1440, 2560, "Mozilla/5.0 (Linux; Android 6.0.1; Nexus 6P Build/MMB29P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.83 Mobile Safari/537.36", "Qualcomm Snapdragon 810 8994", "XT1103", BrowserType.HUAWEI_NEXUS_6P, true, 1.0);

	public static final MobileDeviceType LG_PHOENIX_3 = new MobileDeviceType("Phoenix 3", "LG", "002ebf12-a125-5ddf-a739-67c3c5d20154", "Android 6.0.1", MobileOS.ANDROID, 480, 854, "Mozilla/5.0 (Linux; Android 6.0.1; LG-M150 Build/M150) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/102.0.5005.102 Mobile Safari/537.36", "Qualcomm Snapdragon 210 8909", "M150", BrowserType.LG_PHOENIX_3, false, 1.0);
	public static final MobileDeviceType LG_FORTUNE = new MobileDeviceType("Fortune", "LG", "002ebf12-a125-5ddf-a739-67c3c5d20155", "Android 6.0.1", MobileOS.ANDROID, 480, 854, "Mozilla/5.0 (Linux; Android 6.0.1; LG-M153 Build/M153) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/102.0.5005.102 Mobile Safari/537.36", "Qualcomm Snapdragon 210 8909", "M153", BrowserType.LG_FORTUNE, false, 2.0);
	public static final MobileDeviceType LG_G7 = new MobileDeviceType("G7", "LG", "002ebf12-a125-5ddf-a739-67c3c5d20156", "Android 7.0.1", MobileOS.ANDROID, 720, 1280, "Mozilla/5.0 (Linux; Android 7.0.1; LG-LS777 Build/LS777) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/102.0.5005.102 Mobile Safari/537.36", "Qualcomm Snapdragon 435 8940", "LS777", BrowserType.LG_STYLO_3, false, 2.5);

	public static final MobileDeviceType HTC_DESIRE_650 = new MobileDeviceType("Desire 650", "HTC", "002ebf12-a125-5ddf-a739-67c3c5d20152", "Android 6.0.1", MobileOS.ANDROID, 720, 1280, "Mozilla/5.0 (Linux; Android 6.0; HTC Desire 650 Build/MMB29M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.124 Mobile Safari/537.36", "Quad-core 1.6 GHz Cortex-A7", "MMB29M", BrowserType.HTC_DESIRE_650, false, 1.5);
	public static final MobileDeviceType HTC_U_PLAY = new MobileDeviceType("U Play", "HTC", "002ebf12-a125-5ddf-a739-67c3c5d20153", "Android 6.0.1", MobileOS.ANDROID, 1080, 1920, "Mozilla/5.0 (Linux; Android 6.0; HTC U Play Build/MRA58K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.91 Mobile Safari/537.3", "MediaTek Helio P10", "MRA58K", BrowserType.HTC_U_PLAY, false, 3.0);
	public static final MobileDeviceType HTC_U12_LIFE = new MobileDeviceType("U12 Life", "HTC", "product:99HAPK004-00 androidId:cf8ea35205cb060c getDeviceId:356440045901814", "Android 6.0", MobileOS.ANDROID, 540, 960, "ksoap2-android/2.6.0+", "armv7", "Z710e", BrowserType.ANDROID_24, false, 1.5);
	public static final MobileDeviceType HTC_ONE_M9 = new MobileDeviceType("One M9", "HTC", "002ebf12-a125-5ddf-a739-67c3c5d20128", "Android 6.0.1", MobileOS.ANDROID, 1080, 1920, "Mozilla/5.0 (Linux; Android 6.0; HTC One M9 Build/MRA58K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.98 Mobile Safari/537.36", "	Quad-core ARM Cortex A57 ", "M9", BrowserType.HTC_ONE_M9, false, 3.0);

	public static final MobileDeviceType XIAOMI_MI_5 = new MobileDeviceType("MI 5", "Xiaomi", "002ebf12-a125-5ddf-a739-67c3c5d20150", "Android 6.0.1", MobileOS.ANDROID, 1080, 1920, "Mozilla/5.0 (Linux; Android 6.0; MI 5 Build/MRA58K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.76 Mobile Safari/537.36", "Qualcomm Snapdragon 820 8996", "MRA58K", BrowserType.XIAOMI_MI_5, false, 1.0);
	public static final MobileDeviceType XIAOMI_NOTE_4 = new MobileDeviceType("Redmi Note 4", "Xiaomi", "002ebf12-a125-5ddf-a739-67c3c5d20151", "Android 6.0.1", MobileOS.ANDROID, 1080, 1920, "Mozilla/5.0 (Linux; Android 6.0; Redmi Note 4 Build/MRA58K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.85 Mobile Safari/537.36", "Qualcomm Snapdragon 625 8953", "MRA58K", BrowserType.XIAOMI_NOTE_4, false, 1.0);

	public static final MobileDeviceType GOOGLE_PIXEL_C = new MobileDeviceType("Pixel C", "Google", "002ebf12-a125-5ddf-a739-67c3c5d20149", "Android 7.0.1", MobileOS.ANDROID, 2560, 1800, "Mozilla/5.0 (Linux; Android 7.0; Pixel C Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/99.0.4844.98 Safari/537.36", "ARMv8-A", "NRD90M", BrowserType.GOOGLE_PIXEL_C, true, 3.5);
	public static final MobileDeviceType SONY_XPERIA_Z4_TAB = new MobileDeviceType("Xperia Z4 Tablet", "Sony", "002ebf12-a125-5ddf-a739-67c3c5d20148", "Android 6.0.1", MobileOS.ANDROID, 2560, 1600, "Mozilla/5.0 (Linux; Android 6.0.1; SGP771 Build/32.2.A.0.253; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/99.0.4844.98 Safari/537.36", "Quad-core ARM Cortex A57", "SGP771", BrowserType.SONY_XPERIA_Z4_TAB, true, 2.0);
	public static final MobileDeviceType SONY_XPERIA_Z5 = new MobileDeviceType("Xperia Z5", "Sony", "002ebf12-a125-5ddf-a739-67c3c5d20126", "Android 6.0.1", MobileOS.ANDROID, 1920, 1080, "Mozilla/5.0 (Linux; Android 6.0.1; E6653 Build/32.2.A.0.253) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.98 Mobile Safari/537.36", "Quad-core ARM Cortex A57", "E6653", BrowserType.SONY_XPERIA_Z5, true, 2.0);
	public static final MobileDeviceType NVIDIA_SHIELD = new MobileDeviceType("Nvidia Shield", "Nvidia", "002ebf12-a125-5ddf-a739-67c3c5d20147", "Android 5.1.1", MobileOS.ANDROID, 1920, 1080, "Mozilla/5.0 (Linux; Android 5.1.1; SHIELD Tablet Build/LMY48C) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.98 Safari/537.36", "Cortex-A15 R3 2.2 GHz", "LMY48C", BrowserType.NVIDIA_SHIELD, true, 2.0);
	public static final MobileDeviceType AMAZON_KINDLE_FIRE_HDX_7 = new MobileDeviceType("Kindle Fire HDX 7", "Amazon", "002ebf12-a125-5ddf-a739-67c3c5d20146", "Android 4.4.3", MobileOS.ANDROID, 1920, 1200, "Mozilla/5.0 (Linux; Android 4.4.3; KFTHWI Build/KTU84M) AppleWebKit/537.36 (KHTML, like Gecko) Silk/47.1.79 like Chrome/97.0.4692.80 Safari/537.36", "Quad-core 2.2 GHz ARM Krait 400", "KFTHWI", BrowserType.AMAZON_KINDLE_FIRE_HDX_7, true, 1.5);
	public static final MobileDeviceType LG_G_PAD_7 = new MobileDeviceType("G Pad 7.0", "LG", "002ebf12-a125-5ddf-a739-67c3c5d20145", "Android 5.0.2", MobileOS.ANDROID, 1280, 800, "Mozilla/5.0 (Linux; Android 5.0.2; LG-V410/V41020c Build/LRX22G) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/90.0.4430.118 Safari/537.36", "1.2 GHz quad-core Qualcomm MSM822", "LG-V410", BrowserType.LG_G_PAD_7, true, 1.0);

	//iOS Devices
	public static final MobileDeviceType APPLE_IPHONE_8 = new MobileDeviceType("iPhone 8", "Apple", "21234321", "iOS 11.4.1", MobileOS.IOS, 750, 1334, "CFNetwork/548.1.4", "armv7", "iPhone10,4", BrowserType.IPHONE_40, false, 1.0);
	public static final MobileDeviceType APPLE_IPAD_PRO_2 = new MobileDeviceType("iPad Pro 2", "Apple", "7563936473", "iOS 12.1.4", MobileOS.IOS, 768, 1024, "CFNetwork/485.13.9", "armv7", "iPad7,2", BrowserType.SAFARI_IPAD, true, 1.0);
	public static final MobileDeviceType APPLE_IPHONE_7_PLUS = new MobileDeviceType("iPhone 7 Plus", "Apple", "2468412", "iOS 11.4.1", MobileOS.IOS, 640, 1136, "CFNetwork/609.1.4", "armv7s", "iPhone9,4", BrowserType.IPHONE_4S, false, 2.0);

	public static final MobileDeviceType IPAD_6 = new MobileDeviceType("iPad 6", "Apple", "002ebf12-a125-5ddf-a739-67c3c5d20169", "iOS 11.4.1", MobileOS.IOS, 2048, 1536, "Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A403 Safari/8536.25", "1.4 GHz dual-core Apple Swift", "A1460", BrowserType.MOBILE_SAFARI_6_IPAD, true, 2.0);
	public static final MobileDeviceType IPAD_MINI_2 = new MobileDeviceType("iPad Mini 2", "Apple", "002ebf12-a125-5ddf-a739-67c3c5d20170", "iOS 11.4.1", MobileOS.IOS, 2048, 1536, "Mozilla/5.0 (iPad; CPU OS 7_0_3 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/7.0 Mobile/10A403 Safari/8536.25", "1.4 GHz dual-core Apple Cyclone", "A1476", BrowserType.MOBILE_SAFARI_7_IPAD, true, 2.0);
	public static final MobileDeviceType IPAD_MINI_4 = new MobileDeviceType("iPad Mini 4", "Apple", "002ebf12-a125-5ddf-a739-67c3c5d20171", "iOS 12.1.4", MobileOS.IOS, 2048, 1536, "Mozilla/5.0 (iPad; CPU OS 8_1 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/10.0 Mobile/10A403 Safari/8536.25", "1.5 GHz tri-core Apple Typhoon", "A1567", BrowserType.MOBILE_SAFARI_10_IPAD, true, 2.0);
	public static final MobileDeviceType IPHONE_6 = new MobileDeviceType("iPhone 6", "Apple", "002ebf12-a125-5ddf-a739-67c3c5d20172", "iOS 8.0", MobileOS.IOS, 750, 1334, "Mozilla/5.0 (iPhone; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5376e Safari/8536.25", "ARMv8-A Typhoon", "A1549", BrowserType.MOBILE_SAFARI_6, false, 2.0);
	public static final MobileDeviceType IPHONE_7 = new MobileDeviceType("iPhone 7", "Apple", "002ebf12-a125-5ddf-a739-67c3c5d20173", "iOS 11.4.1", MobileOS.IOS, 750, 1334, "Mozilla/5.0 (iPhone; CPU iPhone OS 9_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/7.0 Mobile/10A5376e Safari/8536.25", "ARMv8-A Typhoon", "A1633", BrowserType.MOBILE_SAFARI_7, false, 2.0);
	public static final MobileDeviceType IPHONE_7_PLUS = new MobileDeviceType("iPhone 7 Plus", "Apple", "002ebf12-a125-5ddf-a739-67c3c5d20174", "iOS 11.4.1", MobileOS.IOS, 1080, 1920, "Mozilla/5.0 (iPhone; CPU iPhone OS 9_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/8.0 Mobile/10A5376e Safari/8536.25", "ARMv8-A Typhoon", "A1688", BrowserType.MOBILE_SAFARI_8, false, 3.0);
	public static final MobileDeviceType IPHONE_8 = new MobileDeviceType("iPhone 8", "Apple", "002ebf12-a125-5ddf-a739-67c3c5d20175", "iOS 11.4.1", MobileOS.IOS, 640, 1136, "Mozilla/5.0 (iPhone; CPU iPhone OS 9_3 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13E233 Safari/601.1", "armv8-A", "A1662", BrowserType.MOBILE_SAFARI_9, false, 2.0);
	public static final MobileDeviceType IPHONE_X = new MobileDeviceType("iPhone X", "Apple", "002ebf12-a125-5ddf-a739-67c3c5d20176", "iOS 12.1.4", MobileOS.IOS, 750, 1334, "Mozilla/5.0 (iPhone; CPU iPhone OS 10_0 like Mac OS X) AppleWebKit/602.1.38 (KHTML, like Gecko) Version/10.0 Mobile/14A5297c Safari/602.1", "Quad-core 2.34 GHz", "A1660", BrowserType.MOBILE_SAFARI_10, false, 2.0);
	public static final MobileDeviceType IPHONE_XR = new MobileDeviceType("iPhone XR", "Apple", "002ebf12-a125-5ddf-a739-67c3c5d20177", "iOS 12.1.4", MobileOS.IOS, 1080, 1920, "Mozilla/5.0 (iPhone; CPU iPhone OS 10_0 like Mac OS X) AppleWebKit/602.1.38 (KHTML, like Gecko) Version/10.0 Mobile/14A5297c Safari/602.1", "Quad-core 2.34 GHz", "A1661", BrowserType.MOBILE_SAFARI_10, false, 3.0);

	// Game consoles
	public static final MobileDeviceType NINTENDO_WII_U = new MobileDeviceType("Wii U", "Nintendo", "002ebf12-a125-5ddf-a739-67c3c5d20145", "Nintendo OS", null, 854, 480, "Mozilla/5.0 (Nintendo WiiU) AppleWebKit/536.30 (KHTML, like Gecko) NX/3.0.4.2.12 NintendoBrowser/4.3.1.11264.US", "1.24 GHz Tri-Core IBM PowerPC Espresso", "WiiU", BrowserType.NINTENDO_WII_U, true, 1.0);
	public static final MobileDeviceType NINTENDO_3DS = new MobileDeviceType("3DS", "Nintendo", "002ebf12-a125-5ddf-a739-67c3c5d20144", "Nintendo OS", null, 854, 480, "Mozilla/5.0 (Nintendo 3DS; U; ; en) Version/1.7412.EU", "Dual-Core ARM11 MPCore", "3DS", BrowserType.NINTENDO_3DS, true, 1.0);
	public static final MobileDeviceType PLAYSTATION_VITA = new MobileDeviceType("PS Vita", "Sony", "002ebf12-a125-5ddf-a739-67c3c5d20143", "PS Vita 3.61", null, 960, 544, "Mozilla/5.0 (PlayStation Vita 3.61) AppleWebKit/537.73 (KHTML, like Gecko) Silk/3.2", "ARM Cortex-A9 MPCore", "PCH-1000", BrowserType.PS_VITA, true, 1.5);


	private final String deviceType;
	private final String manufacturer;
	private final String deviceId;
	private final String os;
	private final MobileOS osType;
	private final int screenWidth;
	private final int screenHeight;
	private final String userAgent;
	private final String cpu;
	private final String modelId;
	private BrowserType browserType;
	private final boolean isTablet;
	private final double pixelRatio;

	private static Map<MobileDeviceType,String> deviceNames = null;

	public MobileDeviceType(String deviceType, String manufacturer, String deviceId, String os, MobileOS osType, int screenWidth, int screenHeight, String userAgent, String cpu, String modelId, BrowserType browserType, boolean isTablet, double pixelRatio) {
		super();
		this.deviceType = deviceType;
		this.manufacturer = manufacturer;
		this.deviceId = deviceId;
		this.os = os;
		this.osType = osType;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.userAgent = userAgent;
		this.cpu = cpu;
		this.modelId = modelId;
		this.browserType = browserType;
		this.isTablet = isTablet;
		this.pixelRatio = pixelRatio;
	}

	public String getScreeenResolution() {
		return screenWidth + "x" + screenHeight;
	}

	/**
	 * @return the deviceType
	 */
	public String getDeviceType() {
		return deviceType;
	}

	/**
	 * @return the manufacturer
	 */
	public String getManufacturer() {
		return manufacturer;
	}

	/**
	 * @return the os
	 */
	public String getOs() {
		return os;
	}

	/**
	 * @return the screenWidth
	 */
	public int getScreenWidth() {
		return screenWidth;
	}

	/**
	 * @return the screenHeight
	 */
	public int getScreenHeight() {
		return screenHeight;
	}

	/**
	 * @return the deviceId
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * @return the userAgent
	 */
	public String getUserAgent() {
		return userAgent;
	}

	public String getModelId() {
		return modelId;
	}

	public String getCpu() {
		return cpu;
	}

	public double getPixelRatio() {
		return pixelRatio;
	}

	public MobileOS getOsType() {
		return osType;
	}

	@Override
	public String toString() {
		return String.format("MobileDeviceType [deviceType=%s, manufacturer=%s, os=%s, modelId=%s, cpu=%s, pixelRatio=%f]", deviceType, manufacturer, os, modelId, cpu, pixelRatio);
	}

	public BrowserType getBrowserType() {
		return browserType;
	}

	public static String getDeviceFieldName(MobileDeviceType t) throws IllegalAccessException {
		if(deviceNames == null){
			Map<MobileDeviceType, String> bNames = new HashMap<>();
			for(Field field : MobileDeviceType.class.getDeclaredFields()){
				if((field.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) != 0 && MobileDeviceType.class == field.getType()){
					bNames.put((MobileDeviceType)field.get(null), field.getName());
				}
			}
			deviceNames=bNames;
		}
		return deviceNames.get(t);
	}

	public static MobileDeviceType getFieldByName(String name) throws NoSuchFieldException, IllegalAccessException {
		Field field = MobileDeviceType.class.getField(name);
		return (MobileDeviceType)field.get(null);
	}

	public boolean isTablet() {
		return isTablet;
	}

	public boolean isDeviceAppmonSupported() {
		return os.toLowerCase().contains("android") || os.toLowerCase().contains("ios");
	}
	
	public boolean isIOS() {
		return os.toLowerCase().startsWith(IOS);
	}
}
