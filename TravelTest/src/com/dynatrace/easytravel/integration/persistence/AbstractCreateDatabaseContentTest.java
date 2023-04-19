/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: CreateDatabaseContent.java
 * @date: 11.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.persistence.DatabaseFactory;
import com.dynatrace.easytravel.model.GenericDataAccess;
import com.dynatrace.easytravel.persistence.Database;


/**
 *
 * @author stefan.moschinski
 */
@Ignore("Abstract Class")
public abstract class AbstractCreateDatabaseContentTest {

	private static final Logger log = Logger.getLogger(AbstractCreateDatabaseContentTest.class.getName());


	@Before
	public void setUp() {
		Database database = DatabaseFactory.createDatabase(getPersistenceMode());
		database.initialize(database.getBusinessController());
		database.dropContents();
	}


	@Test
	public void testName() throws Exception {
		Database database = DatabaseFactory.createDatabase(getPersistenceMode());
		try {
			database.initialize(database.getBusinessController());
			database.createContents(EasyTravelConfig.read(), true);

			GenericDataAccess access = new GenericDataAccess(database.getBusinessController());

			assertThat(access.getTenant("Speed Travel Agency"), is(not(nullValue())));

			assertThat(access.allUsers().size(), is(greaterThan(100)));
			assertThat(access.getUser("monica"), is(not(nullValue())));
			assertThat(access.getUser("monica").getName(), is("monica"));

			assertThat("Having: " + access.allUsers(),
					access.getUser("armelle"), is(not(nullValue())));
			assertThat("Having: " + access.allUsers(),
					access.getUser("armelle").getName(), is("armelle"));

			access.findJourneys("New York", new Date(), new Date(), false);

			// verify that both versions of some cities are stored
			assertNotNull("Having: " + access.allLocations(),
					access.getLocation("Gdansk"));
			assertNotNull("Having: " + access.allLocations(),
					access.getLocation("Gdañsk"));
		} catch (Exception e) {
			log.log(Level.SEVERE, "COULD NOT CREATE DATABASE CONTENT", e);
			fail(e.toString());
		} finally {
			database.closeConnection();
		}
	}

//	@Test
//	public void testCreationViaProcedure() throws Exception {
//		DatabaseContentCreationProcedure procedure = StartProcedure.newCreateDatabaseContentProcedure(getPersistenceMode());
//		procedure.run();
//	}

	protected abstract String getPersistenceMode();
}
