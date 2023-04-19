package org.apache.catalina.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.UUID;

import org.apache.catalina.Session;
import org.junit.Test;

import com.dynatrace.easytravel.config.Directories;



public class PersistentManagerTest {
	@Test
	public void testSessionExpiry() throws Exception {
		PersistentManager manager = new PersistentManager();
        FileStore store = new FileStore();
        store.setDirectory(new File(Directories.getTempDir(), "store").getAbsolutePath());
		manager.setStore(store);

		ManagerTestBase managerTest = new ManagerTestBase();
		managerTest.start(manager);

		manager.setMaxActiveSessions(-1);
		manager.setMaxIdleSwap(5);
		manager.setMinIdleSwap(4);
		manager.setMaxIdleBackup(3);

		manager.start();

		manager.processExpires();

		////// Preparation done, here come the actual tests

		Session session = manager.createSession(UUID.randomUUID().toString());
		assertNotNull(session);

		assertEquals("Now we have one Session available",
				1, manager.getActiveSessions());

		manager.processExpires();

		assertEquals("We still have the Session here as it is not old enough yet",
				1, manager.getActiveSessions());

		Thread.sleep(6000);

		manager.processExpires();

		assertEquals("Now the session should have been expired",
				0, manager.getActiveSessions());

		manager.stop();
	}
}
