package com.dynatrace.easytravel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.dynatrace.easytravel.database.DatabaseWithContent;
import com.dynatrace.easytravel.jpa.JpaAccessUtils;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.spring.PluginConstants;


public class DBSpammingTest extends DatabaseWithContent {

	@Test
	public void testDoExecuteLight() {
		DBSpamming spamming = new DBSpamming();
		spamming.setMode("light");

		// does not set anything on "light"
		assertNull(spamming.doExecute("someloca", (Object[]) null));
	}

	@Test
	public void testDoExecuteOthers() {
		DBSpamming spamming = new DBSpamming();
		// does set on all other modes, unless location is not a lifecycle event
		spamming.setMode("heavy");
		AtomicBoolean bool = new AtomicBoolean(false);
		assertNull(spamming.doExecute("someloca", new Object[] { bool }));
		assertTrue(bool.get());
	}

	@Test
	public void testDoExecuteLifecycle() {
		DataAccess dataAccess = createNewAccess();

		DBSpamming spamming = new DBSpamming();
		spamming.setMode("heavy");
		AtomicBoolean bool = new AtomicBoolean(false);

		// stop without start is handled gracefully
		assertNull(spamming.doExecute(PluginConstants.LIFECYCLE_PLUGIN_DISABLE, new Object[] { bool, dataAccess }));
		assertFalse("Boolean not set on lifecycle events",bool.get());
		assertEquals(10000, JpaAccessUtils.getCacheConfiguration("com.dynatrace.easytravel.jpa.business.Journey").getMaxEntriesLocalHeap());


		assertNull(spamming.doExecute(PluginConstants.LIFECYCLE_PLUGIN_ENABLE, new Object[] { bool, dataAccess }));
		assertFalse("Boolean not set on lifecycle events",bool.get());
		assertEquals(500, JpaAccessUtils.getCacheConfiguration("com.dynatrace.easytravel.jpa.business.Journey").getMaxEntriesLocalHeap());

		assertNull(spamming.doExecute(PluginConstants.LIFECYCLE_PLUGIN_DISABLE, new Object[] { bool, dataAccess }));
		assertFalse("Boolean not set on lifecycle events",bool.get());
		assertEquals(10000, JpaAccessUtils.getCacheConfiguration("com.dynatrace.easytravel.jpa.business.Journey").getMaxEntriesLocalHeap());
	}
}
