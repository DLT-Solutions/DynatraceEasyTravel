/*****************************************************
  *  dynaTrace Diagnostics (c) dynaTrace software GmbH
  *
  * @file: JarRunnerTest.java
  * @date: 09.04.2010
  * @author: dominik.stadler
  *
  */

package com.dynatrace.easytravel.plugin;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.List;
import java.util.jar.Attributes;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.PluginFinder;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;
import com.dynatrace.easytravel.utils.TestEnvironment;

/**
 * @author dominik.stadler
 *
 */
public class PluginFinderTest {
    private static final Logger log = LoggerFactory.make();

    private static final File FILES_DIR = new File(TestEnvironment.RUNTIME_DATA_PATH);

	@Before
	public void setUp() {
		if (!FILES_DIR.exists()) {
			assertTrue(FILES_DIR.getAbsolutePath(), FILES_DIR.mkdirs());
		}
	}

	@After
	public void tearDown() throws IOException {
		TestEnvironment.clearRuntimeData();
	}

	// helper method to get coverage of the unused constructor
	 @Test
	 public void testPrivateConstructor() throws Exception {
	 	PrivateConstructorCoverage.executePrivateConstructor(PluginFinder.class);
	 }

	@Test
	@Ignore
	public void testGetPlugins() throws IOException {
		List<String> plugins = PluginFinder.getPlugins(this.getClass());
		assertEquals("In the test we don't find any plugins because the current jar does not have anything set",
				0, plugins.size());
	}

	// the test further down failed on Linux, we used this test to narrow what happened
	@Test
	@Ignore
	public void testGetClasspathLibrariesFailureLinux() throws IOException {
		File jarfile = new File(TestEnvironment.TEST_DATA_PATH, "testplugin.jar");
		assertTrue("Should find jarfile at 'testdata/testplugin.jar'", jarfile.exists());

		URL url = jarfile.toURI().toURL();

		log.info("Looking at URL '" + url + "'");

		// Note: WebStart uses names without ".jar", but we don't observe that here for now
		assertTrue(url.getFile().toLowerCase().endsWith(".jar"));

		URL rootJarUrl = new URL("jar", "/", url + "!/"); //$NON-NLS-1$ //$NON-NLS-2$
		JarURLConnection uc = (JarURLConnection) rootJarUrl.openConnection();
		assertTrue("Expected some content, but had content-length: " + uc.getContentLength(), uc.getContentLength() > 0);

		Attributes attr = uc.getMainAttributes();
		assertNotNull(attr);

		log.info("Class-Path: " + attr.getValue("Class-Path"));
		assertNotNull(attr.getValue("Class-Path"));
	}

	@Test
	@Ignore
	public void testGetClasspathLibraries() throws IOException {
		// ensure that the required pointed-to file is available at the place where we expect it
		// when running in CI in the Distribution directory
		{
			File mailjar = new File(TestEnvironment.TEST_DATA_PATH, "testmail.jar");
			assertTrue("File 'testmail.jar' should exist at " + mailjar.getAbsolutePath(), mailjar.exists());
			File dir = new File("testdata");

			// if the original testdata location and the location where we expect it, then create the directory and copy the file
			if(!mailjar.getCanonicalPath().equals(new File(dir, "testmail.jar").getCanonicalPath())) {
				assertTrue(dir.exists() || dir.mkdirs());
				FileUtils.copyFile(mailjar, new File(dir, "testmail.jar"));
			}
		}

		List<String> libs = PluginFinder.getClasspathLibraries(new File(TestEnvironment.TEST_DATA_PATH, "testplugin.jar"));
		assertNotNull(libs);
		assertEquals("Exactly one lib 'testmail.jar' should be found, others are not found in the current directory.", 1, libs.size());
		assertTrue(libs.toString(), libs.get(0).contains("testdata/testmail.jar"));
	}

	@Test
	public void testGetClasspathJars() throws IOException {
		assertEquals(0, PluginFinder.getClasspathJars(new String[] {}).size());
		assertEquals(0, PluginFinder.getClasspathJars(null).size());
		int sizeLib = PluginFinder.getClasspathJars(new String[] {"../Distribution/dist/lib"}).size();
		assertTrue(sizeLib > 0);
		assertEquals(0, PluginFinder.getClasspathJars(new String[] { "notexisting" }).size());
		assertEquals(sizeLib, PluginFinder.getClasspathJars(new String[] {"notexisting", "../Distribution/dist/lib"}).size());

		assertTrue(PluginFinder.getClasspathJars(new String[] {"notexisting", "../Distribution/dist/lib", "build.xml", "../ThirdPartyLibraries/Apache/Commons"}).size() > sizeLib);
	}
}
