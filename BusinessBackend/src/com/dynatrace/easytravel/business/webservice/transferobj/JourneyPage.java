package com.dynatrace.easytravel.business.webservice.transferobj;

import com.dynatrace.easytravel.jpa.business.Journey;


public class JourneyPage extends Page<Journey> {
    
    public JourneyPage(Journey[] objects, int fromIdx, int count, int total) {
        super(objects, fromIdx, count, total);
    }
    
    public Journey[] getJourneys() {
        return objects;
    }
    
}
