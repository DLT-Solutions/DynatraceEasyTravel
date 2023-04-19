package com.dynatrace.easytravel.integration.spring;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.logging.Logger;

import org.junit.Assert;

import org.junit.Test;

import com.dynatrace.easytravel.frontend.data.DataProvider;
import com.dynatrace.easytravel.frontend.data.DataProviderInterface;
import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.misc.UserType;
import com.dynatrace.easytravel.spring.SpringTestBase;

public class BookingTest extends SpringTestBase {

	private static Logger log = Logger.getLogger(FindJourneysTest.class.getName());

	@Test
	public void testBook() throws RemoteException {

		DataProviderInterface provider = new DataProvider();
		String userName = "hainer";
		String destination = "Mumbasa";
		String creditCard = "1234567890000";
		Date fromDate = null;
		Date toDate = null;
		JourneyDO[] journeys = provider.findJourneys(destination, fromDate, toDate);

		Assert.assertNotNull("Expecting some journeys", journeys);

		log.info("Journeys found: " + journeys.length);
		for (JourneyDO journey : journeys)
		{
    		log.info("Journey: " + journey.getName() + " from " + journey.getStart() + " to " + journey.getDestination() + " fromDate=" + journey.getFromDate() + ", toDate=" + journey.getToDate() + ", amount=" + journey.getAmount());

    		boolean ok = provider.checkCreditCard(creditCard);
    		Assert.assertTrue("CreditCard should be ok: " + creditCard, ok);
    		String bookingId = provider.storeBooking(journey.getId(), userName, UserType.WEB, creditCard, journey.getAmount());
    		System.out.println("Booking ID: " + bookingId);
		}
    }
}
