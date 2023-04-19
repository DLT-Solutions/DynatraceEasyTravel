/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: PersistenceContextProviderImpl.java
 * @date: 18.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence.controller;

/**
 *
 * @author stefan.moschinski
 */
public class DataAccessControllers {

	final BusinessDatabaseController businessController;

	/**
	 *
	 * @param businessController
	 * @author stefan.moschinski
	 */
	DataAccessControllers(BusinessDatabaseController businessController) {
		super();
		this.businessController = businessController;
	}


	/**
	 * @return the businessController
	 */
	protected BusinessDatabaseController getBusinessController() {
		return businessController;
	}
}
