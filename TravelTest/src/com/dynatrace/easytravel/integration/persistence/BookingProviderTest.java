/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: BookingProviderTest.java
 * @date: 04.02.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Date;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.jpa.business.Booking;
import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.jpa.business.Tenant;
import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.persistence.provider.BookingProvider;


/**
 *
 * @author stefan.moschinski
 */
@Ignore("ABSTRACT-CLASS")
public abstract class BookingProviderTest extends EasyTravelPersistenceProviderTest<BookingProvider> {

	@Test
	public void testGetBookingsByUserName() throws Exception {
		Journey journey1 = new Journey();
		journey1.setName("name1");
		journey1.setTenant(new Tenant("name1", "pw1", "desc1"));
		journey1.setStart(new Location("Linz"));
		journey1.setDestination(new Location("Wien"));


		Booking booking1 = new Booking(UUID.randomUUID().toString(), journey1, new User("user1", "user user", "user@user"),
				new Date());
		Booking booking2 = new Booking(UUID.randomUUID().toString(), journey1, new User("user1", "user user", "user@user"),
				new Date());
		Booking booking3 = new Booking(UUID.randomUUID().toString(), journey1, new User("userX", "user user", "user@user"),
				new Date());

		provider.add(booking1);
		provider.add(booking2);
		provider.add(booking3);

		assertThat(provider.getBookingsByUserName("user1").size(), is(2));
		assertThat(provider.getBookingsByUserName("user1"), containsInAnyOrder(booking1, booking2));
	}


	@Test
	public void testGetBookingsByTenant() throws Exception {
		Journey journey1 = new Journey("journey1", new Location(), new Location(), new Tenant("tenant1", "pw1", "desc1"),
				new Date(), new Date(), 1111.11, null);
		User user1 = new User();
		user1.setName("user1");
		Booking booking1 = new Booking(UUID.randomUUID().toString(), journey1, user1, new Date());

		Journey journey2 = new Journey("journey2", new Location(), new Location(), new Tenant("tenant2", "pw2", "desc2"),
				new Date(), new Date(), 1111.11, null);
		User user2 = new User();
		user2.setName("user2");
		Booking booking2 = new Booking(UUID.randomUUID().toString(), journey2, user2, new Date());

		User user3 = new User();
		user3.setName("user3");
		Journey journey3 = new Journey("journey3", new Location(), new Location(), new Tenant("tenant1", "pw3", "desc3"),
				new Date(), new Date(), 1111.11, new byte[] { 1, 2, 3, 4, 5 });
		Booking booking3 = new Booking(UUID.randomUUID().toString(), journey3, user3, new Date());


		provider.add(booking1);
		provider.add(booking2);
		provider.add(booking3);


		assertThat(provider.getBookingsByTenant("tenant1").size(), is(2));
		assertThat(provider.getBookingsByTenant("tenant1"), containsInAnyOrder(booking1, booking3));
	}

	@Test
	public void testGetTotalSalesByTenant() throws Exception {
		assertThat(provider.getTotalSalesByTenant("tenant1"), is(0.0));
		assertThat(provider.getTotalSalesByTenant("tenant2"), is(0.0));

		Journey journey1 = new Journey("journey1", new Location(), new Location(), new Tenant("tenant1", "pw1", "desc1"),
				new Date(), new Date(), 1111.11, null);
		String bookingId1 = UUID.randomUUID().toString();
		Booking booking1 = new Booking(bookingId1, journey1, new User("user1"), new Date());

		Journey journey2 = new Journey("journey2", new Location(), new Location(), new Tenant("tenant2", "pw2", "desc2"),
				new Date(), new Date(), 1111.11, null);
		String bookingId2 = UUID.randomUUID().toString();
		Booking booking2 = new Booking(bookingId2, journey2, new User("user2"), new Date());

		Journey journey3 = new Journey("journey3", new Location(), new Location(), new Tenant("tenant1", "pw3", "desc3"),
				new Date(), new Date(), 1111.11, null);
		String bookingId3 = UUID.randomUUID().toString();
		Booking booking3 = new Booking(bookingId3, journey3, new User("user3"), new Date());

		provider.add(booking1);
		provider.add(booking2);
		provider.add(booking3);


		assertThat(provider.getTotalSalesByTenant("tenant1"), is(2222.22));
		assertThat(provider.getTotalSalesByTenant("tenant2"), is(1111.11));
		assertThat(provider.getTotalSalesByTenant("tenant3"), is(0d));

		provider.removeBookingById(bookingId1);
		assertThat(provider.getTotalSalesByTenant("tenant1"), is(1111.11));

		provider.removeBookingById(bookingId3);
		assertThat(provider.getTotalSalesByTenant("tenant1"), is(0.0));

		provider.removeBookingById(bookingId2);
		assertThat(provider.getTotalSalesByTenant("tenant2"), is(0.0));
	}

