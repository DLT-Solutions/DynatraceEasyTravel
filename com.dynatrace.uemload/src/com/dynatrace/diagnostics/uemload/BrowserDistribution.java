package com.dynatrace.diagnostics.uemload;

import com.dynatrace.diagnostics.uemload.BrowserMarketShare.BrowserManufacturer;

/**
 * Browser distribution for UEMLoad.
 * 
 * @author Michal.Bakula
 *
 */

public class BrowserDistribution {
	
	private BrowserDistribution(){
		throw new IllegalAccessError("Utility class");
	}

	private static RandomSet<BrowserType> setDesktopBrowserWeights(BrowserMarketShare share) {
		BrowserDistributionBuilder b = new BrowserDistributionBuilder();

		// Chrome
		b.use(BrowserType.CHROME_36, share.getWeight(1 , BrowserManufacturer.Chrome));
		b.use(BrowserType.CHROME_37, share.getWeight(1 , BrowserManufacturer.Chrome));
		b.use(BrowserType.CHROME_38, share.getWeight(1 , BrowserManufacturer.Chrome));
		b.use(BrowserType.CHROME_39, share.getWeight(1 , BrowserManufacturer.Chrome));
		b.use(BrowserType.CHROME_40, share.getWeight(1 , BrowserManufacturer.Chrome));
		b.use(BrowserType.CHROME_41, share.getWeight(1 , BrowserManufacturer.Chrome));
		b.use(BrowserType.CHROME_42, share.getWeight(3  , BrowserManufacturer.Chrome));
		b.use(BrowserType.CHROME_43, share.getWeight(3 , BrowserManufacturer.Chrome));
		b.use(BrowserType.CHROME_44, share.getWeight(3 , BrowserManufacturer.Chrome));
		b.use(BrowserType.CHROME_45, share.getWeight(3 , BrowserManufacturer.Chrome));
		b.use(BrowserType.CHROME_46, share.getWeight(4  , BrowserManufacturer.Chrome));
		b.use(BrowserType.CHROME_47, share.getWeight(4 , BrowserManufacturer.Chrome));
		b.use(BrowserType.CHROME_48, share.getWeight(4 , BrowserManufacturer.Chrome));
		b.use(BrowserType.CHROME_49, share.getWeight(3 , BrowserManufacturer.Chrome));
		b.use(BrowserType.CHROME_50, share.getWeight(15 , BrowserManufacturer.Chrome));
		b.use(BrowserType.CHROME_51, share.getWeight(8 , BrowserManufacturer.Chrome));
		b.use(BrowserType.CHROME_52, share.getWeight(10 , BrowserManufacturer.Chrome));
		b.use(BrowserType.CHROME_53, share.getWeight(11 , BrowserManufacturer.Chrome));
		b.use(BrowserType.CHROME_54, share.getWeight(23 , BrowserManufacturer.Chrome));
		b.use(BrowserType.CHROME_55, share.getWeight(467 , BrowserManufacturer.Chrome));
		b.use(BrowserType.CHROME_56, share.getWeight(429 , BrowserManufacturer.Chrome));
		b.use(BrowserType.CHROME_57, share.getWeight(5 , BrowserManufacturer.Chrome));

		// Firefox
		b.use(BrowserType.FF_380, share.getWeight(3 , BrowserManufacturer.Firefox));
		b.use(BrowserType.FF_390, share.getWeight(3 , BrowserManufacturer.Firefox));
		b.use(BrowserType.FF_400, share.getWeight(6 , BrowserManufacturer.Firefox));
		b.use(BrowserType.FF_410, share.getWeight(6  , BrowserManufacturer.Firefox));
		b.use(BrowserType.FF_420, share.getWeight(13 , BrowserManufacturer.Firefox));
		b.use(BrowserType.FF_430, share.getWeight(13 , BrowserManufacturer.Firefox));
		b.use(BrowserType.FF_440, share.getWeight(19 , BrowserManufacturer.Firefox));
		b.use(BrowserType.FF_450, share.getWeight(19 , BrowserManufacturer.Firefox));
		b.use(BrowserType.FF_460, share.getWeight(89 , BrowserManufacturer.Firefox));
		b.use(BrowserType.FF_470, share.getWeight(13 , BrowserManufacturer.Firefox));
		b.use(BrowserType.FF_480, share.getWeight(44 , BrowserManufacturer.Firefox));
		b.use(BrowserType.FF_490, share.getWeight(25 , BrowserManufacturer.Firefox));
		b.use(BrowserType.FF_500, share.getWeight(44 , BrowserManufacturer.Firefox));
		b.use(BrowserType.FF_510, share.getWeight(625 , BrowserManufacturer.Firefox));
		b.use(BrowserType.FF_520, share.getWeight(38 , BrowserManufacturer.Firefox));
		b.use(BrowserType.FF_530, share.getWeight(13 , BrowserManufacturer.Firefox));

		// IE
		b.use(BrowserType.IE_6, share.getWeight(15 , BrowserManufacturer.IE));
		b.use(BrowserType.IE_7, share.getWeight(15 , BrowserManufacturer.IE));
		b.use(BrowserType.IE_8, share.getWeight(29 , BrowserManufacturer.IE));
		b.use(BrowserType.IE_9, share.getWeight(29 , BrowserManufacturer.IE));
		b.use(BrowserType.IE_10, share.getWeight(29 , BrowserManufacturer.IE));
		b.use(BrowserType.IE_11, share.getWeight(882 , BrowserManufacturer.IE));

		// EDGE
		b.use(BrowserType.EDGE_12, share.getWeight(77 , BrowserManufacturer.Edge));
		b.use(BrowserType.EDGE_13, share.getWeight(154 , BrowserManufacturer.Edge));
		b.use(BrowserType.EDGE_14, share.getWeight(769 , BrowserManufacturer.Edge));

		// Safari
		b.use(BrowserType.SAFARI_4, share.getWeight(14 , BrowserManufacturer.Safari));
		b.use(BrowserType.SAFARI_5, share.getWeight(14 , BrowserManufacturer.Safari));
		b.use(BrowserType.SAFARI_6, share.getWeight(28 , BrowserManufacturer.Safari));
		b.use(BrowserType.SAFARI_7, share.getWeight(56 , BrowserManufacturer.Safari));
		b.use(BrowserType.SAFARI_8, share.getWeight(56 , BrowserManufacturer.Safari));
		b.use(BrowserType.SAFARI_9, share.getWeight(222 , BrowserManufacturer.Safari));
		b.use(BrowserType.SAFARI_10, share.getWeight(611 , BrowserManufacturer.Safari));

		// Opera
		b.use(BrowserType.OPERA_35, share.getWeight(42 , BrowserManufacturer.Opera));
		b.use(BrowserType.OPERA_36, share.getWeight(42 , BrowserManufacturer.Opera));
		b.use(BrowserType.OPERA_37, share.getWeight(83 , BrowserManufacturer.Opera));
		b.use(BrowserType.OPERA_38, share.getWeight(83 , BrowserManufacturer.Opera));
		b.use(BrowserType.OPERA_39, share.getWeight(83 , BrowserManufacturer.Opera));
		b.use(BrowserType.OPERA_40, share.getWeight(167 , BrowserManufacturer.Opera));
		b.use(BrowserType.OPERA_41, share.getWeight(333 , BrowserManufacturer.Opera));
		b.use(BrowserType.OPERA_42, share.getWeight(167 , BrowserManufacturer.Opera));
		
		// Other
		b.use(BrowserType.X_BOX_ONE, share.getWeight(50, BrowserManufacturer.Other));
		b.use(BrowserType.PS_4, share.getWeight(50, BrowserManufacturer.Other));

		return b.build();
	}

