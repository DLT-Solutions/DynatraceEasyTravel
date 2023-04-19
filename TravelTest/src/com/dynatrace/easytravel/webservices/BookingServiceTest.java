package com.dynatrace.easytravel.webservices;

import static org.easymock.EasyMock.anyObject;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.dynatrace.easytravel.DummyPaymentService;
import com.dynatrace.easytravel.business.webservice.transferobj.BookingSummary;
import com.dynatrace.easytravel.business.webservice.transferobj.BookingTO;
import com.dynatrace.easytravel.cache.PaymentService;
import com.dynatrace.easytravel.jpa.business.*;
import com.dynatrace.easytravel.misc.UserType;
import com.dynatrace.easytravel.utils.TestHelpers;

@RunWith(MockitoJUnitRunner.class)
public class BookingServiceTest extends WSTestBase {

    private static final String CC_NUMBER = "4111111111111111";

    private static final int LOCATION_COUNT = 2;

    private static final int BOOKING_COUNT = 45;

    private static final double TOTAL_SALES = 47000.0;

    private static final Integer LOCATION1_COUNT = 15;

    private static final Integer LOCATION2_COUNT = 30;

    private static final String DB_STATISTICS = "db statistics";

    private static final String BOOKING_ID = "booking id";

    private static final String BOOKING_ID2 = "booking id2";


    @Test
    public void checkCreditCard() {
        assertTrue(bookingService.checkCreditCard(UserType.WEB, CC_NUMBER));
    }


    @Test
    public void getBooking() {
        List<Booking> bookings = createBookingList();
		when(dbAccess.findBookings(USER_NAME)).thenReturn(bookings);

        Booking[] bArr = bookingService.getBooking(USER_NAME);
        assertEquals(bookings.size(), bArr.length);
        for (int i = 0; i < bArr.length; i++) {
            assertEquals(bookings.get(i), bArr[i]);
        }
    }

    @Test
    public void getBookingNull() {
		when(dbAccess.findBookings(USER_NAME)).thenReturn(null);

        Booking[] bArr = bookingService.getBooking(USER_NAME);
        assertEquals(0, bArr.length);
    }

    @Test
    public void getBookingIds() {
        List<Booking> bookings = createBookingList();
		when(dbAccess.findBookings(USER_NAME)).thenReturn(bookings);

        String[] bNames = bookingService.getBookingIds(USER_NAME);
        assertEquals(bookings.size(), bNames.length);
        for (int i = 0; i < bNames.length; i++) {
            assertEquals(bookings.get(i).getId(), bNames[i]);
        }
    }

    @Test
    public void getBookingIdsNull() {
		when(dbAccess.findBookings(USER_NAME)).thenReturn(null);

        String[] bNames = bookingService.getBookingIds(USER_NAME);
        assertEquals(0, bNames.length);
    }

    @Test
    public void getBookingPageByTenant() {
        List<Booking> bookings = createBookingList();
		when(dbAccess.getBookingCountByTenant(TENANT_NAME)).thenReturn(BOOKING_COUNT);
		when(dbAccess.getBookingsByTenant(TENANT_NAME, PAGE_FROM, PAGE_COUNT)).thenReturn(bookings);

        BookingTO[] bArr = bookingService.getBookingPageByTenant(TENANT_NAME, PAGE_FROM, PAGE_COUNT).getBookings();
        assertEquals(bookings.size(), bArr.length);
        for (int i = 0; i < bArr.length; i++) {
            assertEquals(bookings.get(i).getId(), bArr[i].getId());
        }
    }

