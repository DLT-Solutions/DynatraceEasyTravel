/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: ApacheHttpdUtilsTest.java
 * @date: 07.01.2012
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.launcher.httpd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.TestUtil;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;
import com.dynatrace.easytravel.utils.TestHelpers;


/**
 *
 * @author dominik.stadler
 */
public class ApacheHttpdUtilsTest {

    static {
    	TestUtil.setInstallDirCorrection();
    }

	
	private static File pidFile = new File(Directories.getTempDir(), "httpd.pid");

	@Before
	public void setUp() {
		if(pidFile.exists()) {
			assertTrue(pidFile.delete());
		}
	}

	@After
	public void tearDown() {
		// clean up file afterwards as well to avoid failing tests which run afterwards
		if(pidFile.exists()) {
			assertTrue(pidFile.delete());
		}
	}

	/**
	 * Test method for {@link com.dynatrace.easytravel.launcher.httpd.ApacheHttpdUtils#getExecutableDependingOnOs()}.
	 */
	@Test
	public void testGetExecutableDependingOnOs() {
		assertNotNull(ApacheHttpdUtils.getExecutableDependingOnOs());
	}

	/**
	 * Test method for {@link com.dynatrace.easytravel.launcher.httpd.ApacheHttpdUtils#killIfNotTerminatedLinux()}.
	 * @throws IOException
	 */
	@Test
	public void testKillIfNotTerminatedLinux() throws IOException {
		// create Dummy-PID file
		FileUtils.writeStringToFile(pidFile, "1234567890");

		ApacheHttpdUtils.killIfNotTerminatedLinux();
	}

	@Test
	public void testKillIfNotTerminatedLinuxIOException() throws IOException {
		// create Dummy-PID file
		FileUtils.writeStringToFile(pidFile, "notanumber");

		try {
			ApacheHttpdUtils.killIfNotTerminatedLinux();
			// only fail on non-Windows systems here...
			if(!OperatingSystem.pickUp().equals(OperatingSystem.WINDOWS)) {
				fail("Should throw Exception with invalid pid-number");
			}
		} catch (IOException e) {
			TestHelpers.assertContains(e, "Could not convert", "notanumber");
		}
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(ApacheHttpdUtils.class);
	}
	
	@Test
	public void testIsUsedOsWindows() {
		assertEquals(ApacheHttpdUtils.isUsedOsWindows(), OperatingSystem.pickUp().equals(OperatingSystem.WINDOWS));
	}
		
	@Test
	public void testApachePaths() {
		assertEquals( Directories.getInstallDir().getAbsolutePath() + File.separator + ApacheHttpdUtils.APACHE_VERSION, ApacheHttpdUtils.INSTALL_APACHE_PATH );
		assertEquals( ApacheHttpdUtils.INSTALL_APACHE_PATH + File.separator + ApacheHttpdUtils.getApacheInstallDirForUsedOs(), ApacheHttpdUtils.APACHE_OS_SPECIFIC_PATH );
		assertEquals( ApacheHttpdUtils.APACHE_OS_SPECIFIC_PATH + File.separator + "bin", ApacheHttpdUtils.APACHE_BIN_PATH );
		assertEquals( ApacheHttpdUtils.APACHE_BIN_PATH + File.separator + "httpd", ApacheHttpdUtils.HTTPD_EXE_PATH );
		assertEquals( ApacheHttpdUtils.INSTALL_APACHE_PATH + File.separator + "plain_conf", ApacheHttpdUtils.APACHE_PLAIN_CONF );
		assertEquals( Directories.getConfigDir().getAbsolutePath() + File.separator + "httpd.conf", ApacheHttpdUtils.APACHE_CONF );
		assertEquals( Directories.getTempDir().getAbsolutePath(), ApacheHttpdUtils.APACHE_RUNTIME_DIR );
		assertEquals( Directories.getConfigDir().getAbsolutePath() + File.separator + "php.ini", ApacheHttpdUtils.PHP_INI );
	}
	
}
