package com.dynatrace.easytravel.launcher.httpd;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author Rafal.Psciuk
 * Helper class for creating an dummy file simulating agent dll
 */
public class DummyAgentDLL {
	private File myTestDir = null;
	
	private File dummyDLL = null; // test file
	private String myTestPath = null;
	
	private static final Logger LOGGER = Logger.getLogger(DummyAgentDLL.class.getName());

	public void createDummyAgentDLL() throws IOException {

		//========================================
		// We do this because HttpdConfSetup.write()
		// will check for the existence of this file,
		// before writing the entry to httpd.conf.
		//========================================

		// create a temporary file, delete it and use this name to create a test directory
		File tempFile = File.createTempFile("dummyAgentDLL", null);
		tempFile.delete();
		assertFalse(tempFile.exists());
	
		// Create our own test dir
		myTestDir = new File(tempFile.getAbsolutePath());
		LOGGER.info("myTestDir is: " + myTestDir);
		myTestDir.mkdirs(); // We do not insist on successful creation so long as the directory exists, hence no assert here.
		assertTrue(myTestDir.exists());

		// Create a dummy agent dll in our test dir.
		
		dummyDLL = new File(myTestDir, "ruxitagentapache22.dll");
		dummyDLL.createNewFile(); // We do not insist on successful creation so long as the directory exists, hence no assert here.
		assertTrue(dummyDLL.exists());
		
		myTestPath = dummyDLL.getAbsolutePath();
		LOGGER.info("test DLL path: " + myTestPath);
	}
	
	public void destroyDummyAgentDLL() {
		if (dummyDLL != null && dummyDLL.exists()) {
			assertTrue(dummyDLL.delete());
			dummyDLL = null;
		}
		if (myTestDir != null && myTestDir.exists()) {
			assertTrue(myTestDir.delete());
			myTestDir = null;
		}
	}
	
	public String getMyTestPath() {
		return myTestPath;
	}
}
