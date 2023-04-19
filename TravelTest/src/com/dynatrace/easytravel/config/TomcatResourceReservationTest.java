package com.dynatrace.easytravel.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.dynatrace.easytravel.utils.TestHelpers;


public class TomcatResourceReservationTest {

	@Test
	public void test() {
		TomcatResourceReservation res = new TomcatResourceReservation(1, 2, 3, "con", "web") {
		};

		assertEquals(1, res.getPort());
		assertEquals(2, res.getShutdownPort());
		assertEquals(3, res.getAjpPort());
		assertEquals("con", res.getContextRoot());
		assertEquals("web", res.getWebappBase());

		// tries to free the ports
		res.release();

		// does nothing the second time
		res.release();

		TestHelpers.ToStringTest(res);
		TestHelpers.ToStringTest(new TomcatResourceReservation(0, -1, -2, null, null) {
		});
	}
}
