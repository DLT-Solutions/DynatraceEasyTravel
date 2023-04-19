/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: DataAccessRegistry.java
 * @date: 09.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence;

import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.model.GenericDataAccess;


/**
 *
 * @author stefan.moschinski
 */
public class DataAccessFactory {


	public DataAccess newInstance(Database database) {
		return new GenericDataAccess(database.getBusinessController());
	}

}
