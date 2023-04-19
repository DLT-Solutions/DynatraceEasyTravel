package com.dynatrace.easytravel.config;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;

import org.junit.Test;

import com.dynatrace.easytravel.util.DirUtils;

public class DirUtilsTest {

	/**
	 * This test should work both local on dev-machine (working inside TravelTest/)
	 * and in dist execution environment (working inside Distribution/dist/test).
	 * Note: Needs an ant build in prior.
	 *
	 * @author philipp.grasboeck
	 */
	@Test
	public void testExpectedPaths() {
		File[] startArray = {
				new File("."),
				new File("..")
		};
		String[] pathArray = {
				"Distribution/dist/customer",
				"Distribution/dist/customer/webapp",
				"Distribution/dist/business",
				"Distribution/dist/business/webapp",
		};

		for (File start : startArray) {
			for (String path : pathArray) {
				try {
					start = start.getCanonicalFile();
				} catch (IOException e) {
					start = start.getAbsoluteFile();
				}
				File result = DirUtils.findPathRelativeTo(start, path);
				Assert.assertNotNull("Expecting to find " + path + " from " + start, result);
				Assert.assertTrue("Expecting to find " + path + " from " + start, result.exists());
				System.out.println(path + " -> " + result);
			}
		}
	}

	/**
	 * Should also work in the installed folder (Program Files etc.)
	 *
	 * @throws IOException
	 * @author philipp.grasboeck
	 */
	@Test
	public void testExpectedDirs() throws IOException {
		String[] dirNames = {
				"customer",
				"business",
				"plugins-frontend",
				"plugins-backend",
				"plugins-shared"
		};
		String[] relativePaths = {
				".",
				"..",
				"../Distribution/dist"
		};

		for (String dirName : dirNames) {
			File result = DirUtils.findDir(dirName, relativePaths);

			Assert.assertNotNull("Expecting to find " + dirName, result);
			Assert.assertTrue("Expecting to find " + dirName, result.exists());

			System.out.println(result);

		}


	}
}
