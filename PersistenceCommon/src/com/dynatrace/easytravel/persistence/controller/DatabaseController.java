/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: DatabasesController.java
 * @date: 19.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence.controller;




/**
 *
 * @author stefan.moschinski
 */
public interface DatabaseController {

	/**
	 * Starts a new transaction
	 * Attention: implementor <b>may not implement</b> this method
	 * 
	 * @author stefan.moschinski
	 */
	public abstract void startTransaction();

	/**
	 * Commits the currently active transtaction
	 * Attention: implementor <b>may not implement</b> this method
	 * 
	 * @author stefan.moschinski
	 */
	public abstract void commitTransaction();

	/**
	 * 
	 * Attention: implementor <b>may not implement</b> this method
	 * @author stefan.moschinski
	 */
	void flushAndClear();

	/**
	 * 
	 * Attention: implementor <b>may not implement</b> this method
	 * @author stefan.moschinski
	 */
	void flush();

	/**
	 * Closes the underlying connection to the persistence store.
	 * Subsequent calls to the persistence store will fail!
	 * @author stefan.moschinski
	 */
	void close();

	/**
	 * Performs a rollback of the current transaction
	 * Attention: implementor <b>may not implement</b> this method
	 * 
	 * @author stefan.moschinski
	 */
	void rollbackTransaction();

	/**
	 * Drops all contents of the providers contained within the controller
	 * 
	 * @author stefan.moschinski
	 */
	void dropContents();

}
