package com.dynatrace.easytravel.business.webservice.transferobj;

import java.util.Calendar;


public class BookingTO {

    private final String id;
    private final Calendar bookingDate;
    private final String userName;
    private final int journeyId;
    private final String journeyName;
    private final String departure;
    private final String destination;

    public BookingTO(String id, Calendar bookingDate, String userName, int journeyId, String journeyName, String departure, String destination) {
        this.id = id;
        this.bookingDate = bookingDate;
        this.userName = userName;
        this.journeyId = journeyId;
        this.journeyName = journeyName;
        this.departure = departure;
        this.destination = destination;
    }


    public String getId() {
        return id;
    }


    public Calendar getBookingDate() {
        return bookingDate;
    }


    public String getUserName() {
        return userName;
    }


    public int getJourneyId() {
        return journeyId;
    }


    public String getJourneyName() {
        return journeyName;
    }
    
    
    public String getDeparture() {
        return departure;
    }
    
    
    public String getDestination() {
        return destination;
    }

}