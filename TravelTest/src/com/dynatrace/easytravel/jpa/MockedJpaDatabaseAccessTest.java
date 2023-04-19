package com.dynatrace.easytravel.jpa;

import static com.dynatrace.easytravel.MiscConstants.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.dynatrace.easytravel.jpa.business.*;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.model.GenericDataAccess;
import com.dynatrace.easytravel.model.LoyaltyStatus;
import com.dynatrace.easytravel.persistence.JpaBusinessController;
import com.dynatrace.easytravel.persistence.JpaDatabaseController;
import com.dynatrace.easytravel.utils.TestHelpers;
@RunWith(MockitoJUnitRunner.class)
public class MockedJpaDatabaseAccessTest {

	private DataAccess access;

	@Mock
	EntityManager emMock;

	@Mock
	Query emQuery;

	@SuppressWarnings("rawtypes")
	@Mock
	TypedQuery queryMock;


	@Before
	public void setUp() {
		access = getDatabaseAccess();
	}


	@Test
	public void testAllQueries() {
		when(emMock.createNamedQuery(QueryNames.JOURNEY_ALL)).thenReturn(queryMock);
		when(queryMock.setHint("org.hibernate.cacheable", true)).thenReturn(queryMock);
		when(queryMock.getResultList()).thenReturn(new ArrayList<Journey>());

		when(emMock.createNamedQuery(QueryNames.USER_ALL)).thenReturn(queryMock);
		when(queryMock.setHint("org.hibernate.cacheable", true)).thenReturn(queryMock);
		when(queryMock.getResultList()).thenReturn(new ArrayList<User>());

		when(emMock.createNamedQuery(QueryNames.TENANT_ALL)).thenReturn(queryMock);
		when(queryMock.setHint("org.hibernate.cacheable", true)).thenReturn(queryMock);
		when(queryMock.getResultList()).thenReturn(new ArrayList<Tenant>());

		when(emMock.createNamedQuery(QueryNames.LOCATION_ALL)).thenReturn(queryMock);
		when(queryMock.setHint("org.hibernate.cacheable", true)).thenReturn(queryMock);
		when(queryMock.getResultList()).thenReturn(new ArrayList<Location>());

		DataAccess jpaDatabaseAccess = getDatabaseAccess();

		jpaDatabaseAccess.allJourneys();
		jpaDatabaseAccess.allUsers();
		jpaDatabaseAccess.allTenants();
		jpaDatabaseAccess.allLocations();

		verify(emMock).createNamedQuery(QueryNames.JOURNEY_ALL);
		verify(emMock).createNamedQuery(QueryNames.USER_ALL);
		verify(emMock).createNamedQuery(QueryNames.TENANT_ALL);
		verify(emMock).createNamedQuery(QueryNames.LOCATION_ALL);
	}

