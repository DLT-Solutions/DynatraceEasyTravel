package com.dynatrace.easytravel.jpa;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.dynatrace.easytravel.database.DatabaseWithContentAndPlugins;
import com.dynatrace.easytravel.jpa.business.*;
import com.dynatrace.easytravel.model.DataAccess;

@RunWith(MockitoJUnitRunner.class)
public class JpaDatabaseAccessTest extends DatabaseWithContentAndPlugins {

	private static final Logger log = Logger.getLogger(JpaDatabaseAccessTest.class.getName());
	private DataAccess access;


	@Before
	public void setUp() {
		access = createNewAccess();
	}

	@After
	public void tearDown() {
		access.close();
	}


	@Test
	public void testFindJourneys() {
		Collection<Journey> list = access.allJourneys();
		assertThat(list.isEmpty(), is(false));

		for (Journey journey : list) {
			log.fine("Journey: " + journey.getName() + " to " + journey.getDestination().getName() +
					" date: " + journey.getFromDate() + "-" + journey.getToDate());
		}

		// run via JpaDatabaseAccessTest
		access.findJourneys("New York", new Date(0),
				new Date(new GregorianCalendar(3000, 0, 1).getTimeInMillis()), false);
		assertThat(list.isEmpty(), is(false));
	}

	@Test
	public void testBookingSummaryStuff() {
		System.setProperty("hibernate.show_sql", "true");
		DataAccess access = createNewAccess();
		try {
			String tenantName = "Personal Travel Inc.";
			access.getBookingCountByTenant(tenantName);
			access.getTotalSalesByTenant(tenantName);
			access.getDeparturesByTenant(tenantName, 20);
			access.getDestinationsByTenant(tenantName, 20);
		} finally {
			access.close();
		}
	}

	@Test
	public void testGetUser() {
		assertNotNull(access.getUser("demouser"));
		assertNull(access.getUser("someuser"));
	}


	@Test
	public void testJourneyById() {
		Collection<Journey> resultList = access.findJourneys("New York", new Date(0), new Date(33333333333333l), false);

		assertTrue("Should find at least one Journey for New York", resultList.size() > 0);

		assertNotNull("Need to find journey with id 1", resultList.iterator().next());
		assertNotNull("Need to find Journey by id", access.getJourneyById(resultList.iterator().next().getId()));

		assertNull(access.getJourneyById(999999999));
	}

	@Test
	public void testDeleteJourneyWithBookings() {
		access.startTransaction();
		String userId = UUID.randomUUID().toString();
		access.addUser(new User(userId, "dumpti", "haupti"));

		String location = UUID.randomUUID().toString();
		assertTrue(access.addLocation(location));

		String tenant = UUID.randomUUID().toString();
		access.addTenant(new Tenant(tenant, "32l4kj2k43", "asdfasd"));

		Journey journey = access.createJourney(UUID.randomUUID().toString(), location, location, tenant, new Date(),
				new Date(), 234.23, null);

		access.flush();
		access.commitTransaction();


		access.startTransaction();
		// create a booking with this journey
		String bookingId = UUID.randomUUID().toString();
		access.storeBooking(new Booking(bookingId, journey, access.getUser("dumpti"), new Date()));

		access.deleteJourney(journey.getId());

		access.commitTransaction();
	}

	@Test
	public void testAddDeleteLocation() {
		Collection<Location> list = access.getLocations(0, 10);
		assertNotNull(list);
		assertTrue(list.toString(), list.size() > 0);

		for (Location location : list) {
			log.fine("Location: " + location.getName());
		}

		// make sure it is removed at first
		access.deleteLocation("testlocation");

		assertTrue(access.addLocation("testlocation"));
		assertTrue(access.deleteLocation("testlocation"));
	}

}
