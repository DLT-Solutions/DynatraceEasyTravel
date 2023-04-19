/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: PluginDependencyTest.java
 * @date: 07.08.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.spring;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Set;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.dynatrace.easytravel.constants.BaseConstants.BusinessBackend.Persistence;
import com.dynatrace.easytravel.constants.BaseConstants.SystemProperties;
import com.dynatrace.easytravel.utils.TestHelpers;
import com.google.common.collect.Sets;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 *
 * @author stefan.moschinski
 */
public class PluginDependencyTest {

	@BeforeClass
	public static void setUpLogLevel() {
		Logger logger = (Logger) LoggerFactory.getLogger(PluginDependency.class.getName());
		logger.setLevel(Level.ALL);
		logger.setLevel(Level.ALL);
	}

	@After
	public void tearDown() {
		// don't fail on previously set property
		System.clearProperty(SystemProperties.PERSISTENCE_MODE);
	}

	@Test
	public void testIsAvailable() {
		assertFalse(PluginDependency.CASSANDRA.isAvailable());
		assertFalse(PluginDependency.JPA.isAvailable());

		System.setProperty(SystemProperties.PERSISTENCE_MODE, Persistence.CASSANDRA);
		assertTrue(PluginDependency.CASSANDRA.isAvailable());
		assertFalse(PluginDependency.JPA.isAvailable());

		System.setProperty(SystemProperties.PERSISTENCE_MODE, Persistence.JPA);
		assertFalse(PluginDependency.CASSANDRA.isAvailable());
		assertTrue(PluginDependency.JPA.isAvailable());

		assertTrue(PluginDependency.NONE.isAvailable());
	}

	@Test
	public void testFromName() {
		assertThat(PluginDependency.forName("jibbet et ned"), equalTo(PluginDependency.NONE));
		assertThat(PluginDependency.forName(null), equalTo(PluginDependency.NONE));
		assertThat(PluginDependency.forName(""), equalTo(PluginDependency.NONE));

		assertThat(PluginDependency.forName(Persistence.CASSANDRA), equalTo(PluginDependency.CASSANDRA));
		assertThat(PluginDependency.forName(Persistence.JPA), equalTo(PluginDependency.JPA));
		assertThat(PluginDependency.forName(Persistence.HBASE), equalTo(PluginDependency.NONE));
	}

	@Test
	public void testForNames() {
		assertEquals(0, Sets.newHashSet(PluginDependency.forNames("xyz", null, "")).size());

		Set<PluginDependency> result = Sets.newHashSet(PluginDependency.forNames(Persistence.CASSANDRA, "xyz", null, ""));
		assertEquals(1, result.size());
		assertThat(result, contains(PluginDependency.CASSANDRA));

		result = Sets.newHashSet(PluginDependency.forNames(Persistence.CASSANDRA, Persistence.JPA, "xyz", null, ""));
		assertEquals(2, result.size());
		assertThat(result, containsInAnyOrder(PluginDependency.CASSANDRA, PluginDependency.JPA));

		assertFalse(PluginDependency.forNames(new String[] {}).iterator().hasNext());
	}

	@Test
	public void testToString() {
		TestHelpers.ToStringTest(PluginDependency.CASSANDRA);
		TestHelpers.ToStringTest(PluginDependency.JPA);
		//TestHelpers.ToStringTest(PluginDependency.NONE);
	}

	@Test
	public void testWithDifferentLogLevel() {
		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {
			@Override
			public void run() {
				testIsAvailable();
			}
		}, PluginDependency.class.getName(), Level.DEBUG);
		tearDown();
		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {
			@Override
			public void run() {
				testIsAvailable();
			}
		}, PluginDependency.class.getName(), Level.WARN);
	}

	@Test
	public void testValueOf() {
		assertEquals(PluginDependency.CASSANDRA, PluginDependency.valueOf("CASSANDRA"));
		assertEquals(PluginDependency.JPA, PluginDependency.valueOf("JPA"));
		assertEquals(PluginDependency.NONE, PluginDependency.valueOf("NONE"));

		try {
			PluginDependency.valueOf("invalidenum");
			fail("Expected an exception here!");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "invalidenum");
		}
	}
}
