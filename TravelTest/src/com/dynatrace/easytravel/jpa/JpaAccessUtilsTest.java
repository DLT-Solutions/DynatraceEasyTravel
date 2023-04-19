package com.dynatrace.easytravel.jpa;

import static org.junit.Assert.*;
import net.sf.ehcache.CacheManager;

import org.junit.Test;

import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;


public class JpaAccessUtilsTest {

	@Test
	public void testSetProperties() {
		// the normal case is covered by other tests, we just verify that we do not overwrite existing properties

		System.setProperty("javax.persistence.jdbc.driver", "1");
		System.setProperty("javax.persistence.jdbc.url", "2");
		System.setProperty("javax.persistence.jdbc.user", "3");
		System.setProperty("javax.persistence.jdbc.password", "4");

		try {
			JpaAccessUtils.setProperties("5", "6", "7", "8");
			assertEquals("1", System.getProperty("javax.persistence.jdbc.driver"));
			assertEquals("2", System.getProperty("javax.persistence.jdbc.url"));
			assertEquals("3", System.getProperty("javax.persistence.jdbc.user"));
			assertEquals("4", System.getProperty("javax.persistence.jdbc.password"));

			// a second time without the properties being set
			System.clearProperty("javax.persistence.jdbc.driver");
			System.clearProperty("javax.persistence.jdbc.url");
			System.clearProperty("javax.persistence.jdbc.user");
			System.clearProperty("javax.persistence.jdbc.password");

			JpaAccessUtils.setProperties("5", "6", "7", "8");
			assertEquals("5", System.getProperty("javax.persistence.jdbc.driver"));
			assertEquals("6", System.getProperty("javax.persistence.jdbc.url"));
			assertEquals("7", System.getProperty("javax.persistence.jdbc.user"));
			assertEquals("8", System.getProperty("javax.persistence.jdbc.password"));

			// a third time around, they are not overwritten any more
			JpaAccessUtils.setProperties("15", "16", "17", "18");
			assertEquals("5", System.getProperty("javax.persistence.jdbc.driver"));
			assertEquals("6", System.getProperty("javax.persistence.jdbc.url"));
			assertEquals("7", System.getProperty("javax.persistence.jdbc.user"));
			assertEquals("8", System.getProperty("javax.persistence.jdbc.password"));
		} finally {
			System.clearProperty("javax.persistence.jdbc.driver");
			System.clearProperty("javax.persistence.jdbc.url");
			System.clearProperty("javax.persistence.jdbc.user");
			System.clearProperty("javax.persistence.jdbc.password");
		}
	}

	@Test
	public void testGetCacheConfiguration() {
		assertNull(JpaAccessUtils.getCacheConfiguration("somename"));

		// init with default config
		CacheManager.newInstance();

		assertNull(JpaAccessUtils.getCacheConfiguration("somename"));
		assertNotNull(JpaAccessUtils.getCacheConfiguration("com.dynatrace.easytravel.jpa.business.Journey"));
	}

	@Test
	public void testChangeMaxEntriesInLocalHeap() {
		// init with default config
		CacheManager.newInstance();

		assertEquals(10000, JpaAccessUtils.changeMaxEntriesInLocalHeap("com.dynatrace.easytravel.jpa.business.Booking", 499));
		assertEquals(499, JpaAccessUtils.changeMaxEntriesInLocalHeap("com.dynatrace.easytravel.jpa.business.Booking", 10000));

		assertEquals(-1, JpaAccessUtils.changeMaxEntriesInLocalHeap("nonexistingcache", 499));
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(JpaAccessUtils.class);
	}
}
