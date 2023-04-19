/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: UserProviderTest.java
 * @date: 04.02.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.persistence.provider.UserProvider;
import com.google.common.collect.Lists;


/**
 *
 * @author stefan.moschinski
 */
public abstract class UserProviderTest extends EasyTravelPersistenceProviderTest<UserProvider> {

	@Test
	public void testGetUserByName() throws Exception {
		User user = new User("name1", "fullName1", "email1", "pw1");
		provider.add(user);

		User user2 = new User("name2", "fullName2", "email2", "pw1");
		provider.add(user2);

		assertThat(provider.getCount(), is(2));
		assertThat(provider.getUserByName("name1"), is(user));
		assertThat(provider.getUserByName("name2"), is(user2));
	}

	@Test
	public void testGetAll() throws Exception {
		int userNo = 101;

		List<User> expectedUsers = Lists.newArrayList();

		for (int i = 0; i < userNo; i++) {
			User user = new User("name" + i, "fullName" + i, "email" + i, "pw" + i);
			provider.add(user);
			expectedUsers.add(user);

		}

		Collection<User> all = provider.getAll();
		assertThat(all.size(), is(userNo));
		assertThat(all, containsInAnyOrder(expectedUsers.toArray(new User[all.size()])));
	}


	@Test
	public void testAddDuplicate() throws Exception {
		User user = new User("name1", "fullName1", "email1", "pw1");
		provider.add(user);

		User user2 = new User("name2", "fullName2", "email1", "pw1");
		provider.add(user2);

		User newUser = new User("name1", "NewFullName", "email1", "pw1");
		provider.add(newUser);

		assertThat(provider.getUserByName("name1").getFullName(), is(newUser.getFullName()));
		assertThat(provider.getUserByName("name1"), is(not(user2)));
		assertThat(provider.getUserByName("name2").getFullName(), is(user2.getFullName()));

	}

	@Test
	public void testUpdate() throws Exception {
		User user = new User("name1", "fullName1", "email1", "pw1");
		provider.add(user);

		User user2 = new User("name2", "fullName2", "email1", "pw1");
		provider.add(user2);

		User newUser = new User("name1", "NewFullName", "email1", "pw1");
		provider.update(newUser);

		assertThat(provider.getUserByName("name1").getFullName(), is(newUser.getFullName()));
		assertThat(provider.getUserByName("name2").getFullName(), is(user2.getFullName()));

	}
}
