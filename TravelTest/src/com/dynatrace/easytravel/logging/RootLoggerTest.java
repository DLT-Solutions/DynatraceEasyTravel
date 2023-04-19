package com.dynatrace.easytravel.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import static org.junit.Assert.*;
import org.junit.Test;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.json.JSONException;
import com.dynatrace.easytravel.json.JSONObject;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;
import com.dynatrace.easytravel.utils.TestHelpers;

public class RootLoggerTest {

	Logger logger = LoggerFactory.make();

	@Test
	public void testSpecialities() {
		// test things that we miss in other tests
		RootLogger.setup("somelogfilename");
		logger.info("persistentManager.swapInInvalid");
		DefaultFormatter.setAppId("short");
		logger.info("testlog");
		DefaultFormatter.setAppId("verlongapplicationnametoseeifwecutit");
		logger.info("testlog2");

		logger.info("some exception", new Exception());
	}

	@Test
	public void testCreateLogDir() throws IOException {
		try {
			FileUtils.deleteDirectory(Directories.getLogDir());
		} catch (IOException e) {
			// deleting may fail on Windows because we still have the lock-file from previous tests open
			// ignore this specific error here
			TestHelpers.assertContains(e, "Unable to delete file", "somelogfilename_0-0.log.lck");
		}

		RootLogger.setup("somename");
		RootLogger.setup("somename");
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(RootLogger.class);
	}

	@Test
	public void testJSONLayoutEnabled() throws JSONException, IOException {
		System.setProperty("useJSONLogging", "true");
		generateAndCaptureLogOutput();
	}
	
	@Test(expected = JSONException.class)
	public void testJSONLayoutDisabled() throws JSONException, IOException {
		System.setProperty("useJSONLogging", "false");
		generateAndCaptureLogOutput();
	}
		
	private void generateAndCaptureLogOutput() throws JSONException, IOException {
		PrintStream old = System.out;
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos)){			
			System.setOut(ps);
			((Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).detachAndStopAllAppenders();
			RootLogger.setup("test");
			
			String result = baos.toString();
			String[] lines = result.split(System.getProperty("line.separator"));
			
			assertTrue(lines.length > 0);
			
			for(int i=0;i<lines.length;i++) {
				new JSONObject(lines[i]);
			}
		} finally {
			System.out.flush();
			System.setOut(old);		
		}
	}
}
