package com.dynatrace.easytravel.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.dynatrace.easytravel.business.client.ConfigurationServiceStub;
import com.dynatrace.easytravel.business.client.JourneyServiceStub;
import com.dynatrace.easytravel.business.webservice.JourneyService;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;
import com.dynatrace.easytravel.utils.TestHelpers;

import ch.qos.logback.classic.Level;

public class ServiceStubFactoryTest {

	@Test
	public void testGetServiceName() {
		assertEquals("String", ServiceStubFactory.getServiceName(String.class));
		assertEquals("ConfigurationService", ServiceStubFactory.getServiceName(ConfigurationServiceStub.class));
		assertEquals("JourneyService", ServiceStubFactory.getServiceName(JourneyServiceStub.class));
		assertEquals("JourneyService", ServiceStubFactory.getServiceName(JourneyService.class));
	}

	@Test
	public void testMakeStub() throws Exception {
		assertNotNull(ServiceStubFactory.makeStub(JourneyServiceStub.class, ServiceStubFactory.getServiceName(JourneyServiceStub.class)));
	}

	@Test
	public void testWithDifferentLogLevel() throws Exception {
		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {

			@Override
			public void run() {
				try {
					testMakeStub();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}, ServiceStubFactory.class.getName(), Level.DEBUG);
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(ServiceStubFactory.class);
	}
}
