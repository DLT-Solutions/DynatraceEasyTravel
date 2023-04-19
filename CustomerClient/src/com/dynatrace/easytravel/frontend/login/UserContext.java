package com.dynatrace.easytravel.frontend.login;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

public class UserContext implements Serializable
{
	private static final String LOYALTY_STATUS_NONE = "None";

	private static final long serialVersionUID = 8882698368215665217L;

	private String userName;
	private String password;
	private String fullName;
	private boolean authenticated;
	private Set<String> roles = Collections.emptySet();
	private String loyaltyStatus = LOYALTY_STATUS_NONE;

	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 *  Note: this is used in the system profile for BTs, do not change/move it!
	 */
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	public boolean isUserInRole(String role)
	{
		return roles.contains(role);
	}

	public String getValidUserName()
	{
		return authenticated ? userName : null;
	}

	@Override
	public String toString() {
		return "UserContext [userName=" + userName + ", authenticated="
				+ authenticated + ", fullName=" + fullName + ", roles="
				+ roles + "]";
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getLoyaltyStatus() {
		return loyaltyStatus;
	}

	public void setLoyaltyStatus(String loyaltyStatus) {
		this.loyaltyStatus = loyaltyStatus;
	}

	public boolean isHasLoyaltyStatus() {
		// null or "None" mean no status
		return loyaltyStatus != null && !LOYALTY_STATUS_NONE.equals(loyaltyStatus);
	}
}
