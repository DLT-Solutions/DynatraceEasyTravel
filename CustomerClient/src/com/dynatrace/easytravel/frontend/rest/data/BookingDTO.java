package com.dynatrace.easytravel.frontend.rest.data;

import java.util.Calendar;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class BookingDTO {
	
	private String bookingId;
	private JourneyDTO journey;
	private String username;
	private Calendar date;
	
	public BookingDTO() { // NOSONAR - empty on purpose
	}
	
	public BookingDTO(String bookingId, JourneyDTO journey, String username, Calendar date) {
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

	public JourneyDTO getJourney() {
		return journey;
	}

	public void setJourney(JourneyDTO journey) {
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
