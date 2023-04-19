/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: AbstractDatabase.java
 * @date: 18.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence;

import java.io.IOException;

import com.dynatrace.easytravel.CreateDatabaseContent;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.persistence.controller.BusinessDatabaseController;
import com.google.common.base.Preconditions;



/**
 *
 * @author stefan.moschinski
 */
public abstract class AbstractDatabase implements Database {

	private BusinessDatabaseController businessController;

	private final String name;


	protected AbstractDatabase(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}


	@Override
	public final void createContents(EasyTravelConfig config, boolean randomContent) throws IOException {
		setupPersistenceLayer();
		CreateDatabaseContent createDatabaseContent = null;
		try {
			createDatabaseContent = new CreateDatabaseContent(this, randomContent);
			createDatabaseContent.create();
		} finally {
			if (createDatabaseContent != null)
				createDatabaseContent.close();
		}
	}


	/**
	 * Gives the {@link AbstractDatabase} the possibility to configure persistence layer specific settings
	 *
	 * @author stefan.moschinski
	 */
	protected void setupPersistenceLayer() {
	}

	@Override
	public synchronized void closeConnection() {
		if (businessController != null) {
			businessController.close();
		}
	}

	@Override
	public boolean isDerbyDb() {
		// actually wrong place for this method
		return false;
	}
	
	@Override
	public boolean isOracleDb() {
		return false;
	};
	
	@Override
	public boolean isMssqlDb() {
		return false;
	};
	
	@Override
	public boolean isMySqlDb() {
		return false;
	};
	
	@Override
	public synchronized BusinessDatabaseController getBusinessController() {
		if (businessController == null) {
			businessController = createNewBusinessController();
		}
		return businessController;
	}


	@Override
	public Database initialize() {
		return initialize(getBusinessController());
	}

	@Override
	public Database initialize(BusinessDatabaseController businessController) {
		this.businessController = Preconditions.checkNotNull(businessController);
		return this;
	}

	@Override
	public void dropContents() {
		businessController.dropContents();
	}
}
