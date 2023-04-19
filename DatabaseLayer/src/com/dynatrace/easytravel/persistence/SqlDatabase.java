/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: JpaDatabase.java
 * @date: 19.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence;

import javax.persistence.EntityManagerFactory;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.persistence.controller.BusinessDatabaseController;


/**
 *
 * @author stefan.moschinski
 */
public class SqlDatabase extends AbstractDatabase {

	protected EntityManagerFactory businessEmFactory;

	private final String password;
	private final String user;
	private final String url;
	private final String driver;

	public SqlDatabase() {
		super("SQL");

		EasyTravelConfig config = EasyTravelConfig.read();
		driver = config.databaseDriver;
		url = config.databaseUrl;
		user = config.databaseUser;
		password = config.databasePassword;
	}

	@Override
	public boolean isDerbyDb() {
		return EasyTravelConfig.isDerbyDatabase();
	}
	
	@Override
	public boolean isOracleDb() {
		return EasyTravelConfig.isOracleDatabase();
	};
	
	@Override
	public boolean isMssqlDb() {
		return EasyTravelConfig.isMssqlDatabase();
	};

	@Override
	public boolean isMySqlDb() {
		return EasyTravelConfig.isMySqlDatabase();
	};

	@Override
	public synchronized BusinessDatabaseController createNewBusinessController() {
		EntityManagerFactory emFactory = getBusinessEntityManagerFactory();
		JpaDatabaseController internalController = new JpaDatabaseController(emFactory.createEntityManager());
		return new JpaBusinessController(internalController);
	}

	protected EntityManagerFactory getBusinessEntityManagerFactory() {
		if (businessEmFactory == null) {
			businessEmFactory = JpaDatabaseControllerFactory.createEntityManagerFactory("easyTravel-Business", driver, url, user,
					password);
		}
		return businessEmFactory;
	}

	@Override
	public synchronized void closeConnection() {
		closeEmFactory(businessEmFactory);

		super.closeConnection();
	}

	private void closeEmFactory(EntityManagerFactory entityManagerFactory) {
		if (entityManagerFactory != null) {
			if (entityManagerFactory.isOpen()) {
				entityManagerFactory.close();
			}
			entityManagerFactory = null;
		}
	}


}
