package com.dynatrace.diagnostics.uemload.mobileopenkit.parameters;

import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.openkit.api.ConnectionType;

public class ConnectionTypeDistribution extends MobileParamDistribution {
	public static RandomSet<ConnectionType> get() {
		return create(new RandomSet.RandomSetEntry<>(ConnectionType.Wifi, 5), new RandomSet.RandomSetEntry<>(ConnectionType.Mobile, 3),
				new RandomSet.RandomSetEntry<>(ConnectionType.Offline, 2), new RandomSet.RandomSetEntry<>(ConnectionType.Lan, 1));
	}
}
