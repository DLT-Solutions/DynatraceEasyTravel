package com.dynatrace.easytravel.business.webservice.transferobj;

import java.util.Map;

import com.dynatrace.easytravel.jpa.business.Location;


public class BookingSummary {

    
    private final int nBookings;
    private final double totalSales;
	private final Location[] departures;
    private final int[] departureCounts;
	private final Location[] destinations;
    private final int[] destinationCounts;
    
    
	public BookingSummary(int nBookings, double totalSales, Map<? extends Location, Integer> departures,
			Map<? extends Location, Integer> destinations) {
        this.nBookings = nBookings;
        this.totalSales = totalSales;
        
        this.departures = new Location[departures.size()];
        this.departureCounts = new int[departures.size()];
        int cnt = 0;
		for (Map.Entry<? extends Location, Integer> e : departures.entrySet()) {
            this.departures[cnt] = e.getKey();
            this.departureCounts[cnt] = e.getValue();
            cnt++;
            
        }
        cnt = 0;
        this.destinations = new Location[destinations.size()];
        this.destinationCounts = new int[destinations.size()];
		for (Map.Entry<? extends Location, Integer> e : destinations.entrySet()) {
            this.destinations[cnt] = e.getKey();
            this.destinationCounts[cnt] = e.getValue();
            cnt++;
        }
        
    }
    
    
    public int getNBookings() {
        return nBookings;
    }
    
    
    public double getTotalSales() {
        return totalSales;
    }

    
	public Location[] getDepartures() {
        return departures;
    }
    
    
    public int[] getDepartureCounts() {
        return departureCounts;
    }

    
	public Location[] getDestinations() {
        return destinations;
    }
    
    
    public int[] getDestinationCounts() {
        return destinationCounts;
    }
    
    
}