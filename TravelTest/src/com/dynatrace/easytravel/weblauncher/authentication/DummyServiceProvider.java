package com.dynatrace.easytravel.weblauncher.authentication;

import static org.junit.Assert.assertTrue;

import com.dynatrace.easytravel.tomcat.UserServiceProvider;

/**
 * cwpl-rorzecho
 */
public class DummyServiceProvider implements UserServiceProvider {

    @Override
    public boolean verifyUser(String userName, char[] password) {
        assertTrue(userName.equals("admin"));
        assertTrue(String.valueOf(password).equals("adminpass"));
        return true;
    }

    @Override
    public String getUserRole(String userName) {
        assertTrue(userName.equals("admin"));
        return "demo";
    }
}
