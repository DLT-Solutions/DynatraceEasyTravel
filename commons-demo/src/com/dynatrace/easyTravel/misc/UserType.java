package com.dynatrace.easytravel.misc;

public enum UserType {
	WEB(false),
	MOBILE(true);
	
	private boolean isMobile;
	
	private UserType(boolean isMobile) {
		this.isMobile = isMobile;
	}
	
	public boolean isMobileUser() {
		return isMobile;
	}
}
