package com.dynatrace.easytravel.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cache.spi.Region;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.database.DatabaseBase;
import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.persistence.Database;
import com.dynatrace.easytravel.persistence.JpaBusinessController;
import com.dynatrace.easytravel.persistence.JpaDatabaseController;
import com.dynatrace.easytravel.persistence.SqlDatabase;


public class HibernateCacheTest /* don't do this here! extends DatabaseBase */ {
	static final Logger log = LoggerFactory.make();

	private static final String[] LOCATION_NAMES = { "New York", "Paris" };

	private Session session; // the Hibernate session
	private Statistics statistics; // Hibernate cache statistics
	private Database sqlDb;

	private JpaDatabaseController delegateController;

	@BeforeClass
	public static void setUpContent() throws IOException {
		// use a different ehcache.xml file so that we have enough cache-entries to
		// store all locations, we have more than 20k cities in the Cities.txt file and thus
		// would get unpredictable results if using 10k as maxInMemory...
		System.setProperty("net.sf.ehcache.configurationResourceName", "ehcache-test.xml");

		System.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.SingletonEhCacheProvider");
		System.setProperty("hibernate.cache.use_query_cache", "true");
		System.setProperty("hibernate.cache.use_second_level_cache", "true");
		System.setProperty("hibernate.generate_statistics", "true");

		DatabaseBase.setUpClass();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		DatabaseBase.tearDownClass();
	}

	@Before
	public void init() throws IOException {
		// we use a separate Factory here because we want fresh statistics for every test, although this is quite time consuming
		sqlDb = new SqlDatabase();

		// populate database
		log.info("Start filling the database with content.");
		sqlDb.initialize(sqlDb.getBusinessController()).createContents(EasyTravelConfig.read(), /* randomContent */ false);
		log.info("Done filling the database with content.");

		// creating access members
		delegateController = (JpaDatabaseController) ((JpaBusinessController) sqlDb.createNewBusinessController()).getDelegateController();

		session = (Session) delegateController.getEm().getDelegate();

		statistics = session.getSessionFactory().getStatistics();

		// verify that one of the items is still in the cache to catch problem early
		checkLocationInCache();
	}
	
	@Test
	public void simpleTest() {
		checkLocationInCache();
	}

	private void checkLocationInCache() {
		SessionFactory sessionFactory = session.getSessionFactory();		

		Region secondLevelCacheRegion = ((SessionFactoryImpl)sessionFactory).getSecondLevelCacheRegion("com.dynatrace.easytravel.jpa.business.Location");
		assertNotNull(secondLevelCacheRegion);				

		EntityPersister p = ((SessionFactoryImpl) sessionFactory).getEntityPersister( "com.dynatrace.easytravel.jpa.business.Location" );
		assertTrue(p.hasCache());
		EntityRegionAccessStrategy cacheRegion = p.getCacheAccessStrategy();
		Object key = cacheRegion.generateCacheKey( "New York", p, (SessionFactoryImplementor)sessionFactory, null ); // have to assume non tenancy		
		assertTrue("Expect to have 'New York' in the cache now, but did not find it! " +
				"SizeInMemory: " + secondLevelCacheRegion.getSizeInMemory() +
				" ElementsInMemory: " + secondLevelCacheRegion.getElementCountInMemory() +
				" ElementsOnDisk: " + secondLevelCacheRegion.getElementCountOnDisk() +
				" Timeout: " + secondLevelCacheRegion.getTimeout(),secondLevelCacheRegion.contains(key));
		
		Object key1 = cacheRegion.generateCacheKey( "xxx", p, (SessionFactoryImplementor)sessionFactory, null ); // have to assume non tenancy
		assertFalse(secondLevelCacheRegion.contains(key1));
		
		assertTrue(((SessionFactoryImpl)sessionFactory).getCache().containsEntity("com.dynatrace.easytravel.jpa.business.Location", "New York"));
	}

	@After
	public void exit() {
		/*if(session != null) {
			session.close();
		}*/
		if(delegateController != null) {
			delegateController.close();
		}
		if(sqlDb != null) {
			sqlDb.closeConnection();
		}
	}

	@Test
	public void testSomeJourneys() {
		org.hibernate.Query query = session.createQuery("select j from Journey j");
		Iterator<?> it = query.iterate();
		assertTrue("Expecting at least 1 journey", it.hasNext());
		assertTrue("Expecting at least 1 journey", it.next() instanceof Journey);
	}

