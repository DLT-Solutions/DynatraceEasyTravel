/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: UserCollectionTest.java
 * @date: 13.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence.hbase;

import org.junit.BeforeClass;

import com.dynatrace.easytravel.hbase.HbaseDatabase;
import com.dynatrace.easytravel.integration.persistence.JourneyProviderTest;
import com.dynatrace.easytravel.persistence.controller.BusinessDatabaseController;


/**
 *
 * @author stefan.moschinski
 */
public class HbaseJourneyColumnFamilyTest extends JourneyProviderTest {

	@BeforeClass
	public static void setUpClass() {
		HbaseDatabase database = new HbaseDatabase();
		BusinessDatabaseController controller = database.createNewBusinessController("testXy");
		initializeTest(controller, controller.getJourneyProvider());
	}

}
