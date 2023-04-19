/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: HbaseUserSerializerTest.java
 * @date: 24.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.serializer;

import static com.dynatrace.easytravel.jpa.business.Journey.*;
import static com.dynatrace.easytravel.jpa.business.Location.LOCATION_NAME;
import static com.dynatrace.easytravel.jpa.business.Tenant.*;
import static java.lang.String.format;
import static org.apache.hadoop.hbase.util.Bytes.toBytes;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.jpa.business.Tenant;
/**
 *
 * @author stefan.moschinski
 */
@RunWith(MockitoJUnitRunner.class)
public class HbaseJourneySerializerTest {


	@Test
	public void testSerialize() {
		Tenant tenant = new Tenant("tenant1", "pw1", "desc1");
		Journey journey = new Journey("journey1", new Location("Linz"), new Location("Wien"), tenant, new Date(), new Date(),
				103.11, new byte[] { 0, 1, 0 });


		ColumnPrefix prefix = ColumnPrefix.createPrefix("start");
		PersistableHbaseObject serialize = new HbaseJourneySerializer("any", prefix).serialize(journey);

		Map<byte[], byte[]> mapping = serialize.getMapping();
		Set<Entry<byte[], byte[]>> entries = mapping.entrySet();


		assertThat(Arrays.equals(serialize.getKey(), Bytes.toBytes(journey.getId())), is(true));
		assertThat(entries, Matchers.<Entry<byte[], byte[]>> hasItem(byteArrayEntry(
				prefix.getPrefixedColumnName(JOURNEY_NAME), journey.getName())));
		String prefixedColumnName = prefix.getPrefixedColumnName(JOURNEY_PICTURE);
		TypeSafeMatcher<Entry<byte[], byte[]>> byteArrayEntry;
		byteArrayEntry = byteArrayEntry(prefixedColumnName, journey.getPicture());
		assertThat(entries, Matchers.<Entry<byte[], byte[]>>hasItem(byteArrayEntry));
		assertThat(entries,
				Matchers.<Entry<byte[], byte[]>> hasItem(byteArrayEntry(prefix.getPrefixedColumnName(JOURNEY_AMOUNT),
						Bytes.toBytes(journey.getAmount()))));
		assertThat(
				entries,
				Matchers.<Entry<byte[], byte[]>> hasItem(byteArrayEntry(prefix.getPrefixedColumnName(JOURNEY_FROM_DATE),
						journey.getFromDate())));
		assertThat(
				entries,
				Matchers.<Entry<byte[], byte[]>> hasItem(byteArrayEntry(prefix.getPrefixedColumnName(JOURNEY_TO_DATE),
						journey.getToDate())));

		assertThat(mapping.get(Bytes.toBytes(JOURNEY_DESC)), is(nullValue()));
//		assertThat(entries, Matchers.<Entry<byte[], byte[]>>hasItem(byteArrayEntry(prefix.getPrefixedColumnName(JOURNEY_DESC), journey.getDescription())));

		ColumnPrefix startPrefix = ColumnPrefix.apprehendPrefix(prefix, JOURNEY_START);
		assertThat(entries,
				Matchers.<Entry<byte[], byte[]>> hasItem(byteArrayEntry(startPrefix.getPrefixedColumnName(LOCATION_NAME),
						journey.getStart().getName())));

		ColumnPrefix destPrefix = ColumnPrefix.apprehendPrefix(prefix, JOURNEY_DESTINATION);
		assertThat(entries,
				Matchers.<Entry<byte[], byte[]>> hasItem(byteArrayEntry(destPrefix.getPrefixedColumnName(LOCATION_NAME),
						journey.getDestination().getName())));

		ColumnPrefix tenantPrefix = ColumnPrefix.apprehendPrefix(prefix, JOURNEY_TENANT);
		assertThat(entries,
				Matchers.<Entry<byte[], byte[]>> hasItem(byteArrayEntry(tenantPrefix.getPrefixedColumnName(TENANT_NAME),
						journey.getTenant().getName())));
		assertThat(entries,
				Matchers.<Entry<byte[], byte[]>> hasItem(byteArrayEntry(tenantPrefix.getPrefixedColumnName(TENANT_DESC),
						journey.getTenant().getDescription())));
		assertThat(entries,
				Matchers.<Entry<byte[], byte[]>> hasItem(byteArrayEntry(tenantPrefix.getPrefixedColumnName(TENANT_PASSWORD),
						journey.getTenant().getPassword())));
	}



	@Mock
	Result result;

