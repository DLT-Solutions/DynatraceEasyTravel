package com.dynatrace.easytravel.launcher.security;

import java.io.Serializable;
import java.security.Principal;

/**
 * @author cwpl-rorzecho
 */
public class RolePrincipal implements Principal, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6091700194746628017L;
	private final String role;

	public RolePrincipal(String role) {
		if (role == null) {
			throw new NullPointerException("NULL role");
		}
		this.role = role;
	}

	@Override
	public String getName() {
		return role;
	}

	@Override
	public String toString() {
		return "role=" + role;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((role == null) ? 0 : role.hashCode());
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
		RolePrincipal other = (RolePrincipal) obj;
		if (role == null) {
			if (other.role != null) {
				return false;
			}
		} else if (!role.equals(other.role))
			return false;
		return true;
	}
}
