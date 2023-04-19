/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: JourneyProviderTest.java
 * @date: 04.02.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.jpa.business.Tenant;
import com.dynatrace.easytravel.persistence.provider.JourneyProvider;


/**
 *
 * @author stefan.moschinski
 */
public class JourneyProviderTest extends EasyTravelPersistenceProviderTest<JourneyProvider> {

	@Test
	public void testGetUserByName() throws Exception {
		Journey journey1 = new Journey();
		journey1.setName("name1");
		journey1.setTenant(new Tenant("name1", "pw1", "desc1"));
		journey1.setStart(new Location("Linz"));
		journey1.setDestination(new Location("Wien"));

		provider.add(journey1);

		assertThat(provider.getCount(), is(1));
		assertThat(provider.getJourneyByName("name1"), is(journey1));
	}

	@Test
	public void testGetMatching() throws Exception {
		Location ingolstadt = new Location("Ingolstadt");
		Journey journey1 = new Journey("journey1", new Location("Linz"), ingolstadt, new Tenant("tenant1", "pw1",
				"desc1"),
				new Date(), new Date(), 1111.11, null);

		Location malmoe = new Location("Malmö");
		Journey journey2 = new Journey("journey2", new Location("Berlin"), malmoe, new Tenant("tenant2", "pw2",
				"desc2"),
				new Date(), new Date(), 1111.11, null);

		Location eisenstadt = new Location("Eisenstadt");
		Journey journey3 = new Journey("journey3", malmoe, eisenstadt, new Tenant("tenant1",
				"pw3",
				"desc3"),
				new Date(), new Date(), 1111.11, null);

		provider.add(journey1);
		provider.add(journey2);
		provider.add(journey3);

		assertThat(provider.getMatchingJourneyDestinations("Malmö", false), contains(malmoe));
		assertThat(provider.getMatchingJourneyDestinations("malmö", false), contains(malmoe));

		assertThat(provider.getMatchingJourneyDestinations("stadt", false), containsInAnyOrder(eisenstadt, ingolstadt));

	}

	@Test
	public void testIsDestination() throws Exception {
		Location ingolstadt = new Location("Ingolstadt");
		Journey journey1 = new Journey("journey1", ingolstadt, new Location("Linz"), new Tenant("tenant1", "pw1",
				"desc1"),
				new Date(), new Date(), 1111.11, null);

		Journey journey2 = new Journey("journey2", new Location("Berlin"), new Location("Malmö"), new Tenant("tenant2", "pw2",
				"desc2"),
				new Date(), new Date(), 1111.11, null);

		Location eisenstadt = new Location("Eisenstadt");
		Journey journey3 = new Journey("journey3", new Location("Malmö"), eisenstadt, new Tenant("tenant1",
				"pw3",
				"desc3"),
				new Date(), new Date(), 1111.11, null);

		provider.add(journey1);
		provider.add(journey2);
		provider.add(journey3);

		assertThat(provider.isJourneyDestination("Eisenstadt"), is(true));
		assertThat(provider.isJourneyDestination("Ingolstadt"), is(false));

	}

//	@Rule
//	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testNoDuplicateJourneyNames() throws Exception {
		Journey journey1 = new Journey("journey1", new Location("Ingolstadt"), new Location("Linz"), new Tenant("tenant1", "pw1",
				"desc1"),
				new Date(), new Date(), 1111.11, null);
		Journey journey2 = new Journey("journey1", new Location("Berlin"), new Location("Malmö"), new Tenant("tenant2", "pw2",
				"desc2"),
				new Date(), new Date(), 1111.11, null);

		provider.add(journey1);

