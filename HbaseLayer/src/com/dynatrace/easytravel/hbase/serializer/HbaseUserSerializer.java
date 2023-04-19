/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 * 
 * @file: HbaseUserSerializer.java
 * @date: 23.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.serializer;

import static com.dynatrace.easytravel.hbase.serializer.HbaseSerializerUtil.*;
import static com.dynatrace.easytravel.jpa.business.User.*;

import org.apache.hadoop.hbase.client.Result;

import com.dynatrace.easytravel.jpa.business.User;

/**
 * 
 * @author stefan.moschinski
 */
public class HbaseUserSerializer extends HbaseSerializer<User> {


	HbaseUserSerializer(String columnnFamilyName, ColumnPrefix prefix) {
		super(columnnFamilyName, prefix);
	}

	HbaseUserSerializer(HbaseSerializer<?> baseSerializer, String prefix) {
		super(baseSerializer, prefix);
	}

	@Override
	protected User deserializeInternal(ResultDeserializer deserializer) {
		User user = new User();
		user.setName(deserializeUserName(deserializer));

		user.setEmail(deserializer.getColumnString(USER_EMAIL));
		user.setFullName(deserializer.getColumnString(USER_FULL_NAME));
		user.setPassword(deserializer.getColumnString(USER_PASSWORD));
		user.setLastLogin(deserializer.getColumnDate(USER_LAST_LOGIN));
		user.setLoyaltyStatus(deserializer.getColumnString(USER_LOYALTY_STATUS));
		return user;
	}

	protected String deserializeUserName(ResultDeserializer deserializer) {
		return deserializer.getKeyAsString();
	}


	@Override
	protected PersistableHbaseObject serializeInternal(PersistableHbaseObject persistableObj, User value) {
		addUserName(persistableObj, value.getName())
				.add(USER_EMAIL, value.getEmail())
				.add(USER_FULL_NAME, value.getFullName())
				.add(USER_LAST_LOGIN, fromDate(value.getLastLogin()))
				.add(USER_PASSWORD, value.getPassword())
				.add(USER_LOYALTY_STATUS, value.getLoyaltyStatus());
		return persistableObj;
	}

	protected PersistableHbaseObject addUserName(PersistableHbaseObject persistableObj, String userName) {
		return persistableObj.setKey(userName);
	}

	static SubColumnsSerializer<User> getSubSerializer(HbaseSerializer<?> baseSerializer, String prefix) {
		return new HbaseUserSubSerializer(baseSerializer, prefix);
	}

	private static class HbaseUserSubSerializer extends HbaseUserSerializer implements SubColumnsSerializer<User> {

		HbaseUserSubSerializer(HbaseSerializer<?> baseSerializer, String prefix) {
			super(baseSerializer, prefix);
		}

		@Override
		public User deserializeSubColumns(Result result) {
			return deserialize(result);
		}

		@Override
		public PersistableHbaseObject serializeSubColumns(User user) {
			return serialize(user);
		}

		@Override
		protected PersistableHbaseObject addUserName(PersistableHbaseObject persistableObj, String userName) {
			return persistableObj.add(USER_NAME, userName);
		}

		@Override
		protected String deserializeUserName(ResultDeserializer deserializer) {
			return deserializer.getColumnString(USER_NAME);
		}



	}
}
