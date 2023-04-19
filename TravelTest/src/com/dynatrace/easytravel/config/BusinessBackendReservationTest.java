package com.dynatrace.easytravel.config;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;

import com.dynatrace.easytravel.utils.TestHelpers;


public class BusinessBackendReservationTest extends AbstractReservationTester {
	@Override
	protected TomcatResourceReservation reserveResources() throws IOException {
		return BusinessBackendReservation.reserveResources();
	}

	@Test
	public void testConstruct() {
		BusinessBackendReservation res = new BusinessBackendReservation(1, 2, 3, "con", "web");
		assertNotNull(res);
	}

	@Test
	public void testCompareTo() {
		TomcatResourceReservation res = new BusinessBackendReservation(1, 2, 3, "con", "web");
		TomcatResourceReservation equ = new BusinessBackendReservation(1, 2, 3, "con", "web");
		TomcatResourceReservation notequ = new BusinessBackendReservation(2, 2, 3, "con", "web");

		TestHelpers.CompareToTest(res, equ, notequ, false);

		notequ = new BusinessBackendReservation(-1, 2, 3, "con", "web");
		TestHelpers.CompareToTest(res, equ, notequ, true);
	}
}
