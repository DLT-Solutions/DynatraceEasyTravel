package com.dynatrace.diagnostics.uemload.utils;


public class AngularUTMParams {
	public static final int GCLID_LENGTH = 50; 
	private String utmCampaign;
	private String utmMedium;
	private String utmSource;
	private String utmTerm;
	private String utmContent;
	private String gclid;
	
	private AngularUTMParams(AngularUTMParamsBuilder builder) {
		this.utmCampaign = builder.utmCampaign;
		this.utmMedium = builder.utmMedium;
		this.utmSource = builder.utmSource;
		this.utmTerm = builder.utmTerm;
		this.utmContent = builder.utmContent;
		this.gclid = builder.gclid;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("?");
		sb.append("utm_source=" + this.utmSource);
		sb.append("&utm_medium=" + this.utmMedium);
		sb.append("&utm_campaign=" + this.utmCampaign);

		if (this.utmTerm != null && !this.utmTerm.isEmpty()) { sb.append("&utm_term=" + this.utmTerm); }
		if (this.utmContent != null && !this.utmContent.isEmpty()) { sb.append("&utm_content=" + this.utmContent); }
		if (this.gclid != null && !this.gclid.isEmpty()) { sb.append("&gclid=" + this.gclid); }

		return sb.toString();
	}
	
	public static class AngularUTMParamsBuilder {
		// Required params
		private String utmCampaign;
		private String utmMedium;
		private String utmSource;
		// Optional params
		private String utmTerm;
		private String utmContent;
		private String gclid;

		public AngularUTMParamsBuilder(String utmCampaign, String utmMedium, String utmSource) {
			this.utmCampaign = utmCampaign;
			this.utmMedium   = utmMedium;
			this.utmSource   = utmSource;
		}

		public AngularUTMParamsBuilder utmTerm(String utmTerm) {
			this.utmTerm = utmTerm;
			return this;
		}

		public AngularUTMParamsBuilder utmContent(String utmContent) {
			this.utmContent = utmContent;
			return this;
		}

		public AngularUTMParamsBuilder gclid(String gclid) {
			this.gclid = gclid;
			return this;
		}
		
		public AngularUTMParams build(){
			return new AngularUTMParams(this);
		}
	}
}