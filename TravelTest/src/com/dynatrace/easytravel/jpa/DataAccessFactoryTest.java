package com.dynatrace.easytravel.jpa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import com.dynatrace.easytravel.database.DatabaseBase;
import com.dynatrace.easytravel.persistence.DataAccessFactory;
import com.dynatrace.easytravel.persistence.Database;
import com.dynatrace.easytravel.persistence.SqlDatabase;


public class DataAccessFactoryTest extends DatabaseBase {

	@Test
	public void test() throws InterruptedException {
		Database sqlDatabase = new SqlDatabase().initialize();
		DataAccessFactory dataAccessFactory = new DataAccessFactory();

		assertThat(dataAccessFactory.newInstance(sqlDatabase), is(not(nullValue())));

		sqlDatabase.closeConnection();
	}

}