	private static RandomSet<BrowserType> setMobileBrowserWeights(BrowserMarketShare share) {
		BrowserDistributionBuilder b = new BrowserDistributionBuilder();

		// Mobile Safari
		b.use(BrowserType.MOBILE_SAFARI_4, share.getWeight(14, BrowserManufacturer.Safari));
		b.use(BrowserType.MOBILE_SAFARI_5, share.getWeight(14, BrowserManufacturer.Safari));
		b.use(BrowserType.MOBILE_SAFARI_6, share.getWeight(27, BrowserManufacturer.Safari));
		b.use(BrowserType.MOBILE_SAFARI_7, share.getWeight(53, BrowserManufacturer.Safari));
		b.use(BrowserType.MOBILE_SAFARI_8, share.getWeight(53, BrowserManufacturer.Safari));
		b.use(BrowserType.MOBILE_SAFARI_9, share.getWeight(210, BrowserManufacturer.Safari));
		b.use(BrowserType.MOBILE_SAFARI_10, share.getWeight(580, BrowserManufacturer.Safari));
		// Tablets with iOS
		b.use(BrowserType.MOBILE_SAFARI_6_IPAD, share.getWeight(1, BrowserManufacturer.Safari));
		b.use(BrowserType.MOBILE_SAFARI_7_IPAD, share.getWeight(3, BrowserManufacturer.Safari));
		b.use(BrowserType.MOBILE_SAFARI_8_IPAD, share.getWeight(3, BrowserManufacturer.Safari));
		b.use(BrowserType.MOBILE_SAFARI_9_IPAD, share.getWeight(12, BrowserManufacturer.Safari));
		b.use(BrowserType.MOBILE_SAFARI_10_IPAD, share.getWeight(31, BrowserManufacturer.Safari));

		// Mobile Chrome
		b.use(BrowserType.MOBILE_CHROME_48, share.getWeight(4, BrowserManufacturer.Chrome));
		b.use(BrowserType.MOBILE_CHROME_49, share.getWeight(3, BrowserManufacturer.Chrome));
		b.use(BrowserType.MOBILE_CHROME_50, share.getWeight(15, BrowserManufacturer.Chrome));
		b.use(BrowserType.MOBILE_CHROME_51, share.getWeight(8, BrowserManufacturer.Chrome));
		b.use(BrowserType.MOBILE_CHROME_52, share.getWeight(10, BrowserManufacturer.Chrome));
		b.use(BrowserType.MOBILE_CHROME_53, share.getWeight(11, BrowserManufacturer.Chrome));
		b.use(BrowserType.MOBILE_CHROME_54, share.getWeight(24, BrowserManufacturer.Chrome));
		b.use(BrowserType.MOBILE_CHROME_55, share.getWeight(479, BrowserManufacturer.Chrome));
		b.use(BrowserType.MOBILE_CHROME_56, share.getWeight(24, BrowserManufacturer.Chrome));
		b.use(BrowserType.MOBILE_CHROME_57, share.getWeight(6, BrowserManufacturer.Chrome));
		b.use(BrowserType.TABLET_CHROME_54, share.getWeight(2, BrowserManufacturer.Chrome));
		b.use(BrowserType.TABLET_CHROME_55, share.getWeight(24, BrowserManufacturer.Chrome));
		b.use(BrowserType.TABLET_CHROME_56, share.getWeight(2, BrowserManufacturer.Chrome));
		b.use(BrowserType.TABLET_CHROME_57, share.getWeight(1, BrowserManufacturer.Chrome));

		// Mobile UC
		b.use(BrowserType.MOBILE_UC_9, share.getWeight(29, BrowserManufacturer.UC));
		b.use(BrowserType.MOBILE_UC_10, share.getWeight(245, BrowserManufacturer.UC));
		b.use(BrowserType.MOBILE_UC_11, share.getWeight(674, BrowserManufacturer.UC));
		b.use(BrowserType.TABLET_UC_9, share.getWeight(3, BrowserManufacturer.UC));
		b.use(BrowserType.TABLET_UC_10, share.getWeight(13, BrowserManufacturer.UC));
		b.use(BrowserType.TABLET_UC_11, share.getWeight(36, BrowserManufacturer.UC));

		// Mobile Opera
		b.use(BrowserType.MOBILE_OPERA_39, share.getWeight(30, BrowserManufacturer.Opera));
		b.use(BrowserType.MOBILE_OPERA_40, share.getWeight(61, BrowserManufacturer.Opera));
		b.use(BrowserType.MOBILE_OPERA_41, share.getWeight(242, BrowserManufacturer.Opera));
		b.use(BrowserType.MOBILE_OPERA_42, share.getWeight(667, BrowserManufacturer.Opera));

		// Android
		b.use(BrowserType.ANDROID_2_4, share.getWeight(13, BrowserManufacturer.Android));
		b.use(BrowserType.ANDROID_4_1, share.getWeight(50, BrowserManufacturer.Android));
		b.use(BrowserType.ANDROID_4_2, share.getWeight(69, BrowserManufacturer.Android));
		b.use(BrowserType.ANDROID_4_3, share.getWeight(21, BrowserManufacturer.Android));
		b.use(BrowserType.ANDROID_4_4, share.getWeight(248, BrowserManufacturer.Android));
		b.use(BrowserType.ANDROID_5_0, share.getWeight(117, BrowserManufacturer.Android));
		b.use(BrowserType.ANDROID_5_1, share.getWeight(196, BrowserManufacturer.Android));
		b.use(BrowserType.ANDROID_6_0, share.getWeight(168, BrowserManufacturer.Android));
		b.use(BrowserType.ANDROID_7_0, share.getWeight(90, BrowserManufacturer.Android));
		b.use(BrowserType.ANDROID_7_1, share.getWeight(28, BrowserManufacturer.Android));
		// Tablets with Android
		b.use(BrowserType.GOOGLE_PIXEL_C, share.getWeight(20, BrowserManufacturer.Android));
		b.use(BrowserType.SONY_XPERIA_Z4_TAB, share.getWeight(20, BrowserManufacturer.Android));
		b.use(BrowserType.NVIDIA_SHIELD, share.getWeight(20, BrowserManufacturer.Android));
		b.use(BrowserType.AMAZON_KINDLE_FIRE_HDX_7, share.getWeight(20, BrowserManufacturer.Android));
		b.use(BrowserType.LG_G_PAD_7, share.getWeight(20, BrowserManufacturer.Android));
		b.use(BrowserType.SAMSUNG_GALAXY_TAB_A, share.getWeight(20, BrowserManufacturer.Android));
		b.use(BrowserType.SAMSUNG_GALAXY_TAB_S, share.getWeight(20, BrowserManufacturer.Android));

		// Samsung Internet
		b.use(BrowserType.MOBILE_SAMSUNG_3, share.getWeight(57, BrowserManufacturer.Samsung));
		b.use(BrowserType.MOBILE_SAMSUNG_4, share.getWeight(237, BrowserManufacturer.Samsung));
		b.use(BrowserType.MOBILE_SAMSUNG_5, share.getWeight(653, BrowserManufacturer.Samsung));
		// Samsung Internet
		b.use(BrowserType.TABLET_SAMSUNG_3, share.getWeight(4, BrowserManufacturer.Samsung));
		b.use(BrowserType.TABLET_SAMSUNG_4, share.getWeight(13, BrowserManufacturer.Samsung));
		b.use(BrowserType.TABLET_SAMSUNG_5, share.getWeight(35, BrowserManufacturer.Samsung));

		// Mobile IE
		b.use(BrowserType.MOBILE_IE_7, share.getWeight(15, BrowserManufacturer.IE));
		b.use(BrowserType.MOBILE_IE_8, share.getWeight(15, BrowserManufacturer.IE));
		b.use(BrowserType.MOBILE_IE_9, share.getWeight(29, BrowserManufacturer.IE));
		b.use(BrowserType.MOBILE_IE_10, share.getWeight(59, BrowserManufacturer.IE));
		b.use(BrowserType.MOBILE_IE_11, share.getWeight(235, BrowserManufacturer.IE));
		b.use(BrowserType.MOBILE_IE_11_1, share.getWeight(647, BrowserManufacturer.IE));

		// Mobile EDGE
		b.use(BrowserType.MOBILE_EDGE_12, share.getWeight(63, BrowserManufacturer.Edge));
		b.use(BrowserType.MOBILE_EDGE_13, share.getWeight(250, BrowserManufacturer.Edge));
		b.use(BrowserType.MOBILE_EDGE_14, share.getWeight(688, BrowserManufacturer.Edge));
		
		// Other - Consoles, E-readers
		b.use(BrowserType.NINTENDO_WII_U, share.getWeight(10, BrowserManufacturer.Other));
		b.use(BrowserType.NINTENDO_3DS, share.getWeight(5, BrowserManufacturer.Other));
		b.use(BrowserType.PS_VITA, share.getWeight(5, BrowserManufacturer.Other));
		b.use(BrowserType.AMAZON_KINDLE_3, share.getWeight(40, BrowserManufacturer.Other));
		b.use(BrowserType.AMAZON_KINDLE_4, share.getWeight(40, BrowserManufacturer.Other));

		return b.build();
	}

