/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: CopyDatabaseContentTest.java
 * @date: 23.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.launcher.procedures;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.dynatrace.easytravel.utils.TestEnvironment;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;


/**
 *
 * @author stefan.moschinski
 */
public class CopyDatabaseContentTest {

	/**
	 * Test method for {@link com.dynatrace.easytravel.launcher.procedures.CopyDatabaseContent#copyDbDataFrom(java.lang.String)}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCopyDbDataFrom() throws IOException {
		File source = new File(TestEnvironment.TEST_DATA_PATH, "dbfakedata");

		Set<String> expectedFiles = getSimpleFileNames(source);


		File dest = new File(FileUtils.getTempDirectory(), "filesTemp");

		new CopyDatabaseContent(dest).copyDbDataFrom(source.getAbsolutePath());

		Set<String> realFiles = getSimpleFileNames(dest);

		assertThat(realFiles, containsInAnyOrder(expectedFiles.toArray(new String[0])));
	}

	@Test
	public void testCopyDbDataFromZip() throws IOException {
		File source = new File(TestEnvironment.TEST_DATA_PATH, "dbfakedata");

		Set<String> expectedFiles = getSimpleFileNames(source); // we use the normal directory for expectations --> contains the
// same content like the zip

		File sourceZip = new File(TestEnvironment.TEST_DATA_PATH, "dbfakedata.zip");
		File dest = new File(FileUtils.getTempDirectory(), "dbfakedata");


		new CopyDatabaseContent(dest).copyDbDataFrom(sourceZip.getAbsolutePath());

		Set<String> realFiles = getSimpleFileNames(dest);

		assertThat(expectedFiles, hasItems(realFiles.toArray(new String[0])));
	}

	@Test
	public void testCopyDbDataFromZipWithSuperDir() throws IOException {
		File source = new File(TestEnvironment.TEST_DATA_PATH, "dbfakedata");

		Set<String> expectedFiles = getSimpleFileNames(source); // we use the normal directory for expectations --> contains the
// same content like the zip

		File sourceZip = new File(TestEnvironment.TEST_DATA_PATH, "dbfakedata2.zip");
		File dest = new File(FileUtils.getTempDirectory(), "ZipFileFolder/");

		new CopyDatabaseContent(dest).copyDbDataFrom(sourceZip.getAbsolutePath());

		Set<String> realFiles = getSimpleFileNames(dest);

		assertThat(expectedFiles, hasItems(realFiles.toArray(new String[0])));
	}


	private Set<String> getSimpleFileNames(File dest) {
		return FluentIterable.from(Arrays.asList(dest.listFiles())).transform(new Function<File, String>() {

			@Override
			public String apply(File input) {
				return input.getName();
			}
		}).toSet();
	}


}
