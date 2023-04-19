/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: BookingSummaryTest.java
 * @date: 01.07.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.business.webservice.transferobj;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;

import com.dynatrace.easytravel.jpa.business.Location;


/**
 *
 * @author dominik.stadler
 */
public class BookingSummaryTest {

	/**
	 * Test method for {@link com.dynatrace.easytravel.business.webservice.transferobj.BookingSummary#BookingSummary(int, double, java.util.Map, java.util.Map)}.
	 */
	@Test
	public void testBookingSummary() {
		BookingSummary bookingSummary = new BookingSummary(4, 234.23, Collections.singletonMap(new Location("deploc"), 3),
				Collections.singletonMap(new Location("destloc"), 2));
		assertEquals(3, bookingSummary.getDepartureCounts()[0]);
		assertEquals("deploc", bookingSummary.getDepartures()[0].getName());
		assertEquals(2, bookingSummary.getDestinationCounts()[0]);
		assertEquals("destloc", bookingSummary.getDestinations()[0].getName());

		assertEquals(4, bookingSummary.getNBookings());
		assertEquals(234.23, bookingSummary.getTotalSales(), 0);
	}
}
