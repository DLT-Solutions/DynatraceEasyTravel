/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: HbaseSerializer.java
 * @date: 23.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.serializer;

import static org.apache.hadoop.hbase.util.Bytes.toBytes;

import java.util.Date;

import org.apache.hadoop.hbase.client.Result;

import com.dynatrace.easytravel.jpa.Base;
/**
 *
 * @author stefan.moschinski
 */
public abstract class HbaseSerializer<T extends Base> {

	private final String columnFamily;
	private final ColumnPrefix prefix;

	HbaseSerializer(String columnnFamilyName, ColumnPrefix prefix) {
		this.columnFamily = columnnFamilyName;
		this.prefix = prefix;
	}

	HbaseSerializer(HbaseSerializer<?> baseSerializer, String prefix) {
		this.columnFamily = baseSerializer.columnFamily;
		this.prefix = ColumnPrefix.apprehendPrefix(baseSerializer.prefix, prefix);
	}

	public PersistableHbaseObject serialize(T value) {
		PersistableHbaseObject mapping = new PersistableHbaseObject(prefix);
		serializeInternal(mapping, value);

		if (value.getCreated() == null) {
			value.setCreated(new Date());
		}

		mapping.add(Base.DATE_CREATED, value.getCreated());
		return mapping;
	}

	protected abstract PersistableHbaseObject serializeInternal(PersistableHbaseObject mapping, T value);

	public T deserialize(Result result) {
		ResultDeserializer deserializer = new ResultDeserializer(toBytes(columnFamily), result, prefix);
		T value = deserializeInternal(deserializer);
		value.setCreated(deserializer.getColumnDate(Base.DATE_CREATED));
		return value;
	}

	abstract T deserializeInternal(ResultDeserializer deserializer);

}
