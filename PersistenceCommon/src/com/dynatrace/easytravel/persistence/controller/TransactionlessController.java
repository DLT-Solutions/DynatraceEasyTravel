/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: TransactionlessController.java
 * @date: 07.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence.controller;

import com.dynatrace.easytravel.persistence.Database;


/**
 * {@link Database}s that does not support transactions or that usage in easyTravel does not make
 * use of transactions should inherit from this class.
 * 
 * @author stefan.moschinski
 */
public abstract class TransactionlessController implements DatabaseController {


	@Override
	public final void startTransaction() {
	}

	@Override
	public final void commitTransaction() {
	}

	@Override
	public final void flushAndClear() {
	}

	@Override
	public final void flush() {
	}

	@Override
	public final void rollbackTransaction() {
	}


}