    @Test
    public void getBookingSummaryByTenant() {
		when(dbAccess.getBookingCountByTenant(TENANT_NAME)).thenReturn(BOOKING_COUNT);
		when(dbAccess.getTotalSalesByTenant(TENANT_NAME)).thenReturn(TOTAL_SALES);
        Map<Location, Integer> departures = new HashMap<Location, Integer>();
        departures.put(LOCATION1, LOCATION1_COUNT);
        departures.put(LOCATION2, LOCATION2_COUNT);
		when(dbAccess.getDeparturesByTenant(TENANT_NAME, LOCATION_COUNT)).thenReturn(departures);
        Map<Location, Integer> destinations = new HashMap<Location, Integer>();
        destinations.put(LOCATION1, LOCATION1_COUNT);
        destinations.put(LOCATION2, LOCATION2_COUNT);
		when(dbAccess.getDestinationsByTenant(TENANT_NAME, LOCATION_COUNT)).thenReturn(destinations);

        BookingSummary bs = bookingService.getBookingSummaryByTenant(TENANT_NAME, LOCATION_COUNT);
        assertEquals(TOTAL_SALES, bs.getTotalSales(), 0.01);
        assertEquals(BOOKING_COUNT, bs.getNBookings());
    }

    @Test
    public void getDatabaseStatistics() {
		when(dbAccess.getStatistics()).thenReturn(DB_STATISTICS);

        assertEquals(DB_STATISTICS, bookingService.getDatabaseStatistics());
    }

    @Test
    public void testStoreBooking() throws Exception {
        expectBooking();

        assertNotNull(bookingService.storeBooking(JOURNEY_ID, USER_NAME, UserType.WEB, CC_NUMBER, null));
    }

    @Test
    public void testStoreBookingCCCheckInvalid() throws Exception {
        expectBooking();

        DummyPaymentService.response = PaymentService.CC_INVALID;
        try {
        	bookingService.storeBooking(JOURNEY_ID, USER_NAME, UserType.WEB, CC_NUMBER, null);
        	fail("Should catch exception here");
    	} catch (IllegalArgumentException e) {
    		TestHelpers.assertContains(e, "The Payment-Service reported that the provided credit card number " + CC_NUMBER + " is invalid");
        } finally {
        	DummyPaymentService.response = PaymentService.PAYMENT_ACCEPTED;
        }
    }

    @Test
    public void testStoreBookingCCAlreadyPais() throws Exception {
        expectBooking();

        DummyPaymentService.response = PaymentService.ALREADY_PAID;
        try {
        	bookingService.storeBooking(JOURNEY_ID, USER_NAME, UserType.WEB, CC_NUMBER, null);
        	fail("Should catch exception here");
    	} catch (IllegalArgumentException e) {
    		TestHelpers.assertContains(e, "This booking has already been paid");
        } finally {
        	DummyPaymentService.response = PaymentService.PAYMENT_ACCEPTED;
        }
    }

    @Test
    public void testStoreBookingCCWrongUser() throws Exception {
        expectBooking();

        DummyPaymentService.response = PaymentService.CC_WRONG_USER;
        try {
        	bookingService.storeBooking(JOURNEY_ID, USER_NAME, UserType.WEB, CC_NUMBER, null);
        	fail("Should catch exception here");
    	} catch (IllegalArgumentException e) {
    		TestHelpers.assertContains(e, "This credit card has already been registered for another user");
        } finally {
        	DummyPaymentService.response = PaymentService.PAYMENT_ACCEPTED;
        }
    }

    @Test
    public void testStoreBookingCCExpired() throws Exception {
        expectBooking();

        DummyPaymentService.response = PaymentService.CC_EXPIRED;
        try {
        	bookingService.storeBooking(JOURNEY_ID, USER_NAME, UserType.WEB, CC_NUMBER, null);
        	fail("Should catch exception here");
    	} catch (IllegalArgumentException e) {
    		TestHelpers.assertContains(e, "Credit card has expired");
        } finally {
        	DummyPaymentService.response = PaymentService.PAYMENT_ACCEPTED;
        }
    }

