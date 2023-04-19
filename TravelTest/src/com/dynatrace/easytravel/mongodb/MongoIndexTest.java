/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: MongoIndexTest.java
 * @date: 18.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;


/**
 *
 * @author stefan.moschinski
 */
public class MongoIndexTest {


	@Test
	public void testName() throws Exception {

		assertThat((Integer) MongoIndex.createIndex("Key1").getKey().get("Key1"), is(1));
		assertThat((Integer) MongoIndex.createIndex("Key1", "subkey").getKey().get("Key1.subkey"), is(1));
		assertThat((Integer) MongoIndex.createIndex("Key1", "subkey", "two").getKey().get("Key1.subkey.two"), is(1));


		MongoIndex compoundIndex = MongoIndex.createCompoundIndex("key1", "key2");
		assertThat((Integer) compoundIndex.getKey().get("key1"), is(1));
		assertThat((Integer) compoundIndex.getKey().get("key2"), is(1));

		MongoIndex compoundIndex2 = MongoIndex.createCompoundIndex("key1", "key2", "key3");
		assertThat((Integer) compoundIndex2.getKey().get("key1"), is(1));
		assertThat((Integer) compoundIndex2.getKey().get("key2"), is(1));
		assertThat((Integer) compoundIndex2.getKey().get("key3"), is(1));
	}
}
