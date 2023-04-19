package com.dynatrace.easytravel.booking;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;


public class BookingErrorAsHttp500Test {

	@Test
	public void test() {
		BookingErrorAsHttp500 plugin = new BookingErrorAsHttp500();

		AtomicBoolean enabled = new AtomicBoolean();
		plugin.doExecute(null, enabled);
		assertTrue(enabled.get());
	}

}
