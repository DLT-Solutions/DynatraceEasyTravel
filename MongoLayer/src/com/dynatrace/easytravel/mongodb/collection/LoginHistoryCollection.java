/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: LoginHistoryCollection.java
 * @date: 20.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb.collection;

import static com.dynatrace.easytravel.jpa.business.LoginHistory.LOGIN_HISTORY_USER;
import static com.dynatrace.easytravel.mongodb.SimpleQueryBuilder.buildQuery;

import java.util.Collection;
import java.util.List;

import com.dynatrace.easytravel.jpa.business.LoginHistory;
import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.mongodb.MongoConstants;
import com.dynatrace.easytravel.persistence.provider.LoginHistoryProvider;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryOperators;

/**
 *
 * @author stefan.moschinski
 */
public class LoginHistoryCollection extends MongoDbCollection<LoginHistory> implements
		LoginHistoryProvider {


	private static final String LOGIN_HISTORY_COLLECTION_NAME = "LoginHistoryCollection";

	public LoginHistoryCollection(DB database) {
		super(database, LOGIN_HISTORY_COLLECTION_NAME);
	}

	@Override
	public LoginHistory add(LoginHistory login) {
		if (login.getId() == 0) {
			login.setId(login.hashCode());
		}
		return super.add(login);
	}


	@Override
	public int getLoginCountForUser(String name) {
		return find(buildQuery(LOGIN_HISTORY_USER, MongoConstants.ID).value(name).create()).size();
	}


	@Override
	public void removeLoginHistoryById(Integer id) {
		delete(buildQuery(MongoConstants.ID).value(id).create());
	}

	@Override
	public int getLoginCountExcludingUser(User userToExclude) {
		return getSelectiveCount(createUserToExcludeQuery(userToExclude));
	}



	private DBObject createUserToExcludeQuery(User userToExclude) {
		return buildQuery(LOGIN_HISTORY_USER, MongoConstants.ID).value(
				buildQuery(QueryOperators.NE).value(userToExclude.getName()).create()).create();
	}

	@Override
	public Collection<Integer> getLoginIdsExcludingUser(User userToExclude, int maxResults) {
		DBCursor find = getCollection().find(createUserToExcludeQuery(userToExclude),
				buildQuery(MongoConstants.ID).value(1).create()).limit(
				maxResults); // we only need the id field
		List<Integer> ids = Lists.newArrayListWithCapacity(find.size());
		while (find.hasNext()) {
			BasicDBObject next = (BasicDBObject) find.next();
			ids.add(next.getInt(MongoConstants.ID));
		}
		return ids;
	}

}
