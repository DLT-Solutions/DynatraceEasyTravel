package com.dynatrace.easytravel.frontend.data;

import java.util.Calendar;

public class BookingDO {
	
	private String bookingId;
	private JourneyDO journey;
	private String username;
	private Calendar date;
	
	public BookingDO(String bookingId, JourneyDO journey, String username, Calendar date) {
		this.bookingId = bookingId;
		this.journey = journey;
		this.username = username;		
		this.date = date;
	}

	public String getBookingId() {
		return bookingId;
	}

	public void setBookingId(String bookingId) {
		this.bookingId = bookingId;
	}


	public JourneyDO getJourney() {
		return journey;
	}

	public void setJourney(JourneyDO journey) {
		this.journey = journey;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Calendar getDate() {
		return date;
	}

	public void setDate(Calendar date) {
		this.date = date;
	}
}
