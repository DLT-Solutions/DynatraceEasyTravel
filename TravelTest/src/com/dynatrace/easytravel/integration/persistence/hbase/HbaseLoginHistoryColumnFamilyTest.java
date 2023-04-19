/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: LoginHistoryCollectionTest.java
 * @date: 10.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence.hbase;

import org.junit.BeforeClass;

import com.dynatrace.easytravel.hbase.HbaseDatabase;
import com.dynatrace.easytravel.integration.persistence.LoginHistoryProviderTest;
import com.dynatrace.easytravel.persistence.controller.BusinessDatabaseController;


/**
 *
 * @author stefan.moschinski
 */
public class HbaseLoginHistoryColumnFamilyTest extends LoginHistoryProviderTest {

	@BeforeClass
	public static void setUpClass() {
		HbaseDatabase database = new HbaseDatabase();
		BusinessDatabaseController controller = database.createNewBusinessController("testXy");
//		sometimes the counter return a wrong value, if this is the case, uncomment 
//		controller.dropContents();
//		controller = database.createNewBusinessController("testXy");
		initializeTest(controller, controller.getLoginHistoryProvider());
	}



}
