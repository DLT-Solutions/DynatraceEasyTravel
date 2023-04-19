package com.dynatrace.easytravel.tomcat;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author cwpl-rorzecho
 */
public class WebLauncherUser {

	private static final Logger LOGGER = LoggerFactory.make();

	private final String userName;
	private final char[] password;
	private final String role;


	public WebLauncherUser(String username, char[] password, String role) {
		this.userName = username;
		this.password = password;
		this.role = role;
	}

	public static List<WebLauncherUser> getUsers() {
		List<WebLauncherUser> webLauncherUsers = Collections.emptyList();
		try (FileInputStream is = new FileInputStream(new File(Directories.getConfigDir(), BaseConstants.Security.WEB_LAUNCHER_USERS));
				BufferedReader br = new BufferedReader(new InputStreamReader(is, BaseConstants.UTF8));) {

			webLauncherUsers = new ArrayList<WebLauncherUser>();
			String line;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(",");
				String userName = tokens[0];
				String password = tokens[1];
				String role = tokens[2];
				WebLauncherUser webLauncherUser = new WebLauncherUser(userName, password.toCharArray(), role);
				webLauncherUsers.add(webLauncherUser);
			}
		} catch (Exception e) {
			LOGGER.error("Cannot read file with WebLauncher user names.", e.getMessage());
		}

		return webLauncherUsers;
	}

	public String getUserName() {
		return userName;
	}

	public char[] getPassword() {
		return password;
	}

	public String getRole() {
		return role;
	}

}
