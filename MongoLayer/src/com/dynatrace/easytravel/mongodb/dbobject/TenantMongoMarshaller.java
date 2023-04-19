/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: TenantMongoMarshaller.java
 * @date: 13.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb.dbobject;

import static com.dynatrace.easytravel.jpa.business.Tenant.*;

import com.dynatrace.easytravel.jpa.business.Tenant;
import com.dynatrace.easytravel.mongodb.MongoConstants;
import com.mongodb.BasicDBObject;


/**
 *
 * @author stefan.moschinski
 */
public class TenantMongoMarshaller extends MongoObjectMarshaller<Tenant> {

	private static final long serialVersionUID = 1L;


	@Override
	protected BasicDBObject marshalTypeSpecific(Tenant value, boolean withId) {
		if (withId) {
			put(MongoConstants.ID, value.getName());
		}
		put(TENANT_PASSWORD, value.getPassword());
		put(TENANT_DESC, value.getDescription());
		put(TENANT_LAST_LOGIN, value.getLastLogin());
		return this;
	}

	@Override
	protected Tenant unmarshalTypeSpecific() {
		Tenant tenant = new Tenant();
		tenant.setName(getString(MongoConstants.ID));
		tenant.setPassword(getString(TENANT_PASSWORD));
		tenant.setDescription(getString(TENANT_DESC));
		tenant.setLastLogin(getDate(TENANT_LAST_LOGIN));

		return tenant;
	}

	@Override
	MongoObjectMarshaller<Tenant> newInstance() {
		return new TenantMongoMarshaller();
	}



}
