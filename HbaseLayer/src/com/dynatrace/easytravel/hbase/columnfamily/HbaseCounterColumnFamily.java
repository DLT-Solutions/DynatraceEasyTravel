/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: HbaseCounterColumnFamily.java
 * @date: 29.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.columnfamily;

import static org.apache.hadoop.hbase.util.Bytes.toBytes;
import static org.apache.hadoop.hbase.util.Bytes.toLong;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import com.dynatrace.easytravel.hbase.HbaseDataController;
import com.dynatrace.easytravel.jpa.Base;

/**
 *
 * @author stefan.moschinski
 */
public class HbaseCounterColumnFamily extends HbaseColumnFamily<Base> {

	public static final String COUNTER_COLUMN_FAMILY_NAME = "CounterColumnFamily";

	/**
	 * 
	 * @param controller
	 * @param columnFamilyName
	 * @param namespace
	 * @author stefan.moschinski
	 */
	public HbaseCounterColumnFamily(HbaseDataController controller) {
		super(controller, COUNTER_COLUMN_FAMILY_NAME, "cou");
	}

	public void incrementByOne(String key, String columnName) {
		increment(key, columnName, 1L);
	}

	public void increment(String key, String columnName, double amount) {
		increment(key, columnName, decimalToLong(amount));
	}

	public void decrement(String key, String columnName, long amount) {
		super.increment(key, columnName, -1L * amount);
	}

	public void decrementByOne(String key, String columnName) {
		decrement(key, columnName, 1L);
	}

	public void decrement(String key, String columnName, double amount) {
		increment(key, columnName, decimalToLong(-1.0 * amount));
	}

	public long getLong(String rowKey, String columnName) {
		Result byKey = getByKey(columnName, toBytes(rowKey));
		if (byKey == null) {
			return 0;
		}

		return toLong(byKey.getValue(Bytes.toBytes(columnFamilyName),
				Bytes.toBytes(columnName)));
	}


	public double getDouble(String rowKey, String columnName) {
		return longToDecimal(getLong(rowKey, columnName));
	}

	private long decimalToLong(double amount) {
		return BigDecimal.valueOf(amount).movePointRight(2).longValue();
	}

	private double longToDecimal(long amount) {
		return BigDecimal
				.valueOf(amount, 2)
				.setScale(2, RoundingMode.HALF_UP)
				.doubleValue();
	}





}
