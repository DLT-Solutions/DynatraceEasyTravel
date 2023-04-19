/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: HbaseDatabase.java
 * @date: 23.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase;

import com.dynatrace.easytravel.persistence.AbstractDatabase;
import com.dynatrace.easytravel.persistence.MandatoryPersistencePreparation;
import com.dynatrace.easytravel.persistence.PersistenceStoreNotAvailableException;
import com.dynatrace.easytravel.persistence.controller.BusinessDatabaseController;


/**
 *
 * @author stefan.moschinski
 */
public class HbaseDatabase extends AbstractDatabase {

	/**
	 *
	 * @param name
	 * @author stefan.moschinski
	 */
	public HbaseDatabase() {
		super("HBase");
	}

	@Override
	public BusinessDatabaseController createNewBusinessController() {
		return createNewBusinessController("easyTravelBusiness");
	}

	public BusinessDatabaseController createNewBusinessController(String tableName) {
		HbaseConnection hbaseConnection = null;
		try {
			hbaseConnection = HbaseConnection.openConnection("localhost");
			HbaseDataController hBaseDataController = new HbaseDataController(hbaseConnection, tableName);
			return new HbaseBusinessController(hBaseDataController);
		} catch (PersistenceStoreNotAvailableException e) {
			if (hbaseConnection != null)
				hbaseConnection.closeConnection();
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void setupPersistenceLayer() {
		((MandatoryPersistencePreparation) getBusinessController()).createSchema();
	}
}
