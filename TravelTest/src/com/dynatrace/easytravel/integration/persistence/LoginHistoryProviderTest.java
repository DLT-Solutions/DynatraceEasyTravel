/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: LoginHistoryProviderTest.java
 * @date: 04.02.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.jpa.business.LoginHistory;
import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.persistence.provider.LoginHistoryProvider;


/**
 *
 * @author stefan.moschinski
 */
@Ignore("ABSTRACT-CLASS")
public abstract class LoginHistoryProviderTest extends EasyTravelPersistenceProviderTest<LoginHistoryProvider> {

	@Test
	public void testGetUserLoginCount() {
		User user1 = new User("user1", "name1", "email1", "pw1");
		User user2 = new User("user2", "name2", "email2", "pw2");

		LoginHistory lh1 = new LoginHistory(user1, new Date());
		lh1.setId(1);
		LoginHistory lh2 = new LoginHistory(user1, DateUtils.addDays(new Date(), 1));
		lh2.setId(2);
		LoginHistory lh3 = new LoginHistory(user2, DateUtils.addDays(new Date(), 1));
		lh3.setId(3);


		provider.add(lh1);
		provider.add(lh2);
		provider.add(lh3);

		assertThat(provider.getLoginCountForUser("user1"), is(2));
		assertThat(provider.getLoginCountForUser("user2"), is(1));
		assertThat(provider.getLoginCountForUser("not-existing"), is(0));

		provider.removeLoginHistoryById(1);
		assertThat(provider.getLoginCountForUser("user1"), is(1));
		provider.removeLoginHistoryById(2);
		assertThat(provider.getLoginCountForUser("user1"), is(0));

		provider.removeLoginHistoryById(3);
		assertThat(provider.getLoginCountForUser("user2"), is(0));
	}

	@Test
	public void testRemoveLoginHistoryById() {
		User user1 = new User("user1", "name1", "email1", "pw1");
		User user2 = new User("user2", "name2", "email2", "pw2");

		LoginHistory login1 = new LoginHistory(user1, new Date());
		provider.add(login1);
		LoginHistory login2 = new LoginHistory(user1, DateUtils.addDays(new Date(), 1));
		provider.add(login2);
		LoginHistory login3 = new LoginHistory(user2, DateUtils.addDays(new Date(), 1));
		provider.add(login3);

		assertThat(provider.getAll().size(), is(3));
		assertThat(provider.getAll(), containsInAnyOrder(login1, login2, login3));

		provider.removeLoginHistoryById(login1.getId());
		assertThat(provider.getAll().size(), is(2));
		assertThat(provider.getAll(), containsInAnyOrder(login2, login3));

		provider.removeLoginHistoryById(login2.getId());
		assertThat(provider.getAll().size(), is(1));
		assertThat(provider.getAll(), containsInAnyOrder(login3));

		provider.removeLoginHistoryById(login3.getId());
		assertThat(provider.getAll().size(), is(0));
		assertThat(provider.getAll(), is(empty()));

	}

	@Test
	public void testGetCountExcludingUser() {
		User user1 = new User("user1", "name1", "email1", "pw1");
		User user2 = new User("user2", "name2", "email2", "pw2");
		User user3 = new User("user3", "name3", "email3", "pw3");
		User notAdded = new User("user4", "name4", "email4", "pw4");

		provider.add(new LoginHistory(user1, new Date()));
		provider.add(new LoginHistory(user2, new Date()));
		provider.add(new LoginHistory(user3, new Date()));

		assertThat(provider.getLoginCountExcludingUser(user1), is(2));
		assertThat(provider.getLoginCountExcludingUser(user2), is(2));
		assertThat(provider.getLoginCountExcludingUser(user3), is(2));
		assertThat(provider.getLoginCountExcludingUser(notAdded), is(provider.getCount()));
	}

	@Test
	public void testGetLoginIdsExcludingUser() {
		User user1 = new User("user1", "name1", "email1", "pw1");
		User user2 = new User("user2", "name2", "email2", "pw2");
		User user3 = new User("user3", "name3", "email3", "pw3");

		LoginHistory loginUser1 = new LoginHistory(user1, new Date());
		LoginHistory loginUser11 = new LoginHistory(user1, DateUtils.addDays(new Date(), 1));
		LoginHistory loginUser2 = new LoginHistory(user2, new Date());
		LoginHistory loginUser3 = new LoginHistory(user3, new Date());

		provider.add(loginUser1);
		provider.add(loginUser11);
		provider.add(loginUser2);
		provider.add(loginUser3);

		assertThat(provider.getLoginIdsExcludingUser(user1, 100), containsInAnyOrder(loginUser2.getId(), loginUser3.getId()));
		assertThat(provider.getLoginIdsExcludingUser(user2, 100),
				containsInAnyOrder(loginUser1.getId(), loginUser11.getId(), loginUser3.getId()));
	}

}
