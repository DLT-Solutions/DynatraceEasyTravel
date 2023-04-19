package com.dynatrace.easytravel.jpa.business;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.dynatrace.easytravel.jpa.Base;
import com.dynatrace.easytravel.jpa.QueryNames;

@Entity
@NamedQuery(name=QueryNames.TENANT_ALL, query="select t from Tenant t order by lower(name)")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Tenant extends Base {

	public static final String TENANT_NAME = "name";
	public static final String TENANT_PASSWORD = "password";
	public static final String TENANT_DESC = "description";
	public static final String TENANT_LAST_LOGIN = "lastLogin";

	@Id
	@Column(length = 100)
	private String name;

	@Basic
	private String password;

	@Basic
	private String description;

	@Basic
	private Date lastLogin;


	public Tenant() {
		super();
	}

	public Tenant(String name, String password, String description) {
		super();
		this.name = name;
		this.password = password;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

	@Override
	public String toString() {
		return "Tenant [name=" + name + ", password=" + password + ", description=" + description + ", lastLogin=" + lastLogin +
				"]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tenant other = (Tenant) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		return true;
	}
}