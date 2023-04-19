package com.dynatrace.easytravel.frontend.beans;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Test;

import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.spring.SpringTestBase;


public class JourneyAccountTest extends SpringTestBase {

    
    private static final double DELTA = 0.0000001;
    private static final double AMOUNT = 100;

    @Test
    public void testJourneyAccount() {
        JourneyAccount journeyAccount = new JourneyAccount();
        JourneyDO journey = new JourneyDO(1, "journey_name", Calendar.getInstance(), Calendar.getInstance(), "departure", "destination", "tenant", AMOUNT, null);
        journeyAccount.setJourney(journey);
        journeyAccount.setTravellers(2);
        assertEquals(0, journeyAccount.getTravellingNights());
        
        assertEquals(journey.getAmount(), journeyAccount.getAvgPerPerson(), DELTA);
        assertEquals(JourneyAccount.TRAVELLERS_COST_FACTOR[2] * AMOUNT, journeyAccount.getTotalCosts(), DELTA);
        journeyAccount.setTravellers(1);
        double flightCost = (AMOUNT - JourneyAccount.TAXES_AND_FEES) * JourneyAccount.FLIGHT_COST_FACTOR;
        assertEquals(flightCost, journeyAccount.getFlightCosts(), DELTA);
        assertEquals(AMOUNT - flightCost - JourneyAccount.TAXES_AND_FEES, journeyAccount.getHotelCosts(), DELTA);
    }
}
