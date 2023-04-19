package com.dynatrace.easytravel.launcher.httpd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.TestUtil;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;
import com.dynatrace.easytravel.utils.TestEnvironment;
import com.dynatrace.easytravel.utils.TestHelpers;
import com.google.common.collect.Lists;


public class LogWriterTest {
	private static final Logger logger = LoggerFactory.make();
	
    static {
    	TestUtil.setInstallDirCorrection();
    }
	
	private ByteArrayOutputStream out = new ByteArrayOutputStream();
	private PrintWriter writer = new PrintWriter(out);

	@Before
	public void setUp() throws IOException {
		TestEnvironment.createOrClearRuntimeData();
	}

	@Test
	public void testAddModLogRotateToModules() throws IOException {
		LogWriter.addModLogRotateToModules(writer);
		writer.close();
		out.close();

		String result = out.toString();

		if(OperatingSystem.IS_WINDOWS) {
			TestHelpers.assertContains(result, "log_rotate_module");
		} else {
			TestHelpers.assertNotContains(result, "log_rotate_module");
		}
	}

	@Test
	public void testDeleteOldLog() throws IOException {
		List<String> names = Lists.newArrayList("error.log","access.log");
		for (int i=0; i<24; i++) {
			names.add(MessageFormat.format("access_{0,number,00}.log",i));			
			names.add(MessageFormat.format("error_{0,number,00}.log",i));
		}		
		logger.info("Log file names to test: " + names);
		
		for (String name : names) {
			File file = new File(Directories.getLogDir().getAbsolutePath(), name);
			FileUtils.writeStringToFile(file, "test");
		}

		LogWriter.deleteOldLog();

		for (String name : names) {
			File file = new File(Directories.getLogDir().getAbsolutePath(), name);
			assertFalse("File exists: " + name, file.exists());
		}		
	}

	@Test
	public void testWriteAll() throws IOException {
		LogWriter.writeModLogRotateConfigToHttpdConf(writer);
		writer.close();
		out.close();

		String result = out.toString();

        TestHelpers.assertNotContains(result, "access_%H.log", "CustomLog");

		TestHelpers.assertContains(result, "ErrorLog", OperatingSystem.IS_WINDOWS ? "RotateInterval 3600" : " 3600");
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(LogWriter.class);
	}

	@Test
	public void testGetLastModifiedFile() throws IOException, InterruptedException {

		File dir = new File(TestEnvironment.ABS_RUNTIME_DATA_PATH);

		File fileFirstModified = File.createTempFile("LogWriterTest1", ".test", dir);
		File fileLastModified = File.createTempFile("LogWriterTest2", ".test", dir);
		File fileNotFound = File.createTempFile("LogWriterTest2", ".notfound", dir);

		// wait a bit to ensure we have a lastmodified date
		Thread.sleep(1000);

		FileUtils.writeStringToFile(fileLastModified, "somedata");
		FileUtils.writeStringToFile(fileNotFound, "someotherdata");

		// ensure the dates are ok
		assertTrue(fileLastModified.lastModified() > fileFirstModified.lastModified());

		String lastModifiedFile = LogWriter.getLastModifiedFile(dir, "*.test");

		assertEquals(fileLastModified.getAbsolutePath(), lastModifiedFile);
	}

	@Test
	public void testGetLastModifiedFileNotFound() throws IOException, InterruptedException {
		File dir = new File(TestEnvironment.ABS_RUNTIME_DATA_PATH);

		String lastModifiedFile = LogWriter.getLastModifiedFile(dir, "*.test");

		assertEquals(new File(dir, "*.test").getAbsolutePath(), lastModifiedFile);
	}

	@Test
	public void testGetLastModifiedFileInvalidDirectory() throws IOException, InterruptedException {
		File dir = new File(TestEnvironment.ABS_RUNTIME_DATA_PATH, "someinvaliddir");

		String lastModifiedFile = LogWriter.getLastModifiedFile(dir, "*.test");

		assertEquals(new File(dir, "*.test").getAbsolutePath(), lastModifiedFile);
	}
}
