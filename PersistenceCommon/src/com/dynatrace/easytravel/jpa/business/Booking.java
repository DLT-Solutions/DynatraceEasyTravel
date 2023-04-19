package com.dynatrace.easytravel.jpa.business;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.dynatrace.easytravel.jpa.Base;
import com.dynatrace.easytravel.jpa.QueryNames;

@Entity
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries({
	@NamedQuery(name=QueryNames.BOOKING_GET, query="select b from Booking b where b.user.name = :username order by b.bookingDate"),
	@NamedQuery(name=QueryNames.BOOKING_ALL, query="from Booking order by journey, user"),
	@NamedQuery(name=QueryNames.BOOKING_BY_JOURNEY, query="from Booking b where b.journey = :journey")
})
public class Booking extends Base {

	public static final String BOOKING_ID = "id";
	public static final String BOOKING_USER = "user";
	public static final String BOOKING_JOURNEY = "journey";
	public static final String BOOKING_DATE = "bookingDate";

	@Id
	private String id;

	@ManyToOne
	private Journey journey;

	@ManyToOne
	private User user;

	@Basic
	private Date bookingDate;

	public Booking() {
		super();
	}

	public Booking(String id, Journey journey, User user, Date bookingDate) {
		super();
		this.id = id;
		this.journey = journey;
		this.user = user;
		this.bookingDate = bookingDate;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Journey getJourney() {
		return journey;
	}

	public void setJourney(Journey journey) {
		this.journey = journey;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getBookingDate() {
		return bookingDate;
	}

	public void setBookingDate(Date bookingDate) {
		this.bookingDate = bookingDate;
	}

	@Override
	public String toString() {
		return "Booking [id=" + id + ", journey=" + journey + ", user=" + user + ", bookingDate=" + bookingDate + "]";
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
		result = prime * result + ((bookingDate == null) ? 0 : bookingDate.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((journey == null) ? 0 : journey.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
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
		Booking other = (Booking) obj;
		if (bookingDate == null) {
			if (other.bookingDate != null)
				return false;
		} else if (!bookingDate.equals(other.bookingDate))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (journey == null) {
			if (other.journey != null)
				return false;
		} else if (!journey.equals(other.journey))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}
}
