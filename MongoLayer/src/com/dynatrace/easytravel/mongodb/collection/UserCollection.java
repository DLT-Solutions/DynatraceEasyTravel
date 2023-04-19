/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: UserCollection.java
 * @date: 13.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb.collection;

import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.mongodb.MongoConstants;
import com.dynatrace.easytravel.persistence.provider.UserProvider;
import com.mongodb.DB;
/**
 *
 * @author stefan.moschinski
 */
public class UserCollection extends MongoDbCollection<User> implements UserProvider {

	private static final String USER_COLLECTION_NAME = "UserCollection";

	public UserCollection(DB database) {
		super(database, USER_COLLECTION_NAME);
	}

	@Override
	public User getUserByName(String userName) {
		return findOneByKeyValue(MongoConstants.ID, userName);
	}

}
