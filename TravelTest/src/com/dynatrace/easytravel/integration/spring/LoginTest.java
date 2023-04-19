package com.dynatrace.easytravel.integration.spring;

import static org.junit.Assert.*;

import org.junit.Test;

import com.dynatrace.easytravel.frontend.login.LoginLogic;
import com.dynatrace.easytravel.frontend.login.UserContext;
import com.dynatrace.easytravel.spring.SpringTestBase;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;
import com.dynatrace.easytravel.utils.TestHelpers;

public class LoginTest extends SpringTestBase {
	@Test
	public void testLogin1() {
	    login("maria", "maria", true);
	}

	@Test
    public void testLogin2() {
        login("monica", "monica", true);
    }

	@Test
    public void testLoginBlacklisted() {
        login("", "", false);
    }

	private void login(String userName, String password, boolean shouldAuth) {
	    UserContext context = new UserContext();

        LoginLogic.authenticate(userName, password, context);

        assertNotNull("UserContext must have a user name", context.getUserName());
        assertEquals("UserContext username should be " + userName, userName, context.getUserName());
        assertEquals(userName + " should" + (shouldAuth ? "" : " not") + " be authenticated", shouldAuth, context.isAuthenticated());
        TestHelpers.ToStringTest(context);
        assertNotNull(context.getRoles());
        assertFalse(context.isUserInRole("somerole"));
	}

	@Test
	public void testGetPassword() {
		String userName = "maria";
		String password = "maria";

		String actualPassword = LoginLogic.getUserPassword(userName);

		assertNotNull("Password should not be null", actualPassword);
		assertEquals("Expecting password '" + password + "' for user '" + userName + "'", password, actualPassword);
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(LoginLogic.class);
	}
}
