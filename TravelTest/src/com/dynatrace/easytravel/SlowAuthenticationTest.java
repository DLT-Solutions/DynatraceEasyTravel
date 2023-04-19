package com.dynatrace.easytravel;

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.junit.Test;

import com.dynatrace.easytravel.jpa.business.Booking;
import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.jpa.business.LoginHistory;
import com.dynatrace.easytravel.jpa.business.Tenant;
import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.spring.PluginConstants;

public class SlowAuthenticationTest {

    public static final String SOMETHING_ELSE = "something.else";

    private static final String BOOKING_ID1 = "booking1";
    private static final String BOOKING_ID2 = "booking2";
    private static final String BOOKING_ID3 = "booking3";

    private static final String USER_NAME = "george";
    private static final String USER_PASSWORD = "password";

    private static final Journey JOURNEY = new Journey("journey", new Location("start"), new Location("dest"), new Tenant("tenant", "pwd", ""), new Date(), new Date(), 123.0, null);
    private static final User USER = new User(USER_NAME, "full name", USER_PASSWORD);
    private static final Date BOOKING_DATE1 = new Date();
    private static final Date BOOKING_DATE2 = new Date();
    private static final Date BOOKING_DATE3 = new Date();


	@Test
	public void testExecute() {

	    EntityManager emMock = createMock(EntityManager.class);

		CalculateUserStats plugin = new CalculateUserStats(emMock);
		plugin.setExtensionPoint(new String[] { PluginConstants.BACKEND_AUTHENTICATE });
		plugin.setEnabled(true);

		replay(emMock);
		plugin.execute(SOMETHING_ELSE, "george", "george");
		verify(emMock);
		reset(emMock);

		@SuppressWarnings("unchecked")
        TypedQuery<Booking> bookingQueryMock = createMock(TypedQuery.class);
		@SuppressWarnings("unchecked")
        TypedQuery<LoginHistory> loginHistoryQueryMock = createMock(TypedQuery.class);
		Query queryMock = createMock(Query.class);
		EntityTransaction transactionMock = createMock(EntityTransaction.class);

		//Query bookings
		expect(emMock.createQuery(anyObject(String.class), eq(Booking.class))).andReturn(bookingQueryMock);
		expect(bookingQueryMock.setParameter("userName", USER_NAME)).andReturn(bookingQueryMock);
		List<Booking> bookings = new ArrayList<Booking>();
		Booking booking1 = new Booking(BOOKING_ID1, JOURNEY, USER, BOOKING_DATE1);
		Booking booking2 = new Booking(BOOKING_ID2, JOURNEY, USER, BOOKING_DATE2);
		Booking booking3 = new Booking(BOOKING_ID3, JOURNEY, USER, BOOKING_DATE3);
		bookings.add(booking1);
		bookings.add(booking2);
		bookings.add(booking3);

        expect(bookingQueryMock.getResultList()).andReturn(bookings);
		expect(emMock.getTransaction()).andReturn(transactionMock);

		transactionMock.begin();
		expectLastCall();

		//verifyBooking 3 times
        expect(emMock.createNativeQuery(anyObject(String.class))).andReturn(queryMock);
        expect(queryMock.setParameter(1, 15)).andReturn(queryMock);
        expect(queryMock.executeUpdate()).andReturn(0);
        expect(emMock.createNativeQuery(anyObject(String.class))).andReturn(queryMock);
        expect(queryMock.setParameter(1, 15)).andReturn(queryMock);
        expect(queryMock.executeUpdate()).andReturn(0);
        expect(emMock.createNativeQuery(anyObject(String.class))).andReturn(queryMock);
        expect(queryMock.setParameter(1, 15)).andReturn(queryMock);
        expect(queryMock.executeUpdate()).andReturn(0);

        transactionMock.commit();
        expectLastCall();

        //query login history
        expect(emMock.createQuery(anyObject(String.class), eq(LoginHistory.class))).andReturn(loginHistoryQueryMock);
        expect(loginHistoryQueryMock.setParameter("userName", USER_NAME)).andReturn(loginHistoryQueryMock);
        expect(loginHistoryQueryMock.getResultList()).andReturn(new ArrayList<LoginHistory>());
		expectLastCall();
        replay(emMock, queryMock, bookingQueryMock, loginHistoryQueryMock, transactionMock);
        plugin.execute(PluginConstants.BACKEND_AUTHENTICATE, USER_NAME, USER_PASSWORD);

        verify(emMock, queryMock, bookingQueryMock, loginHistoryQueryMock, transactionMock);
	}
}
