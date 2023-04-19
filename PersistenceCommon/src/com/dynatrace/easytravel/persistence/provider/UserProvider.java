/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: UserProvider.java
 * @date: 14.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence.provider;

import com.dynatrace.easytravel.jpa.business.User;


/**
 *
 * @author stefan.moschinski
 */
public interface UserProvider extends EasyTravelPersistenceProvider<User> {

	/**
	 * 
	 * @param name
	 * @return
	 * @author stefan.moschinski
	 */
	User getUserByName(String name);

}
