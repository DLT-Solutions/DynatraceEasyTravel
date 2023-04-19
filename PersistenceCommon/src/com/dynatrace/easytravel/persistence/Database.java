/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: PersistenceMode.java
 * @date: 18.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence;

import java.io.IOException;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.persistence.controller.BusinessDatabaseController;



/**
 *
 * @author stefan.moschinski
 */
public interface Database {



	/**
	 * Using this method we can initialize lazily the connection to the real database
	 *
	 * @author stefan.moschinski
	 * @return
	 */
	@Deprecated
	Database initialize();

	/**
	 *
	 * @author stefan.moschinski
	 */
	void closeConnection();

	/**
	 * Call {@code #initialize()} before!
	 *
	 * @param config
	 * @param createDbConf
	 * @throws IOException
	 * @author stefan.moschinski
	 */
	void createContents(EasyTravelConfig config, boolean randomContent) throws IOException;

	/**
	 *
	 * @return
	 * @author stefan.moschinski
	 */
	BusinessDatabaseController getBusinessController();

	/**
	 * actually not optimal, but good for now
	 *
	 * @return
	 * @author stefan.moschinski
	 */
	boolean isDerbyDb();
	
	/**
	 * @return
	 * @author cwpl-rpsciuk
	 */
	boolean isOracleDb();

	/**
	 * @return
	 * @author cwpl-kkulikow
	 */
	boolean isMssqlDb();
	
	/**
	 * @return
	 * @author cwpl-wjarosz
	 */
	boolean isMySqlDb();
	
	/**
	 *
	 * @return a <b>new</b> {@link BusinessDatabaseController} instance
	 * @author stefan.moschinski
	 */
	BusinessDatabaseController createNewBusinessController();

	/**
	 * Method that gives you the possibility to inject the used {@link BusinessDatabaseController},
	 * especially useful for testing
	 *
	 * @param businessController
	 * @return initialized database
	 */
	Database initialize(BusinessDatabaseController businessController);

	/**
	 *
	 * @author stefan.moschinski
	 */
	void dropContents();
}
