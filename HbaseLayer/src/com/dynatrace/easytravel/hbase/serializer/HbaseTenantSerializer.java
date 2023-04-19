/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 * 
 * @file: HbaseUserSerializer.java
 * @date: 23.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.serializer;

import static com.dynatrace.easytravel.jpa.business.Tenant.*;

import org.apache.hadoop.hbase.client.Result;

import com.dynatrace.easytravel.jpa.business.Tenant;

/**
 * 
 * @author stefan.moschinski
 */
public class HbaseTenantSerializer extends HbaseSerializer<Tenant> {


	HbaseTenantSerializer(HbaseSerializer<?> baseSerializer, String prefix) {
		super(baseSerializer, prefix);
	}

	HbaseTenantSerializer(String columnnFamilyName, ColumnPrefix prefix) {
		super(columnnFamilyName, prefix);
	}


	@Override
	protected Tenant deserializeInternal(ResultDeserializer deserializer) {
		Tenant tenant = new Tenant();
		tenant.setName(deserializeName(deserializer));
		tenant.setDescription(deserializer.getColumnString(TENANT_DESC));
		tenant.setPassword(deserializer.getColumnString(TENANT_PASSWORD));
		tenant.setLastLogin(deserializer.getColumnDate(TENANT_LAST_LOGIN));
		return tenant;
	}


	protected String deserializeName(ResultDeserializer deserializer) {
		return deserializer.getKeyAsString();
	}

	@Override
	protected PersistableHbaseObject serializeInternal(PersistableHbaseObject persistableObj, Tenant tenant) {
		addTenantName(persistableObj, tenant)
				.add(TENANT_DESC, tenant.getDescription())
				.add(TENANT_PASSWORD, tenant.getPassword())
				.add(TENANT_LAST_LOGIN, tenant.getLastLogin());
		return persistableObj;
	}


	protected PersistableHbaseObject addTenantName(PersistableHbaseObject persistableObj, Tenant tenant) {
		return persistableObj.setKey(tenant.getName());
	}


	static SubColumnsSerializer<Tenant> getSubSerializer(HbaseSerializer<?> baseSerializer, String prefix) {
		return new HbaseTenantSubColumnFamily(baseSerializer, prefix);
	}


	static class HbaseTenantSubColumnFamily extends HbaseTenantSerializer implements SubColumnsSerializer<Tenant> {

		public HbaseTenantSubColumnFamily(HbaseSerializer<?> baseSerializer, String prefix) {
			super(baseSerializer, prefix);
		}

		@Override
		public Tenant deserializeSubColumns(Result result) {
			return super.deserialize(result);
		}

		@Override
		protected String deserializeName(ResultDeserializer deserializer) {
			return deserializer.getColumnString(TENANT_NAME);
		}

		@Override
		public PersistableHbaseObject serializeSubColumns(Tenant tenant) {
			return serialize(tenant);
		}

		@Override
		protected PersistableHbaseObject addTenantName(PersistableHbaseObject persistableObj, Tenant tenant) {
			return persistableObj.add(TENANT_NAME, tenant.getName());
		}

	}

}