	/**
	 * Creates default browser distribution
	 * 
	 * @param boolean
	 *            onlyMobile
	 * @return RandomSet<BrowserType>
	 */
	public static RandomSet<BrowserType> createDefaultBrowserDistribution(boolean onlyMobile) {
		if (!onlyMobile) {
			BrowserMarketShare share = new BrowserMarketShare.BrowserMarketShareBuilder()
					.setChromeShare(61.7)
					.setFirefoxShare(15.31)
					.setIEShare(11.45)
					.setSafariShare(4.87)
					.setEdgeShare(2.85)
					.setOperaShare(1.6)
					.setOtherShare(2)
					.build();
			return setDesktopBrowserWeights(share);
		} else {
			BrowserMarketShare share = new BrowserMarketShare.BrowserMarketShareBuilder()
					.setChromeShare(37.68)
					.setSafariShare(22.22)
					.setUCShare(15.71)
					.setOperaShare(8.6)
					.setAndroidShare(7.47)
					.setSamsungShare(5.37)
					.setIEShare(1.16)
					.setOtherShare(2)
					.build();
			return setMobileBrowserWeights(share);
		}
	}

	/**
	 * Creates browser distribution for Europe
	 * 
	 * @param boolean
	 *            onlyMobile
	 * @return RandomSet<BrowserType>
	 */
	public static RandomSet<BrowserType> createDefaultBrowserDistributionEurope(boolean onlyMobile) {
		if (!onlyMobile) {
			BrowserMarketShare share = new BrowserMarketShare.BrowserMarketShareBuilder()
					.setChromeShare(53.37)
					.setFirefoxShare(21.14)
					.setIEShare(10.86)
					.setSafariShare(5.91)
					.setEdgeShare(3.63)
					.setOperaShare(2.89)
					.setOtherShare(2)
					.build();
			return setDesktopBrowserWeights(share);
		} else {
			BrowserMarketShare share = new BrowserMarketShare.BrowserMarketShareBuilder()
					.setChromeShare(44.23)
					.setSafariShare(33.27)
					.setUCShare(1.05)
					.setOperaShare(1.45)
					.setAndroidShare(8.74)
					.setSamsungShare(7.49)
					.setIEShare(2.14)
					.setEdgeShare(0.38)
					.setOtherShare(2)
					.build();
			return setMobileBrowserWeights(share);
		}
	}

