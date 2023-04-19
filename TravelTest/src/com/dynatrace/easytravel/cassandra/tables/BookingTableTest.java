package com.dynatrace.easytravel.cassandra.tables;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.cassandra.base.BookingLocationType;
import com.dynatrace.easytravel.cassandra.tables.cql.CountersTable;
import com.dynatrace.easytravel.cassandra.tables.objectmapper.BookingTable;
import com.dynatrace.easytravel.jpa.business.Booking;
import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.jpa.business.User;

@Ignore
public class BookingTableTest extends CassandraTableTestUtil {

    public static final int JOURNEYS_NUBER = 10;
    public static final int TENANTS_NUBER = 2;
    public static final int USERS_NUBER = 2;
    public static final int JOURNEY_BOOKINGS = 5;
    public static final int TOTAL_BOOKINGS = JOURNEYS_NUBER * USERS_NUBER * TENANTS_NUBER * JOURNEY_BOOKINGS;
    public static final int USER_BOOKINGS = JOURNEYS_NUBER * TENANTS_NUBER * JOURNEY_BOOKINGS;
    public static final int TENANT_BOOKINGS = JOURNEYS_NUBER * USERS_NUBER * JOURNEY_BOOKINGS;
    public static final int TENANT_DESTINATION_COUNT = USERS_NUBER * JOURNEY_BOOKINGS;
    public static final int TENANT_DEPARTURE_COUNT = TENANT_DESTINATION_COUNT;

    List<Journey> journeys;
    List<Booking> bookings;
    Map<String,Booking> idToBooking;

    CountersTable counters = new CountersTable(getCluster());
    BookingTable bookingTable = new BookingTable(getCluster(), counters);

    public BookingTableTest() {
        setCassandraObjects(bookingTable, counters);
    }

    @Before
    public void addBookings() {
        assertThat(bookingTable.getCount(), is(0));
        createBookings();
        assertThat(bookingTable.getCount(), is(TOTAL_BOOKINGS));
    }

    private void createBookings() {
        createJourneys();
        bookings = new ArrayList<>();
        idToBooking = new HashMap<>();
        for (int i=0; i<USERS_NUBER; i++) {
            String user = getUser(i);
            journeys.forEach(journey -> {
                createJourneyBookings(journey, user, JOURNEY_BOOKINGS);
            });
        }
    }

    private void createJourneys() {
        JourneyTestUtil journeyTestUtil = new JourneyTestUtil();
        journeys = new ArrayList<>();
        for(int i=0; i<TENANTS_NUBER; i++ ) {
            journeys.addAll(journeyTestUtil.createJourneys(JOURNEYS_NUBER, getTenant(i)));
        }
    }

    private void createJourneyBookings(Journey journey, String userName, int cnt) {
        User user = new User(userName);
        for(int i=0; i<cnt; i++) {
            Booking booking = new Booking(null, journey, user, getRandomDate());
            bookingTable.addModel(booking);
            booking.getJourney().setFromDate(null);
            booking.getJourney().setToDate(null);
            bookings.add(booking);
            idToBooking.put(booking.getId(), booking);
        }
    }

    private Date getRandomDate() {
        int year = getRandomInt(2015, 2020);
        int month = getRandomInt(0,11);
        int day  = getRandomInt(0,31);
        int hour = getRandomInt(0,23);
        int minute = getRandomInt(0,59);
        Calendar calendar = new GregorianCalendar(year, month, day, hour, minute);
        return calendar.getTime();
    }

