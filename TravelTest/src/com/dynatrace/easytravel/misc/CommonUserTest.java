package com.dynatrace.easytravel.misc;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;



public class CommonUserTest {
	@Test
	public void test() {
		CommonUser user = new CommonUser("user", "pwd");
		assertEquals("user", user.getName());
		user.setName("myuser");
		assertEquals("myuser", user.getName());

		assertEquals("pwd", user.getPassword());
		user.setPassword("mypwd");
		assertEquals("mypwd", user.getPassword());

		assertNull(user.getFullName());
		user.setFullName("myuser is this");
		assertEquals("myuser is this", user.getFullName());

		assertNull(user.getLoyaltyStatus());
		user.setLoyaltyStatus(LoyaltyStatus.GOLD);
		assertEquals("Gold", user.getLoyaltyStatus());
	}

	@Test
	public void testReadUsers() {
		List<CommonUser> users = CommonUser.getUsers();
		assertTrue("Had: " + users.size(), users.size() >= 100);

		/* Tried to test exception-case, but could not find a way...
		ClassLoader classLoader = CommonUser.class.getClassLoader();
		assertNull("Had: " + classLoader, classLoader);

		users = CommonUser.getUsers();
		assertEquals("Had: " + users.size(), 0, users.size());
		*/
	}
}
