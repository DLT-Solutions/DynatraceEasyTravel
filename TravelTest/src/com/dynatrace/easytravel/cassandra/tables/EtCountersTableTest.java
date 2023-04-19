package com.dynatrace.easytravel.cassandra.tables;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.datastax.driver.core.Metadata;
import com.dynatrace.easytravel.cassandra.tables.cql.CountersTable;

@Ignore
public class EtCountersTableTest extends CassandraTableTestUtil {

    private CountersTable countersTable;

    @Before
    public void createTable() {
        countersTable = new CountersTable(getCluster());
        reset(countersTable);
    }

    @After
    public void cleanup() {
        countersTable.delete();
    }

    @Test
    public void testCreate() {
        countersTable.create();

        Metadata metadata = getSession().getCluster().getMetadata();
        System.out.println("keyspaces: " + metadata.getKeyspaces());
    }

    @Test
    public  void testLoginCount1() {
        String user = "maria";
        assertEquals(0, countersTable.getLoginCount(user));
        countersTable.incrementLoginCountForUser(user);
        assertEquals(1, countersTable.getLoginCount(user));
    }

    @Test
    public void testGetLoginCount() {
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

        assertEquals(2, countersTable.getTotalBookingCountUser());
    }

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
        assertEquals(10, countersTable.getDestinationCountForTenant(tenant1, "Istanbul"));
        assertEquals(0, countersTable.getDestinationCountForTenant(tenant1, "non_existing"));
    }
}
