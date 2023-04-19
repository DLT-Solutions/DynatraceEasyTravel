/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 * 
 * @file: HbaseUserSerializer.java
 * @date: 23.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.serializer;

import static com.dynatrace.easytravel.jpa.business.Booking.*;

import org.apache.hadoop.hbase.util.Bytes;

import com.dynatrace.easytravel.jpa.business.Booking;

/**
 * 
 * @author stefan.moschinski
 */
public class HbaseBookingSerializer extends HbaseSerializer<Booking> {

	HbaseBookingSerializer(String columnnFamilyName, ColumnPrefix prefix) {
		super(columnnFamilyName, prefix);
	}

	@Override
	protected Booking deserializeInternal(ResultDeserializer deserializer) {
		Booking booking = new Booking();
		booking.setId(deserializer.getKeyAsString());
		booking.setBookingDate(deserializer.getColumnDate(BOOKING_DATE));
		booking.setUser(HbaseUserSerializer.getSubSerializer(this, BOOKING_USER).deserializeSubColumns(deserializer.getResult()));
		booking.setJourney(HbaseJourneySerializer.getSubSerializer(this, BOOKING_JOURNEY).deserializeSubColumns(
				deserializer.getResult()));
		return booking;
	}

	@Override
	protected PersistableHbaseObject serializeInternal(PersistableHbaseObject persistableObj, Booking booking) {
		persistableObj.setKey(Bytes.toBytes(booking.getId()))
				.add(BOOKING_DATE, booking.getBookingDate())
				.add(HbaseUserSerializer.getSubSerializer(this, BOOKING_USER).serializeSubColumns(booking.getUser()))
				.add(HbaseJourneySerializer.getSubSerializer(this, BOOKING_JOURNEY).serializeSubColumns(booking.getJourney()));
		return persistableObj;
	}
}
