package com.dynatrace.easytravel.spring;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Test;

import com.dynatrace.easytravel.DummyNativeApplication;
import com.dynatrace.easytravel.utils.TestHelpers;


public class GenericPluginListTest {
	@After
	public void tearDown() {
		// ensure that timer is cancelled
		GenericPluginList.cancelTimer();
	}

	@Test
	public void test() {
		GenericPluginList list = new GenericPluginList("somepoint");
		assertFalse(list.interested(new DummyNativeApplication()));
		TestHelpers.ToStringTest(list);

		GenericPluginList.cancelTimer();
		GenericPluginList.cancelTimer();
	}

	@Test
	public void testNull() {
		try {
			new GenericPluginList(null);
			fail("Should catch Exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "must not be null");
		}
	}

	@Test
	public void testExecuteInvalidLocation() {
		GenericPluginList list = new GenericPluginList("somepoint");

		try {
			list.execute("someotherpoint", (Object[])null);
			fail("Should catch Exception here");
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "not a subpath of", "somepoint", "someotherpoint");
		}
	}
}
