package com.dynatrace.easytravel.launcher.misc;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;


public class DocumentStarterTest {
	@Test
	public void testIsLinux() {
		DocumentStarter starter = new DocumentStarter();
		assertNotNull(starter);
		
		// TODO: other tests open files on the test-machine, maybe we should put in some way to prevent this for testing...
	}
}
