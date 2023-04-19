package com.dynatrace.diagnostics.uemload;

import java.util.Date;

/**
 * 
 * @author Michal.Bakula
 *
 */

public class VisitorInfo {	
	public static final int BASE_NUMBER_OF_RECURRING_VISITS = 5;
	public static final int MAX_NUMBER_OF_ADDITIONAL_VISITS = 5;
	private static final int RECURRING_VISITOR_PERCENTAGE = 40;
	private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	private final boolean temporaryVisitor;
	private final boolean returningVisitor;
	private String visitorID;
	private int returnsLeft;

	public VisitorInfo(String visitorID) {
		this.visitorID = visitorID;
		this.temporaryVisitor = false;
		this.returningVisitor = true;
	}
	
	public VisitorInfo(boolean temporaryVisitor){
		this.visitorID = generateVisitorID();
		this.temporaryVisitor = temporaryVisitor;
		returnsLeft = (int) ((Math.random()*MAX_NUMBER_OF_ADDITIONAL_VISITS)+BASE_NUMBER_OF_RECURRING_VISITS);
		this.returningVisitor = false;
	}
		
	public boolean isNewVisitor() {
		return returningVisitor == false && temporaryVisitor == false;
	}
	
	public boolean isReturningVisitor() {
		return returningVisitor;
	}
	
	public void decreaseNumberOfReturns() {
		if(!returningVisitor) {
			if(temporaryVisitor){
				if(returnsLeft > 0){
					returnsLeft--;
				} else {
					visitorID = generateVisitorID();
					returnsLeft = (int) ((Math.random()*MAX_NUMBER_OF_ADDITIONAL_VISITS)+BASE_NUMBER_OF_RECURRING_VISITS);
				}
			} else {
				visitorID = generateVisitorID();
			}
		}
	}

	public VisitorId createVisitorID() {
		return new VisitorId(visitorID, isNewVisitor());
	}	
	
	public String getVisitorID() {
		return visitorID;
	}
	
	public static VisitorId getRandomVisitorId(){
		if(Math.random()*100  < RECURRING_VISITOR_PERCENTAGE){
			return new VisitorId(generateVisitorID(), false);
		}
		return new VisitorId(generateVisitorID(), true);
	}
	
	public static String generateVisitorID() {
		int len = 32;
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)		
			sb.append(AB.charAt((int)(Math.random()*AB.length())));
		return new Date().getTime() + sb.toString();
	}
}