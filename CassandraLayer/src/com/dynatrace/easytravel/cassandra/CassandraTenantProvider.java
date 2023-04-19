package com.dynatrace.easytravel.cassandra;

import java.util.Collection;

import com.dynatrace.easytravel.cassandra.tables.objectmapper.TenantTable;
import com.dynatrace.easytravel.jpa.business.Tenant;
import com.dynatrace.easytravel.persistence.provider.TenantProvider;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class CassandraTenantProvider implements TenantProvider {
	
	private final TenantTable tenantTable;
	
	public CassandraTenantProvider(TenantTable tenantTable) {
		this.tenantTable = tenantTable;
	}

	@Override
	public Tenant add(Tenant value) {
		tenantTable.addModel(value);
		return value;
	}

	@Override
	public Tenant update(Tenant value) {
		tenantTable.updateModel(value);
		return value;
	}

	@Override
	public Collection<Tenant> getAll() {
		return tenantTable.getAllModels();
	}

	@Override
	public Collection<Tenant> getWithLimit(int limit) {
		return tenantTable.getModelsWithLimit(limit);
	}

	@Override
	public int getCount() {
		return tenantTable.getCount();
	}

	@Override
	public void reset() {
		tenantTable.reset();
	}

	@Override
	public Tenant getTenantByName(String tenantName) {
		return tenantTable.getTenantByName(tenantName);
	}

}
