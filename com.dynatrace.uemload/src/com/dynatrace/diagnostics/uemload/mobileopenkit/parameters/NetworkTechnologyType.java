package com.dynatrace.diagnostics.uemload.mobileopenkit.parameters;

public enum NetworkTechnologyType {
	WIRELESS_2G("2G"),
	WIRELESS_3G("3G"),
	WIRELESS_4G("4G"),
	WIRELESS_4G_LTE("4G LTE"),
	WIRELESS_5G("5G"),
	WIRED_802_11x("802.11x"),
	OFFLINE("Offline");

	public final String value;

	NetworkTechnologyType(String value) {
		this.value = value;
	}
}
