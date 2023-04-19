package com.dynatrace.diagnostics.uemload;

public class CachedThirdPartyResouce {
	private Integer responseSize;
	private String mimeType;
	
	public CachedThirdPartyResouce(Integer responseSize, String mimeType) {
		this.responseSize = responseSize;
		this.mimeType = mimeType;
	}

	public Integer getResponseSize() {
		return responseSize;
	}

	public String getMimeType() {
		return mimeType;
	}
}
