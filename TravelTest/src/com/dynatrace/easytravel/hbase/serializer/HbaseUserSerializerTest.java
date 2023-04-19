/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: HbaseUserSerializerTest.java
 * @date: 24.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.serializer;

import static com.dynatrace.easytravel.jpa.business.User.*;
import static java.lang.String.format;
import static org.apache.hadoop.hbase.util.Bytes.toBytes;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
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

import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.model.LoyaltyStatus;
/**
 *
 * @author stefan.moschinski
 */
@RunWith(MockitoJUnitRunner.class)
public class HbaseUserSerializerTest {


	@Test
	public void testSerialize() {
		User user1 = new User("user1", "fullName1", "email1", "pw1");
		user1.setLoyaltyStatus(LoyaltyStatus.Gold.name());

		ColumnPrefix prefix = ColumnPrefix.createPrefix("start");
		PersistableHbaseObject serialize = new HbaseUserSerializer("any", prefix).serialize(user1);

		Map<byte[], byte[]> mapping = serialize.getMapping();
		Set<Entry<byte[], byte[]>> entries = mapping.entrySet();

		assertThat(serialize.getKey(), is(toBytes(user1.getName())));

		assertThat(
				entries,
				Matchers.<Entry<byte[], byte[]>> hasItem(byteArrayEntry(prefix.getPrefixedColumnName(USER_EMAIL),
						user1.getEmail())));
		assertThat(
				entries,
				Matchers.<Entry<byte[], byte[]>> hasItem(byteArrayEntry(prefix.getPrefixedColumnName(USER_FULL_NAME),
						user1.getFullName())));
		assertThat(
				entries,
				Matchers.<Entry<byte[], byte[]>> hasItem(byteArrayEntry(prefix.getPrefixedColumnName(USER_PASSWORD),
						user1.getPassword())));

		assertThat(mapping.get(toBytes(prefix.getPrefixedColumnName(USER_LAST_LOGIN))), is(nullValue()));
	}

	public static TypeSafeMatcher<Map.Entry<byte[], byte[]>> byteArrayEntry(String key, String value) {
		return new ByteArrayEntyMatcher(key, value);
	}


	@Mock
	Result result;

	@Test
	public void testDeserialize() {
		User user1 = new User("user1", "fullName1", "email1", "pw1");
		user1.setLoyaltyStatus(LoyaltyStatus.Gold.name());

		byte[] columnFamily = Bytes.toBytes("cf");

		ColumnPrefix prefix = ColumnPrefix.createPrefix("start");

		when(result.getRow()).thenReturn(toBytes(user1.getName()));
		when(result.getValue(columnFamily, toBytes(prefix.getPrefixedColumnName(USER_EMAIL)))).thenReturn(
				toBytes(user1.getEmail()));
		when(result.getValue(columnFamily, toBytes(prefix.getPrefixedColumnName(USER_FULL_NAME)))).thenReturn(
				toBytes(user1.getFullName()));
		when(result.getValue(columnFamily, toBytes(prefix.getPrefixedColumnName(USER_PASSWORD)))).thenReturn(
				toBytes(user1.getPassword()));
		when(result.getValue(columnFamily, toBytes(prefix.getPrefixedColumnName(USER_LOYALTY_STATUS)))).thenReturn(
				toBytes(user1.getLoyaltyStatus()));
		User deserializedUser = new HbaseUserSerializer("cf", prefix).deserialize(result);


		assertThat(deserializedUser, is(user1)); // test only name equality!!!!!!

		assertThat(deserializedUser.getEmail(), is(user1.getEmail()));
		assertThat(deserializedUser.getPassword(), is(user1.getPassword()));
		assertThat(deserializedUser.getFullName(), is(user1.getFullName()));
		assertThat(deserializedUser.getLoyaltyStatus(), is(user1.getLoyaltyStatus()));
	}



	private static class ByteArrayEntyMatcher extends TypeSafeMatcher<Map.Entry<byte[], byte[]>> {

		private final byte[] key;
		private final byte[] value;

		ByteArrayEntyMatcher(byte[] key, byte[] value) {
			this.key = key;
			this.value = value;
		}

		ByteArrayEntyMatcher(String key, String value) {
			this(toBytes(key), toBytes(value));
		}


		@Override
		public void describeTo(Description description) {
			description.appendText(format("Could not find key: '%s' - value: '%s' pair", Bytes.toString(key),
					Bytes.toString(value)));
		}

		@Override
		protected boolean matchesSafely(Entry<byte[], byte[]> item) {
			System.out.println(Bytes.toString(item.getKey()));
			return Arrays.equals(this.key, item.getKey()) && Arrays.equals(this.value, item.getValue());
		}

	}
}
