package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import java.util.Calendar;

import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;


public class CreditCard {

	private final long number;
	private final String owner;
	private final String type;
	private final String expirationMonth;
	private final int expirationYear;
	private final int verificationNumber;

	private static String[] MONTHS = {"January", "February",
			  "March", "April", "May", "June", "July",
			  "August", "September", "October", "November",
			  "December"};
	private static String[] CC_TYPES = {"VISA", "MasterCard", "Diner's Club", "AmEx"};

	public CreditCard(String name) {
		this.number = RandomCreditCard.getNumber();
		this.owner = name;
		this.type = RandomCreditCard.getType();
		this.expirationMonth = RandomCreditCard.getExpirationMonth();
		this.expirationYear = RandomCreditCard.getExpirationYear();
		this.verificationNumber = RandomCreditCard.getVerificationNumber();
	}

	private static class RandomCreditCard{

		public static long getNumber() {
			return getRandomNumber(14);
		}

		public static int getVerificationNumber() {
			return (int) getRandomNumber(4);
		}

		public static int getExpirationYear() {
			return Calendar.getInstance().get(Calendar.YEAR) + 1 + UemLoadUtils.randomInt(5); // at least next year
		}

		public static String getType(){
			return UemLoadUtils.getRandomElement(CC_TYPES);
		}

		public static String getExpirationMonth(){
			return UemLoadUtils.getRandomElement(MONTHS);
		}

		private static long getRandomNumber(int length) {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < length; i++) {
				sb.append(UemLoadUtils.randomInt(10));
			}
			return Long.parseLong(sb.toString().trim());
		}

	}


	public long getNumber() {
		return number;
	}


	public String getOwner() {
		return owner;
	}


	public String getType() {
		return type;
	}


	public String getExpirationMonth() {
		return expirationMonth;
	}


	public int getExpirationYear() {
		return expirationYear;
	}


	public int getVerificationNumber() {
		return verificationNumber;
	}




}
