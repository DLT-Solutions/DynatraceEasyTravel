package com.dynatrace.easytravel.cassandra;

import java.util.Collection;

import com.dynatrace.easytravel.cassandra.tables.objectmapper.UserTable;
import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.persistence.provider.UserProvider;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class CassandraUserProvider implements UserProvider {
	
	private final UserTable userTable;
	
	public CassandraUserProvider(UserTable userTable) {
		this.userTable = userTable;
	}

	@Override
	public User add(User value) {
		userTable.addModel(value);
		return value;
	}

	@Override
	public User update(User value) {
		userTable.updateModel(value);
		return value;
	}

	@Override
	public Collection<User> getAll() {
		return userTable.getAllModels();
	}

	@Override
	public Collection<User> getWithLimit(int limit) {
		return userTable.getModelsWithLimit(limit);
	}

	@Override
	public int getCount() {
		return userTable.getCount();
	}

	@Override
	public void reset() {
		userTable.reset();
		
	}

	@Override
	public User getUserByName(String name) {
		return userTable.getUserByName(name);
	}

}
