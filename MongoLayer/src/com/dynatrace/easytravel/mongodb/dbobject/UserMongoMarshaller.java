/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: UserMongo.java
 * @date: 13.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb.dbobject;

import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.mongodb.MongoConstants;


/**
 *
 * @author stefan.moschinski
 */
public class UserMongoMarshaller extends MongoObjectMarshaller<User> {

	private static final long serialVersionUID = 1L;

	@Override
	protected UserMongoMarshaller marshalTypeSpecific(User user, boolean withId) {
		if (withId) {
			put(MongoConstants.ID, user.getName());
		}
		put("fullName", user.getFullName());
		put("email", user.getFullName());
		put("password", user.getPassword());
		put("loyaltyStatus", user.getLoyaltyStatus());
		return this;
	}

	@Override
	protected User unmarshalTypeSpecific() {
		String name = getString(MongoConstants.ID);
		String fullName = getString("fullName");
		String email = getString("email");
		String password = getString("password");

		User user = new User(name, fullName, email, password);

		user.setLoyaltyStatus(getString("loyaltyStatus"));

		return user;
	}

	@Override
	MongoObjectMarshaller<User> newInstance() {
		return new UserMongoMarshaller();
	}
}
