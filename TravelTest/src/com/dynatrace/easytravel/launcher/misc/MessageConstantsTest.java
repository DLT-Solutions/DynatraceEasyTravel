package com.dynatrace.easytravel.launcher.misc;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Test;

import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;



public class MessageConstantsTest {
	@After
	public void tearDown() {
		DtVersionDetector.enforceInstallationType(null);
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(MessageConstants.class);
	}

	@Test
	public void test() {
		assertEquals("visits", MessageConstants.getAdaptedVisitString(0));
		assertEquals("visit", MessageConstants.getAdaptedVisitString(1));
		assertEquals("visits", MessageConstants.getAdaptedVisitString(238));
	}
}
