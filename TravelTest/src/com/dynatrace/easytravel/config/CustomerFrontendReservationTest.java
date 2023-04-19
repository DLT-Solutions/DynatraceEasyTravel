package com.dynatrace.easytravel.config;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;

import com.dynatrace.easytravel.utils.TestHelpers;


public class CustomerFrontendReservationTest extends AbstractReservationTester {
	@Override
	protected TomcatResourceReservation reserveResources() throws IOException {
		return CustomerFrontendReservation.reserveResources();
	}

	@Test
	public void testConstruct() {
		CustomerFrontendReservation res = new CustomerFrontendReservation(1, 2, 3, "con", "web");
		assertNotNull(res);
	}

	@Test
	public void testCompareTo() {
		TomcatResourceReservation res = new CustomerFrontendReservation(1, 2, 3, "con", "web");
		TomcatResourceReservation equ = new CustomerFrontendReservation(1, 2, 3, "con", "web");
		TomcatResourceReservation notequ = new CustomerFrontendReservation(2, 2, 3, "con", "web");

		TestHelpers.CompareToTest(res, equ, notequ, false);

		notequ = new CustomerFrontendReservation(-1, 2, 3, "con", "web");
		TestHelpers.CompareToTest(res, equ, notequ, true);
	}
}
