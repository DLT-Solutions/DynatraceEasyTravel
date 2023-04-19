/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: LoginHistoryProvider.java
 * @date: 18.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence.provider;

import java.util.Collection;

import com.dynatrace.easytravel.jpa.business.LoginHistory;
import com.dynatrace.easytravel.jpa.business.User;


/**
 *
 * @author stefan.moschinski
 */
public interface LoginHistoryProvider extends EasyTravelPersistenceProvider<LoginHistory> {

	/**
	 * 
	 * @param name
	 * @author stefan.moschinski
	 * @return
	 */
	int getLoginCountForUser(String userName);

	/**
	 * 
	 * @param id
	 * @author stefan.moschinski
	 */
	void removeLoginHistoryById(Integer id);

	/**
	 * 
	 * @param userToExclude
	 * @return
	 * @author stefan.moschinski
	 */
	int getLoginCountExcludingUser(User userToExclude);


	/**
	 * 
	 * @param userToExclude
	 * @param maxResults
	 * @return
	 * @author stefan.moschinski
	 */
	Collection<Integer> getLoginIdsExcludingUser(User userToExclude, int maxResults);


}
