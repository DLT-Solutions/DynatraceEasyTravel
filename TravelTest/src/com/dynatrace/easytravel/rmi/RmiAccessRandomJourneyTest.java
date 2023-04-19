/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: RmiAccessRandomJourneyTest.java
 * @date: 15.01.2012
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.rmi;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.dynatrace.easytravel.RandomJourneyProvider;
import com.dynatrace.easytravel.spring.SpringTestBase;


/**
 *
 * @author dominik.stadler
 */
@RunWith(MockitoJUnitRunner.class)
public class RmiAccessRandomJourneyTest extends SpringTestBase {

	@Mock
	RandomJourneyProvider randomJourneyProvider;

	/**
	 * Test method for {@link com.dynatrace.easytravel.rmi.RmiAccessRandomJourney#getHotDealIds()}.
	 * @throws IOException
	 */
	@Test
	public void testGetHotDealIds() throws IOException {
		RmiAccessRandomJourney random = new RmiAccessRandomJourney(randomJourneyProvider);
		assertNotNull(random);
	}

}
