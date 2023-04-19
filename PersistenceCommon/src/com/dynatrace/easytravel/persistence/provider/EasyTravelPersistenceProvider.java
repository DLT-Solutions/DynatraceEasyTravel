/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: PersitenceProvider.java
 * @date: 14.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence.provider;

import java.util.Collection;

import com.dynatrace.easytravel.jpa.Base;

/**
 * Classes implementing this interface provide access to a special domain
 * of a data store.
 * 
 * @author stefan.moschinski
 */
public interface EasyTravelPersistenceProvider<T extends Base> {



	/**
	 * Adds a value to the persistence provider
	 * Updates existing value if a matching value (determined by its 'primary' key)
	 * does exist.
	 * 
	 * @param value value that should be added
	 * @return added value
	 * @author stefan.moschinski
	 */
	T add(T value);
	
	/**
	 * Updates entry matching the key of the given 'new' value
	 * @param value
	 * @return
	 * @author stefan.moschinski
	 */
	T update(T value);

	/**
	 * 
	 * @return all entities stored within the persistence provider
	 * @author stefan.moschinski
	 */
	Collection<T> getAll();

	/**
	 * 
	 * @param limit
	 * @return
	 * @author stefan.moschinski
	 */
	Collection<T> getWithLimit(int limit);

	int getCount();

	/**
	 * Gives you the possibility to clear all contents of the provider.
	 * Implementors have to guarantee that subsequent operations to the provider
	 * are possible without further setup.
	 * @author stefan.moschinski
	 */
	void reset();

}
