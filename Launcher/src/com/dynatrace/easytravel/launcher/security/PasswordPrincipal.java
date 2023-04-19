package com.dynatrace.easytravel.launcher.security;

import java.io.Serializable;
import java.security.Principal;

/**
 * @author cwpl-rorzecho
 */
public class PasswordPrincipal implements Principal, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7822632395482443538L;
	private final String password;

	public PasswordPrincipal(String password) {
		if (password == null) {
			throw new NullPointerException("NULL password");
		}
		this.password = password;
	}

	@Override
	public String getName() {
		return password;
	}

	@Override
	public String toString() {
		return "password=" + password;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((password == null) ? 0 : password.hashCode());
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
		PasswordPrincipal other = (PasswordPrincipal) obj;
		if (password == null) {
			if (other.password != null) {
				return false;
			}
		} else if (!password.equals(other.password))
			return false;
		return true;
	}
}