	@Test
	public void testQueryCacheOn() {
		for (int i = 0; i < 10; i++) {
			org.hibernate.Query query = session.createQuery("from Location where lower(name) like '%' || :name || '%'");
			query.setParameter("name", "paris");
			query.setCacheable(true);
			List<?> list = query.list();
			assertTrue("Expecting some results: " + list.toString(), list.size() > 0);
			assertEquals("Expecting hit count", i, statistics.getQueryCacheHitCount());
		}
	}

	@Test
	public void testQueryCacheOnJPA() {
		for (int i = 0; i < 10; i++) {
			javax.persistence.Query query = delegateController.getEm().createQuery(
					"from Location where lower(name) like '%' || :name || '%'");
			query.setParameter("name", "paris");
			query.setHint("org.hibernate.cacheable", true);
			List<?> list = query.getResultList();
			assertTrue("Expecting some results: " + list.toString(), list.size() > 0);
			assertEquals("Expecting hit count", i, statistics.getQueryCacheHitCount());
		}
	}

	@Test
	public void testQueryCacheOff() {
		for (int i = 0; i < 10; i++) {
			org.hibernate.Query query = session.createQuery("from Location where lower(name) like '%' || :name || '%'");
			query.setParameter("name", "paris");
			query.setCacheable(false);
			List<?> list = query.list();
			assertTrue("Expecting some results: " + list.toString(), list.size() > 0);
			assertEquals("Expecting no hit count", 0, statistics.getQueryCacheHitCount());
		}
	}

	@Test
	public void testQueryCacheOffJPA() {
		for (int i = 0; i < 10; i++) {
			javax.persistence.Query query = delegateController.getEm().createQuery(
					"from Location where lower(name) like '%' || :name || '%'");
			query.setParameter("name", "paris");
			query.setHint("org.hibernate.cacheable", false);
			List<?> list = query.getResultList();
			assertTrue("Expecting some results: " + list.toString(), list.size() > 0);
			assertEquals("Expecting no hit count", 0, statistics.getQueryCacheHitCount());
		}
	}

	@Test
	public void testSecondLevelCacheOn() {
		String regionName =  Location.class.getName();
		SecondLevelCacheStatistics stats = statistics.getSecondLevelCacheStatistics(regionName);
		assertNotNull("Expecting cache region " + regionName + " (check hiberante @Cache annotation)", stats);
		int count = 0;
		session.setCacheMode(CacheMode.NORMAL);

		for (String locationName : LOCATION_NAMES) {
			count++;
			for (int i = 0; i < 10; i++) {
				String statsBefore = statistics.toString();

				Location location = session.get(Location.class, locationName);
				assertNotNull("Expecting to find " + locationName, location);
				assertEquals("Expecting to find "  + locationName, locationName, location.getName());

				assertEquals("Expecting hit count, miss: " + stats.getMissCount() + " for " + locationName +
						"\nStats : " + statistics +
						"\nBefore: " + statsBefore,
						count, stats.getHitCount());
			}
		}
	}

	@Test
	public void testSecondLevelCacheOff() {
		String regionName =  Location.class.getName();
		SecondLevelCacheStatistics stats = statistics.getSecondLevelCacheStatistics(regionName);
		assertNotNull("Expecting cache region " + regionName + " (check hiberante @Cache annotation)", stats);
		session.setCacheMode(CacheMode.IGNORE);

		for (String locationName : LOCATION_NAMES) {
			for (int i = 0; i < 10; i++) {
				Location location = session.get(Location.class, locationName);
				assertNotNull("Expecting to find New York" , location);
				assertEquals("Expecting to find New York" , locationName, location.getName());
				assertEquals("Expecting hit count", 0, stats.getHitCount());
			}
		}
	}

	@Test
	public void testSecondLevelCacheOnJPA() {
		String regionName =  Location.class.getName();
		SecondLevelCacheStatistics stats = statistics.getSecondLevelCacheStatistics(regionName);
		assertNotNull("Expecting cache region " + regionName + " (check hiberante @Cache annotation)", stats);
		int count = 0;

		for (String locationName : LOCATION_NAMES) {
			count++;
			for (int i = 0; i < 10; i++) {
				String statsBefore = statistics.toString();

				Location location = delegateController.getEm().find(Location.class, locationName);
				assertNotNull("Expecting to find " + locationName, location);
				assertEquals("Expecting to find "  + locationName, locationName, location.getName());

				assertEquals("Expecting hit count, miss: " + stats.getMissCount() + " for " + locationName +
							"\nStats : " + statistics +
							"\nBefore: " + statsBefore,
						count, stats.getHitCount());
			}
		}
	}
}
