package com.dynatrace.easytravel.cassandra;

import java.util.Collection;

import com.dynatrace.easytravel.cassandra.tables.objectmapper.LoginHistoryTable;
import com.dynatrace.easytravel.jpa.business.LoginHistory;
import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.persistence.provider.LoginHistoryProvider;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class CassandraLoginHistoryProvider implements LoginHistoryProvider {
	
	private final LoginHistoryTable loginHistoryTable;
	
	public CassandraLoginHistoryProvider(LoginHistoryTable loginHistoryTable) {
		this.loginHistoryTable = loginHistoryTable;
	}

	@Override
	public LoginHistory add(LoginHistory value) {
		loginHistoryTable.addModel(value);
		return value;
	}

	@Override
	public LoginHistory update(LoginHistory value) {
		loginHistoryTable.updateModel(value);
		return value;
	}

	@Override
	public Collection<LoginHistory> getAll() {
		return loginHistoryTable.getAllModels();
	}

	@Override
	public Collection<LoginHistory> getWithLimit(int limit) {
		return loginHistoryTable.getModelsWithLimit(limit);
	}

	@Override
	public int getCount() {
		return loginHistoryTable.getCount();
	}

	@Override
	public void reset() {
		loginHistoryTable.reset();
	}

	@Override
	public int getLoginCountForUser(String userName) {
		return loginHistoryTable.getLoginCountForUser(userName);
	}

	@Override
	public void removeLoginHistoryById(Integer id) {
		loginHistoryTable.removeLoginHistoryById(id);
	}

	@Override
	public int getLoginCountExcludingUser(User userToExclude) {
		return loginHistoryTable.getLoginCountExcludingUser(userToExclude);
	}

	@Override
	public Collection<Integer> getLoginIdsExcludingUser(User userToExclude, int maxResults) {
		return loginHistoryTable.getLoginIdsExcludingUser(userToExclude, maxResults);
	}

}
