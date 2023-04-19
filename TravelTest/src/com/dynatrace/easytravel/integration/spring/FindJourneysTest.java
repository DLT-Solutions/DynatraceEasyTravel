package com.dynatrace.easytravel.integration.spring;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.logging.Logger;

import org.junit.Assert;

import org.junit.Test;

import com.dynatrace.easytravel.frontend.data.DataProvider;
import com.dynatrace.easytravel.frontend.data.DataProviderInterface;
import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.frontend.data.LocationDO;
import com.dynatrace.easytravel.spring.SpringTestBase;

public class FindJourneysTest extends SpringTestBase {

    private static Logger log = Logger.getLogger(FindJourneysTest.class.getName());

	@Test
	public void testFind() throws RemoteException {

		DataProviderInterface provider = new DataProvider();
		String destination = "New York"; // should find a journey that has a picture!
		Date fromDate = null;
		Date toDate = null;
		long time = System.currentTimeMillis();

		int count = 100;
		while (count-- > 0)
		{
			LocationDO[] locations = provider.findLocations(destination, 100);
			Assert.assertNotNull("Expecting some locations", locations);

			log.info("Locations found: " + locations.length);
			for (LocationDO location : locations)
			{
				if (!location.getName().equals(destination)) {
					continue; // assertions only work for "New York", not "West New York" a.s.o.
				}
				log.info("Location: " + location.getName());
				JourneyDO[] journeys = provider.findJourneys(location.getName(), fromDate, toDate);

				Assert.assertNotNull("Expecting some journeys", journeys);

				log.info("Journeys found: " + journeys.length);
				for (JourneyDO journey : journeys)
				{
		    		log.info("Journey: " + journey.getName() + " from " + journey.getStart() + " to " + journey.getDestination() + " fromDate=" + journey.getFromDate() + ", toDate=" + journey.getToDate());
		    		Assert.assertNotNull("Expecting a picture", journey.getPicture());
				}
			}
		}

		time = System.currentTimeMillis() - time;
		log.info("Elapsed [ms]: " + time);
    }
}