	/**
	 * Creates browser distribution for North America
	 * 
	 * @param boolean
	 *            onlyMobile
	 * @return RandomSet<BrowserType>
	 */
	public static RandomSet<BrowserType> createDefaultBrowserDistributionNorthAmerica(boolean onlyMobile) {
		if (!onlyMobile) {
			BrowserMarketShare share = new BrowserMarketShare.BrowserMarketShareBuilder()
					.setChromeShare(54.86)
					.setFirefoxShare(13.14)
					.setIEShare(17.33)
					.setSafariShare(8.41)
					.setEdgeShare(4.33)
					.setOperaShare(0.6)
					.setOtherShare(2)
					.build();
			return setDesktopBrowserWeights(share);
		} else {
			BrowserMarketShare share = new BrowserMarketShare.BrowserMarketShareBuilder()
					.setChromeShare(34.45)
					.setSafariShare(50.98)
					.setUCShare(0.93)
					.setOperaShare(0.85)
					.setAndroidShare(5.52)
					.setSamsungShare(5.27)
					.setIEShare(0.74)
					.setOtherShare(2)
					.build();
			return setMobileBrowserWeights(share);
		}
	}

	/**
	 * Creates browser distribution for Asia
	 * 
	 * @param boolean
	 *            onlyMobile
	 * @return RandomSet<BrowserType>
	 */
	public static RandomSet<BrowserType> createDefaultBrowserDistributionAsia(boolean onlyMobile) {
		if (!onlyMobile) {
			BrowserMarketShare share = new BrowserMarketShare.BrowserMarketShareBuilder()
					.setChromeShare(70.56)
					.setFirefoxShare(12.16)
					.setIEShare(9.25)
					.setSafariShare(1.91)
					.setEdgeShare(1.49)
					.setOperaShare(1.21)
					.setOtherShare(2)
					.build();
			return setDesktopBrowserWeights(share);
		} else {
			BrowserMarketShare share = new BrowserMarketShare.BrowserMarketShareBuilder()
					.setChromeShare(36.84)
					.setSafariShare(11.83)
					.setUCShare(27.33)
					.setOperaShare(8.64)
					.setAndroidShare(7.52)
					.setSamsungShare(5.13)
					.setIEShare(0.81)
					.setOtherShare(2)
					.build();
			return setMobileBrowserWeights(share);
		}
	}

