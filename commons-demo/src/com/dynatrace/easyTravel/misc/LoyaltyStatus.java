package com.dynatrace.easytravel.misc;

/**
 * 
 * @author Michal.Bakula
 *
 */

public enum LoyaltyStatus {
	
	PLATINUM("Platinum"),
	GOLD("Gold"),
	SILVER("Silver"),
	NONE("None");
	
	private final String status;
	
	LoyaltyStatus(String status){
		this.status=status;
	}
	
	@Override
	public String toString() {
		return status;
	}
	
	public static LoyaltyStatus get(String name) {
		switch (name) {
		case "Platinum":
			return LoyaltyStatus.PLATINUM;
		case "Gold":
			return LoyaltyStatus.GOLD;
		case "Silver":
			return LoyaltyStatus.SILVER;
		case "None":
			return LoyaltyStatus.NONE;
		default:
			return LoyaltyStatus.NONE;
		}
	}
}
