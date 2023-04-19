package com.dynatrace.diagnostics.uemload;

public class VisitorId {
	private final String visitorId;
	private final boolean newVisitor;
	
	public VisitorId(String id, boolean isNew) {
		this.visitorId = id;
		this.newVisitor = isNew;
	}
	
	public String getVisitorId() {
		return visitorId;
	}

	public boolean isNewVisitor() {
		return newVisitor;
	} 
}
