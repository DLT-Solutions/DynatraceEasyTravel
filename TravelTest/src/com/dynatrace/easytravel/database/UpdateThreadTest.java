/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: UpdateThreadTest.java
 * @date: 08.09.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.database;

import java.util.Collection;

import org.junit.Test;

import com.dynatrace.easytravel.database.JourneyUpdate.Mode;
import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.model.DataAccess;


/**
 *
 * @author dominik.stadler
 */
public class UpdateThreadTest extends DatabaseWithContent {

	@Test
	public void testFast() {
		DataAccess access = createNewAccess();

		try {
			Collection<Journey> journeys = access.getJourneys(150);
			UpdateThread thread = new UpdateThread(access, Mode.fast, journeys);
			thread.run();
		} finally {
			access.close();
		}

	}

	@Test
	public void testSlow() {
		DataAccess access = createNewAccess();

		try {
			Collection<Journey> journeys = access.getJourneys(150);

			UpdateThread thread = new UpdateThread(access, Mode.slow, journeys);
			thread.run();
		} finally {
			access.close();
		}

	}
}
