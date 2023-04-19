package com.dynatrace.easytravel.weblauncher.beans;

import com.dynatrace.easytravel.launcher.security.UserPrincipal;
import com.dynatrace.easytravel.weblauncher.security.HttpSessionManager;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Date;

/**
 * @author cwpl-rorzecho
 */
public class LoggedInUserBean {

	private HttpSession storedHttpSession;

	private String userName;
	private String lastAccessTime;

	private boolean isLoggedIn;

    public LoggedInUserBean() {
        this.storedHttpSession = HttpSessionManager.getStoredHttpSession();
    }

    public LoggedInUserBean(HttpSession httpSession) {
        this.storedHttpSession = httpSession;
    }

	public String getUserName() {
		if (isLoggedIn()) {
			for (Principal principal : HttpSessionManager.getSubject(storedHttpSession).getPrincipals()) {
				if (principal instanceof UserPrincipal) {
					return principal.getName();
				}
			}
		}
		return "Guest";
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getLastAccessTime() {
		long lastAccessedTime = storedHttpSession.getLastAccessedTime();
		Date date = new Date(lastAccessedTime);
		return date.toString();
	}

	public void setLastAccessTime(String lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	public boolean isLoggedIn() {
		if (storedHttpSession != null) {
			return true;
		}
		return false;
	}

	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}

}
