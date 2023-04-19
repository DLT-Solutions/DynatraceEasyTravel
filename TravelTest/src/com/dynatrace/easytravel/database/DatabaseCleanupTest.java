package com.dynatrace.easytravel.database;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.jpa.business.Booking;
import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.jpa.business.LoginHistory;
import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.spring.SpringUtils;
import com.dynatrace.easytravel.utils.TestHelpers;
import com.dynatrace.easytravel.utils.ThreadTestHelper;

import ch.qos.logback.classic.Level;

public class DatabaseCleanupTest extends DatabaseWithContent {

	private static final Logger log = LoggerFactory.make();

	// we keep 5000, therefore create more than that to ensure that the
	// cleanup-code is entered
	private static final int CREATE_COUNT = 600;



	@Test
	public void testExecute() throws Exception {
		log.info("Starting database cleanup procedure");
		DatabaseCleanup cleanup = new DatabaseCleanup();
		cleanup.setMaxToKeep(500);
		cleanup.setExtensionPoint(new String[] { PluginConstants.BACKEND_JOURNEY_SEARCH });
		cleanup.setEnabled(true);

		cleanup.execute(PluginConstants.BACKEND_JOURNEY_SEARCH, database);
		log.info("Done cleaning database");

		// second time, this time it should not execute anything
		log.info("Starting second database cleanup procedure");
		cleanup.execute(PluginConstants.BACKEND_JOURNEY_SEARCH, database);
		log.info("Done cleaning database");
	}



	@Test
	public void testExecuteFullBookings() throws Exception {
		DataAccess access = createNewAccess();

		try {
			createBookings(access, "hainer");
			log.info("Starting database cleanup procedure");
			DatabaseCleanup cleanup = new DatabaseCleanup();
			cleanup.setMaxToKeep(500);
			cleanup.setExtensionPoint(new String[] { PluginConstants.BACKEND_JOURNEY_SEARCH });
			cleanup.setEnabled(true);
			cleanup.execute(PluginConstants.BACKEND_JOURNEY_SEARCH, database);
			log.info("Done cleaning database");
		} finally {
			access.close();
		}
	}

	@Test
	public void testExecuteFullLoginHistory() throws Exception {
		DataAccess access = createNewAccess();

		try {
			createLoginHistory(access, "peter");
			log.info("Starting database cleanup procedure");
			DatabaseCleanup cleanup = new DatabaseCleanup();
			cleanup.setMaxToKeep(500);
			cleanup.setExtensionPoint(new String[] { PluginConstants.BACKEND_JOURNEY_SEARCH });
			cleanup.setEnabled(true);
			cleanup.execute(PluginConstants.BACKEND_JOURNEY_SEARCH, database);
			log.info("Done cleaning database");
		} finally {
			access.close();
		}
	}

	@Test
	public void testOtherPluginLocation() {
		// nothing happens on other plugin points
		DatabaseCleanup cleanup = new DatabaseCleanup();
		cleanup.setMaxToKeep(500);
		cleanup.setExtensionPoint(new String[] { PluginConstants.BACKEND_JOURNEY_SEARCH });
		cleanup.setEnabled(true);
		cleanup.execute(PluginConstants.BACKEND_JOURNEY_ADD);
	}

	@Test
	public void testWithDifferentLoglevel() {
		final AtomicReference<Exception> exception = new AtomicReference<Exception>(null);
		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {

			@Override
			public void run() {
				try {
					testExecute();
				} catch (Exception e) {
					exception.set(e);
				}
			}
		}, DatabaseCleanup.class.getName(), Level.DEBUG);

