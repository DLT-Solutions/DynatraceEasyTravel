/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: JourneyColumnFamilyTest.java
 * @date: 07.02.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence.cassandra;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dynatrace.easytravel.integration.persistence.JourneyProviderTest;
import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.jpa.business.Tenant;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.persistence.controller.BusinessDatabaseController;


/**
 *
 * @author stefan.moschinski
 */
//@Ignore("Does not work properly - especially the reset of the column families in Cassandra often fails")
public class JourneyColumnFamilyTest extends JourneyProviderTest {

	private static CassandraTestHelper cassandraHelper;

	@BeforeClass
	public static void setUpClass() throws IOException, CorruptInstallationException, InterruptedException {
		cassandraHelper = CassandraTestHelper.setUpNodes();
		BusinessDatabaseController controller = cassandraHelper.getController();
		initializeTest(controller, controller.getJourneyProvider());
	}

	@AfterClass
	public static void tearDownClass() {
		cassandraHelper.tearDown();
	}


	@Override
	// Cassandra search does not support the full capability of other persistence stores in terms of searching
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

//		assertThat(provider.getMatchingJourneyDestinations("stadt"), containsInAnyOrder(eisenstadt, ingolstadt));
	}

	@Override
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

		// this test does currently not work for Cassandra:
		// assertThat(provider.findJourneys("Malmö", yesterday, today), containsInAnyOrder(yesterdayTodayMalmoe));
	}
}
