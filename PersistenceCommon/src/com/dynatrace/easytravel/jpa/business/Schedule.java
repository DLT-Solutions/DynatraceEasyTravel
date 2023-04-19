package com.dynatrace.easytravel.jpa.business;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.dynatrace.easytravel.jpa.Base;
import com.dynatrace.easytravel.jpa.QueryNames;

@Entity
@NamedQuery(name=QueryNames.SCHEDULE_ALL, query="from Schedule order by name")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Schedule extends Base {

	public final static String SCHEDULE_NAME = "name";
	public final static String SCHEDULE_LAST_EXECUTION = "lastExecution";
	public final static String SCHEDULE_PERIOD = "period";

	@Id
	private String name;

	@Basic
	private Date lastExecution;

	@Basic
	private long period;

	public Schedule() {
		super();
	}

	public Schedule(String name, long period) {
		super();
		this.name = name;
		this.period = period;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getLastExecution() {
		return lastExecution;
	}

	public void setLastExecution(Date lastExecution) {
		this.lastExecution = lastExecution;
	}

	public long getPeriod() {
		return period;
	}

	public void setPeriod(long period) {
		this.period = period;
	}

	@Override
	public String toString() {
		return "Schedule [name=" + name + ", lastExecution=" + lastExecution + ", period=" + period + "]";
	}
}
