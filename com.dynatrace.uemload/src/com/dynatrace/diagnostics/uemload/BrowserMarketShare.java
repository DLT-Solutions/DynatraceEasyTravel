package com.dynatrace.diagnostics.uemload;

public class BrowserMarketShare {
	
	public enum BrowserManufacturer {
		Chrome, Firefox, Safari, IE, Edge, Opera, UC, Android, Samsung, Other
	}
	
	private double chromeShare;
	private double firefoxShare;
	private double safariShare;
	private double ieShare;
	private double edgeShare;
	private double operaShare;
	private double ucShare;
	private double androidShare;
	private double samsungShare;
	private double otherShare;
	
	private BrowserMarketShare(BrowserMarketShareBuilder builder){
		this.chromeShare=builder.chromeShare;
		this.firefoxShare=builder.firefoxShare;
		this.safariShare=builder.safariShare;
		this.ieShare=builder.ieShare;
		this.edgeShare=builder.edgeShare;
		this.operaShare=builder.operaShare;
		this.ucShare=builder.ucShare;
		this.androidShare=builder.androidShare;
		this.samsungShare=builder.samsungShare;
		this.otherShare=builder.otherShare;
	}
	
	public int getWeight(int browserTypeWeight, BrowserManufacturer type) {
		int weight;
		double share;
		switch (type) {
		case Chrome:
			weight = (int) Math.round(browserTypeWeight * chromeShare);
			share = chromeShare;
			break;
		case Firefox:
			weight = (int) Math.round(browserTypeWeight * firefoxShare);
			share = firefoxShare;
			break;
		case Safari:
			weight = (int) Math.round(browserTypeWeight * safariShare);
			share = safariShare;
			break;
		case IE:
			weight = (int) Math.round(browserTypeWeight * ieShare);
			share = ieShare;
			break;
		case Edge:
			weight = (int) Math.round(browserTypeWeight * edgeShare);
			share = edgeShare;
			break;
		case Opera:
			weight = (int) Math.round(browserTypeWeight * operaShare);
			share = operaShare;
			break;
		case UC:
			weight = (int) Math.round(browserTypeWeight * ucShare);
			share = ucShare;
			break;
		case Android:
			weight = (int) Math.round(browserTypeWeight * androidShare);
			share = androidShare;
			break;
		case Samsung:
			weight = (int) Math.round(browserTypeWeight * samsungShare);
			share = samsungShare;
			break;
		case Other:
			weight = (int) Math.round(browserTypeWeight * otherShare);
			share = otherShare;
			break;
		default:
			return 0;
		}
		return (!(weight == 0 && share > 0.0)) ? weight : 1;
	}
	
	public static class BrowserMarketShareBuilder {
		private double chromeShare;
		private double firefoxShare;
		private double safariShare;
		private double ieShare;
		private double edgeShare;
		private double operaShare;
		private double ucShare;
		private double androidShare;
		private double samsungShare;
		private double otherShare;

		public BrowserMarketShareBuilder setChromeShare(double chromeShare) {
			this.chromeShare = chromeShare;
			return this;
		}

		public BrowserMarketShareBuilder setFirefoxShare(double firefoxShare) {
			this.firefoxShare = firefoxShare;
			return this;
		}

		public BrowserMarketShareBuilder setSafariShare(double safariShare) {
			this.safariShare = safariShare;
			return this;
		}

		public BrowserMarketShareBuilder setIEShare(double ieShare) {
			this.ieShare = ieShare;
			return this;
		}

		public BrowserMarketShareBuilder setEdgeShare(double edgeShare) {
			this.edgeShare = edgeShare;
			return this;
		}

		public BrowserMarketShareBuilder setOperaShare(double operaShare) {
			this.operaShare = operaShare;
			return this;
		}

		public BrowserMarketShareBuilder setUCShare(double ucShare) {
			this.ucShare = ucShare;
			return this;
		}

		public BrowserMarketShareBuilder setAndroidShare(double androidShare) {
			this.androidShare = androidShare;
			return this;
		}

		public BrowserMarketShareBuilder setSamsungShare(double samsungShare) {
			this.samsungShare = samsungShare;
			return this;
		}
		
		public BrowserMarketShareBuilder setOtherShare(double otherShare) {
			this.otherShare = otherShare;
			return this;
		}

		public BrowserMarketShare build() {
			return new BrowserMarketShare(this);
		}
	}
}
