package com.dynatrace.diagnostics.uemload;

import java.util.Random;

/**
 * 
 * @author Michal.Bakula
 *
 */
public enum Bandwidth {

	DIALUP(56L, "connection:dialup"),
	DSL_LOW(384L, "connection:dsl_low"),
	DSL_MED(768L, "connection:dsl_medium"),
	ADSL_LITE(1496L, "ADSL Lite"),
	DSL_HIGH(1500L, "connection:dsl_high"),
	ADSL1(4000L, "ADSL1"),
	ETHERNET(10000L, "Ethernet"),
	ADSL2(24000L, "ADSL2+"),
	BROADBAND(50000L, "connection:broadband"),
	WIRELESS_802_11G(54000L, "Wireless 802.11g"),
	FAST_ETHERNET(100000L, "Fast Ethernet"),
	WIRELESS_802_11N(600000L, "Wireless 802.11n"),
	GIGABIT_ETHERNET(1000000L, "Gigabit Ethernet"),
	UNLIMITED(0, null); // if you do not want to use the bandwidth simulation	

	private final long bandwidthKiloBytesPerSecond;
	private String connectionName;

	Bandwidth(long bandwidthKiloBytesPerSecond, String connectionName) {
		this.bandwidthKiloBytesPerSecond = bandwidthKiloBytesPerSecond;
		this.connectionName = connectionName;
	}

	public long get() {
		return bandwidthKiloBytesPerSecond;
	}

	public String getConnectionBandwidth() {
		return connectionName;
	}

	public int getLatency() {
		Random random = new Random();
		switch (this) {
			case GIGABIT_ETHERNET:
			case WIRELESS_802_11N:
			case FAST_ETHERNET:
			case WIRELESS_802_11G:
				return random.nextInt(6) + 5; //5-10
			case BROADBAND:
				return random.nextInt(11) + 10; //10-20
			case ADSL2:
				return random.nextInt(16) + 15; //15-30
			case ETHERNET:
				return random.nextInt(21) + 15; //20-35
			case ADSL1:
				return random.nextInt(26) + 15; //25-40
			case DSL_HIGH:
			case ADSL_LITE:
				return random.nextInt(31) + 20; //20-50
			case DSL_MED:
				return random.nextInt(31) + 30; //30-60
			case DSL_LOW:
				return random.nextInt(151) + 50; //50-200
			case DIALUP:
				return random.nextInt(201) + 100; //100-300
			default:
				return 0;
		}
	}
	
	public long getBandwidthInBytesPerSecond() {
		return bandwidthKiloBytesPerSecond * 1000 / 8;
	}
}
