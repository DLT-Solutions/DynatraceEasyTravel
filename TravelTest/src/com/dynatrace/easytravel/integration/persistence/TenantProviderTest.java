/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 * 
 * @file: providerTest.java
 * @date: 04.02.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.jpa.business.Tenant;
import com.dynatrace.easytravel.persistence.provider.TenantProvider;


/**
 *
 * @author stefan.moschinski
 */
@Ignore("ABSTRACT TEST")
public abstract class TenantProviderTest extends EasyTravelPersistenceProviderTest<TenantProvider> {


	@Test
	public void testGetTenantByName() throws Exception {
		Tenant tenant1 = new Tenant("name1", "pw1", "desc1");
		provider.add(tenant1);

		Tenant tenant2 = new Tenant("name2", "pw2", "desc2");
		provider.add(tenant2);

		assertThat(provider.getCount(), is(2));
		assertThat(provider.getTenantByName("name1"), is(tenant1));
		assertThat(provider.getTenantByName("name2"), is(tenant2));
	}

	@Test
	public void testUpdate() throws Exception {
		Tenant tenant = new Tenant("name1", "pw1", "desc1");
		provider.add(tenant);

		Tenant newTenant = new Tenant("name1", "pw1", "desc1");
		provider.update(newTenant);

		assertThat(provider.getCount(), is(1));
		assertThat(provider.getTenantByName("name1"), is(newTenant));
	}
}
