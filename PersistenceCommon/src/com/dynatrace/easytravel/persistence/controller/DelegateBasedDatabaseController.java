/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: AbstractDatabaseController.java
 * @date: 10.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence.controller;



/**
 *
 * @author stefan.moschinski
 */
abstract class DelegateBasedDatabaseController implements DatabaseController {

	protected final DatabaseController delegateController;

	/**
	 * 
	 * @param controller
	 * @author stefan.moschinski
	 */
	protected DelegateBasedDatabaseController(DatabaseController controller) {
		this.delegateController = controller;
	}

	@Override
	public void startTransaction() {
		delegateController.startTransaction();
	}

	@Override
	public void commitTransaction() {
		delegateController.commitTransaction();
	}

	@Override
	public void flushAndClear() {
		delegateController.flushAndClear();
	}

	@Override
	public void flush() {
		delegateController.flush();
	}

	@Override
	public void close() {
		delegateController.close();
	}

	@Override
	public void rollbackTransaction() {
		delegateController.rollbackTransaction();
	}
	
	
	public DatabaseController getDelegateController() {
		return delegateController;
	}

	@Override
	public void dropContents() {
		delegateController.dropContents();
	}



}