	@Test
	public void testGetDeparturesByTenant() throws Exception {
		Location dresden = new Location("dresden");
		Journey journey1 = new Journey("journey1", dresden, new Location(),
				new Tenant("tenant1", "pw1", "desc1"),
				new Date(), new Date(), 1111.11, null);
		Booking booking1 = new Booking(UUID.randomUUID().toString(), journey1, new User("user1"), new Date());

		Journey journey2 = new Journey("journey2", dresden, new Location(),
				new Tenant("tenant1", "pw2", "desc2"),
				new Date(), new Date(), 1111.11, null);
		Booking booking2 = new Booking(UUID.randomUUID().toString(), journey2, new User("user2"), new Date());

		Location wien = new Location("wien");
		Journey journey3 = new Journey("journey3", wien, new Location(), new Tenant("tenant1", "pw3", "desc3"),
				new Date(), new Date(), 1111.11, null);
		Booking booking3 = new Booking(UUID.randomUUID().toString(), journey3, new User("user3"), new Date());


		provider.add(booking1);
		provider.add(booking2);
		provider.add(booking3);


		assertThat(provider.getDeparturesByTenant("tenant1", 100).size(), is(2));
		assertThat(provider.getDeparturesByTenant("tenant1", 100).keySet(), containsInAnyOrder(dresden, wien));
		assertThat(provider.getDeparturesByTenant("tenant1", 100).get(dresden), is(2));
		assertThat(provider.getDeparturesByTenant("tenant1", 100).get(wien), is(1));
	}

	@Test
	public void testGetBookingCountForTenant() throws Exception {
		int count = 222;
		for (int i = 0; i < count; i++) {
			Journey journey = new Journey("journey" + i, new Location(), new Location(),
					new Tenant("tenantONLY", "pw1", "desc1"),
					new Date(), new Date(), 1111.11, null);
			Booking booking = new Booking(String.valueOf(i), journey, new User("user1"), new Date());
			provider.add(booking);
		}

		assertThat(provider.getBookingCountForTenant("tenantONLY"), is(count));
		assertThat(provider.getBookingCountForTenant("tenantONLY2"), is(0));

		for (int i = 0; i < count; i++) {
			provider.removeBookingById(String.valueOf(i));
			assertThat(provider.getBookingCountForTenant("tenantONLY"), is(count - (i + 1)));
		}
	}

	@Test
	public void testGetBookingCountForUser() throws Exception {
		int count = 222;
		User user = new User();
		user.setName("user1");

		for (int i = 0; i < count; i++) {
			Journey journey = new Journey("journey" + i, new Location(), new Location(),
					new Tenant("tenantONLY", "pw1", "desc1"),
					new Date(), new Date(), 1111.11, null);
			Booking booking = new Booking(UUID.randomUUID().toString(), journey, user, new Date());
			provider.add(booking);
		}

		assertThat(provider.getBookingCountForUser("user1"), is(count));
		assertThat(provider.getBookingCountForUser("user2"), is(0));
	}