		assertNull("Did not expect exception, but had: " + exception.get(), exception.get());
	}

	@Test
	public void testWithSpringFactory() {
		System.setProperty(
				"com.dynatrace.easytravel.propertiesfile",
				Thread.currentThread().getContextClassLoader().getResource(EasyTravelConfig.PROPERTIES_FILE + ".properties").toString());
		System.setProperty(BaseConstants.SystemProperties.PERSISTENCE_MODE, BaseConstants.BusinessBackend.Persistence.JPA);
		SpringUtils.initBusinessBackendContext();
		try {
			DatabaseCleanup cleanup = new DatabaseCleanup();
			cleanup.setExtensionPoint(new String[] { PluginConstants.BACKEND_JOURNEY_SEARCH });
			cleanup.setEnabled(true);
			cleanup.execute(PluginConstants.BACKEND_JOURNEY_SEARCH);
		} finally {
			SpringUtils.disposeBusinessBackendContext();
		}
	}

	private static final int NUMBER_OF_THREADS = 10;
	private static final int NUMBER_OF_TESTS = 10;

	@Test
	public void testExecuteFullBookingsThreaded() throws Throwable {
		final DataAccess dataAccess = createNewAccess();

		try {
			createBookings(dataAccess, "hainer");
			createLoginHistory(dataAccess, "maria");

			ThreadTestHelper helper =
					new ThreadTestHelper(NUMBER_OF_THREADS, NUMBER_OF_TESTS);

			helper.executeTest(new ThreadTestHelper.TestRunnable() {

				@Override
				public void doEnd(int threadnum) throws Exception {
					// do stuff at the end ...
				}

				@Override
				public void run(int threadnum, int iter) throws Exception {
					// do the actual threaded work ...
					log.info("Starting database cleanup procedure");
					DatabaseCleanup cleanup = new DatabaseCleanup();
					cleanup.setMaxToKeep(500);
					cleanup.setExtensionPoint(new String[] { PluginConstants.BACKEND_JOURNEY_SEARCH });
					cleanup.setEnabled(true);
					cleanup.execute(PluginConstants.BACKEND_JOURNEY_SEARCH, database);
					log.info("Done cleaning database");
				}
			});
		} finally {
			dataAccess.close();
		}
	}


	@Test
	public void testSkipCleanupForMonica() {
		DataAccess access = createNewAccess();
		try {
			createBookings(access, "monica");
			createLoginHistory(access, "monica");
			log.info("Starting database cleanup procedure");
			DatabaseCleanup cleanup = new DatabaseCleanup();
			cleanup.setMaxToKeep(500);
			cleanup.setExtensionPoint(new String[] { PluginConstants.BACKEND_JOURNEY_SEARCH });
			cleanup.setEnabled(true);
			cleanup.execute(PluginConstants.BACKEND_JOURNEY_SEARCH, database);
			int count = access.getBookingCountByUser("monica");
			assertTrue("shouldn't have deleted monicas bookings", count >= CREATE_COUNT);

			int loginCount = access.getLoginCountForUser("monica");
			assertTrue("shouldn't have deleted monicas login history", loginCount >= CREATE_COUNT);


		} finally {
			access.close();
		}
	}

	private void createBookings(DataAccess dataAccessX, String userName) {
		DataAccess dataAccess = createNewAccess();
		try {
			User user = dataAccess.getUser(userName);
			assertNotNull("Need to find user '" + userName + "'", user);

			// Journey journey = em.find(Journey.class, 1);
			Collection<Journey> journeys = dataAccess.findJourneys("New York", new Date(0), new Date(33333333333333l), false);

			assertTrue("Should find at least one Journey for New York", journeys.size() > 0);

//			assertNotNull("Need to find journey with id 1", journeys.get(0));

			Journey journey = journeys.iterator().next();
			dataAccess.startTransaction();
			for (int i = 0; i < CREATE_COUNT; i++) {
				String id = UUID.randomUUID().toString();
				dataAccess.addBooking(new Booking(id, journey, user, new Date()));
			}
			dataAccess.commitTransaction();
		} finally {
			dataAccess.close();
		}
	}


	private void createLoginHistory(DataAccess dataAccessX, String userName) {
		System.out.println("+++++++++ create a new login history"); // NOSONAR
		DataAccess dataAccess = createNewAccess();
		try {
			User user = dataAccess.getUser(userName);
			assertNotNull("Need to find user '" + userName + "'", user);

			dataAccess.startTransaction();
			long t = System.currentTimeMillis();
			for (int i = 0; i < CREATE_COUNT; i++) {
				dataAccess.addLoginHistory(new LoginHistory(user, new Date(t+i*1000)));
			}
			dataAccess.commitTransaction();
			System.out.println("+++++++++ creating a new login history finished"); // NOSONAR
		} finally {
			dataAccess.close();
		}
	}
}
