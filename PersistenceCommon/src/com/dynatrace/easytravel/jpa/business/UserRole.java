package com.dynatrace.easytravel.jpa.business;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.dynatrace.easytravel.jpa.Base;

@Entity
@Table(name="LoginRole")	// rename to not clash with reserved word in Derby!
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserRole extends Base
{
	@Id
	private String name;

//	@ManyToMany(fetch = FetchType.LAZY)
//	private List<User> users;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
		UserRole other = (UserRole) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "UserRole [name=" + name + "]";
	}
}