	/**
	 *
	 * @return
	 * @author stefan.moschinski
	 */
	private DataAccess getDatabaseAccess() {
		DataAccess jpaDatabaseAccess = new GenericDataAccess(new JpaBusinessController(new JpaDatabaseController(emMock)));
		return jpaDatabaseAccess;
	}
//
//
//	@Test
//	public void testCreateLocation() {
//		EntityManager emMock = createMock(EntityManager.class);
//
//		when(emMock.find(Location.class, LOCATION_NAME1)).thenReturn(null);
//		emMock.persist(cmp(new Location(LOCATION_NAME1), new Comparator<Location>() {
//
//			@Override
//			public int compare(Location o1, Location o2) {
//				return o1.getName().compareTo(o2.getName());
//			}
//		}, LogicalOperator.EQUAL));
//		expectLastCall();
//
//		replay(emMock);
//
//		JpaDatabaseAccess jpaDatabaseAccess = new JpaDatabaseAccess();
//		jpaDatabaseAccess.setEntityManager(emMock);
//
//		assertNull(jpaDatabaseAccess.createLocation(LOCATION_NAME1));
//
//		verify(emMock);
//	}
//
//
	@SuppressWarnings({ "unchecked" })
	@Test
	public void testFindBookings() {
		when(emMock.createNamedQuery(QueryNames.BOOKING_GET, Booking.class)).thenReturn(queryMock);
		when(queryMock.setHint("org.hibernate.cacheable", true)).thenReturn(queryMock);
		when(queryMock.setParameter("username", USER_NAME)).thenReturn(queryMock);

		List<Booking> bookings = new ArrayList<Booking>();

		assertThat(
				emMock.createNamedQuery(QueryNames.BOOKING_GET, Booking.class).setParameter("username", "user_name").getResultList(),
				is(bookings));
		assertNotNull(emMock.createNamedQuery(QueryNames.BOOKING_GET, Booking.class).setParameter("username", "user_name").getResultList());
		new JpaBusinessController(new JpaDatabaseController(emMock));
		DataAccess jpaDatabaseAccess = getDatabaseAccess();

		assertEquals(bookings, jpaDatabaseAccess.findBookings(USER_NAME));

	}
//
//	EntityManager emMock = createMock(EntityManager.class);
//	@SuppressWarnings("unchecked")
//	TypedQuery<Journey> queryMock = createMock(TypedQuery.class);
//
	@Test
	public void testCreateJourney() {
		Location departure = new Location(LOCATION_NAME1);
		Location destination = new Location(LOCATION_NAME2);
		Tenant tenant = new Tenant(TENANT_NAME, TENANT_PASSWORD, null);

		prepareJourneyMock(LOCATION_NAME1, departure, LOCATION_NAME2, destination, TENANT_NAME, tenant, JOURNEY_NAME,
				new ArrayList<Journey>(), true, 1);

		DataAccess databaseAccess = getDatabaseAccess();

		Journey created = databaseAccess.createJourney(JOURNEY_NAME, LOCATION_NAME1, LOCATION_NAME2, TENANT_NAME, FROM_DATE,
				TO_DATE, AMOUNT, null);
		assertEquals(departure, created.getStart());
		assertEquals(destination, created.getDestination());
		assertEquals(tenant, created.getTenant());
		assertEquals(FROM_DATE, created.getFromDate());
		assertEquals(TO_DATE, created.getToDate());
		assertEquals(AMOUNT, created.getAmount(), 0.0);

		verifyJourneyMock(LOCATION_NAME1, departure, LOCATION_NAME2, destination, TENANT_NAME, tenant, JOURNEY_NAME,
				new ArrayList<Journey>(), true, 1);
	}

	@SuppressWarnings("unchecked")
	private void prepareJourneyMock(String start, Location departure, String dest, Location destination, String tenantName,
			Tenant tenant, String journey, List<Journey> result, boolean persisted, int expectCount) {
		when(emMock.find(Location.class, start)).thenReturn(departure);
		when(emMock.find(Location.class, dest)).thenReturn(destination);
		when(emMock.find(Tenant.class, tenantName)).thenReturn(tenant);
		when(emMock.createNamedQuery(QueryNames.JOURNEY_GET, Journey.class)).thenReturn(queryMock);
		when(queryMock.setHint("org.hibernate.cacheable", true)).thenReturn(queryMock);
		when(queryMock.setParameter("name", journey)).thenReturn(queryMock);
		when(queryMock.getResultList()).thenReturn(result);
	}

	private void verifyJourneyMock(String start, Location departure, String dest, Location destination, String tenantName,
			Tenant tenant, String journey, List<Journey> result, boolean persisted, int expectCount) {
		verify(emMock).find(Location.class, start);
		verify(emMock, atMost(expectCount)).find(Location.class, dest);
		verify(emMock, atMost(expectCount)).find(Tenant.class, tenantName);
		verify(emMock, atMost(expectCount)).createNamedQuery(QueryNames.JOURNEY_GET, Journey.class);
		verify(queryMock, atMost(expectCount)).setParameter("name", journey);
		verify(queryMock, atMost(expectCount)).getResultList();
	}

	@Test
	public void testCreateJourneyWithZeroSizePicture() {
		Location departure = new Location(LOCATION_NAME1);
		Location destination = new Location(LOCATION_NAME2);
		Tenant tenant = new Tenant(TENANT_NAME, TENANT_PASSWORD, null);

		prepareJourneyMock(LOCATION_NAME1, departure, LOCATION_NAME2, destination, TENANT_NAME, tenant, JOURNEY_NAME,
				new ArrayList<Journey>(), true, 1);

		DataAccess access = getDatabaseAccess();

		// with null-picture
		Journey created = access.createJourney(JOURNEY_NAME, LOCATION_NAME1, LOCATION_NAME2, TENANT_NAME, FROM_DATE,
				TO_DATE, AMOUNT, new byte[] {});
		assertEquals(departure, created.getStart());
		assertEquals(destination, created.getDestination());
		assertEquals(tenant, created.getTenant());
		assertEquals(FROM_DATE, created.getFromDate());
		assertEquals(TO_DATE, created.getToDate());
		assertEquals(AMOUNT, created.getAmount(), 0.0);
	}