	@Test
	public void testDeserialize() {
		Tenant tenant = new Tenant("tenant1", "pw1", "desc1");
		Journey journey = new Journey("journey1", new Location("Linz"), new Location("Wien"), tenant, new Date(), new Date(),
				103.11, new byte[] { 0, 1, 0 });


		ColumnPrefix prefix = ColumnPrefix.createPrefix("start");
		String columnFamilyName = "any";

		byte[] columnFamily = Bytes.toBytes(columnFamilyName);

		when(result.getRow()).thenReturn(toBytes(journey.getId()));
		when(result.getValue(columnFamily, toBytes(prefix.getPrefixedColumnName(JOURNEY_NAME)))).thenReturn(
				toBytes(journey.getName()));
		when(result.getValue(columnFamily, toBytes(prefix.getPrefixedColumnName(JOURNEY_PICTURE)))).thenReturn(
				journey.getPicture());
		when(result.getValue(columnFamily, toBytes(prefix.getPrefixedColumnName(JOURNEY_AMOUNT)))).thenReturn(
				toBytes(journey.getAmount()));
		when(result.getValue(columnFamily, toBytes(prefix.getPrefixedColumnName(JOURNEY_FROM_DATE)))).thenReturn(
				HbaseSerializerUtil.fromDate(journey.getFromDate()));
		when(result.getValue(columnFamily, toBytes(prefix.getPrefixedColumnName(JOURNEY_TO_DATE)))).thenReturn(
				HbaseSerializerUtil.fromDate(journey.getToDate()));
		when(result.getValue(columnFamily, toBytes(prefix.getPrefixedColumnName(JOURNEY_DESC)))).thenReturn(
				null);

		ColumnPrefix startPrefix = ColumnPrefix.apprehendPrefix(prefix, JOURNEY_START);
		when(result.getValue(columnFamily, toBytes(startPrefix.getPrefixedColumnName(LOCATION_NAME)))).thenReturn(
				toBytes(journey.getStart().getName()));

		ColumnPrefix destPrefix = ColumnPrefix.apprehendPrefix(prefix, JOURNEY_DESTINATION);
		when(result.getValue(columnFamily, toBytes(destPrefix.getPrefixedColumnName(LOCATION_NAME)))).thenReturn(
				toBytes(journey.getDestination().getName()));


		ColumnPrefix tenantPrefix = ColumnPrefix.apprehendPrefix(prefix, JOURNEY_TENANT);
		when(result.getValue(columnFamily, toBytes(tenantPrefix.getPrefixedColumnName(TENANT_NAME)))).thenReturn(
				toBytes(journey.getTenant().getName()));
		when(result.getValue(columnFamily, toBytes(tenantPrefix.getPrefixedColumnName(TENANT_DESC)))).thenReturn(
				toBytes(journey.getTenant().getDescription()));
		when(result.getValue(columnFamily, toBytes(tenantPrefix.getPrefixedColumnName(TENANT_PASSWORD)))).thenReturn(
				toBytes(journey.getTenant().getPassword()));

		Journey deserializedJourney = new HbaseJourneySerializer(columnFamilyName, prefix).deserialize(result);
		assertThat(deserializedJourney, is(journey));
	}


	public static TypeSafeMatcher<Map.Entry<byte[], byte[]>> byteArrayEntry(String key, Date value) {
		return new ByteArrayEntyMatcher(key, HbaseSerializerUtil.fromDate(value));
	}

	public static TypeSafeMatcher<Map.Entry<byte[], byte[]>> byteArrayEntry(String key, String value) {
		return new ByteArrayEntyMatcher(key, Bytes.toBytes(value));
	}

	public static TypeSafeMatcher<Map.Entry<byte[], byte[]>> byteArrayEntry(String key, byte[] value) {
		return new ByteArrayEntyMatcher(key, value);
	}


	private static class ByteArrayEntyMatcher extends TypeSafeMatcher<Map.Entry<byte[], byte[]>> {

		private final byte[] key;
		private final byte[] value;

		ByteArrayEntyMatcher(byte[] key, byte[] value) {
			this.key = key;
			this.value = value;
		}


		ByteArrayEntyMatcher(String key, byte[] value) {
			this(toBytes(key), value);
		}


		@Override
		public void describeTo(Description description) {
			description.appendText(format("Could not find key: '%s' - value: '%s' pair", Bytes.toString(key),
					Bytes.toString(value)));
		}

		@Override
		protected boolean matchesSafely(Entry<byte[], byte[]> item) {
			return Arrays.equals(this.key, item.getKey()) && Arrays.equals(this.value, item.getValue());
		}

	}
}
