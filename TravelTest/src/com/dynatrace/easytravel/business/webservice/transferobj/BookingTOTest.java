/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: BookingTOTest.java
 * @date: 01.07.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.business.webservice.transferobj;

import static org.junit.Assert.assertEquals;

import java.util.GregorianCalendar;

import org.junit.Test;


/**
 *
 * @author dominik.stadler
 */
public class BookingTOTest {

	/**
	 * Test method for {@link com.dynatrace.easytravel.business.webservice.transferobj.BookingTO#BookingTO(java.lang.String, java.util.Calendar, java.lang.String, int, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testBookingTO() {
		GregorianCalendar date = new GregorianCalendar();
		BookingTO to = new BookingTO("id", date, "user", 839, "journey1", "dep1", "dest1");
		assertEquals("id", to.getId());
		assertEquals(date, to.getBookingDate());
		assertEquals("dep1", to.getDeparture());
		assertEquals("dest1", to.getDestination());
		assertEquals(839, to.getJourneyId());
		assertEquals("journey1", to.getJourneyName());
		assertEquals("user", to.getUserName());
	}

}
