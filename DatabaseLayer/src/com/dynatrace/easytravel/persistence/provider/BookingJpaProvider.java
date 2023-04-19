/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: BookingJpaProvider.java
 * @date: 18.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence.provider;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.dynatrace.easytravel.jpa.QueryNames;
import com.dynatrace.easytravel.jpa.business.Booking;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.persistence.JpaDatabaseController;


/**
 *
 * @author stefan.moschinski
 */
public class BookingJpaProvider extends JpaProvider<Booking> implements BookingProvider {

	/**
	 *
	 * @param controller
	 * @param cls
	 * @author stefan.moschinski
	 */
	public BookingJpaProvider(JpaDatabaseController controller) {
		super(controller, Booking.class);
	}

	@Override
	public Collection<Booking> getBookingsByUserName(String username) {
		TypedQuery<Booking> q = createNamedQuery(QueryNames.BOOKING_GET, Booking.class);
		return q.setParameter("username", username).getResultList();
	}

	@Override
	public Collection<Booking> getBookingsByTenant(String tenantName) {
		return createQuery("select b from Booking b fetch all properties" +
				" inner join b.journey as j" +
				" with j.tenant.name = :tenantName order by b.bookingDate desc", Booking.class).setParameter("tenantName",
				tenantName).getResultList();
	}

	@Override
	public double getTotalSalesByTenant(String tenantName) {
		Double d = createQuery("select sum(j.amount) from Booking b" +
				" inner join b.journey as j" +
				" with j.tenant.name = :tenantName", Double.class)
				.setParameter("tenantName", tenantName)
				.getSingleResult();
		return d != null ? d.doubleValue() : 0;
	}

	@Override
	public int getBookingCountForTenant(String tenantName) {
		return createQuery("select count(b) from Booking b" +
				" inner join b.journey as j" +
				" with j.tenant.name = :tenantName", Long.class)
				.setParameter("tenantName", tenantName)
				.getSingleResult().intValue();
	}

	@Override
	public Map<Location, Integer> getDestinationsByTenant(String tenantName, int limit) {
		Map<Location, Integer> ret = new HashMap<Location, Integer>();
		@SuppressWarnings("unchecked")
		List<Object[]> tmp = createQuery("select l, count(b) from Booking b" +
				" inner join b.journey as j" +
				" with j.tenant.name = :tenantName" +
				" inner join j.destination as l" +
				" group by l" +
				" order by count(b) desc")
				.setParameter("tenantName", tenantName)
				.setMaxResults(limit)
				.getResultList();
		for (Object[] oArr : tmp) {
			ret.put((Location) oArr[0], ((Long) oArr[1]).intValue());
		}
		return ret;
	}

	@Override
	public Map<Location, Integer> getDeparturesByTenant(String tenantName, int limit) {
		Map<Location, Integer> ret = new HashMap<Location, Integer>();
		@SuppressWarnings("unchecked")
		List<Object[]> tmp = createQuery("select l, count(b) from Booking b" +
				" inner join b.journey as j" +
				" with j.tenant.name = :tenantName" +
				" inner join j.start as l" +
				" group by l" +
				" order by count(b) desc")
				.setParameter("tenantName", tenantName)
				.setMaxResults(limit)
				.getResultList();
		if (tmp != null) {
			for (Object[] oArr : tmp) {
				ret.put((Location) oArr[0], ((Long) oArr[1]).intValue());
			}
		}
		return ret;
	}

	@Override
	public int getBookingCountForUser(String name) {
		Query query = createQuery("select count(b) from Booking b where b.user.name = :username");
		query.setParameter("username", name);
		return ((Number) query.getSingleResult()).intValue();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Booking> getBookingsByTenant(String tenantName, int fromIdx, int count) {
		return createQuery("select b from Booking b fetch all properties" +
				" inner join b.journey as j" +
				" with j.tenant.name = :tenantName order by b.bookingDate desc")
				.setParameter("tenantName", tenantName)
				.setFirstResult(fromIdx)
				.setMaxResults(count)
				.getResultList();
	}

	@Override
	public int getBookingCountExcludingUser(String userToExclude) {
		User skippy = findUserByName(userToExclude);

		TypedQuery<Long> q = createQuery("select count(m) from Booking m where user <> :user", Long.class);
		q.setParameter("user", skippy);
		return q.getSingleResult().intValue();
	}


	@Override
	public Collection<String> getBookingIdsExcludingUser(String userToExclude, int resultLimit) {
		User skippy = findUserByName(userToExclude);

		return createQuery("select b.id from Booking b where user <> :user order by b.bookingDate asc", String.class)
				.setParameter("user", skippy)
				.setFirstResult(0)
				.setMaxResults(resultLimit)
				.getResultList();
	}


	private User findUserByName(String userName) {
		return find(User.class, userName);
	}

	@Override
	public void removeBookingById(String bookingId) {
		Booking bookingToRemove = find(bookingId);
		remove(bookingToRemove);
	}

	@Override
	public Booking getBookingById(String bookingId) {
		return find(bookingId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Booking> getRecentBookings(int bookingsLimit) {
		return createQuery("select b from Booking b fetch all properties" +
				" inner join b.journey as j" +
				" order by b.bookingDate desc")
				.setFirstResult(0)
				.setMaxResults(bookingsLimit)
				.getResultList();
	}

}