	/**
	 * Creates browser distribution for South America
	 * 
	 * @param boolean
	 *            onlyMobile
	 * @return RandomSet<BrowserType>
	 */
	public static RandomSet<BrowserType> createDefaultBrowserDistributionSouthAmerica(boolean onlyMobile) {
		if (!onlyMobile) {
			BrowserMarketShare share = new BrowserMarketShare.BrowserMarketShareBuilder()
					.setChromeShare(80.05)
					.setFirefoxShare(10.53)
					.setIEShare(4.41)
					.setSafariShare(2)
					.setEdgeShare(1.25)
					.setOperaShare(0.88)
					.setOtherShare(2)
					.build();
			return setDesktopBrowserWeights(share);
		} else {
			BrowserMarketShare share = new BrowserMarketShare.BrowserMarketShareBuilder()
					.setChromeShare(64.56)
					.setSafariShare(10.63)
					.setUCShare(1.44)
					.setOperaShare(3.62)
					.setAndroidShare(11.07)
					.setSamsungShare(3.77)
					.setIEShare(3.49)
					.setEdgeShare(0.28)
					.setOtherShare(2)
					.build();
			return setMobileBrowserWeights(share);
		}
	}

	/**
	 * Creates browser distribution for Oceania
	 * 
	 * @param boolean
	 *            onlyMobile
	 * @return RandomSet<BrowserType>
	 */
	public static RandomSet<BrowserType> createDefaultBrowserDistributionOceanic(boolean onlyMobile) {
		if (!onlyMobile) {
			BrowserMarketShare share = new BrowserMarketShare.BrowserMarketShareBuilder()
					.setChromeShare(56.88)
					.setFirefoxShare(12.5)
					.setIEShare(13.99)
					.setSafariShare(10.17)
					.setEdgeShare(5.08)
					.setOperaShare(0.43)
					.setOtherShare(2)
					.build();
			return setDesktopBrowserWeights(share);
		} else {
			BrowserMarketShare share = new BrowserMarketShare.BrowserMarketShareBuilder()
					.setChromeShare(25.01)
					.setSafariShare(60.03)
					.setUCShare(2.29)
					.setOperaShare(0.88)
					.setAndroidShare(3.81)
					.setSamsungShare(6.34)
					.setIEShare(0.72)
					.setEdgeShare(0.15)
					.setOtherShare(2)
					.build();
			return setMobileBrowserWeights(share);
		}
	}

