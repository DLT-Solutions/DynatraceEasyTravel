/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: UserCollectionTest.java
 * @date: 13.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence.mongodb;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dynatrace.easytravel.integration.persistence.LocationProviderTest;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.persistence.controller.BusinessDatabaseController;
import com.google.common.collect.Lists;


/**
 *
 * @author stefan.moschinski
 */
public class LocationCollectionTest extends LocationProviderTest {


	@BeforeClass
	public static void setUpClass() throws IOException, CorruptInstallationException, InterruptedException {
		BusinessDatabaseController controller = MongoTestHelper.initializeController();
		initializeTest(controller, controller.getLocationProvider());
	}

	@AfterClass
	public static void tearDown() throws Exception {
		MongoDbProcedureRunner.tearDownClass();
	}

	@Test
	public void testGetLocationsSpecialCase() throws Exception {
		int no = 200;
		ArrayList<Location> expected = Lists.newArrayList();

		for (int i = 0; i < no; i++) {
			Location location = new Location("location" + i);
			provider.add(location);
			expected.add(location);
		}

		assertThat(provider.getLocations(100, 100).size(), is(100));
		assertThat(provider.getLocations(100, 100), contains(expected.subList(100, 200).toArray(new Location[0])));
	}

}
