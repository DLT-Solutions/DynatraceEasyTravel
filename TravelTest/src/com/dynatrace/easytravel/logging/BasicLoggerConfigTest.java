package com.dynatrace.easytravel.logging;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;



public class BasicLoggerConfigTest {
	@Test
	public void testGetLogFilePath() {
		assertNotNull(BasicLoggerConfig.getLogFilePath("somelogfile"));
		assertNotNull(BasicLoggerConfig.getLogFilePath(""));
		assertNotNull(BasicLoggerConfig.getLogFilePath(null));
		
		assertNotNull(new BasicLoggerConfig("somename"));
		assertNotNull(BasicLoggerConfig.getLogFilePath("somelogfile", "suffix"));
	}
}