	@Test
	public void testRemoveBookingById() {
		Location dresden = new Location("dresden");
		Journey journey1 = new Journey("journey1", dresden, new Location(),
				new Tenant("tenant1", "pw1", "desc1"),
				new Date(), new Date(), 1111.11, null);
		Booking booking1 = new Booking(UUID.randomUUID().toString(), journey1, new User("user1"), new Date());

		Journey journey2 = new Journey("journey2", dresden, new Location(),
				new Tenant("tenant1", "pw2", "desc2"),
				new Date(), new Date(), 1111.11, null);
		Booking booking2 = new Booking(UUID.randomUUID().toString(), journey2, new User("user2"), new Date());

		Location wien = new Location("wien");
		Journey journey3 = new Journey("journey3", wien, new Location(), new Tenant("tenant1", "pw3", "desc3"),
				new Date(), new Date(), 1111.11, null);
		Booking booking3 = new Booking(UUID.randomUUID().toString(), journey3, new User("user3"), new Date());

		provider.add(booking1);
		provider.add(booking2);
		provider.add(booking3);

		assertThat(provider.getAll(), containsInAnyOrder(booking1, booking2, booking3));

		provider.removeBookingById(booking1.getId());
		assertThat(provider.getAll(), containsInAnyOrder(booking2, booking3));

		provider.removeBookingById(booking2.getId());
		assertThat(provider.getAll(), containsInAnyOrder(booking3));

		provider.removeBookingById(booking3.getId());
		assertThat(provider.getAll(), is(empty()));
	}

	@Test
	public void testGetBookingIdsExcludingUser() {
		Location dresden = new Location("dresden");
		Journey journey1 = new Journey("journey1", dresden, new Location(),
				new Tenant("tenant1", "pw1", "desc1"),
				new Date(), new Date(), 1111.11, null);
		User user1 = new User();
		user1.setName("user1");

		Booking booking1 = new Booking(UUID.randomUUID().toString(), journey1, user1, new Date());

		Journey journey2 = new Journey("journey2", dresden, new Location(),
				new Tenant("tenant1", "pw2", "desc2"),
				new Date(), new Date(), 1111.11, null);
		Booking booking2 = new Booking(UUID.randomUUID().toString(), journey2, user1, new Date());


		User user2 = new User();
		user2.setName("user2");

		Location wien = new Location("wien");
		Journey journey3 = new Journey("journey3", wien, new Location(), new Tenant("tenant1", "pw3", "desc3"),
				new Date(), new Date(), 1111.11, null);
		Booking booking3 = new Booking(UUID.randomUUID().toString(), journey3, user2, new Date());

		provider.add(booking1);
		provider.add(booking2);
		provider.add(booking3);

		assertThat(provider.getBookingIdsExcludingUser("user1", 100), containsInAnyOrder(booking3.getId()));
		assertThat(provider.getBookingIdsExcludingUser("user2", 100),
				containsInAnyOrder(booking1.getId(), booking2.getId()));

		assertThat(provider.getBookingIdsExcludingUser("user2", 1).size(), is(1));
	}

	@Test
	public void testGetBookingCountExcludingUser() {
		Location dresden = new Location("dresden");
		Journey journey1 = new Journey("journey1", dresden, new Location(),
				new Tenant("tenant1", "pw1", "desc1"),
				new Date(), new Date(), 1111.11, null);
		User user1 = new User();
		user1.setName("user1");

		Booking booking1 = new Booking(UUID.randomUUID().toString(), journey1, user1, new Date());

		Journey journey2 = new Journey("journey2", dresden, new Location(),
				new Tenant("tenant1", "pw2", "desc2"),
				new Date(), new Date(), 1111.11, null);
		Booking booking2 = new Booking(UUID.randomUUID().toString(), journey2, user1, new Date());


		User user2 = new User();
		user2.setName("user2");

		Location wien = new Location("wien");
		Journey journey3 = new Journey("journey3", wien, new Location(), new Tenant("tenant1", "pw3", "desc3"),
				new Date(), new Date(), 1111.11, null);
		Booking booking3 = new Booking(UUID.randomUUID().toString(), journey3, user2, new Date());

		provider.add(booking1);
		provider.add(booking2);
		provider.add(booking3);

		assertThat(provider.getBookingCountExcludingUser(user1.getName()), is(1));
		assertThat(provider.getBookingCountExcludingUser(user2.getName()), is(2));
	}
}
