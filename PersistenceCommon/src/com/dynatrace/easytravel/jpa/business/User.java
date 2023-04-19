package com.dynatrace.easytravel.jpa.business;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.dynatrace.easytravel.jpa.Base;
import com.dynatrace.easytravel.jpa.QueryNames;
import com.dynatrace.easytravel.model.LoyaltyStatus;

@Entity
@Table(name="LoginUser")	// rename to not clash with reserved word in Derby!
@NamedQuery(name=QueryNames.USER_ALL, query="from User u order by u.name")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class User extends Base {

	public static final String USER_NAME = "name";
	public static final String USER_EMAIL = "email";
	public static final String USER_FULL_NAME = "fullName";
	public static final String USER_PASSWORD = "password";
	public static final String USER_LAST_LOGIN = "lastLogin";
	public static final String USER_LOYALTY_STATUS = "loyaltyStatus";

	@Id
	private String name;

	@Basic
	private String password; // clear-text password for authentication

	@Basic
	private String email;

	@Basic
	private LoyaltyStatus loyaltyStatus = LoyaltyStatus.None;


	// not used currently
//	@ManyToMany(fetch = FetchType.LAZY)
//	private Set<UserRole> roles;

	@Basic
	private String fullName;

	@Basic
	private Date lastLogin;

	public User() {
		super();
	}

	// primarily for testing
	public User(String name) {
		this.name = name;
	}

	public User(String name, String fullName, String email, String password) {
		this.name = name;
		this.fullName = fullName;
		this.email = email;
		this.password = password;
	}

	public User(String name, String fullName, String password) {
		super();
		this.name = name;
		this.fullName = fullName;
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		User other = (User) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	@Override
	public String toString() {
		return "User [name=" + name + ", password=" + password + ", fullName=" + fullName + ", lastLogin=" + lastLogin + "]";
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	// we cannot return the enum directly here because Axis2/WSDL does not support enums!
	public String getLoyaltyStatus() {
		// can be null if loaded via hibernate
		if(loyaltyStatus == null) {
			loyaltyStatus = LoyaltyStatus.None;
		}

		return loyaltyStatus.name();
	}


	public void setLoyaltyStatus(String loyaltyStatus) {
		if(StringUtils.isEmpty(loyaltyStatus)) {
			this.loyaltyStatus = LoyaltyStatus.None;
		} else {
			this.loyaltyStatus = LoyaltyStatus.valueOf(loyaltyStatus);
		}
	}
}
