/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: JpaDatabaseControllerFactory.java
 * @date: 09.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.dynatrace.easytravel.jpa.JpaAccessUtils;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.google.common.base.Preconditions;

import ch.qos.logback.classic.Logger;


/**
 *
 * @author stefan.moschinski
 */
public class JpaDatabaseControllerFactory {

	private static final Logger log = LoggerFactory.make();

	/**
	 * Create a new instance of the DataAccess class for accessing the
	 * JPA tables.
	 *
	 * @return A newly create DataAccess object
	 * @throws IllegalStateException If initializing failed.
	 */
	public static synchronized JpaDatabaseController getNewInstanceBusiness(String driver, String url, String user,
			String password) {
		log.info("Creating DataAccessLayer for Business model, driver: " + driver + " URL: " + url + " User: " + user);
		return createController("easyTravel-Business", driver, url, user, password);
	}


	private static JpaDatabaseController createController(String name, String driver, String url, String user, String password) {
		// take System properties and put our url/user/pwd in place unless they are already defined on the commandline
		// i.e. commandline takes precedence
		JpaAccessUtils.setProperties(driver, url, user, password);

		EntityManagerFactory factory = Persistence.createEntityManagerFactory(
				name, System.getProperties());
		Preconditions.checkNotNull(factory, "Could not initialize EntityManagerFactory.");

		EntityManager em = factory.createEntityManager();
		Preconditions.checkNotNull(em, "Could not initialize EntityManager.");

		return new JpaDatabaseController(em);
	}

	public static EntityManagerFactory createEntityManagerFactory(String name, String driver, String url, String user,
			String password) {
		// take System properties and put our url/user/pwd in place unless they are already defined on the commandline
		// i.e. commandline takes precedence
		JpaAccessUtils.setProperties(driver, url, user, password);

		return Persistence.createEntityManagerFactory(name, System.getProperties());
	}



}