    @Test
    public void testStoreBookingCCOtherError() throws Exception {
        expectBooking();

        DummyPaymentService.response = PaymentService.OTHER_ERROR;
        try {
        	bookingService.storeBooking(JOURNEY_ID, USER_NAME, UserType.WEB, CC_NUMBER, null);
        	fail("Should catch exception here");
    	} catch (IllegalArgumentException e) {
    		TestHelpers.assertContains(e, "PaymentService returned 'OTHER_ERROR', check logFile of dotNet Service.");
        } finally {
        	DummyPaymentService.response = PaymentService.PAYMENT_ACCEPTED;
        }
    }

    @Test
    public void testStoreBookingCCUpdateError() throws Exception {
        expectBooking();

        DummyPaymentService.response = PaymentService.UPDATE_ERROR;
        try {
        	bookingService.storeBooking(JOURNEY_ID, USER_NAME, UserType.WEB, CC_NUMBER, null);
        	fail("Should catch exception here");
    	} catch (IllegalArgumentException e) {
    		TestHelpers.assertContains(e, "PaymentService returned 'UPDATE_ERROR', there seems to have been a problem with the database, check logFile of dotNet Service.");
        } finally {
        	DummyPaymentService.response = PaymentService.PAYMENT_ACCEPTED;
        }
    }

    @Test
    public void testStoreBookingCCUnknown() throws Exception {
        expectBooking();

        DummyPaymentService.response = "something";
        try {
        	bookingService.storeBooking(JOURNEY_ID, USER_NAME, UserType.WEB, CC_NUMBER, null);
        	fail("Should catch exception here");
    	} catch (IllegalArgumentException e) {
    		TestHelpers.assertContains(e, "Unknown exception calling PaymentService", "something");
        } finally {
        	DummyPaymentService.response = PaymentService.PAYMENT_ACCEPTED;
        }
    }

    @Test
    public void testStoreBookingCCNullResponse() throws Exception {
        expectBooking();

        DummyPaymentService.response = null;
        try {
        	bookingService.storeBooking(JOURNEY_ID, USER_NAME, UserType.WEB, CC_NUMBER, null);
        	fail("Should catch exception here");
    	} catch (IllegalStateException e) {
    		TestHelpers.assertContains(e, "No PaymentService found or all PaymentService plugins returned null");
        } finally {
        	DummyPaymentService.response = PaymentService.PAYMENT_ACCEPTED;
        }
    }

    @Test
    public void testStoreBookingIOException() throws Exception {
        expectBooking();

        DummyPaymentService.exception = new IOException("TestException");
        try {
        	bookingService.storeBooking(JOURNEY_ID, USER_NAME, UserType.WEB, CC_NUMBER, null);
        	fail("Should catch exception here");
    	} catch (IllegalArgumentException e) {
    		TestHelpers.assertContains(e, "TestException");
        } finally {
        	DummyPaymentService.exception = null;
        }
    }

    protected void expectBooking() {
		User user = new User(USER_NAME, USER_FULLNAME, USER_EMAIL, USER_PASSWORD);
        List<Integer> journeyIds = createJourneyIdsList();
		when(dbAccess.getUser(USER_NAME)).thenReturn(user);
		when(config.isDBSpammingEnabled()).thenReturn(true);

		when(dbAccess.allJourneyIds()).thenReturn(journeyIds);
		when(dbAccess.getJourneyByIdNormalize(1,false)).thenReturn(createJourneyList().get(0));
		when(dbAccess.getJourneyByIdNormalize(2,false)).thenReturn(createJourneyList().get(1));
        dbAccess.storeBooking(anyObject(Booking.class));
	}

    @Test
    public void testStoreBookingNotexistingUser() throws Exception {
    	try {
        	bookingService.storeBooking(JOURNEY_ID, USER_NAME, UserType.WEB, CC_NUMBER, null);
        	fail("Should catch exception");
    	} catch (IllegalArgumentException e) {
    		TestHelpers.assertContains(e, "Could not find", USER_NAME);
    	}
    }

