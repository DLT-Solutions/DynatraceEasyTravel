/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: SubColumnsSerializer.java
 * @date: 25.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.serializer;

import org.apache.hadoop.hbase.client.Result;



/**
 * Represents serializers that are used to serialize entities within other entities.
 * For example, if a booking entity contains a user entity and you want to save both in the same
 * column family you can use a {@link SubColumnsSerializer} to serialize the user as a part of
 * booking.
 * 
 * @author stefan.moschinski
 */
interface SubColumnsSerializer<T> {

	/**
	 * 
	 * @param result
	 * @return
	 * @author stefan.moschinski
	 */
	abstract T deserializeSubColumns(Result result);

	/**
	 * 
	 * @param location
	 * @author stefan.moschinski
	 * @return
	 */
	PersistableHbaseObject serializeSubColumns(T location);
}
