package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import com.google.common.base.Strings;


/**
 * @author Rafal.Psciuk
 *
 */
public class UtmSource {	
	private final String source;
	private final String url;
	
	public UtmSource(String src, String url) {
		if(Strings.isNullOrEmpty(src) || Strings.isNullOrEmpty(url)) {
			throw new IllegalArgumentException("Utm source and url canot be empty or null");
		}
		this.source = src;
		this.url = url;
	}
	
	public String getSource() {
		return source;		
	}
	
	public String getUrl() {
		return url;
	}
}
