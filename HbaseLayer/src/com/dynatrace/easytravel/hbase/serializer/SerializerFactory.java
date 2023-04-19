/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: SerializerFactory.java
 * @date: 23.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.serializer;

import com.dynatrace.easytravel.hbase.columnfamily.HbaseBookingColumnFamily;
import com.dynatrace.easytravel.hbase.columnfamily.HbaseCounterColumnFamily;
import com.dynatrace.easytravel.hbase.columnfamily.HbaseJourneyColumnFamily;
import com.dynatrace.easytravel.hbase.columnfamily.HbaseLocationColumnFamily;
import com.dynatrace.easytravel.hbase.columnfamily.HbaseLoginHistoryColumnFamily;
import com.dynatrace.easytravel.hbase.columnfamily.HbaseTenantColumnFamily;
import com.dynatrace.easytravel.hbase.columnfamily.HbaseUserColumnFamily;
import com.dynatrace.easytravel.jpa.Base;


/**
 *
 * @author stefan.moschinski
 */
public class SerializerFactory {


	@SuppressWarnings("unchecked")
	public static <OV extends Base> HbaseSerializer<OV> createSerializer(
			String columnnFamilyName, ColumnPrefix prefix) {

		if (columnnFamilyName.equals(HbaseUserColumnFamily.USER_COLUMN_FAMILY_NAME)) {
			return (HbaseSerializer<OV>) new HbaseUserSerializer(columnnFamilyName, prefix);
		}

		if (columnnFamilyName.equals(HbaseTenantColumnFamily.TENANT_COLUMN_FAMILY_NAME)) {
			return (HbaseSerializer<OV>) new HbaseTenantSerializer(columnnFamilyName, prefix);
		}

		if (columnnFamilyName.equals(HbaseLocationColumnFamily.LOCATION_COLUMN_FAMILY_NAME)) {
			return (HbaseSerializer<OV>) new HbaseLocationSerializer(columnnFamilyName, prefix);
		}

		if (columnnFamilyName.equals(HbaseJourneyColumnFamily.JOURNEY_COLUMN_FAMILY_NAME)) {
			return (HbaseSerializer<OV>) new HbaseJourneySerializer(columnnFamilyName, prefix);
		}

		if (columnnFamilyName.equals(HbaseBookingColumnFamily.BOOKING_COLUMN_FAMILY_NAME)) {
			return (HbaseSerializer<OV>) new HbaseBookingSerializer(columnnFamilyName, prefix);
		}

		if (columnnFamilyName.equals(HbaseLoginHistoryColumnFamily.LOGIN_HISTORY_COLUMN_FAMILY_NAME)) {
			return (HbaseSerializer<OV>) new HbaseLoginHistorySerializer(columnnFamilyName, prefix);
		}

		if (columnnFamilyName.equals(HbaseCounterColumnFamily.COUNTER_COLUMN_FAMILY_NAME)) {
			return null; // should not be serialized!
		}

		throw new IllegalArgumentException();
	}
}
