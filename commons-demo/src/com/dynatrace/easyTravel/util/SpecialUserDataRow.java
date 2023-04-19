package com.dynatrace.easytravel.util;

public class SpecialUserDataRow {
	public String visitorInfo;
	public int yearOfVisit;
	public int monthOfVisit;
	
	public SpecialUserDataRow() {
	}
	
	public SpecialUserDataRow(String visitorInfo, int yearOfVisit, int monthOfVisit) {
		this.visitorInfo = visitorInfo;
		this.yearOfVisit = yearOfVisit;
		this.monthOfVisit = monthOfVisit;
	}
}
