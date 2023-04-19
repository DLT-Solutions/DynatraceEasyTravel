package com.dynatrace.easytravel.business.webservice.transferobj;



public class BookingPage extends Page<BookingTO> {
        
    public BookingPage(BookingTO[] objects, int fromIdx, int count, int total) {
        super(objects, fromIdx, count, total);
    }
    
    
    public BookingTO[] getBookings() {
        return objects;
    }
    
}
