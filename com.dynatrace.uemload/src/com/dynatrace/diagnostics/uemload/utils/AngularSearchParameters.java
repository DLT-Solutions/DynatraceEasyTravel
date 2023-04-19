package com.dynatrace.diagnostics.uemload.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PseudoRandomJourneyDestination;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class AngularSearchParameters {
	
	public static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
			.appendPattern("yyyy-MM-dd")
			.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
			.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
			.toFormatter();

	private String fromDate;
	private String toDate;
	private String creditCardNumber;
	private String cvc;
	private String destination; 
	
	public AngularSearchParameters() {
		this(PseudoRandomJourneyDestination.get());
	}
	
	public AngularSearchParameters(String destination) {
		LocalDateTime time = LocalDateTime.now();
		fromDate = FORMATTER.format(time);
		toDate = FORMATTER.format(time.plusMonths(6));
		creditCardNumber = getRandomNumber(16);
		cvc = getRandomNumber(3);		
		this.destination = destination;
	}
		
	public String getDestination() {
		return destination;
	}
	
	public String getFromDate() {
		return fromDate;
	}
	
	public String getToDate() {
		return toDate;
	}
	
	public String getNumberOfTravelers() {
		return (Math.random() > 0.5) ? "1" : "2";
	}
	
	public String getFirstFourDigits() {
		return creditCardNumber.substring(0, 4);
	}
	
	public String getSecondFourDigits() {
		return creditCardNumber.substring(4, 8);
	}
	
	public String getThirdFourDigits() {
		return creditCardNumber.substring(8, 12);
	}
	
	public String getFourthFourDigits() {
		return creditCardNumber.substring(12, creditCardNumber.length());
	}
	
	public String getCVC() {
		return cvc;
	}
	
	public void findNextRandomDestination() {
		this.destination = PseudoRandomJourneyDestination.get(); 
	}
	
	private String getRandomNumber(int length) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < length; i++) {
			sb.append(UemLoadUtils.randomInt(10));
		}
		return sb.toString();
	}	
}
