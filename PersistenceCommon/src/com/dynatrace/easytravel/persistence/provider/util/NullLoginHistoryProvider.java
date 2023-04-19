/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: NullLoginHistoryProvider.java
 * @date: 16.01.2013
 * @author: stmo
 */
package com.dynatrace.easytravel.persistence.provider.util;

import java.util.Collection;
import java.util.Collections;

import com.dynatrace.easytravel.jpa.business.LoginHistory;
import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.persistence.provider.LoginHistoryProvider;


/**
 *
 * @author stmo
 */
public class NullLoginHistoryProvider implements LoginHistoryProvider {

	@Override
	public LoginHistory add(LoginHistory value) {
		return value;
	}

	@Override
	public LoginHistory update(LoginHistory value) {
		return value;
	}

	@Override
	public Collection<LoginHistory> getAll() {
		return Collections.emptyList();
	}

	@Override
	public Collection<LoginHistory> getWithLimit(int limit) {
		return Collections.emptyList();
	}

	@Override
	public int getCount() {
		return 0;
	}

	@Override
	public int getLoginCountForUser(String userName) {
		return 0;
	}

	@Override
	public void removeLoginHistoryById(Integer id) {
	}

	@Override
	public int getLoginCountExcludingUser(User userToExclude) {
		return 0;
	}

	@Override
	public Collection<Integer> getLoginIdsExcludingUser(User userToExclude, int maxResults) {
		return Collections.emptyList();
	}

	@Override
	public void reset() {
	}

}
