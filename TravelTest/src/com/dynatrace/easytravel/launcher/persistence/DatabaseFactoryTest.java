/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: DatabaseFactory.java
 * @date: 11.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.launcher.persistence;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.dynatrace.easytravel.cassandra.CassandraDatabase;
import com.dynatrace.easytravel.constants.BaseConstants.BusinessBackend.Persistence;
import com.dynatrace.easytravel.hbase.HbaseDatabase;
import com.dynatrace.easytravel.mongodb.MongoDb;
import com.dynatrace.easytravel.persistence.SqlDatabase;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;
import com.dynatrace.easytravel.utils.TestHelpers;


/**
 *
 * @author stefan.moschinski
 */
public class DatabaseFactoryTest {

	/**
	 * Test method for {@link com.dynatrace.easytravel.launcher.persistence.DatabaseFactory#createDatabase(java.lang.String)}.
	 */
	@Test
	public void testCreateDatabaseReturnsSqlForNull() {
		assertThat(DatabaseFactory.createDatabase(null), is(instanceOf(SqlDatabase.class)));
	}

	@Test
	public void testCreateDatabaseReturnsCorrectDb() {
		assertThat(DatabaseFactory.createDatabase(Persistence.JPA), is(instanceOf(SqlDatabase.class)));
		assertThat(DatabaseFactory.createDatabase(Persistence.CASSANDRA), is(instanceOf(CassandraDatabase.class)));
		assertThat(DatabaseFactory.createDatabase(Persistence.MONGODB), is(instanceOf(MongoDb.class)));
		assertThat(DatabaseFactory.createDatabase(Persistence.HBASE), is(instanceOf(HbaseDatabase.class)));

		try {
			DatabaseFactory.createDatabase("someother");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "persistence mode", "someother", "is unknown");
		}
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(DatabaseFactory.class);
	}
}
