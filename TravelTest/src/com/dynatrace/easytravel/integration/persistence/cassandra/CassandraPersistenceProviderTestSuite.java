/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 * 
 * @file: CassandraPersistenceProviderTestSuite.java
 * @date: 11.02.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence.cassandra;

import java.io.IOException;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dynatrace.easytravel.integration.persistence.cassandra.CassandraTestHelper.SuiteCassandraTestHelper;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;


/**
 * 
 * @author stefan.moschinski
 */
@RunWith(Suite.class)
@SuiteClasses({
		BookingColumnFamilyTest.class,
		CounterColumnFamilyTest.class,
		JourneyColumnFamilyTest.class,
		LoginHistoryColumnFamilyTest.class,
		TenantColumnFamilyTest.class,
		UserColumnFamilyTest.class })
public class CassandraPersistenceProviderTestSuite {

	private static final Logger log = Logger.getLogger(CassandraPersistenceProviderTestSuite.class.getName());

	private static SuiteCassandraTestHelper registerSingleton;

	@BeforeClass
	public static void setUpCassandraNodes() throws IOException, CorruptInstallationException, InterruptedException {
		log.info("Setting up Cassandra nodes for test suite");
		registerSingleton = CassandraTestHelper.registerSingleton();
	}

	@AfterClass
	public static void tearDownCassandraNodes() throws IOException, CorruptInstallationException, InterruptedException {
		registerSingleton.tearDownTestSuite();
	}
}
