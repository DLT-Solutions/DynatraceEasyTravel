package com.dynatrace.easytravel.business.webservice.transferobj;

import com.dynatrace.easytravel.jpa.business.Location;


public class LocationPage extends Page<Location> {
    
    public LocationPage(Location[] objects, int fromIdx, int count, int total) {
        super(objects, fromIdx, count, total);
    }
    
    
    public Location[] getLocations() {
        return objects;
    }
    
}
