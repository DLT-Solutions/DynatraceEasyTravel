package com.dynatrace.diagnostics.uemload;

import com.dynatrace.easytravel.constants.BaseConstants.Uem;

public class ConnectionTypeDistribution {
	
	private ConnectionTypeDistribution() {
		throw new IllegalAccessError("Utility class");
	}

	public static RandomSet<String> createDefaultConnectionTypeDistribution() {
		ConnectionTypeDistributionBuilder b = new ConnectionTypeDistributionBuilder();
		b.use(Uem.CONNECTION_TYPE_WIFI, 5);
		b.use(Uem.CONNECTION_TYPE_MOBILE, 2);
		return b.build();
	}

	public static class ConnectionTypeDistributionBuilder {

		private RandomSet<String> connectionTypes = new RandomSet<String>();

		public ConnectionTypeDistributionBuilder use(String connectionType, int weight) {
			connectionTypes.add(connectionType, weight);
			return this;
		}

		public RandomSet<String> build() {
			return connectionTypes;
		}
	}
}