    @Test
    public void testStoreBookingNotexistingJourney() throws Exception {
        User user = new User(USER_NAME, USER_FULLNAME, USER_EMAIL, USER_PASSWORD);
		when(dbAccess.getUser(USER_NAME)).thenReturn(user);
		when(config.isDBSpammingEnabled()).thenReturn(false);

		when(dbAccess.getJourneyById(JOURNEY_ID)).thenReturn(null);

        try {
        	bookingService.storeBooking(JOURNEY_ID, USER_NAME, UserType.WEB, CC_NUMBER, null);
        	fail("Should catch exception");
    	} catch (IllegalArgumentException e) {
    		TestHelpers.assertContains(e, "Could not find journey", Integer.toString(JOURNEY_ID));
    	}
    }

    @Test
    public void testStoreBookingCCInvalid() throws Exception {
        User user = new User(USER_NAME, USER_FULLNAME, USER_EMAIL, USER_PASSWORD);
        List<Integer> journeyIds = createJourneyIdsList();
		when(dbAccess.getUser(USER_NAME)).thenReturn(user);
		when(config.isDBSpammingEnabled()).thenReturn(true);

		when(dbAccess.allJourneyIds()).thenReturn(journeyIds);
		when(dbAccess.getJourneyByIdNormalize(1,false)).thenReturn(createJourneyList().get(0));
		when(dbAccess.getJourneyByIdNormalize(2,false)).thenReturn(createJourneyList().get(1));

        try {
        	bookingService.storeBooking(JOURNEY_ID, USER_NAME, UserType.WEB, null, null);
        	fail("Should catch exception");
    	} catch (IllegalArgumentException e) {
    		TestHelpers.assertContains(e, "Cannot create Booking without CreditCard details.");
    	}
        try {
        	bookingService.storeBooking(JOURNEY_ID, USER_NAME, UserType.WEB, "", null);
        	fail("Should catch exception");
    	} catch (IllegalArgumentException e) {
    		TestHelpers.assertContains(e, "Cannot create Booking without CreditCard details.");
    	}
    }

    private List<Booking> createBookingList() {
        List<Booking> bookings = new ArrayList<Booking>();
        bookings.add(new Booking(BOOKING_ID, new Journey(JOURNEY_NAME, new Location(JOURNEY_FROM), new Location(JOURNEY_TO), new Tenant(TENANT_NAME, TENANT_PASSWORD, ""), FROM_DATE, TO_DATE, JOURNEY_AMOUNT, JOURNEY_PICTURE), new User(USER_NAME, USER_FULLNAME, USER_EMAIL, USER_PASSWORD), new Date()));
        bookings.add(new Booking(BOOKING_ID2, new Journey(JOURNEY_NAME, new Location(JOURNEY_FROM), new Location(JOURNEY_TO), new Tenant(TENANT_NAME, TENANT_PASSWORD, ""), FROM_DATE, TO_DATE, JOURNEY_AMOUNT, JOURNEY_PICTURE), new User(USER_NAME, USER_FULLNAME, USER_EMAIL, USER_PASSWORD), new Date()));
        return bookings;
    }


    private List<Integer> createJourneyIdsList() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        return list;
    }

    private List<Journey> createJourneyList() {
        List<Journey> list = new ArrayList<Journey>();
        list.add(new Journey(JOURNEY_NAME, new Location(JOURNEY_FROM), new Location(JOURNEY_TO), new Tenant(TENANT_NAME, TENANT_PASSWORD, ""), FROM_DATE, TO_DATE, JOURNEY_AMOUNT, JOURNEY_PICTURE));
        list.add(new Journey(JOURNEY_NAME2, new Location(JOURNEY_FROM), new Location(JOURNEY_TO), new Tenant(TENANT_NAME, TENANT_PASSWORD, ""), FROM_DATE, TO_DATE, JOURNEY_AMOUNT, JOURNEY_PICTURE));
        return list;
    }    
}