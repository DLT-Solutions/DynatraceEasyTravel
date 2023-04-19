/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 * 
 * @file: HbaseUserSerializer.java
 * @date: 23.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.serializer;

import static com.dynatrace.easytravel.jpa.business.Journey.*;
import static org.apache.hadoop.hbase.util.Bytes.*;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import com.dynatrace.easytravel.jpa.business.Journey;

/**
 * 
 * @author stefan.moschinski
 */
public class HbaseJourneySerializer extends HbaseSerializer<Journey> {

	HbaseJourneySerializer(String columnnFamilyName, ColumnPrefix prefix) {
		super(columnnFamilyName, prefix);
	}

	/**
	 * 
	 * @param baseSerializer
	 * @param prefix
	 * @author stefan.moschinski
	 */
	public HbaseJourneySerializer(HbaseSerializer<?> baseSerializer, String prefix) {
		super(baseSerializer, prefix);
	}

	@Override
	protected Journey deserializeInternal(ResultDeserializer deserializer) {
		Journey journey = new Journey();

		journey.setId(Bytes.toInt(getJourneyId(deserializer)));
		journey.setAmount(deserializer.getColumnDouble(JOURNEY_AMOUNT));
		journey.setDescription(deserializer.getColumnString(JOURNEY_DESC));

		journey.setDestination(HbaseLocationSerializer.getSubSerializer(this, JOURNEY_DESTINATION).deserializeSubColumns(
				deserializer.getResult()));
		journey.setStart(HbaseLocationSerializer.getSubSerializer(this, JOURNEY_START).deserializeSubColumns(
				deserializer.getResult()));

		journey.setFromDate(deserializer.getColumnDate(JOURNEY_FROM_DATE));
		journey.setToDate(deserializer.getColumnDate(JOURNEY_TO_DATE));
		journey.setName(deserializer.getColumnString(JOURNEY_NAME));
		journey.setPicture(deserializer.getColumnByteArray(JOURNEY_PICTURE));

		journey.setTenant(HbaseTenantSerializer.getSubSerializer(this, JOURNEY_TENANT).deserializeSubColumns(
				deserializer.getResult()));

		return journey;
	}

	protected byte[] getJourneyId(ResultDeserializer deserializer) {
		return deserializer.getKey();
	}

	@Override
	protected PersistableHbaseObject serializeInternal(PersistableHbaseObject persistableObj, Journey journey) {
		addJourneyId(persistableObj, journey.getId())
				.add(JOURNEY_AMOUNT, journey.getAmount())
				.add(JOURNEY_DESC, journey.getDescription())

				.add(JOURNEY_FROM_DATE, journey.getFromDate())
				.add(JOURNEY_TO_DATE, journey.getToDate())

				.add(JOURNEY_NAME, journey.getName())
				.add(JOURNEY_PICTURE, journey.getPicture())

				.add(HbaseLocationSerializer.getSubSerializer(this, JOURNEY_START).serializeSubColumns(journey.getStart()))
				.add(HbaseLocationSerializer.getSubSerializer(this, JOURNEY_DESTINATION).serializeSubColumns(
						journey.getDestination()))

				.add(HbaseTenantSerializer.getSubSerializer(this, JOURNEY_TENANT).serializeSubColumns(journey.getTenant()));

		return persistableObj;
	}

	protected PersistableHbaseObject addJourneyId(PersistableHbaseObject persistableObj, int id) {
		return persistableObj.setKey(Bytes.toBytes(id));
	}

	static SubColumnsSerializer<Journey> getSubSerializer(HbaseSerializer<?> baseSerializer, String prefix) {
		return new HbaseJourneySubSerializer(baseSerializer, prefix);
	}


	static class HbaseJourneySubSerializer extends HbaseJourneySerializer implements SubColumnsSerializer<Journey> {

		public HbaseJourneySubSerializer(HbaseSerializer<?> baseSerializer, String prefix) {
			super(baseSerializer, prefix);
		}

		@Override
		public Journey deserializeSubColumns(Result result) {
			return deserialize(result);
		}

		@Override
		public PersistableHbaseObject serializeSubColumns(Journey journey) {
			return serialize(journey);
		}

		@Override
		protected byte[] getJourneyId(ResultDeserializer deserializer) {
			return deserializer.getColumnByteArray(JOURNEY_ID);
		}

		@Override
		protected PersistableHbaseObject addJourneyId(PersistableHbaseObject persistableObj, int journeyId) {
			return persistableObj.add(JOURNEY_ID, toBytes(journeyId));
		}


	}
}
