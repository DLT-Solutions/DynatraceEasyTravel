package com.dynatrace.diagnostics.uemload;

public class MobileDeviceMarketShare {
	
	public enum DeviceManufacturer {
		Samsung, Apple, Nokia, LG, Huawei, HTC, Xiaomi, Other
	}
	private double samsungShare;
	private double appleShare;
	private double nokiaShare;
	private double lgShare;
	private double huaweiShare;
	private double htcShare;
	private double xiaomiShare;
	private double otherShare;
	
	private MobileDeviceMarketShare(MobileDeviceMarketShareBuilder builder){
		this.samsungShare = builder.samsungShare;
		this.appleShare=builder.appleShare;
		this.nokiaShare=builder.nokiaShare;
		this.lgShare=builder.lgShare;
		this.huaweiShare=builder.huaweiShare;
		this.htcShare=builder.htcShare;
		this.xiaomiShare=builder.xiaomiShare;
		this.otherShare=builder.otherShare;
	}
	
	public int getWeight(int deviceTypeWeight, DeviceManufacturer manufacturer) {
		int weight;
		double share;
		switch (manufacturer) {
		case Samsung:
			weight = (int) Math.round(deviceTypeWeight * samsungShare);
			share = samsungShare;
			break;
		case Apple:
			weight = (int) Math.round(deviceTypeWeight * appleShare);
			share = appleShare;
			break;
		case Nokia:
			weight = (int) Math.round(deviceTypeWeight * nokiaShare);
			share=nokiaShare;
			break;
		case LG:
			weight = (int) Math.round(deviceTypeWeight * lgShare);
			share = lgShare;
			break;
		case Huawei:
			weight = (int) Math.round(deviceTypeWeight * huaweiShare);
			share = huaweiShare;
			break;
		case HTC:
			weight = (int) Math.round(deviceTypeWeight * htcShare);
			share = htcShare;
			break;
		case Xiaomi:
			weight = (int) Math.round(deviceTypeWeight * xiaomiShare);
			share = xiaomiShare;
			break;
		case Other:
			weight = (int) Math.round(deviceTypeWeight * otherShare);
			share = otherShare;
			break;
		default:
			return 0;
		}
		return (!(weight == 0 && share > 0.0)) ? weight : 1;
	}

	public static class MobileDeviceMarketShareBuilder {
		private double samsungShare;
		private double appleShare;
		private double nokiaShare;
		private double lgShare;
		private double huaweiShare;
		private double htcShare;
		private double xiaomiShare;
		private double otherShare;
		
		public MobileDeviceMarketShareBuilder setSamsungShare(double samsungShare) {
			this.samsungShare = samsungShare;
			return this;
		}

		public MobileDeviceMarketShareBuilder setAppleShare(double appleShare) {
			this.appleShare = appleShare;
			return this;
		}

		public MobileDeviceMarketShareBuilder setNokiaShare(double nokiaShare) {
			this.nokiaShare = nokiaShare;
			return this;
		}

		public MobileDeviceMarketShareBuilder setLgShare(double lgShare) {
			this.lgShare = lgShare;
			return this;
		}

		public MobileDeviceMarketShareBuilder setHuaweiShare(double huaweiShare) {
			this.huaweiShare = huaweiShare;
			return this;
		}

		public MobileDeviceMarketShareBuilder setHtcShare(double htcShare) {
			this.htcShare = htcShare;
			return this;
		}

		public MobileDeviceMarketShareBuilder setXiaomiShare(double xiaomiShare) {
			this.xiaomiShare = xiaomiShare;
			return this;
		}
		
		public MobileDeviceMarketShareBuilder setOtherShare(double otherShare) {
			this.otherShare = otherShare;
			return this;
		}

		public MobileDeviceMarketShare build() {
			return new MobileDeviceMarketShare(this);
		}
	}
}
