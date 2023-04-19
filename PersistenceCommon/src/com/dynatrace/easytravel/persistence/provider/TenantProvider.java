/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: TenantProvider.java
 * @date: 14.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence.provider;

import com.dynatrace.easytravel.jpa.business.Tenant;


/**
 *
 * @author stefan.moschinski
 */
public interface TenantProvider extends EasyTravelPersistenceProvider<Tenant> {

	Tenant getTenantByName(String tenantName);
}
