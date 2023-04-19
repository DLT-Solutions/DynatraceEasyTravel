/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: DatabaseFactory.java
 * @date: 19.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.launcher.persistence;

import com.dynatrace.easytravel.cassandra.CassandraDatabase;
import com.dynatrace.easytravel.constants.BaseConstants.BusinessBackend;
import com.dynatrace.easytravel.constants.BaseConstants.BusinessBackend.Persistence;
import com.dynatrace.easytravel.hbase.HbaseDatabase;
import com.dynatrace.easytravel.mongodb.MongoDb;
import com.dynatrace.easytravel.persistence.Database;
import com.dynatrace.easytravel.persistence.SqlDatabase;

/**
 *
 * @author stefan.moschinski
 */
public class DatabaseFactory {



	/**
	 * 
	 * @param persistenceMode defines which persistence mode should be used for database
	 * @return {@link Database} according to the passed {@code persistenceMode}
	 */
	public static Database createDatabase(String persistenceMode) {
		if (persistenceMode == null || BusinessBackend.Persistence.JPA.equals(persistenceMode)) {
			return new SqlDatabase();
		}
		if (Persistence.CASSANDRA.equals(persistenceMode)) {
			return new CassandraDatabase();
		}
		if (Persistence.MONGODB.equals(persistenceMode)) {
			return new MongoDb();
		}
		if (Persistence.HBASE.equals(persistenceMode)) {
			return new HbaseDatabase();
		}

		throw new IllegalArgumentException(String.format("The passed persistence mode '%s' is unknown", persistenceMode));
	}

}
