/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: AntThreadTest.java
 * @date: 15.09.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.launcher.ant;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.dynatrace.easytravel.launcher.engine.StopListener;
import com.dynatrace.easytravel.utils.TestEnvironment;
import com.dynatrace.easytravel.utils.TestHelpers;


/**
 *
 * @author dominik.stadler
 */
public class AntThreadTest {

	/**
	 * Test method for {@link com.dynatrace.easytravel.launcher.ant.AntThread#run()}.
	 * @throws Exception
	 */
	@Test
	public void testRunInvalid() throws Exception {
		File file = File.createTempFile("build", ".xml", new File(TestEnvironment.ABS_RUNTIME_DATA_PATH));

		try {
			new AntThread("testant", file, "notexisting", 1, 0, null);
			fail("Should catch exception because of invalid properties");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "must not be null");
		}
	}

	/**
	 * Test method for {@link com.dynatrace.easytravel.launcher.ant.AntThread#AntThread(java.lang.String, java.io.File, java.lang.String, int, long, java.util.Map)}.
	 * @throws Exception
	 */
	@Test
	public void testAntThreadInvalidFile() throws Exception {
		File file = File.createTempFile("build", ".xml", new File(TestEnvironment.ABS_RUNTIME_DATA_PATH));
		AntThread thread = new AntThread("testant", file, "notexisting", 1, 0, new HashMap<String, String>());
		thread.start();

		thread.join();

		assertNull(thread.getVmError());
	}

	@Test
	public void testAntThread() throws Exception {
		File file = getValidBuildFile();

		AntThread thread = new AntThread("testant", file, "all", 1, 0, new HashMap<String, String>());
		thread.start();

		thread.join();

		assertNull(thread.getVmError());
	}

	/**
	 * Test method for {@link com.dynatrace.easytravel.launcher.ant.AntThread#addStopListener(com.dynatrace.easytravel.launcher.engine.StopListener)}.
	 * @throws Exception
	 */
	@Test
	public void testAddStopListener() throws Exception {
		File file = getValidBuildFile();

		AntThread thread = new AntThread("testant", file, "all", 1, 0, new HashMap<String, String>());

		final AtomicBoolean stopped = new AtomicBoolean(false);
		final AtomicBoolean error = new AtomicBoolean(false);
		thread.addStopListener(new StopListener() {

			@Override
			public void notifyProcessStopped() {
				stopped.set(true);
			}

			@Override
			public void notifyProcessFailed() {
				error.set(true);
			}
		});

		thread.start();

		thread.join();

		assertTrue(stopped.get());
		assertFalse(error.get());

		assertNull(thread.getVmError());
	}

	private File getValidBuildFile() throws IOException {
		File file = File.createTempFile("build", ".xml", new File(TestEnvironment.ABS_RUNTIME_DATA_PATH));
		FileUtils.write(file, "<project><target name=\"all\"><echo message=\"ping\"/></target></project>");
		//FileUtils.write(file, "<project><target name=\"all\"/></project>");
		return file;
	}

	/**
	 * Test method for {@link com.dynatrace.easytravel.launcher.ant.AntThread#softStop()}.
	 * @throws Exception
	 */
	@Test
	public void testSoftStop() throws Exception {
		File file = getValidBuildFile();

		AntThread thread = new AntThread("testant", file, "all", 1, 0, new HashMap<String, String>());

		final AtomicBoolean stopped = new AtomicBoolean(false);
		final AtomicBoolean error = new AtomicBoolean(false);
		thread.addStopListener(new StopListener() {

			@Override
			public void notifyProcessStopped() {
				stopped.set(true);
			}

			@Override
			public void notifyProcessFailed() {
				error.set(true);
			}
		});

		thread.start();

		thread.softStop();

		thread.join();

		assertTrue(stopped.get());
		assertFalse(error.get());

		assertFalse(thread.isVmErrorDetected());
		assertNull(thread.getVmError());
	}

	/**
	 * Test method for {@link com.dynatrace.easytravel.launcher.ant.AntThread#isVmErrorDetected()}.
	 */
	@Test
	public void testIsVmErrorDetected() {
		// method is tested above, how can we trigger an actual vm error?
	}

	/**
	 * Test method for {@link com.dynatrace.easytravel.launcher.ant.AntThread#getVmError()}.
	 */
	@Test
	public void testGetVmError() {
		// method is tested above, how can we trigger an actual vm error?
	}

	/**
	 * Test method for {@link com.dynatrace.easytravel.launcher.ant.AntThread#setContinuously(boolean)}.
	 * @throws Exception
	 */
	@Test
	public void testSetContinuously() throws Exception {
		File file = getValidBuildFile();

		AntThread thread = new AntThread("testant", file, "all", 1, 100, new HashMap<String, String>());

		final AtomicBoolean stopped = new AtomicBoolean(false);
		final AtomicBoolean error = new AtomicBoolean(false);
		thread.addStopListener(new StopListener() {

			@Override
			public void notifyProcessStopped() {
				stopped.set(true);
			}

			@Override
			public void notifyProcessFailed() {
				error.set(true);
			}
		});

		// ask thread to keep starting the procedure over and over
		thread.setContinuously(true);

		thread.start();

		assertTrue(thread.isAlive());

		Thread.sleep(10000);

		// still alive
		assertTrue(thread.isAlive());

		// now instruct thread to not continue further
		thread.setContinuously(false);

		thread.join();

		assertTrue(stopped.get());
		assertFalse(error.get());

		assertFalse(thread.isVmErrorDetected());
		assertNull(thread.getVmError());
	}
}
