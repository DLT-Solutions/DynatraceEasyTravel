package com.dynatrace.easytravel.config;

import java.util.Properties;

import org.junit.Test;

import com.dynatrace.easytravel.util.ConfigurationProvider;

public class PropertyFileReaderTest {

	@Test
	public void testPropertyFileReader() throws Exception {
		Properties props = ConfigurationProvider.readPropertyFile("easyTravel");

		props.list(System.out);
	}
}
