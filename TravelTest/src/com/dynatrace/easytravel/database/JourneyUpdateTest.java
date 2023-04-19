/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: JourneyUpdateTest.java
 * @date: 08.09.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.database;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.spring.PluginConstants;


/**
 *
 * @author dominik.stadler
 */
public class JourneyUpdateTest extends DatabaseWithContent {

	@Test
	public void testFast() throws Exception {
		DataAccess access = createNewAccess();

		try {
			JourneyUpdate update = new JourneyUpdate();
			update.setMode("fast");
			update.setExtensionPoint(new String[] { PluginConstants.BACKEND_JOURNEY_SEARCH });
			update.setEnabled(true);
			Object obj = update.execute(PluginConstants.BACKEND_JOURNEY_SEARCH, database);
			assertNotNull("Need to receive the thread-object", obj);
			assertTrue("Need to receive an object of type thread", obj instanceof Thread);
			((Thread)obj).join();
		} finally {
			access.close();
		}
	}

	@Test
	public void testSlow() throws Exception {
		DataAccess access = createNewAccess();

		try {
			JourneyUpdate update = new JourneyUpdate();
			update.setMode("slow");
			update.setExtensionPoint(new String[] { PluginConstants.BACKEND_JOURNEY_SEARCH });
			update.setEnabled(true);
			Object obj = update.execute(PluginConstants.BACKEND_JOURNEY_SEARCH, database);
			assertNotNull("Need to receive the thread-object", obj);
			assertTrue("Need to receive an object of type thread", obj instanceof Thread);
			((Thread)obj).join();
		} finally {
			access.close();
		}
	}
}
