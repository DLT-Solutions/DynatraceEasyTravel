package com.dynatrace.easytravel.jpa.business;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.dynatrace.easytravel.jpa.Base;
import com.dynatrace.easytravel.jpa.QueryNames;

@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"user_name", "loginDate"}))
@NamedQuery(name=QueryNames.LOGINHISTORY_ALL, query="from LoginHistory order by loginDate desc")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class LoginHistory extends Base {

	public static final String LOGIN_HISTORY_ID = "id";
	public static final String LOGIN_HISTORY_USER = "user";
	public static final String LOGIN_HISTORY_LOGIN_DATE = "loginDate";


	@Id @GeneratedValue(strategy=GenerationType.TABLE)
	private int id;

	@ManyToOne
	private User user;

	@Basic
	private Date loginDate;


	public LoginHistory() {
		super();
	}

	public LoginHistory(User user, Date loginDate) {
		super();
		this.user = user;
		this.loginDate = loginDate;
	}

	public int getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getLoginDate() {
		return loginDate;
	}

	public void setLoginDate(Date loginDate) {
		this.loginDate = loginDate;
	}

	@Override
	public String toString() {
		return "LoginHistory [id=" + id + ", user=" + user + ", loginDate=" + loginDate + "]";
	}

	/**
	 * 
	 * @param incrementAndGet
	 * @author cwat-smoschin
	 */
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((loginDate == null) ? 0 : loginDate.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
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
		LoginHistory other = (LoginHistory) obj;
		if (loginDate == null) {
			if (other.loginDate != null)
				return false;
		} else if (!loginDate.equals(other.loginDate))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}



}
