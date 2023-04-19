package com.dynatrace.easytravel.tomcat;

/**
 * @author cwpl-rorzecho
 */
public interface UserServiceProvider {

	boolean verifyUser(String userName, char[] password);

	String getUserRole(String userName);

}
