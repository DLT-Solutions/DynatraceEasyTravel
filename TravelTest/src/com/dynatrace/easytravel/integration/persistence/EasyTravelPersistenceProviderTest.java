/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: EasyTravelPersistenceProviderTest.java
 * @date: 04.02.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;

import com.dynatrace.easytravel.jpa.Base;
import com.dynatrace.easytravel.persistence.controller.BusinessDatabaseController;
import com.dynatrace.easytravel.persistence.provider.EasyTravelPersistenceProvider;
import com.google.common.base.Preconditions;


/**
 *
 * @author stefan.moschinski
 */
@Ignore("ABSTRACT TEST CLASS")
public class EasyTravelPersistenceProviderTest<T extends EasyTravelPersistenceProvider<? extends Base>> {

	protected T provider;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		Preconditions.checkNotNull(tempProvider);
		provider = (T) tempProvider;
		provider.reset();
	}


	// we need a temp provider to allow static access in the @BeforeClass annotated methods
	private static EasyTravelPersistenceProvider<? extends Base> tempProvider;
	private static BusinessDatabaseController controller;

	protected static <X extends EasyTravelPersistenceProvider<? extends Base>> void initializeTest(
			BusinessDatabaseController controller, X provider) {
		tempProvider = Preconditions.checkNotNull(provider);
		EasyTravelPersistenceProviderTest.controller = Preconditions.checkNotNull(controller);
	}

	@AfterClass
	public static void tearDownClass() {
		controller.close();
		tempProvider = null;
		controller = null;
	}

}
