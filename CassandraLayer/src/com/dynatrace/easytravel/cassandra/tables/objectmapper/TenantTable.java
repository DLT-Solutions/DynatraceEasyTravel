package com.dynatrace.easytravel.cassandra.tables.objectmapper;

import com.dynatrace.easytravel.cassandra.base.CassandraModelTable;
import com.dynatrace.easytravel.cassandra.base.EtCluster;
import com.dynatrace.easytravel.cassandra.entities.TenantEntity;
import com.dynatrace.easytravel.jpa.business.Tenant;

/**
 * 
 * @author Rafal.Psciuk
 *
 */
public class TenantTable extends CassandraModelTable<TenantEntity, Tenant> {
    public static final String TENANT_TABLE = "TenantTable";

    public TenantTable(EtCluster cluster) {
        super(cluster, TenantEntity.class);
    }

    @Override
    protected TenantEntity getEntity(Tenant model) {
        return new TenantEntity(model);
    }

    @Override
    public void create() {
        executeQuery("CREATE TABLE IF NOT EXISTS " + TENANT_TABLE + " (name text PRIMARY KEY, password text, description text, lastLogin timestamp);");
    }

    public Tenant getTenantByName(String tenantName) {
        return getModel(tenantName);
    }
}
