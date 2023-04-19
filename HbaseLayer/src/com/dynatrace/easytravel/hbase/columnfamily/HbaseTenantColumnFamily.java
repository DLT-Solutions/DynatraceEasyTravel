/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: HbaseTenantColumnFamily.java
 * @date: 25.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.columnfamily;

import com.dynatrace.easytravel.hbase.HbaseDataController;
import com.dynatrace.easytravel.jpa.business.Tenant;
import com.dynatrace.easytravel.persistence.provider.TenantProvider;


/**
 *
 * @author stefan.moschinski
 */
public class HbaseTenantColumnFamily extends HbaseColumnFamily<Tenant> implements TenantProvider {

	public static final String TENANT_COLUMN_FAMILY_NAME = "TenantColumnFamily";


	/**
	 * 
	 * @param controller
	 * @param columnFamilyName
	 * @author stefan.moschinski
	 */
	public HbaseTenantColumnFamily(HbaseDataController controller) {
		super(controller, TENANT_COLUMN_FAMILY_NAME, "ten");
	}


	@Override
	public Tenant getTenantByName(String tenantName) {
		return getByKey(tenantName);
	}

}
