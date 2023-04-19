/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: UserCollection.java
 * @date: 13.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb.collection;

import static com.dynatrace.easytravel.mongodb.SimpleQueryBuilder.buildQuery;

import com.dynatrace.easytravel.jpa.business.Tenant;
import com.dynatrace.easytravel.mongodb.MongoConstants;
import com.dynatrace.easytravel.persistence.provider.TenantProvider;
import com.mongodb.DB;
import com.mongodb.DBObject;

/**
 *
 * @author stefan.moschinski
 */
public class TenantCollection extends MongoDbCollection<Tenant> implements TenantProvider {

	private static final String TENANT_COLLECTION_NAME = "TenantCollection";

	public TenantCollection(DB database) {
		super(database, TENANT_COLLECTION_NAME);
	}

	@Override
	public Tenant getTenantByName(String tenantName) {
		DBObject query = buildQuery(MongoConstants.ID).value(tenantName).create();
		return findOne(query);
	}

}
