/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: AllTests.java
 * @date: 29.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence.hbase;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


/**
 *
 * @author stefan.moschinski
 */
@RunWith(Suite.class)
@SuiteClasses({
		HbaseBookingColumnFamilyTest.class,
		HbaseJourneyColumnFamilyTest.class,
		HbaseLocationColumnFamilyTest.class,
		HbaseTenantColumnFamilyTest.class,
		HbaseUserColumnFamilyTest.class,
		HbaseLoginHistoryColumnFamilyTest.class })
public class AllHbaseColumnFamilyTests {

}
