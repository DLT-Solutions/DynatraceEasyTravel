package com.dynatrace.diagnostics.uemload;

import com.dynatrace.easytravel.util.DtVersionDetector;

public class BandwidthDistribution {

	private BandwidthDistribution() {
		throw new IllegalAccessError("Utility class");
	}

	public static RandomSet<Bandwidth> createDefaultBandwidthDistribution() {
		BandwidthDistributionBuilder b = new BandwidthDistributionBuilder();
		b.use(Bandwidth.DIALUP, 1)
			.use(Bandwidth.DSL_LOW, 2)
			.use(Bandwidth.DSL_MED, 4)
			.use(Bandwidth.ADSL_LITE, 5)
			.use(Bandwidth.DSL_HIGH, 9)
			.use(Bandwidth.ADSL1, 49)
			.use(Bandwidth.ETHERNET, 521)
			.use(Bandwidth.ADSL2, 40)
			.use(Bandwidth.BROADBAND, 38)
			.use(Bandwidth.WIRELESS_802_11G, 25)
			.use(Bandwidth.FAST_ETHERNET, 20)
			.use(Bandwidth.WIRELESS_802_11N, 15)
			.use(Bandwidth.GIGABIT_ETHERNET, 10);
		return b.build();
	}

	public static RandomSet<Bandwidth> createDefaultBandwidthDistributionAfrica() {
		BandwidthDistributionBuilder b = new BandwidthDistributionBuilder();
		b.use(Bandwidth.DIALUP, 1)
			.use(Bandwidth.DSL_LOW, 2)
			.use(Bandwidth.DSL_MED, 3)
			.use(Bandwidth.ADSL_LITE, 3)
			.use(Bandwidth.DSL_HIGH, 3)
			.use(Bandwidth.ADSL1, 30)
			.use(Bandwidth.ETHERNET,25)
			.use(Bandwidth.ADSL2, 18)
			.use(Bandwidth.BROADBAND, 14)
			.use(Bandwidth.WIRELESS_802_11G, 10)
			.use(Bandwidth.FAST_ETHERNET, 5)
			.use(Bandwidth.WIRELESS_802_11N, 2)
			.use(Bandwidth.GIGABIT_ETHERNET, 1);
		return b.build();
	}

	public static RandomSet<Bandwidth> createDefaultBandwidthDistributionAsia() {
		BandwidthDistributionBuilder b = new BandwidthDistributionBuilder();
		if(DtVersionDetector.isAPM()){
			/*
			 * Ruxit
			 */
			b.use(Bandwidth.DIALUP, 1)
				.use(Bandwidth.DSL_LOW, 2)
				.use(Bandwidth.DSL_MED, 3)
				.use(Bandwidth.ADSL_LITE, 3)
				.use(Bandwidth.DSL_HIGH, 3)
				.use(Bandwidth.ADSL1, 54)
				.use(Bandwidth.ETHERNET, 60)
				.use(Bandwidth.ADSL2, 88)
				.use(Bandwidth.BROADBAND, 70)
				.use(Bandwidth.WIRELESS_802_11G, 44)
				.use(Bandwidth.FAST_ETHERNET, 25)
				.use(Bandwidth.WIRELESS_802_11N, 18)
				.use(Bandwidth.GIGABIT_ETHERNET, 14);
		}
		else{
			/*
			 * AppMon
			 */
			b.use(Bandwidth.DIALUP, 2)
				.use(Bandwidth.DSL_LOW, 3)
				.use(Bandwidth.DSL_MED, 4)
				.use(Bandwidth.ADSL_LITE, 4)
				.use(Bandwidth.DSL_HIGH, 6)
				.use(Bandwidth.ADSL1, 54)
				.use(Bandwidth.ETHERNET, 58)
				.use(Bandwidth.ADSL2, 70)
				.use(Bandwidth.BROADBAND, 50)
				.use(Bandwidth.WIRELESS_802_11G, 39)
				.use(Bandwidth.FAST_ETHERNET, 36)
				.use(Bandwidth.WIRELESS_802_11N, 14)
				.use(Bandwidth.GIGABIT_ETHERNET, 10);
		}
		return b.build();
	}