		provider.add(journey2);
	}



	@Test
	public void testGetJourneyById() throws Exception {
		Journey journey1 = new Journey("journey1", new Location("Ingolstadt"), new Location("Linz"), new Tenant("tenant1", "pw1",
				"desc1"),
				new Date(), new Date(), 1111.11, new byte[] { 1, 0, 1 });
		journey1.setId(1);

		Journey journey2 = new Journey("journey2", new Location("Berlin"), new Location("Malmö"), new Tenant("tenant2", "pw2",
				"desc2"),
				new Date(), new Date(), 1111.11, null);
		journey2.setId(2);

		provider.add(journey1);
		provider.add(journey2);

		assertThat(provider.getJourneyById(1), is(journey1));
		assertThat(provider.getJourneyById(2), is(journey2));
	}

	@Test
	public void testAllJourneyIds() throws Exception {
		Journey journey1 = new Journey("journey1", new Location("Ingolstadt"), new Location("Linz"), new Tenant("tenant1", "pw1",
				"desc1"),
				new Date(), new Date(), 1111.11, null);
		journey1.setId(1);

		Journey journey2 = new Journey("journey2", new Location("Berlin"), new Location("Malmö"), new Tenant("tenant2", "pw2",
				"desc2"),
				new Date(), new Date(), 1111.11, null);
		journey2.setId(2);

		provider.add(journey1);
		provider.add(journey2);

		assertThat(provider.getAllJourneyIds(), containsInAnyOrder(1, 2));
	}

	@Test
	public void testDuplicates() throws Exception {
		Journey journey1 = new Journey("journey1", new Location("Ingolstadt"), new Location("Linz"), new Tenant("tenant1", "pw1",
				"desc1"),
				new Date(), new Date(), 1111.11, null);
		journey1.setId(1);

		Journey journey2 = new Journey("journey2", new Location("Berlin"), new Location("Malmö"), new Tenant("tenant2", "pw2",
				"desc2"),
				new Date(), new Date(), 1111.11, null);
		journey2.setId(2);

		provider.add(journey1);
		provider.add(journey2);

		Journey newJourney2 = new Journey("journey2", new Location("Braunschweig"), new Location("Malmö"), new Tenant("tenant2",
				"pw2",
				"desc2"),
				new Date(), new Date(), 1111.11, null);
		newJourney2.setId(2);

		// actually update
		provider.add(newJourney2);

		assertThat(provider.getJourneyById(2), is(newJourney2));
		assertThat(provider.getJourneyById(1), is(journey1));


	}

	@Test
	public void testRemoveById() throws Exception {
		Journey journey1 = new Journey("journey1", new Location("Ingolstadt"), new Location("Linz"), new Tenant("tenant1", "pw1",
				"desc1"),
				new Date(), new Date(), 1111.11, null);

		Journey journey2 = new Journey("journey2", new Location("Berlin"), new Location("Malmö"), new Tenant("tenant2", "pw2",
				"desc2"),
				new Date(), new Date(), 1111.11, null);

		provider.add(journey1);
		provider.add(journey2);

		assertThat(provider.getAll(), containsInAnyOrder(journey1, journey2));

		provider.removeJourneyById(journey1.getId());
		assertThat(provider.getAll(), containsInAnyOrder(journey2));

		provider.removeJourneyById(journey2.getId());
		assertThat(provider.getAll(), is(empty()));
	}

	@Test
	public void testFindJourneyTime() {
		Location Malmoe = new Location("Malmö");

		Date today = new Date();
		Date tomorrow = DateUtils.addDays(today, 1);
		Date yesterday = DateUtils.addDays(today, -1);

		Journey todayTomorrowMalmoe = new Journey("todayTomorrowMalmö", new Location("Ingolstadt"), Malmoe, new Tenant("tenant1",
				"pw1",
				"desc1"),
				today, tomorrow, 1111.11, null);
		Journey todayTomorrowDresden = new Journey("todayTomorrowDresden", new Location("Ingolstadt"), new Location("Dresden"),
				new Tenant("tenant1", "pw1",
						"desc1"),
				today, tomorrow, 1111.11, null);


		Journey yesterdayTomorrowMalmoe = new Journey("yesterdayTomorrowMalmö", new Location("Berlin"), Malmoe, new Tenant(
				"tenant2", "pw2",
				"desc2"),
				yesterday, tomorrow, 1111.11, null);

		Journey yesterdayTodayMalmoe = new Journey("yesterdayTodayMalmö", new Location("Berlin"), Malmoe, new Tenant("tenant2",
				"pw2",
				"desc2"),
				yesterday, today, 1111.11, null);

		provider.add(todayTomorrowMalmoe);
		provider.add(todayTomorrowDresden);
		provider.add(yesterdayTomorrowMalmoe);
		provider.add(yesterdayTodayMalmoe);

		assertThat(provider.findJourneys("Malmö", today, tomorrow, false), containsInAnyOrder(todayTomorrowMalmoe));
		assertThat(provider.findJourneys("Malmö", yesterday, tomorrow, false),
				containsInAnyOrder(yesterdayTomorrowMalmoe, todayTomorrowMalmoe, yesterdayTodayMalmoe));
		assertThat(provider.findJourneys("Malmö", yesterday, today, false), containsInAnyOrder(yesterdayTodayMalmoe));
	}

	@Test
	public void testFindJourneyTime2() {
		Tenant tenant = new Tenant();
		tenant.setName("unknown");
		tenant.setPassword("pw1");
		tenant.setDescription("desc");

		Journey paris = new Journey("Paris - Blub", new Location("Berlin"), new Location("Paris"), tenant, new Date(),
				new Date(), 7223.99, new byte[0]);

		provider.add(paris);
		assertThat(provider.getJourneyByName("Paris - Blub"), is(paris));

	}

	@Test
	public void testGetJourneyByTenant() throws Exception {
		Journey journey1 = new Journey("journey1", new Location("Ingolstadt"), new Location("Linz"), new Tenant("tenant1", "pw1",
				"desc1"),
				new Date(), new Date(), 1111.11, new byte[] { 1, 0, 1 });
		journey1.setId(1);

		Journey journey2 = new Journey("journey2", new Location("Berlin"), new Location("Malmö"), new Tenant("tenant2", "pw2",
				"desc2"),
				new Date(), new Date(), 1111.11, null);
		journey2.setId(2);

		Journey journey3 = new Journey("journey3", new Location("Berlin"), new Location("Malmö"), new Tenant("tenant2", "pw2",
				"desc2"),
				new Date(), new Date(), 1111.11, null);
		journey2.setId(3);

		provider.add(journey1);
		provider.add(journey2);
		provider.add(journey3);

		assertThat(provider.getJourneysByTenant("tenant1"), contains(journey1));
		assertThat(provider.getJourneysByTenant("tenant2"), containsInAnyOrder(journey2, journey3));
		assertThat(provider.getJourneysByTenant("tenantNotExisting"), is(empty()));
	}

	@Test
	public void testGetJourneyCountByTenant() throws Exception {
		Journey journey1 = new Journey("journey1", new Location("Ingolstadt"), new Location("Linz"), new Tenant("tenant1", "pw1",
				"desc1"),
				new Date(), new Date(), 1111.11, new byte[] { 1, 0, 1 });
		journey1.setId(1);

		Journey journey2 = new Journey("journey2", new Location("Berlin"), new Location("Malmö"), new Tenant("tenant2", "pw2",
				"desc2"),
				new Date(), new Date(), 1111.11, null);
		journey2.setId(2);

		Journey journey3 = new Journey("journey3", new Location("Berlin"), new Location("Malmö"), new Tenant("tenant2", "pw2",
				"desc2"),
				new Date(), new Date(), 1111.11, null);
		journey2.setId(3);

		provider.add(journey1);
		provider.add(journey2);
		provider.add(journey3);

		assertThat(provider.getJourneyCountByTenant("tenant1"), is(1));
		assertThat(provider.getJourneyCountByTenant("tenant2"), is(2));
		assertThat(provider.getJourneyCountByTenant("notExisting"), is(0));
	}

	@Test
	public void testGetJourneyIdByName() throws Exception {
		Journey journey1 = new Journey("journey1", new Location("Ingolstadt"), new Location("Linz"), new Tenant("tenant1", "pw1",
				"desc1"),
				new Date(), new Date(), 1111.11, new byte[] { 1, 0, 1 });
		journey1.setId(1);

		Journey journey2 = new Journey("journey2", new Location("Berlin"), new Location("Malmö"), new Tenant("tenant2", "pw2",
				"desc2"),
				new Date(), new Date(), 1111.11, null);
		journey2.setId(2);

		Journey journey3 = new Journey("journey3", new Location("Berlin"), new Location("Malmö"), new Tenant("tenant2", "pw2",
				"desc2"),
				new Date(), new Date(), 1111.11, null);
		journey2.setId(3);

		provider.add(journey1);
		provider.add(journey2);
		provider.add(journey3);

		assertThat(provider.getJourneyIndexByName("tenant1", "journey1"), is(journey1.getId()));
		assertThat(provider.getJourneyIndexByName("tenant2", "journey2"), is(journey2.getId()));
		assertThat(provider.getJourneyIndexByName("tenant2", "journey3"), is(journey3.getId()));
		assertThat(provider.getJourneyIndexByName("notexiting", "journey3"), is(0));
	}


	@Test
	public void testIsJourneyStart() throws Exception {
		Journey journey1 = new Journey("journey1", new Location("Ingolstadt"), new Location("Linz"), new Tenant("tenant1", "pw1",
				"desc1"),
				new Date(), new Date(), 1111.11, new byte[] { 1, 0, 1 });
		journey1.setId(1);

		Journey journey2 = new Journey("journey2", new Location("Berlin"), new Location("Malmö"), new Tenant("tenant2", "pw2",
				"desc2"),
				new Date(), new Date(), 1111.11, null);
		journey2.setId(2);

		Journey journey3 = new Journey("journey3", new Location("Berlin"), new Location("Malmö"), new Tenant("tenant2", "pw2",
				"desc2"),
				new Date(), new Date(), 1111.11, null);
		journey2.setId(3);

		provider.add(journey1);
		provider.add(journey2);
		provider.add(journey3);

		assertThat(provider.isJourneyStart("Berlin"), is(true));
		assertThat(provider.isJourneyStart("Ingolstadt"), is(true));
		assertThat(provider.isJourneyStart("stadt"), is(false));
		assertThat(provider.isJourneyStart("Linz"), is(false));
	}
}
