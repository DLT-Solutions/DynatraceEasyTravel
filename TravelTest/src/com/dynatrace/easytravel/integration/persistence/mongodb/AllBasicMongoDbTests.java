/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: AllTests.java
 * @date: 20.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence.mongodb;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;



/**
 *
 * @author stefan.moschinski
 */
@RunWith(Suite.class)
@SuiteClasses({
		// already moved tests
		BookingCollectionTest.class,
		TenantCollectionTest.class,
		JourneyCollectionTest.class,
		LocationCollectionTest.class,
		LoginHistoryCollectionTest.class,
		MarshallingProviderFactoryTest.class,
		UserCollectionTest.class })
public class AllBasicMongoDbTests {

}
