/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: PersistenceStoreNotAvailableException.java
 * @date: 06.02.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence;


/**
 *
 * @author stefan.moschinski
 */
public class PersistenceStoreNotAvailableException extends Exception {

	private static final long serialVersionUID = 1L;

	public PersistenceStoreNotAvailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
