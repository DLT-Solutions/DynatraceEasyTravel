package com.dynatrace.easytravel.booking;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;


public class CaptureBookingIDTest {

	@Test
	public void test() {
		CaptureBookingID plugin = new CaptureBookingID();

		AtomicBoolean enabled = new AtomicBoolean();
		plugin.doExecute(null, null, null, null, enabled);
		assertTrue(enabled.get());
	}

}
