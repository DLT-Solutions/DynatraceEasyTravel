package com.dynatrace.easytravel.jpa;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Version;

public abstract class Base {

	public static final String DATE_CREATED = "dateCreated";

    @Basic
    private Date created = new Date();

	@Version
	private int version;

	/**
	 * @return the created
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * @param created the created to set
	 */
	public void setCreated(Date created) {
		this.created = created;
	}
}
