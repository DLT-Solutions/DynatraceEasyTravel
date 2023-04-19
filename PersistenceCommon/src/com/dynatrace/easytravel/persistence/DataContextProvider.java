/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: BusinessDataContextProvider.java
 * @date: 21.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence;

import java.util.concurrent.TimeUnit;


/**
 *
 * @author stefan.moschinski
 */
public interface DataContextProvider {


	/**
	 *
	 * @param timeout
	 * @param unit
	 * @return
	 * @author stefan.moschinski
	 */
	Database getInitializedDatabase(long timeout, TimeUnit unit);

}
