/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: ResultDeserializer.java
 * @date: 25.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.serializer;

import static com.dynatrace.easytravel.hbase.serializer.HbaseSerializerUtil.toDate;
import static org.apache.hadoop.hbase.util.Bytes.*;

import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 * @author stefan.moschinski
 */
class ResultDeserializer {

	private final Result result;
	private final byte[] columnFamily;
	private ColumnPrefix prefix;

	public ResultDeserializer(byte[] columnFamily, Result result, ColumnPrefix prefix) {
		this.result = result;
		this.columnFamily = ArrayUtils.clone(columnFamily);
		this.prefix = prefix;
	}

	protected String getKeyAsString() {
		return Bytes.toString(getKey());
	}

	protected byte[] getKey() {
		return result.getRow();
	}

	protected String getColumnString(String value) {
		return Bytes.toString(getValue(value));
	}

	protected Date getColumnDate(String value) {
		byte[] dateBytes = getValue(value);
		return dateBytes == null ? null : toDate(dateBytes);
	}

	public double getColumnDouble(String value) {
		return toDouble(getValue(value));
	}

	/**
	 *
	 * @param journeyId
	 * @return
	 * @author stefan.moschinski
	 */
	public int getColumnInteger(String value) {
		return toInt(getValue(value));
	}

	public byte[] getColumnByteArray(String value) {
		return getValue(value);
	}

	private byte[] getValue(String value) {
		byte[] result = this.result.getValue(columnFamily, toBytes(prefix.getPrefixedColumnName(value)));
		return result;
	}

	/**
	 *
	 * @author stefan.moschinski
	 */
	public Result getResult() {
		return result;
	}

}
