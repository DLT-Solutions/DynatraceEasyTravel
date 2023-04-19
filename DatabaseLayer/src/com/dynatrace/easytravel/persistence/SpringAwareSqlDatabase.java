/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: PluginAwareSqlDatabase.java
 * @date: 16.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence;


import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author stefan.moschinski
 */
public class SpringAwareSqlDatabase extends SqlDatabase {


	@Autowired
	private JpaDatabaseController jpaController;

	@Autowired
	private EntityManagerFactory businessEmFactory;


	@Override
	public synchronized JpaBusinessController getBusinessController() {
		return new JpaBusinessController(jpaController);
	}

	@Override
	protected EntityManagerFactory getBusinessEntityManagerFactory() {
		return businessEmFactory;
	}
	


}
