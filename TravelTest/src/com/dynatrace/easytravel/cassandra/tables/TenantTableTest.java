package com.dynatrace.easytravel.cassandra.tables;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.cassandra.tables.objectmapper.TenantTable;
import com.dynatrace.easytravel.jpa.business.Tenant;

@Ignore
public class TenantTableTest extends CassandraTableTestUtil {

    TenantTable tenantTable = new TenantTable(getCluster());

    public TenantTableTest() {
        setCassandraObjects(tenantTable);
    }

    @Test
    public void testGetTenantByName() throws Exception {
        assertThat(tenantTable.getCount(), is(0));
        Tenant tenant1 = new Tenant("name1", "pw1", "desc1");
        tenantTable.addModel(tenant1);

        Tenant tenant2 = new Tenant("name2", "pw2", "desc2");
        tenantTable.addModel(tenant2);

        assertThat(tenantTable.getCount(), is(2));
        assertThat(tenantTable.getTenantByName("name1"), is(tenant1));
        assertThat(tenantTable.getTenantByName("name2"), is(tenant2));
    }

    @Test
    public void testUpdate() throws Exception {
        assertThat(tenantTable.getCount(), is(0));
        Date d1 = new Date(System.currentTimeMillis());
        Date d2 = new Date(System.currentTimeMillis() - 1000*60*60);
        assertThat(d1, is(not(d2)));
        Tenant tenant = new Tenant("name1", "pw1", "desc1");
        tenant.setLastLogin(d1);
        tenantTable.addModel(tenant);

        assertThat(tenantTable.getTenantByName("name1").getLastLogin(), is(d1));

        Tenant newTenant = new Tenant("name1", "pw1", "desc1");
        newTenant.setLastLogin(d2);
        tenantTable.updateModel(newTenant);

        assertThat(tenantTable.getCount(), is(1));
        assertThat(tenantTable.getTenantByName("name1"), is(newTenant));
        assertThat(tenantTable.getTenantByName("name1").getLastLogin(), is(d2));
    }
}
