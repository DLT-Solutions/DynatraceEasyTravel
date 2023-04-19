/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: CassandraCreatableContent.java
 * @date: 15.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence;


/**
 * You may implement this interface if your database needs special
 * preconfiguration before the actual database content creation
 * can take place.
 * 
 * @author stefan.moschinski
 */
public interface MandatoryPersistencePreparation {

	/**
	 * Creates the needed the schemas (e.g., for column families)
	 * 
	 * @author stefan.moschinski
	 */
	void createSchema();

}
