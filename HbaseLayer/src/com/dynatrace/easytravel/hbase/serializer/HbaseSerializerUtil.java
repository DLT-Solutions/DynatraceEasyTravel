/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: HbaseSerializerUtil.java
 * @date: 23.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.serializer;

import java.util.Date;

import org.apache.hadoop.hbase.util.Bytes;


/**
 *
 * @author stefan.moschinski
 */
class HbaseSerializerUtil {

	public static byte[] fromDate(Date date) {
		if (date == null) {
			return null;
		}
		return Bytes.toBytes(date.getTime());
	}

	public static Date toDate(byte[] dateAsBytes) {
		return new Date(Bytes.toLong(dateAsBytes));
	}

}
