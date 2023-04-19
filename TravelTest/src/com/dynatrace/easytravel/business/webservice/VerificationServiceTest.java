package com.dynatrace.easytravel.business.webservice;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class VerificationServiceTest {

	@Test
	public void test() {
		VerificationService service = new VerificationService();
		assertTrue(service.isUserBlacklisted(null, null));
		assertTrue(service.isUserBlacklisted("", null));
		assertFalse(service.isUserBlacklisted("user", null));
	}
}
