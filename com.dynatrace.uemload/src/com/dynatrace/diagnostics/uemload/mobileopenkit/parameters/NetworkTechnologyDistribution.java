package com.dynatrace.diagnostics.uemload.mobileopenkit.parameters;

import com.dynatrace.diagnostics.uemload.RandomSet;

public class NetworkTechnologyDistribution extends MobileParamDistribution {
	public static RandomSet<NetworkTechnologyType> get() {
		return create(new RandomSet.RandomSetEntry<>(NetworkTechnologyType.WIRELESS_2G, 2), new RandomSet.RandomSetEntry<>(NetworkTechnologyType.WIRELESS_3G, 6),
				new RandomSet.RandomSetEntry<>(NetworkTechnologyType.WIRELESS_4G, 8), new RandomSet.RandomSetEntry<>(NetworkTechnologyType.WIRELESS_4G_LTE, 8),
				new RandomSet.RandomSetEntry<>(NetworkTechnologyType.WIRELESS_5G, 4), new RandomSet.RandomSetEntry<>(NetworkTechnologyType.WIRED_802_11x, 2),
				new RandomSet.RandomSetEntry<>(NetworkTechnologyType.OFFLINE, 1));
	}
}
