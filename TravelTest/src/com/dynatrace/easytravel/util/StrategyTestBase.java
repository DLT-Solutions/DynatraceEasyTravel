package com.dynatrace.easytravel.util;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.business.client.ConfigurationServiceStub;
import com.dynatrace.easytravel.utils.TestHelpers;

import ch.qos.logback.classic.Level;

@Ignore("Abstract base class")
public class StrategyTestBase<T extends ServiceStubStrategy> {
	//private static final Logger log = LoggerFactory.make();

	private static final int TEST_COUNT = 100;

	protected final T strategy;
	protected final boolean pooling;
	protected final boolean singleCache;

	public StrategyTestBase(T strategy, boolean pooling, boolean singleCache) {
		super();
		this.strategy = strategy;
		this.pooling = pooling;
		this.singleCache = singleCache;
	}


	@Test
	public void testGlobalPoolStrategy() throws Exception {
		strategy.clear();

		ConfigurationServiceStub serviceStub = strategy.getServiceStub(ConfigurationServiceStub.class);
		assertNotNull(serviceStub);

		ConfigurationServiceStub serviceStub2 = strategy.getServiceStub(ConfigurationServiceStub.class);
		assertNotNull(serviceStub2);

		if(singleCache) {
			assertTrue("Aquiring twice should not return the same item", serviceStub == serviceStub2);
		} else {
			assertFalse("Aquiring twice should not return the same item", serviceStub == serviceStub2);
		}

		// return the object
		strategy.returnServiceStub(serviceStub);

		ConfigurationServiceStub serviceStub3 = strategy.getServiceStub(ConfigurationServiceStub.class);
		assertNotNull(serviceStub3);

		if(pooling) {
			assertTrue("Should get the one that we returned", serviceStub == serviceStub3);
		}
		if(singleCache) {
			assertTrue("Should not get the one that we did not return", serviceStub2 == serviceStub3);
		} else {
			assertFalse("Should not get the one that we did not return", serviceStub2 == serviceStub3);
		}

		// invalidate the object
		strategy.invalidateServiceStub(serviceStub3);

		ConfigurationServiceStub serviceStub4 = strategy.getServiceStub(ConfigurationServiceStub.class);
		assertNotNull(serviceStub4);

		assertFalse("Should get a new one", serviceStub == serviceStub4);
		assertFalse("Should get a new one", serviceStub2 == serviceStub4);

		strategy.returnServiceStub(serviceStub);
		strategy.returnServiceStub(serviceStub2);
		strategy.returnServiceStub(serviceStub4);

		// clear should get rid of all
		strategy.clear();

		// does not fail on null-stub being returned or invalidated
		strategy.returnServiceStub(null);
		strategy.invalidateServiceStub(null);
	}

	@Test
	public void testGetMany() throws Exception {
		Set<ConfigurationServiceStub> set = new HashSet<ConfigurationServiceStub>();
		for(int i = 0;i < TEST_COUNT;i++) {
			ConfigurationServiceStub serviceStub = strategy.getServiceStub(ConfigurationServiceStub.class);
			assertNotNull(serviceStub);

			if(singleCache && i > 0) {
				assertFalse("Should have the element twice after " + i + " iterations", set.add(serviceStub));
			} else {
				assertTrue("Should not have the element twice after " + i + " iterations", set.add(serviceStub));
			}

			/*if(i % (TEST_COUNT/10) == 0) {
				log.info("Had: " + i);
			}*/
		}

		for(ConfigurationServiceStub stub : set) {
			strategy.returnServiceStub(stub);
		}
	}

	@Test
	public void testWithDifferentLogLevel() throws Exception {
		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {

			@Override
			public void run() {
				try {
					testGlobalPoolStrategy();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}, AbstractMapStrategy.class.getName(), Level.DEBUG);
	}
}
