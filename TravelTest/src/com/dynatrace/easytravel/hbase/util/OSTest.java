package com.dynatrace.easytravel.hbase.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;


public class OSTest {

	@Test
	public void test() {
		assertEquals(OperatingSystem.IS_WINDOWS, OS.isWinOs());
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(OS.class);
	}
}