	/**
	 * Creates browser distribution for Africa
	 * 
	 * @param boolean
	 *            onlyMobile
	 * @return RandomSet<BrowserType>
	 */
	public static RandomSet<BrowserType> createDefaultBrowserDistributionAfrica(boolean onlyMobile) {
		if (!onlyMobile) {
			BrowserMarketShare share = new BrowserMarketShare.BrowserMarketShareBuilder()
					.setChromeShare(58.41)
					.setFirefoxShare(23.66)
					.setIEShare(9.26)
					.setSafariShare(2.53)
					.setEdgeShare(1.44)
					.setOperaShare(4.02)
					.setOtherShare(2)
					.build();
			return setDesktopBrowserWeights(share);
		} else {
			BrowserMarketShare share = new BrowserMarketShare.BrowserMarketShareBuilder()
					.setChromeShare(20.78)
					.setSafariShare(4.22)
					.setUCShare(6.42)
					.setOperaShare(52.33)
					.setAndroidShare(7.71)
					.setSamsungShare(3.66)
					.setIEShare(1.54)
					.setOtherShare(2)
					.build();
			return setMobileBrowserWeights(share);
		}
	}

	public static RandomSet<BrowserType> createDefaultRobotBrowserDistribution() {
		BrowserDistributionBuilder b = new BrowserDistributionBuilder();

		// Bots
		b.use(BrowserType.GOOGLEBOT, 19191);
		b.use(BrowserType.BAIDU_SPIDER, 73);
		b.use(BrowserType.BING_BOT, 1234);
		b.use(BrowserType.UPTIME_ROBOT, 100);
		b.use(BrowserType.YANDEX_BOT, 100);
		b.use(BrowserType.YAHOO_SLURP, 5478);

		return b.build();
	}
	
	public static RandomSet<BrowserType> createDefaultSyntheticBrowserDistribution() {
		BrowserDistributionBuilder b = new BrowserDistributionBuilder();

		// Synthetics
		b.use(BrowserType.GOMEZ_AGENT, 50);
		b.use(BrowserType.KEYNOTE_KTXN, 25);
		b.use(BrowserType.KEYNOTE_KHTE, 25);

		return b.build();
	}

	public static class BrowserDistributionBuilder {

		private RandomSet<BrowserType> browsers = new RandomSet<BrowserType>();

		public BrowserDistributionBuilder use(BrowserType browser, int weight) {
			browsers.add(browser, weight);
			return this;
		}

		public RandomSet<BrowserType> build() {
			return browsers;
		}
	}
}
