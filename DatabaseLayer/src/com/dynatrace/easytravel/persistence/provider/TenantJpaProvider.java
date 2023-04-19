/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: TenantJpaProvider.java
 * @date: 18.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence.provider;

import com.dynatrace.easytravel.jpa.business.Tenant;
import com.dynatrace.easytravel.persistence.JpaDatabaseController;


/**
 *
 * @author stefan.moschinski
 */
public class TenantJpaProvider extends JpaProvider<Tenant> implements TenantProvider {


	public TenantJpaProvider(JpaDatabaseController controller) {
		super(controller, Tenant.class);
	}

	@Override
	public Tenant getTenantByName(String tenantName) {
		return find(tenantName);
	}

}
