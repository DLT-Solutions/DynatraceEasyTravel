package com.dynatrace.diagnostics.uemload;

import com.dynatrace.diagnostics.uemload.MobileDeviceMarketShare.DeviceManufacturer;
import com.dynatrace.diagnostics.uemload.mobile.MobileDeviceType;
import com.dynatrace.easytravel.util.DtVersionDetector;

/**
 * Mobile Device distribution for UEMLoad.
 *
 * @author Michal.Bakula
 *
 */

public class MobileDeviceDistribution {

	private MobileDeviceDistribution() {
		throw new IllegalAccessError("Utility class");
	}

	/**
	 * Sets Mobile device distribution according to given Market Share.
	 * Make sure that weights given for each manufacturer's devices sums up to equal/similar value.
	 *
	 * @param MobileDeviceMarketShare
	 * @return RandomSet<MobileDeviceType>
	 */
	private static RandomSet<MobileDeviceType> setMobileDeviceWeights(MobileDeviceMarketShare share) {
		MobileDeviceDistributionBuilder b = new MobileDeviceDistributionBuilder();

		// Samsung
		b.use(MobileDeviceType.SAMSUNG_GALAXY_TAB, share.getWeight(2, DeviceManufacturer.Samsung));
		b.use(MobileDeviceType.SAMSUNG_GALAXY_TAB_A, share.getWeight(8, DeviceManufacturer.Samsung));
		b.use(MobileDeviceType.SAMSUNG_GALAXY_TAB_S, share.getWeight(10, DeviceManufacturer.Samsung));
		b.use(MobileDeviceType.SAMSUNG_GALAXY_S9, share.getWeight(4, DeviceManufacturer.Samsung));
		b.use(MobileDeviceType.SAMSUNG_GALAXY_S8, share.getWeight(6, DeviceManufacturer.Samsung));
		b.use(MobileDeviceType.SAMSUNG_GALAXY_S5, share.getWeight(15, DeviceManufacturer.Samsung));
		b.use(MobileDeviceType.SAMSUNG_GALAXY_S6, share.getWeight(15, DeviceManufacturer.Samsung));
		b.use(MobileDeviceType.SAMSUNG_GALAXY_S6_EDGE, share.getWeight(20, DeviceManufacturer.Samsung));
		b.use(MobileDeviceType.SAMSUNG_GALAXY_S6_EDGE_PLUS, share.getWeight(5, DeviceManufacturer.Samsung));
		b.use(MobileDeviceType.SAMSUNG_GALAXY_S7, share.getWeight(15, DeviceManufacturer.Samsung));

		// Apple
		b.use(MobileDeviceType.APPLE_IPAD_PRO_2, share.getWeight(2, DeviceManufacturer.Apple));
		b.use(MobileDeviceType.IPAD_6, share.getWeight(2, DeviceManufacturer.Apple));
		b.use(MobileDeviceType.IPAD_MINI_2, share.getWeight(5, DeviceManufacturer.Apple));
		b.use(MobileDeviceType.IPAD_MINI_4, share.getWeight(10, DeviceManufacturer.Apple));
		b.use(MobileDeviceType.APPLE_IPHONE_8, share.getWeight(2, DeviceManufacturer.Apple));
		b.use(MobileDeviceType.APPLE_IPHONE_7_PLUS, share.getWeight(3, DeviceManufacturer.Apple));
		b.use(MobileDeviceType.IPHONE_6, share.getWeight(5, DeviceManufacturer.Apple));
		b.use(MobileDeviceType.IPHONE_7, share.getWeight(10, DeviceManufacturer.Apple));
		b.use(MobileDeviceType.IPHONE_7_PLUS, share.getWeight(10, DeviceManufacturer.Apple));
		b.use(MobileDeviceType.IPHONE_8, share.getWeight(20, DeviceManufacturer.Apple));
		b.use(MobileDeviceType.IPHONE_X, share.getWeight(20, DeviceManufacturer.Apple));
		b.use(MobileDeviceType.IPHONE_XR, share.getWeight(10, DeviceManufacturer.Apple));

		// LG
		b.use(MobileDeviceType.LG_PHOENIX_3, share.getWeight(20, DeviceManufacturer.LG));
		b.use(MobileDeviceType.LG_FORTUNE, share.getWeight(40, DeviceManufacturer.LG));
		b.use(MobileDeviceType.LG_G7, share.getWeight(30, DeviceManufacturer.LG));
		b.use(MobileDeviceType.LG_G_PAD_7, share.getWeight(10, DeviceManufacturer.LG));

		// Huawei
		b.use(MobileDeviceType.HUAWEI_P8_LITE, share.getWeight(10, DeviceManufacturer.Huawei));
		b.use(MobileDeviceType.HUAWEI_P9, share.getWeight(50, DeviceManufacturer.Huawei));
		b.use(MobileDeviceType.HUAWEI_P10, share.getWeight(20, DeviceManufacturer.Huawei));
		b.use(MobileDeviceType.HUAWEI_NEXUS_6P, share.getWeight(20, DeviceManufacturer.Huawei));

		// HTC
		b.use(MobileDeviceType.HTC_U12_LIFE, share.getWeight(30, DeviceManufacturer.HTC));
		b.use(MobileDeviceType.HTC_DESIRE_650, share.getWeight(40, DeviceManufacturer.HTC));
		b.use(MobileDeviceType.HTC_U_PLAY, share.getWeight(20, DeviceManufacturer.HTC));
		b.use(MobileDeviceType.HTC_ONE_M9, share.getWeight(10, DeviceManufacturer.HTC));

		// Xiaomi
		b.use(MobileDeviceType.XIAOMI_MI_5, share.getWeight(60, DeviceManufacturer.Xiaomi));
		b.use(MobileDeviceType.XIAOMI_NOTE_4, share.getWeight(40, DeviceManufacturer.Xiaomi));

		// Other
		b.use(MobileDeviceType.GOOGLE_PIXEL_C, share.getWeight(20, DeviceManufacturer.Other));
		b.use(MobileDeviceType.SONY_XPERIA_Z4_TAB, share.getWeight(20, DeviceManufacturer.Other));
		b.use(MobileDeviceType.SONY_XPERIA_Z5, share.getWeight(20, DeviceManufacturer.Other));
		b.use(MobileDeviceType.AMAZON_KINDLE_FIRE_HDX_7, share.getWeight(20, DeviceManufacturer.Other));
		b.use(MobileDeviceType.NVIDIA_SHIELD, share.getWeight(10, DeviceManufacturer.Other));
		b.use(MobileDeviceType.LG_G_PAD_7, share.getWeight(10, DeviceManufacturer.Other));

		return b.build();
	}

	public static RandomSet<MobileDeviceType> createDefaultMobileDeviceDistribution() {
		MobileDeviceMarketShare share = new MobileDeviceMarketShare.MobileDeviceMarketShareBuilder()
				.setSamsungShare(31.82)
				.setAppleShare(22.85)
				.setXiaomiShare(7.88)
				.setHuaweiShare(7.87)
				.setHtcShare(3.69)
				.setLgShare(3.06)
				.setOtherShare(22.83)
				.build();
		return setMobileDeviceWeights(share);
	}

	public static class MobileDeviceDistributionBuilder {
		private static final boolean IS_RUXIT = DtVersionDetector.isAPM();

		private RandomSet<MobileDeviceType> devices = new RandomSet<MobileDeviceType>();

		public MobileDeviceDistributionBuilder use(MobileDeviceType device, int weight) {
			if (IS_RUXIT || device.isDeviceAppmonSupported()) {
				devices.add(device, weight);
			}
			return this;
		}

		public RandomSet<MobileDeviceType> build() {
			return devices;
		}
	}
}
