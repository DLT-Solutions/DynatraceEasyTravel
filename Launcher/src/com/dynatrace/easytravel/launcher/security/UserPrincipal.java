package com.dynatrace.easytravel.launcher.security;

import java.io.Serializable;
import java.security.Principal;

/**
 * @author cwpl-rorzecho
 *
 * http://www.byteslounge.com/tutorials/jaas-authentication-in-tomcat-example
 * http://www.javacodegeeks.com/2012/06/java-jaas-form-based-authentication.html
 */
public class UserPrincipal implements Principal, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2588559169299694957L;
	private final String username;

	public UserPrincipal(String username) {
		if (username == null) {
			throw new NullPointerException("NULL username");
		}
		this.username = username;
	}

	@Override
	public String getName() {
		return username;
	}

	@Override
	public String toString() {
		return "username=" + username;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserPrincipal other = (UserPrincipal) obj;
		if (username == null) {
			if (other.username != null) {
				return false;
			}
		} else if (!username.equals(other.username))
			return false;
		return true;
	}
}
