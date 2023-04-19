package com.dynatrace.easytravel.tomcat;

import java.util.Arrays;
import java.util.List;

/**
 * @author cwpl-rorzecho
 */
public class FileUserServiceProvider implements UserServiceProvider {

	private static final List<WebLauncherUser> webLauncherUsers;

	public FileUserServiceProvider() { }

	static {
		webLauncherUsers = WebLauncherUser.getUsers();
	}

	@Override
	public boolean verifyUser(String userName, char[] password) {
		for (WebLauncherUser webLauncherUser : webLauncherUsers) {
			if (webLauncherUser.getUserName().equals(userName) && Arrays.equals(webLauncherUser.getPassword(), password)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getUserRole(String userName) {
		for (WebLauncherUser webLauncherUser : webLauncherUsers) {
			if (webLauncherUser.getUserName().equals(userName)) {
				return webLauncherUser.getRole();
			}
		}
		return null;
	}
}
