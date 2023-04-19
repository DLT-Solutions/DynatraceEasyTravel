package com.dynatrace.easytravel.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.TestUtil;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.ZipUtils;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;
import com.dynatrace.easytravel.utils.TestEnvironment;

public class ZipUtilsTest {
	private static final Logger log = LoggerFactory.make();

	@BeforeClass
	public static void setUp() throws IOException {
		TestEnvironment.createOrClearRuntimeData();
	}

	@Test
	public void testZipSrcDir() throws IOException {
		File destFile = new File(TestEnvironment.ABS_RUNTIME_DATA_PATH, "src.zip");
		File srcDir = TestUtil.detectTravelTestSrcDir();
		boolean b = ZipUtils.compressDir(destFile, srcDir, null, null, /*includeRootName*/false);
		assertTrue("Expecting ZIP file to be created", b);
		assertTrue("Expecting ZIP file to be created", destFile.exists());
		log.debug("Zip File: " + destFile);

		File fromZip = ZipUtils.extractZipEntry(destFile, TestUtil.getJavaFileName(getClass()), new File(TestEnvironment.ABS_RUNTIME_DATA_PATH));
		assertNotNull("Expecting file to be extracted", fromZip);
		assertTrue("Expecting ZIP file to be created", fromZip.exists());
		log.debug("Extracted File: " + fromZip);

		compareSourceFiles(srcDir, fromZip);

		fromZip = ZipUtils.extractZipEntry(destFile, getClass() + ".java", new File(TestEnvironment.ABS_RUNTIME_DATA_PATH));
		assertNull("Expecting no file as nothing matches", fromZip);

		fromZip = ZipUtils.extractZipEntry(destFile, "com", new File(TestEnvironment.ABS_RUNTIME_DATA_PATH));
		assertNull("Expecting no file as nothing matches", fromZip);
	}

	@Test
	public void testZipSrcDirExclude() throws IOException {
		File destFile = new File(TestEnvironment.ABS_RUNTIME_DATA_PATH, "src.zip");
		File srcDir = TestUtil.detectTravelTestSrcDir();
		boolean b = ZipUtils.compressDir(destFile, srcDir, null, "com", /*includeRootName*/false);
		assertTrue("Expecting ZIP file to be created", b);
		assertTrue("Expecting ZIP file to be created", destFile.exists());
		log.debug("Zip File: " + destFile);

		File fromZip = ZipUtils.extractZipEntry(destFile, TestUtil.getJavaFileName(getClass()), new File(TestEnvironment.ABS_RUNTIME_DATA_PATH));
		assertNull("Expecting file to be not extracted because of exclude", fromZip);
	}

	@Test
	public void testZipSrcDirExcludeFile() throws IOException {
		File destFile = new File(TestEnvironment.ABS_RUNTIME_DATA_PATH, "src.zip");
		File srcDir = TestUtil.detectTravelTestSrcDir();
		boolean b = ZipUtils.compressDir(destFile, srcDir, getClass().getSimpleName() + ".java", "notmatched", /*includeRootName*/false);
		assertTrue("Expecting ZIP file to be created", b);
		assertTrue("Expecting ZIP file to be created", destFile.exists());
		log.debug("Zip File: " + destFile);

		File fromZip = ZipUtils.extractZipEntry(destFile, TestUtil.getJavaFileName(getClass()), new File(TestEnvironment.ABS_RUNTIME_DATA_PATH));
		assertNull("Expecting file to be not extracted because of exclude", fromZip);
	}

	@Test
	public void testZipSrcDirIncludeRootName() throws IOException {
		File destFile = new File(TestEnvironment.ABS_RUNTIME_DATA_PATH, "src.zip");
		File srcDir = TestUtil.detectTravelTestSrcDir();
		boolean b = ZipUtils.compressDir(destFile, srcDir, null, "\\.svn", /*includeRootName*/true);
		assertTrue("Expecting ZIP file to be created", b);
		assertTrue("Expecting ZIP file to be created", destFile.exists());
		log.debug("Zip File: " + destFile);

		File fromZip = ZipUtils.extractZipEntry(destFile, "src/" + TestUtil.getJavaFileName(getClass()), new File(TestEnvironment.ABS_RUNTIME_DATA_PATH));
		assertNotNull("Expecting file to be extracted", fromZip);
		assertTrue("Expecting ZIP file to be created", fromZip.exists());
		log.debug("Extracted File: " + fromZip);

		compareSourceFiles(srcDir, fromZip);
	}

	private void compareSourceFiles(File srcDir, File fromZip) throws IOException {
		File srcFile = new File(srcDir, TestUtil.getJavaFileName(getClass()));
		InputStream srcIn = new FileInputStream(srcFile);
		InputStream zipIn = new FileInputStream(fromZip);
		assertTrue("Expecting ZipUtilsTest.java from zip to be equal to src", IOUtils.contentEquals(srcIn, zipIn));
		IOUtils.closeQuietly(srcIn);
		IOUtils.closeQuietly(zipIn);
	}

	@Test
	public void testExtractInvalidName() throws IOException {
		File destFile = new File(TestEnvironment.ABS_RUNTIME_DATA_PATH, "src.zip");
		File srcDir = TestUtil.detectTravelTestSrcDir();
		boolean b = ZipUtils.compressDir(destFile, srcDir, null, "\\.svn", /*includeRootName*/true);
		assertTrue("Expecting ZIP file to be created", b);
		assertTrue("Expecting ZIP file to be created", destFile.exists());
		log.debug("Zip File: " + destFile);

		File fromZip = ZipUtils.extractZipEntry(destFile, "non-existing-file", new File(TestEnvironment.ABS_RUNTIME_DATA_PATH));
		assertNull("Expecting file to be extracted", fromZip);
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(ZipUtils.class);
	}
}
