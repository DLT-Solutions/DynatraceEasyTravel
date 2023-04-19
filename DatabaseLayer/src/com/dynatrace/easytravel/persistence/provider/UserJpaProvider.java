/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: UserJpaProvider.java
 * @date: 18.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence.provider;

import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.persistence.JpaDatabaseController;


/**
 *
 * @author stefan.moschinski
 */
public class UserJpaProvider extends JpaProvider<User> implements UserProvider {

	/**
	 * 
	 * @param controller
	 * @param cls
	 * @author stefan.moschinski
	 */
	public UserJpaProvider(JpaDatabaseController controller) {
		super(controller, User.class);
	}

	@Override
	public User getUserByName(String name) {
		return find(name);
	}

}
