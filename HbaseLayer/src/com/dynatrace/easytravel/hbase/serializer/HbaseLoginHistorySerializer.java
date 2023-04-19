/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 * 
 * @file: HbaseUserSerializer.java
 * @date: 23.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.serializer;

import static com.dynatrace.easytravel.jpa.business.LoginHistory.*;
import static org.apache.hadoop.hbase.util.Bytes.*;

import org.apache.hadoop.hbase.util.Bytes;

import com.dynatrace.easytravel.jpa.business.LoginHistory;

/**
 * 
 * @author stefan.moschinski
 */
public class HbaseLoginHistorySerializer extends HbaseSerializer<LoginHistory> {

	HbaseLoginHistorySerializer(String columnnFamilyName, ColumnPrefix prefix) {
		super(columnnFamilyName, prefix);
	}

	@Override
	protected LoginHistory deserializeInternal(ResultDeserializer deserializer) {
		LoginHistory loginHistory = new LoginHistory();
		loginHistory.setId(toInt(deserializer.getKey()));
		loginHistory.setLoginDate(deserializer.getColumnDate(LOGIN_HISTORY_LOGIN_DATE));
		loginHistory.setUser(HbaseUserSerializer.getSubSerializer(this, LOGIN_HISTORY_USER).deserializeSubColumns(
				deserializer.getResult()));
		return loginHistory;
	}

	@Override
	protected PersistableHbaseObject serializeInternal(PersistableHbaseObject persistableObj, LoginHistory loginHistory) {
		persistableObj.setKey(Bytes.toBytes(loginHistory.getId()))
				.add(LOGIN_HISTORY_LOGIN_DATE, loginHistory.getLoginDate())
				.add(HbaseUserSerializer.getSubSerializer(this, LOGIN_HISTORY_USER).serializeSubColumns(loginHistory.getUser()));
		return persistableObj;
	}
}
