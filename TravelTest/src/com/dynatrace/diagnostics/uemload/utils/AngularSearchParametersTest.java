package com.dynatrace.diagnostics.uemload.utils;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class AngularSearchParametersTest {
	
	public static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
			.appendPattern("yyyy-MM-dd")
			.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
			.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
			.toFormatter();
	
	private static AngularSearchParameters params;
	
	@BeforeClass
	public static void setup() {
		params = new AngularSearchParameters();
	}
	
	@Test
	public void getFromDate() {
		String date = params.getFromDate();
		assertTrue(date.matches("^\\d{4}-\\d{2}-\\d{2}") && checkDate(date));
	}
	
	@Test
	public void getToDate() {
		String date = params.getToDate();
		assertTrue(date.matches("^\\d{4}-\\d{2}-\\d{2}") && checkDate(date));
	}
	
	@Test
	public void getNumberOfTravelers() {
		for(int i=0;i<10;i++) {
			assertTrue(params.getNumberOfTravelers().matches("^[1-2]{1}"));
		}
	}
	
	@Test
	public void getFirstFourDigits() {
		assertTrue(params.getFirstFourDigits().matches("^\\d{4}"));
	}
	
	@Test
	public void getSecondFourDigits() {
		assertTrue(params.getSecondFourDigits().matches("^\\d{4}"));
	}
	
	@Test
	public void getThirdFourDigits() {
		assertTrue(params.getThirdFourDigits().matches("^\\d{4}"));
	}
	
	@Test
	public void getFourthFourDigits() {
		assertTrue(params.getFourthFourDigits().matches("^\\d{4}"));
	}
	
	@Test
	public void getCVC() {
		assertTrue(params.getCVC().matches("^\\d{3}"));
	}
	
	private boolean checkDate(String date) {
		try {
			LocalDateTime.parse(date, FORMATTER);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
