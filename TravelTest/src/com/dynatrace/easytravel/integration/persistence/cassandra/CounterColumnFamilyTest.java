/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: LoginCounterColumnFamilyTest.java
 * @date: 13.07.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence.cassandra;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dynatrace.easytravel.cassandra.CassandraBusinessController;
import com.dynatrace.easytravel.cassandra.tables.cql.CountersTable;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.persistence.controller.BusinessDatabaseController;


/**
 *
 * @author stefan.moschinski
 */
public class CounterColumnFamilyTest {
	private static CassandraTestHelper cassandraHelper;
	private static BusinessDatabaseController controller;
	private CountersTable countersTable;

	@BeforeClass
	public static void setUpClass() throws IOException, CorruptInstallationException, InterruptedException {
		cassandraHelper = CassandraTestHelper.setUpNodes();
		controller = cassandraHelper.getController();
	}

	@AfterClass
	public static void tearDownClass() {
		cassandraHelper.tearDown();
	}

	@Before
	public void setUp() throws Exception {
		countersTable = ((CassandraBusinessController) controller).createCountersTable();
		countersTable.reset();
	}

	/**
	 * Test method for {@link com.dynatrace.easytravel.cassandra.columfamily.CounterColumnFamily#getLoginCount(java.lang.String)}.
	 */
	@Test
	public void testGetLoginCount() {
		countersTable.create();

		String userName = "toni2";

		assertEquals(0, countersTable.getLoginCount(userName));
		assertEquals(0, countersTable.getBookingCountForUser(userName));

		countersTable.incrementLoginCountForUser(userName);
		assertEquals(1, countersTable.getLoginCount(userName));
		assertEquals(0, countersTable.getBookingCountForUser(userName));

		countersTable.incrementBookingCountForUser(userName);
		assertEquals(1, countersTable.getLoginCount(userName));
		assertEquals(1, countersTable.getBookingCountForUser(userName));
	}

	@Test
	public void testGetTotalBookingCount() {
		String toni = "toni";
		String toni2 = "toni2";

		countersTable.incrementBookingCountForUser(toni);
		countersTable.incrementBookingCountForUser(toni2);

		countersTable.incrementLoginCountForUser(toni);
		countersTable.incrementLoginCountForUser(toni2);

		assertEquals(1, countersTable.getBookingCountForUser(toni));
		assertEquals(1, countersTable.getBookingCountForUser(toni2));

		assertThat(countersTable.getTotalBookingCountUser(), is(2));
	}

	/**
	 * Test method for {@link com.dynatrace.easytravel.cassandra.columfamily.CounterColumnFamily#incrementLoginCountForUser(java.lang.String)}.
	 */
	@Test
	public void testIncrementLoginCountForUser() {
		String tenantName = "tenant1";

		assertEquals(0.0, countersTable.getSalesAmountForTenant(tenantName), 0.0);

		countersTable.incrementSalesForTenant(tenantName, 100.20);
		assertEquals(100.20, countersTable.getSalesAmountForTenant(tenantName), 0.0);

		countersTable.decrementSalesForTenant(tenantName, 60.05);
		assertEquals(100.20 - 60.05, countersTable.getSalesAmountForTenant(tenantName), 0.005);

		String tenantName2 = "tenant 111";

		assertEquals(0.0, countersTable.getSalesAmountForTenant(tenantName2), 0.0);

		countersTable.incrementSalesForTenant(tenantName2, 100.20);
		assertEquals(100.20, countersTable.getSalesAmountForTenant(tenantName2), 0.0);

		countersTable.decrementSalesForTenant(tenantName2, 60.05);
		assertEquals(100.20 - 60.05, countersTable.getSalesAmountForTenant(tenantName2), 0.005);
	}

	@Test
	public void testLocationCountForTenant() {
		String tenant1 = "tenant1";

		int no = 10;
		for (int i = 0; i < no; i++) {
			countersTable.incrementDestinationCountForTenant(tenant1, "Istanbul");
		}
		assertThat(countersTable.getDestinationCountForTenant(tenant1, "Istanbul"), is(no));
		assertThat(countersTable.getDestinationCountForTenant(tenant1, "non_existing"), is(0));
	}

}
