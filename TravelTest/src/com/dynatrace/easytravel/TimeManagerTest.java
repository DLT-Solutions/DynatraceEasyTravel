package com.dynatrace.easytravel;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;

import com.dynatrace.easytravel.database.TimeManager;


public class TimeManagerTest {

	@Test
	public void testInitialize() {
		assertEquals(TimeManager.initialize(this.getClass(), 10, .334), TimeManager.initialize(this.getClass(), 10, .7));
		assertEquals(TimeManager.initialize(this.getClass(), 10, 0), TimeManager.initialize(this.getClass(), 10, 0.5));
	}
	@After
	public void clearInstances() {
		TimeManager.clearInstances();
	}

	@Test
	public void testContentionWorks() {
		TimeManager manager = TimeManager.initialize(this.getClass(), 1000, 0.9);

		long id1 = manager.start();
		long id2 = manager.start();
		long id3 = manager.start();
		long id4 = manager.start();
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			fail("Exception occured\\n" + e.getMessage());
		}
		manager.stop(id1);
		manager.stop(id2);
		manager.stop(id3);
		manager.stop(id4);

		assertTrue("Manager is not contended, but it should!", manager.isContended());

		long id5 = manager.start();
		long id6 = manager.start();
		long id7 = manager.start();
		long id8 = manager.start();
		long id9 = manager.start();
		long id10 = manager.start();
		manager.stop(id5);
		manager.stop(id6);
		manager.stop(id7);
		manager.stop(id8);
		manager.stop(id9);
		manager.stop(id10);

		assertTrue("Manager is still contended, but it should not!", !manager.isContended());
	}

	@Test
	public void testNoContentionWorks() {
		int maxThreads = 10;
		TimeManager manager = TimeManager.initialize(this.getClass(), maxThreads, 10000);

		long id1 = manager.start();
		long id2 = manager.start();
		long id3 = manager.start();
		long id4 = manager.start();

		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			fail("Exception occured\\n" + e.getMessage());
		}
		manager.stop(id1);
		manager.stop(id2);
		manager.stop(id3);
		manager.stop(id4);

		assertTrue("Manager is still contended, but it should not!", !manager.isContended());
	}

	@Test
	public void testStop() {
		int maxThreads = 10;
		TimeManager manager = TimeManager.initialize(this.getClass(), maxThreads, 10000);

		// nonexisting does nothing
		manager.stop(123);
	}
}
