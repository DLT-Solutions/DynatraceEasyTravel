/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: PersistenceBootstrapTest.java
 * @date: 21.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dynatrace.easytravel.business.cache.LocationCache;
import com.dynatrace.easytravel.model.DataAccess;

/**
 *
 * @author stefan.moschinski
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:**/persistence-bootstrap.xml" })
@Ignore("Does not work in ANT")
public class PersistenceBootstrapTest {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void testName() throws Exception {
		assertThat(applicationContext, is(not(nullValue(ApplicationContext.class))));

		PersistenceBootstrap bootstrap = applicationContext.getBean("persistenceBootstrap", PersistenceBootstrap.class);
		assertThat(bootstrap, is(not(nullValue())));

		DataAccess access = applicationContext.getBean("databaseAccess", DataAccess.class);
		assertThat(access, is(not(nullValue())));

		assertThat(applicationContext.getBean("locationCache", LocationCache.class), is(not(nullValue())));

	}
}
