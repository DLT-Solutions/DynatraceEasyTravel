package com.dynatrace.easytravel.logging;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;



public class LoggerFactoryTest {
	@Test
	public void test() {
		assertNotNull(LoggerFactory.make());
	}

	@Test
	public void testInitLoggingWorks() throws IOException {
		Thread.currentThread().setContextClassLoader(new ClassLoader() {
			@Override
			public InputStream getResourceAsStream(String name) {
				return new ByteArrayInputStream(new byte[] {});
			}
		});

		// now it should work
		LoggerFactory.initLogging();
	}

	// helper method to get coverage of the unused constructor
	 @Test
	 public void testPrivateConstructor() throws Exception {
	 	PrivateConstructorCoverage.executePrivateConstructor(LoggerFactory.class);
	 }
}
