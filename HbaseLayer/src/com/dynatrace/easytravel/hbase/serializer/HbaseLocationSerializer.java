/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 * 
 * @file: HbaseUserSerializer.java
 * @date: 23.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.serializer;

import static com.dynatrace.easytravel.jpa.business.Location.*;

import org.apache.hadoop.hbase.client.Result;

import com.dynatrace.easytravel.jpa.business.Location;

/**
 * 
 * @author stefan.moschinski
 */
public class HbaseLocationSerializer extends HbaseSerializer<Location> {


	HbaseLocationSerializer(String columnnFamilyName, ColumnPrefix prefix) {
		super(columnnFamilyName, prefix);
	}

	HbaseLocationSerializer(HbaseSerializer<?> base, String prefix) {
		super(base, prefix);
	}

	@Override
	protected Location deserializeInternal(ResultDeserializer deserializer) {
		return deserializeInternal(deserializer, false);
	}

	private Location deserializeInternal(ResultDeserializer deserializer, boolean subColumn) {
		Location tenant = new Location();
		tenant.setName(deserializer.getColumnString(LOCATION_NAME));
		return tenant;
	}

	@Override
	protected PersistableHbaseObject serializeInternal(PersistableHbaseObject persistableObj, Location location) {
		addLocationKey(persistableObj, location.getName());
		persistableObj.add(LOCATION_NAME, location.getName());
		return persistableObj;
	}

	protected void addLocationKey(PersistableHbaseObject persistableObj, String locationName) {
		persistableObj.setKey(locationName.toLowerCase());
	}

	private static class HbaseSubLocationSerializer extends HbaseLocationSerializer implements SubColumnsSerializer<Location> {

		HbaseSubLocationSerializer(HbaseSerializer<?> baseSerializer, String prefix) {
			super(baseSerializer, prefix);
		}

		@Override
		public Location deserializeSubColumns(Result result) {
			return deserialize(result);
		}

		@Override
		public PersistableHbaseObject serializeSubColumns(Location location) {
			return serialize(location);
		}

		@Override
		protected void addLocationKey(PersistableHbaseObject persistableObj, String locationName) {
			// NOTHING TO DO HERE - DO NOT DELETE!
		}



	}

	/**
	 * 
	 * @param hbaseJourneySerializer
	 * @return
	 * @author stefan.moschinski
	 * @param prefix
	 */
	static SubColumnsSerializer<Location> getSubSerializer(HbaseSerializer<?> baseSerializer, String prefix) {
		return new HbaseSubLocationSerializer(baseSerializer, prefix);
	}

}
