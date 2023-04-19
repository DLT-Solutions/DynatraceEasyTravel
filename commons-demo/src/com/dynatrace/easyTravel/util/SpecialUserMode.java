package com.dynatrace.easytravel.util;

public enum SpecialUserMode {
	WEEKLY("W"),
	MONTHLY("M"),
	BOTH("B"),
	NONE("N");
	
	public final String optionValue;
	
	private SpecialUserMode(String optionValue) {
		this.optionValue = optionValue;
	}
	
	public static SpecialUserMode valueOfOptionValue(String optionValue) {
	    for (SpecialUserMode e : values()) {
	        if (e.optionValue.equals(optionValue)) {
	            return e;
	        }
	    }
	    return null;
	}
}
