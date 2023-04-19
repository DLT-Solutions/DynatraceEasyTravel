package com.dynatrace.easytravel.jpa.business;


import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.persistence.*;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;

import com.dynatrace.easytravel.jpa.Base;
import com.dynatrace.easytravel.jpa.QueryNames;

@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"name", "start_name", "destination_name", "fromDate", "toDate"}))
@NamedQueries({
	@NamedQuery(name=QueryNames.JOURNEY_ALL, query="select j from Journey j order by fromDate"),
	@NamedQuery(name=QueryNames.JOURNEY_ALL_IDS, query="select id from Journey"),
	@NamedQuery(name=QueryNames.JOURNEY_GET, query="select j from Journey j where name=:name"),
	@NamedQuery(name=QueryNames.JOURNEY_FIND, query="select j from Journey j " +
			" where :destination = destination.name" +
			" and :fromDate <= fromDate" +
			" and :toDate >= toDate"),
	@NamedQuery(name=QueryNames.JOURNEY_FIND_NORMALIZED, query="select j from Journey j " +
			" where :destination = destination.name" +
			" and :fromDate <= fromDate" +
			" and :toDate >= toDate" +
			" and normalize_location(:destination, :factor) is not null"),
	@NamedQuery(name=QueryNames.JOURNEY_FIND_BY_ID_NORMALIZED, query="select j from Journey j " +
			" where :id = id" +
			" and normalize_location('Paris',:factor) is not null"),
	@NamedQuery(name=QueryNames.JOURNEY_FIND_BY_LOCATION_DEST, query="select j from Journey j where destination.name = :destination"),
	@NamedQuery(name=QueryNames.JOURNEY_FIND_BY_LOCATION_START, query="select j from Journey j where start.name = :start"),
	@NamedQuery(name=QueryNames.JOURNEY_FIND_BY_TENANT, query="select j from Journey j where tenant = :tenant"),
	@NamedQuery(name=QueryNames.JOURNEY_ALL_ALPHABETICALLY, query="select name from Journey j  where tenant = :tenant order by lower(name)"),
	@NamedQuery(name=QueryNames.JOURNEY_GET_INDEX, query="select count(j) from Journey j where tenant = :tenant and name <= :name ")
})

@org.hibernate.annotations.Entity(dynamicUpdate = true)
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Journey extends Base {

	public static final String JOURNEY_ID = "id";
	public static final String JOURNEY_DESTINATION = "destination";
	public static final String JOURNEY_START = "start";
	public static final String JOURNEY_NAME = "name";
	public static final String JOURNEY_AMOUNT = "amount";
	public static final String JOURNEY_TENANT = "tenant";
	public static final String JOURNEY_FROM_DATE = "fromDate";
	public static final String JOURNEY_TO_DATE = "toDate";
	public static final String JOURNEY_DESC = "description";
	public static final String JOURNEY_PICTURE = "picture";

	@Id @GeneratedValue(strategy=GenerationType.TABLE)
	private int id;

	@Basic
	@Index(name="journey_name_index")
	@Column(length = 100)
	private String name;

	@ManyToOne
	private Location start;

	@ManyToOne
	private Location destination;

	@ManyToOne
	private Tenant tenant;

	@Basic
	private Date fromDate;

	@Basic
	private Date toDate;

	@Basic
	private String description;

	@Basic
	private double amount;

	// Hibernate has trouble with length of blobs, see
	// 	http://opensource.atlassian.com/projects/hibernate/browse/HHH-2614
	@Lob
	@Column(name="content", length=Integer.MAX_VALUE / 100)
	private byte[] picture;

	public Journey()
	{}

	public Journey(String name, Location start, Location destination, Tenant tenant, Date fromDate, Date toDate, double amount, byte[] picture) {
		this.name = name;
		this.start = start;
		this.destination = destination;
		this.tenant = tenant;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.amount = amount;
		this.picture = ArrayUtils.clone(picture);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Location getStart() {
		return start;
	}

	public void setStart(Location start) {
		this.start = start;
	}

	public Location getDestination() {
		return destination;
	}

	public void setDestination(Location destination) {
		this.destination = destination;
	}

	public Tenant getTenant() {
		return tenant;
	}

	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public Calendar getFromDateTime() {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));
        c.setTime(fromDate);
        return c;
    }

	public void setFromDate(Date from) {
		this.fromDate = from;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public Date getToDate() {
		return toDate;
	}

	public Calendar getToDateTime() {
	    Calendar c = Calendar.getInstance();
	    c.setTime(toDate);
	    return c;
	}

	public void setToDate(Date to) {
		this.toDate = to;
	}

	public byte[] getPicture() {
		return picture;
	}

	public void setPicture(byte[] picture) {
		this.picture = ArrayUtils.clone(picture);
	}

	@Override
	public String toString() {
		// manually implement toString() here otherwise the whole byte-array for the picture is included!!
		return new ToStringBuilder(this).
			append("id", id).
			append("name", name).
				append(JOURNEY_START, start).
				append(JOURNEY_DESTINATION, destination).
			append("tenant", tenant).
			append("fromDate", fromDate).
			append("toDate", toDate).
			append("description", description).
			append("amount", amount).
			append("picture", picture == null ? -1 : picture.length).
			toString();
	}

	/**
	 *
	 * @param id
	 * @author cwat-smoschin
	 */
	public void setId(int id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(amount);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + ((fromDate == null) ? 0 : fromDate.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + Arrays.hashCode(picture);
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((tenant == null) ? 0 : tenant.hashCode());
		result = prime * result + ((toDate == null) ? 0 : toDate.hashCode());
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
		Journey other = (Journey) obj;
		if (Double.doubleToLongBits(amount) != Double.doubleToLongBits(other.amount))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (destination == null) {
			if (other.destination != null)
				return false;
		} else if (!destination.equals(other.destination))
			return false;
		if (fromDate == null) {
			if (other.fromDate != null)
				return false;
		} else if (!fromDate.equals(other.fromDate))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (!Arrays.equals(picture, other.picture))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		if (tenant == null) {
			if (other.tenant != null)
				return false;
		} else if (!tenant.equals(other.tenant))
			return false;
		if (toDate == null) {
			if (other.toDate != null)
				return false;
		} else if (!toDate.equals(other.toDate))
			return false;
		return true;
	}
}
