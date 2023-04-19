package com.dynatrace.diagnostics.uemload.mobileopenkit.parameters;

import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.openkit.OpenKitParams;
import com.dynatrace.openkit.AgentTechnologyType;
import com.dynatrace.openkit.CrashReportingLevel;
import com.dynatrace.openkit.DataCollectionLevel;
import com.dynatrace.openkit.api.ConnectionType;

public class MobileOpenKitParams extends OpenKitParams {

	private final RandomSet<ConnectionType> connectionTypes = ConnectionTypeDistribution.get();
	private final RandomSet<NetworkTechnologyType> networkTechnologyTypes = NetworkTechnologyDistribution.get();

	public ConnectionType getRandomConnectionType() {
		return connectionTypes.getRandom();
	}

	public NetworkTechnologyType getRandomNetworkTechnologyType() {
		return networkTechnologyTypes.getRandom();
	}
}