    private int getRandomInt(int min, int max) {
        return  ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    @Test
    public void getBookingById() {
        bookings.forEach(booking -> {
            assertThat(bookingTable.getModel(booking.getId()), is(equalTo(booking)));
        });
    }

    @Test
    public void testCounters() {
        for (int i=0; i<USERS_NUBER; i++) {
            String user = getUser(i);
            assertThat(counters.getBookingCountForUser(user), is(USER_BOOKINGS));
        }
        for (int i=0; i<TENANTS_NUBER; i++) {
            String tenant = getTenant(i);
            assertThat(counters.getBookingCountForTenant(tenant), is(TENANT_BOOKINGS));
            double tenantSales = getSalesForTenant(tenant);
            assertThat(counters.getSalesAmountForTenant(tenant), is(tenantSales));
        }

        journeys.stream().forEach(journey -> {
            String tenant = journey.getTenant().getName();
            String destination = journey.getDestination().getName();
            String departure = journey.getStart().getName();
            assertThat(counters.getDestinationCountForTenant(tenant, destination), is(TENANT_DESTINATION_COUNT));
            assertThat(counters.getDepatureCountForTenant(tenant, departure), is(TENANT_DEPARTURE_COUNT));
        });
    }

    private String getTenant(int i) {
        return "tenant" + i;
    }

    private String getUser(int i) {
        return "user" + i;
    }

    private double getSalesForTenant(String tenant) {
        return bookings.stream()
                .filter(booking -> tenant.equals(booking.getJourney().getTenant().getName()))
                .mapToDouble(booking -> booking.getJourney().getAmount())
                .sum();
    }

    @Test
    public void getBookingsByUserNameTest() {
        for(int i=0; i<USERS_NUBER; i++) {
            String userName = getUser(i);
            Collection<Booking> bookings = bookingTable.getBookingsByUserName(userName);
            assertThat(bookings.size(), is(Math.min(USER_BOOKINGS, BookingTable.MAX_USER_BOOKINGS)));
            bookings.forEach( booking -> {
                Booking orgBooking = findBooking(booking.getId());
                assertThat(booking, is(equalTo(orgBooking)));
            });
        }
    }

    private Booking findBooking(String id) {
        return bookings.stream()
                .filter( booking -> id.equals(booking.getId()))
                .findFirst()
                .get();
    }

    @Test
    public void getBookingsByTenantTest() {
        for(int i=0; i<TENANTS_NUBER; i++) {
            List<Booking> bookingsByTenant = bookingTable.getBookingsByTenant(getTenant(i));
            assertThat(bookingsByTenant.size(), is(TENANT_BOOKINGS));
        }
    }

    @Test
    public void getTotalSalesByTenantTest() {
        for( int i=0; i<TENANTS_NUBER; i++ ) {
            String tenant = getTenant(i);
            assertThat(bookingTable.getTotalSalesByTenant(tenant), is(getSalesForTenant(tenant)));
        }
    }

    @Test
    public void getBookingCountForTenantTest() {
        for( int i=0; i<TENANTS_NUBER; i++ ) {
            String tenant = getTenant(i);
            assertThat(bookingTable.getBookingCountForTenant(tenant), is(TENANT_BOOKINGS));
        }
    }

    @Test
    public void getDestinationsByTenantTest() {
        getLocationsByTenantTest(BookingLocationType.DESTINATION_LOCATION);
    }

    @Test
    public void getDeparturesByTenantTest() {
        getLocationsByTenantTest(BookingLocationType.DEPARTURE_LOCATION);
    }

    private void getLocationsByTenantTest(BookingLocationType locationType) {
        for( int i=0; i<TENANTS_NUBER; i++) {
            String tenantName = getTenant(i);
            Map<Location, Integer> locationCount;
            if(locationType == BookingLocationType.DESTINATION_LOCATION) {
                locationCount = bookingTable.getDestinationsByTenant(tenantName, 10);
            } else {
                locationCount = bookingTable.getDeparturesByTenant(tenantName, 10);
            }
            assertThat(locationCount.size(), is(lessThanOrEqualTo(10)));
            locationCount
                    .entrySet()
                    .forEach( entry -> {
                        assertThat(entry.getValue(), is(TENANT_DESTINATION_COUNT));
                    });
        }
    }

    @Test
    public void testDeparturesAndDestinationsByTenant() {
        String[] departures = {"dep0", "dep1", "dep2", "dep3", "dep4"};
        String[] destinations = {"dest0", "dest1", "dest2", "dest3", "dest4"};
        Journey[] journeys = new Journey[5];
        int[] journeyBookings = new int[5];

        JourneyTestUtil journeyTestUtil = new JourneyTestUtil();

        int bookingCnt = 5;
        for(int i=0; i<5; i++) {
            journeys[i] = journeyTestUtil.getJourney("name" + i, departures[i], destinations[i], null, null, "tenant3");
            createJourneyBookings(journeys[i], "user3", bookingCnt);
            journeyBookings[i] = bookingCnt;
            bookingCnt += 5;
        }

        Map<Location, Integer>  destinationCount = bookingTable.getDestinationsByTenant("tenant3", 3);
        Map<Location, Integer>  departuresCount = bookingTable.getDeparturesByTenant("tenant3", 3);

        assertThat(destinationCount.size(), is(3));
        assertThat(departuresCount.size(), is(3));

        for(int i=4; i>2; i--) {
            Location destLoc = new Location(destinations[i]);
            Location depLoc = new Location((departures[i]));
            assertThat(destinationCount.get(destLoc), is(journeyBookings[i]));
            assertThat(departuresCount.get(depLoc), is(journeyBookings[i]));
        }


    }

    @Test
    public void getBookingCountForUserTest() {
        Journey j = journeys.get(0);
        createJourneyBookings(j, "testUser", 15);
        assertThat(bookingTable.getBookingCountForUser("testUser"), is(15));
        for(int i=0; i<USERS_NUBER; i++) {
            assertThat(bookingTable.getBookingCountForUser(getUser(i)), is(USER_BOOKINGS));
        }
    }

    @Test
    public void getBookingsByTenantFromToTest() {
        for(int i=0; i<TENANTS_NUBER; i++) {
            Collection<Booking> page1 = bookingTable.getBookingsByTenant(getTenant(i), 0, 5);
            Collection<Booking> page2 = bookingTable.getBookingsByTenant(getTenant(i), 5, 5);
            Collection<Booking> page12 = bookingTable.getBookingsByTenant(getTenant(i), 0, 10);
            assertThat(page1.size(), is(5));
            assertThat(page2.size(), is(5));
            assertThat(page12.size(), is(10));
            page12.removeAll(page1);
            page12.removeAll(page2);
            assertThat(page12.size(), is(0));
        }
    }

    @Test
    public void getBookingIdsExcludingUserTest() {
        String userName1 = getUser(0);
        String userName2 = getUser(1);
        Collection<String> bookingIdsExcludingUser = bookingTable.getBookingIdsExcludingUser(userName1, 10);
        bookingIdsExcludingUser.forEach( bookingId -> {
            assertThat(idToBooking.get(bookingId).getUser().getName(), not(userName1));
            assertThat(idToBooking.get(bookingId).getUser().getName(), is(userName2));
        });
    }

    @Test
    public void removeBookingByIdTest() {
        bookings.stream().forEach( booking -> bookingTable.removeBookingById(booking.getId()));
        assertThat(bookingTable.getCount(), is(0));
    }

    @Test
    public void getBookingCountExcludingUser() {
        Journey j = journeys.get(0);
        createJourneyBookings(j, "testUser", 15);
        assertThat(bookingTable.getCount(), is(TOTAL_BOOKINGS + 15));
        assertThat(bookingTable.getBookingCountExcludingUser("testUser"), is(TOTAL_BOOKINGS));
    }

}