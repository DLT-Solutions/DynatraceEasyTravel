/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: ColumnKeyValueMapping.java
 * @date: 23.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.serializer;

import static com.dynatrace.easytravel.hbase.serializer.HbaseSerializerUtil.fromDate;
import static org.apache.hadoop.hbase.util.Bytes.toBytes;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.hadoop.hbase.util.Bytes;

import com.dynatrace.easytravel.annotation.TestOnly;
import com.google.common.collect.Maps;

/**
 *
 * @author stefan.moschinski
 */
public class PersistableHbaseObject implements Iterable<Entry<byte[], byte[]>> {

	private final Map<byte[], byte[]> keyToValueMapping;
	private byte[] key;
	private ColumnPrefix prefix;


	public PersistableHbaseObject(ColumnPrefix prefix) {
		this.prefix = prefix;
		this.keyToValueMapping = Maps.newHashMapWithExpectedSize(6);
	}

	public PersistableHbaseObject add(String columnName, byte[] value) {
		if (value == null) {
			return this;
		}
		keyToValueMapping.put(Bytes.toBytes(prefix.getPrefixedColumnName(columnName)), value);	// NOSONAR - ignore warning about array init, we are working on byte[] on purpose here
		return this;
	}

	public PersistableHbaseObject add(String key, String value) {
		if (value == null) {
			return this;
		}
		return add(key, Bytes.toBytes(value));
	}

	public PersistableHbaseObject add(String key, Date date) {
		if (date == null) {
			return this;
		}
		return add(key, fromDate(date));
	}

	public PersistableHbaseObject add(String key, double value) {
		return add(key, toBytes(value));
	}

	public PersistableHbaseObject setKey(String key) {
		return setKey(Bytes.toBytes(key));
	}

	public PersistableHbaseObject setKey(byte[] key) {
		this.key = ArrayUtils.clone(key);
		return this;
	}

	public PersistableHbaseObject add(PersistableHbaseObject serializeSubColumns) {
		keyToValueMapping.putAll(serializeSubColumns.keyToValueMapping);
		return this;
	}



	/**
	 *
	 * @return
	 * @author stefan.moschinski
	 */
	public byte[] getKey() {
		return key;
	}

	@Override
	public Iterator<Entry<byte[], byte[]>> iterator() {
		return keyToValueMapping.entrySet().iterator();
	}

	@TestOnly
	Map<byte[], byte[]> getMapping() {
		return keyToValueMapping;
	}


}
