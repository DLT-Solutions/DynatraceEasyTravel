package com.dynatrace.easytravel.frontend.rest.data;

import java.util.Calendar;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class BookingConfirmationDTO {
	
	private String bookingId;
	private Integer journeyId;
	private Calendar from;
	private Calendar to;
	
	public BookingConfirmationDTO() { // NOSONAR - empty on purpose
	}
	
	public BookingConfirmationDTO(String bookingId, Integer journeyId, Calendar from, Calendar to) {
		this.bookingId = bookingId;
		this.journeyId = journeyId;
		this.from = from;
		this.to = to;
	}

	public String getBookingId() {
		return bookingId;
	}

	public void setBookingId(String bookingId) {
		this.bookingId = bookingId;
	}

	public Integer getJourneyId() {
		return journeyId;
	}

	public void setJourneyId(Integer journeyId) {
		this.journeyId = journeyId;
	}

	public Calendar getFrom() {
		return from;
	}

	public void setFrom(Calendar from) {
		this.from = from;
	}

	public Calendar getTo() {
		return to;
	}

	public void setTo(Calendar to) {
		this.to = to;
	}

}
