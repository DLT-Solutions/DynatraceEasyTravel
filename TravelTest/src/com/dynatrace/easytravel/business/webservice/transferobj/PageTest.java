/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: PageTest.java
 * @date: 01.07.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.business.webservice.transferobj;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 *
 * @author dominik.stadler
 */
public class PageTest {

	/**
	 * Test method for {@link com.dynatrace.easytravel.business.webservice.transferobj.Page#Page(T[], int, int, int)}.
	 */
	@Test
	public void testPage() {
		Page<String> page = new Page<String>(new String[] { "1", "2" }, 1, 2, 12) {};

		assertEquals(1, page.getFromIdx());
		assertEquals(2, page.getCount());
		assertEquals(12, page.getTotal());
	}
}