	public static RandomSet<Bandwidth> createDefaultBandwidthDistributionEurope() {
		BandwidthDistributionBuilder b = new BandwidthDistributionBuilder();
		b.use(Bandwidth.DIALUP, 1)
			.use(Bandwidth.DSL_LOW, 2)
			.use(Bandwidth.DSL_MED, 2)
			.use(Bandwidth.ADSL_LITE, 3)
			.use(Bandwidth.DSL_HIGH, 4)
			.use(Bandwidth.ADSL1, 54)
			.use(Bandwidth.ETHERNET, 60)
			.use(Bandwidth.ADSL2, 88)
			.use(Bandwidth.BROADBAND, 70)
			.use(Bandwidth.WIRELESS_802_11G, 44)
			.use(Bandwidth.FAST_ETHERNET, 25)
			.use(Bandwidth.WIRELESS_802_11N, 18)
			.use(Bandwidth.GIGABIT_ETHERNET, 14);
		return b.build();
	}

	public static RandomSet<Bandwidth> createDefaultBandwidthDistributionNorthAmerica() {
		BandwidthDistributionBuilder b = new BandwidthDistributionBuilder();
		b.use(Bandwidth.DIALUP, 1)
			.use(Bandwidth.DSL_LOW, 2)
			.use(Bandwidth.DSL_MED, 2)
			.use(Bandwidth.ADSL_LITE, 3)
			.use(Bandwidth.DSL_HIGH, 4)
			.use(Bandwidth.ADSL1, 54)
			.use(Bandwidth.ETHERNET, 60)
			.use(Bandwidth.ADSL2, 88)
			.use(Bandwidth.BROADBAND, 80)
			.use(Bandwidth.WIRELESS_802_11G, 34)
			.use(Bandwidth.FAST_ETHERNET, 25)
			.use(Bandwidth.WIRELESS_802_11N, 18)
			.use(Bandwidth.GIGABIT_ETHERNET, 14);
		return b.build();
	}

	public static RandomSet<Bandwidth> createDefaultBandwidthDistributionSouthAmerica() {
		BandwidthDistributionBuilder b = new BandwidthDistributionBuilder();
		b.use(Bandwidth.DIALUP, 2)
			.use(Bandwidth.DSL_LOW, 2)
			.use(Bandwidth.DSL_MED, 3)
			.use(Bandwidth.ADSL_LITE, 4)
			.use(Bandwidth.DSL_HIGH, 6)
			.use(Bandwidth.ADSL1, 55)
			.use(Bandwidth.ETHERNET, 52)
			.use(Bandwidth.ADSL2, 43)
			.use(Bandwidth.BROADBAND, 38)
			.use(Bandwidth.WIRELESS_802_11G, 30)
			.use(Bandwidth.FAST_ETHERNET, 20)
			.use(Bandwidth.WIRELESS_802_11N, 12)
			.use(Bandwidth.GIGABIT_ETHERNET, 5);
		return b.build();
	}

	public static RandomSet<Bandwidth> createDefaultBandwidthDistributionOceanic() {
		BandwidthDistributionBuilder b = new BandwidthDistributionBuilder();
		b.use(Bandwidth.DIALUP, 4)
			.use(Bandwidth.DSL_LOW, 4)
			.use(Bandwidth.DSL_MED, 6)
			.use(Bandwidth.ADSL_LITE, 8)
			.use(Bandwidth.DSL_HIGH, 9)
			.use(Bandwidth.ADSL1, 66)
			.use(Bandwidth.ETHERNET, 49)
			.use(Bandwidth.ADSL2, 44)
			.use(Bandwidth.BROADBAND, 38)
			.use(Bandwidth.WIRELESS_802_11G, 26)
			.use(Bandwidth.FAST_ETHERNET, 20)
			.use(Bandwidth.WIRELESS_802_11N, 15)
			.use(Bandwidth.GIGABIT_ETHERNET, 10);
		return b.build();
	}

	public static class BandwidthDistributionBuilder {

		private RandomSet<Bandwidth> Bandwidths = new RandomSet<Bandwidth>();

		public BandwidthDistributionBuilder use(Bandwidth bandwidth, int weight) {
			Bandwidths.add(bandwidth, weight);
			return this;
		}

		public RandomSet<Bandwidth> build() {
			return Bandwidths;
		}
	}
}