	@Test
	public void testCreateJourneyException1() {
		Location destination = new Location(LOCATION_NAME2);
		Tenant tenant = new Tenant(TENANT_NAME, TENANT_PASSWORD, null);

		prepareJourneyMock("notexists", null, LOCATION_NAME2, destination, TENANT_NAME, tenant, JOURNEY_NAME,
				new ArrayList<Journey>(), false, 0);


		DataAccess jpaDatabaseAccess = getDatabaseAccess();
		try {
			jpaDatabaseAccess.createJourney(JOURNEY_NAME, "notexists", LOCATION_NAME2, TENANT_NAME, FROM_DATE, TO_DATE, AMOUNT,
					null);
			fail("Should throw Exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "Could not find location", "notexists");
		}
	}

	@Test
	public void testCreateJourneyException2() {
		Location departure = new Location(LOCATION_NAME1);
		Tenant tenant = new Tenant(TENANT_NAME, TENANT_PASSWORD, null);

		prepareJourneyMock(LOCATION_NAME1, departure, "notexists", null, TENANT_NAME, tenant, JOURNEY_NAME,
				new ArrayList<Journey>(), false, 0);

		DataAccess jpaDatabaseAccess = getDatabaseAccess();
		try {
			jpaDatabaseAccess.createJourney(JOURNEY_NAME, LOCATION_NAME1, "notexists", TENANT_NAME, FROM_DATE, TO_DATE, AMOUNT,
					null);
			fail("Should throw Exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "Could not find location", "notexists");
		}
	}

	@Test
	public void testCreateJourneyException3() {
		Location departure = new Location(LOCATION_NAME1);
		Location destination = new Location(LOCATION_NAME2);

		prepareJourneyMock(LOCATION_NAME1, departure, LOCATION_NAME2, destination, "notexists", null, JOURNEY_NAME,
				new ArrayList<Journey>(), false, 0);


		DataAccess jpaDatabaseAccess = getDatabaseAccess();

		try {
			jpaDatabaseAccess.createJourney(JOURNEY_NAME, LOCATION_NAME1, LOCATION_NAME2, "notexists", FROM_DATE, TO_DATE,
					AMOUNT, null);
			fail("Should throw Exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "Could not find tenant", "notexists");
		}
	}


	@SuppressWarnings({ "unchecked" })
	@Test
	public void testFindLocations() {
		DataAccess jpaDatabaseAccess = getDatabaseAccess();
		assertNotNull(jpaDatabaseAccess);

		List<Location> locations = new ArrayList<Location>();
		Location location1 = new Location(LOCATION_NAME1);
		Location location2 = new Location(LOCATION_NAME2);
		locations.add(location1);
		locations.add(location2);
		// without journeys
		when(emMock.createNamedQuery(QueryNames.LOCATION_FIND, Location.class)).thenReturn(queryMock);
		when(queryMock.setHint("org.hibernate.cacheable", true)).thenReturn(queryMock);
		when(queryMock.setParameter("name", LOCATION_NAME1.toLowerCase())).thenReturn(queryMock);
		when(queryMock.getResultList()).thenReturn(locations);
		// with journeys
		when(emMock.createNamedQuery(QueryNames.LOCATION_FIND_WITH_JOURNEYS, Location.class)).thenReturn(queryMock);
		when(queryMock.setHint("org.hibernate.cacheable", true)).thenReturn(queryMock);
		when(queryMock.setParameter("name", LOCATION_NAME1.toLowerCase())).thenReturn(queryMock);
		when(queryMock.getResultList()).thenReturn(locations);
		// with slow journeys
		when(emMock.createNamedQuery(QueryNames.LOCATION_FIND, Location.class)).thenReturn(queryMock);
		when(queryMock.setHint("org.hibernate.cacheable", true)).thenReturn(queryMock);
		when(queryMock.setParameter("name", LOCATION_NAME1.toLowerCase())).thenReturn(queryMock);
		when(queryMock.getResultList()).thenReturn(locations);
		Journey journey = new Journey();
		List<Journey> journeys = new ArrayList<Journey>();
		journeys.add(journey);

		for (Location location : locations) {
			when(emMock.createNamedQuery(QueryNames.JOURNEY_FIND_BY_LOCATION_DEST, Journey.class)).thenReturn(queryMock);
			when(queryMock.setHint("org.hibernate.cacheable", true)).thenReturn(queryMock);
			when(queryMock.setParameter("destination", location)).thenReturn(queryMock);
			when(queryMock.getResultList()).thenReturn(journeys);
		}
	}


	@Test
	public void testCreateAndUpdateUser() {
		DataAccess jpaDatabaseAccess = new GenericDataAccess(new JpaBusinessController(new JpaDatabaseController(
				emMock)));
		User user = new User(USER_NAME, USER_FULLNAME, USER_PASSWORD);

		when(emMock.find(User.class, USER_NAME)).thenReturn(null);
		when(emMock.merge(user)).thenReturn(user);

		// user found
		when(emMock.find(User.class, USER_NAME)).thenReturn(user);


		jpaDatabaseAccess.addUser(user);
		user.setLoyaltyStatus(LoyaltyStatus.Gold.name());
		jpaDatabaseAccess.addUser(user);

		access.updateUser(user);
	}


	@Test
	public void testCreateAndUpdateTenant() {
		DataAccess jpaDatabaseAccess = new GenericDataAccess(new JpaBusinessController(new JpaDatabaseController(
				emMock)));
		Tenant tenant = new Tenant(TENANT_NAME, TENANT_PASSWORD, TENANT_DESCRIPTION);

		// user not found
		when(emMock.find(Tenant.class, TENANT_NAME)).thenReturn(null);
		when(emMock.merge(tenant)).thenReturn(tenant);

		// user found
		when(emMock.find(Tenant.class, TENANT_NAME)).thenReturn(tenant);

		jpaDatabaseAccess.createTenant(TENANT_NAME, TENANT_PASSWORD, TENANT_DESCRIPTION);
		jpaDatabaseAccess.createTenant(TENANT_NAME, TENANT_PASSWORD, TENANT_DESCRIPTION);

		access.updateTenant(tenant);
	}



	@Test
	public void testVerifyLocation() {
		// user not found
		when(emMock.createNativeQuery(anyString())).thenReturn(emQuery);
		when(emQuery.setParameter(anyInt(), anyInt())).thenReturn(emQuery);
		when(emQuery.executeUpdate()).thenReturn(1);

		access.verifyLocation(1);
	}





//	@Test
//	@Ignore("do not run this, it fails because of duplicate inserts/missed deletes")
//	public void testAddDeleteLocationInThreadsMultipleDataAccess() throws Throwable {
//		ThreadTestHelper helper =
//				new ThreadTestHelper(NUMBER_OF_THREADS, NUMBER_OF_TESTS);
//
//		helper.executeTest(new TestRunnableImplementation());
//	}



//	private final class TestRunnableImplementation implements ThreadTestHelper.TestRunnable {
//
//		private final JpaDatabaseAccess[] access = new JpaDatabaseAccess[NUMBER_OF_THREADS];
//		private final EntityManager[] em = new EntityManager[NUMBER_OF_THREADS];
//
//		private TestRunnableImplementation() {
//			for (int i = 0; i < NUMBER_OF_THREADS; i++) {
//				this.em[i] = createEntityManager();
//				this.access[i] = new JpaDatabaseAccess();
//				this.access[i].setEntityManager(em[i]);
//			}
//		}
//
//		@Override
//		public void doEnd(int threadnum) throws Exception {
//			em[threadnum].close();
//		}
//
//		@Override
//		public void run(int threadnum, int iter) throws Exception {
//			em[threadnum].getTransaction().begin();
//			try {
//				if (iter % 2 == 0) {
//					assertTrue(access[threadnum].addLocation("testlocation"));
//				} else {
//					assertTrue(access[threadnum].deleteLocation("testlocation"));
//				}
//			} finally {
//				em[threadnum].getTransaction().commit();
//			}
//		}
//	}
}
