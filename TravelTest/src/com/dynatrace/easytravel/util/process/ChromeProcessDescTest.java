package com.dynatrace.easytravel.util.process;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ChromeProcessDescTest {
	
	@Test
	public void testToString() {
		ChromeProcessDesc pdesc = new ChromeProcessDesc("1", null, null );
		assertEquals("ChromeProcessDesc toString not correct", "ChromeProcessDesc [pid=1, extensionDir=NA, parentPid=NA]", pdesc.toString());
		pdesc = new ChromeProcessDesc("1", "2", "/some/dir" );
		assertEquals("ChromeProcessDesc toString not correct", "ChromeProcessDesc [pid=1, extensionDir=/some/dir, parentPid=2]", pdesc.toString());
	}

}
