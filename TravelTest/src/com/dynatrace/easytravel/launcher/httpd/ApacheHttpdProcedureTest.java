package com.dynatrace.easytravel.launcher.httpd;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.TestUtil;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.engine.ApacheHttpdProcedure;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.process.AbstractProcess;
import com.dynatrace.easytravel.launcher.process.Process;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.utils.TestEnvironment;

public class ApacheHttpdProcedureTest {
	
    static {
        TestUtil.setInstallDirCorrection();
    }

	private static final String EASYTRAVEL_CONFIG_PATH = Directories.getConfigDir().getAbsolutePath();
	private static final File HTTPD_CONF = new File(EASYTRAVEL_CONFIG_PATH, "httpd.conf");

	private File dest = new File(EASYTRAVEL_CONFIG_PATH + "/httpd.conf");
	private File testHttpdConf = new File(TestEnvironment.TEST_DATA_PATH, "httpd.conf");

	@Before
	public void setUp() throws IOException {
		// make sure we have a clean state before starting the tests
		if(dest.exists()) {
			FileUtils.forceDelete(dest);
		}

		assertTrue(testHttpdConf.exists());
	}

	@After
	public void tearDown() throws IOException {
		// make sure the file can be deleted on Windows afterwards.
		// If the file is still open somewhere, this will fail on Windows because the file will be locked
		if(dest.exists()) {
			FileUtils.forceDelete(dest);
		}
	}

	/**
	 * Test 001
	 * Test if path to the correct Apache configuration file is passed to the Apache process being created.
	 * @throws CorruptInstallationException
	 */
	@Test
	public void testHttpdConf_001() throws CorruptInstallationException {
		MockApacheHttpdProcedure apache = new MockApacheHttpdProcedure(new DefaultProcedureMapping("apache"));
		MyProcess process = (MyProcess) apache.getProcess();
		List<String> appArguments = process.getAppArs();
		assertTrue("Should contain correct path to  httpd.conf in user home area " + appArguments.toString(),
			appArguments.toString().contains(HTTPD_CONF.getAbsolutePath()));
	}

	/**
	 * Test 002:
	 * Input conditions:
	 * 		- config.apacheWebServerUsesGeneratedHttpdConfig=true
	 * 		- config.apacheWebServerHttpdConfig irrelevant
	 * Expected result:
	 * 		A new httpd.conf is created in user home area.
	 * 		The folder and its content in user home area should also
	 * 		be copied there, but we do not test it and it would be difficult to
	 * 		test as it should be copied from the installation directory
	 * 		and it is not obvious how toe access it from the Tests.
	 *
	 * @throws CorruptInstallationException
	 * @throws IOException
	 */
	@Test
	public void testHttpdConf_002() throws CorruptInstallationException, IOException {
		EasyTravelConfig config = EasyTravelConfig.read();
		try {

			config.apacheWebServerUsesGeneratedHttpdConfig = true;

			//========================================
			// Place a know copy of httpd.conf
			// in user home area, so that we can
			// recognize if it has been overwritten.
			//========================================

			/* Leaving this code in as an educational example of how NOT to write JUnit tests
			 if (testHttpdConf.exists()) {

				try {
					FileUtils.copyFile(testHttpdConf, dest);
				} catch (IOException e) {
					assertTrue(false);
				}
			} else {
				// test config file not supplied
				assertTrue(false);
			}
			*/
			FileUtils.copyFile(testHttpdConf, dest);

			//========================================
			// Create the class being tested
			//========================================
			new MockApacheHttpdProcedure(new DefaultProcedureMapping("apache"));

			// assume overwrite will not happen ie. assume failure
			assertFalse("Expected files to have changed, but they are the same.", FileUtils.contentEquals(testHttpdConf, dest));

		} finally {
			EasyTravelConfig.resetSingleton();
		}

	}

	/**
	 * Test 003:
	 * Input conditions:
	 * 		- config.apacheWebServerUsesGeneratedHttpdConfig=false
	 * 		- config.apacheWebServerHttpdConfig=<a custom Apache config file>
	 *		- the custom file exists
	 *		- httpd.conf already exists in user home
	 * Expected result:
	 * 		httpd.conf in the user home area is overwritten with the custom config file
	 *
	 * @throws CorruptInstallationException
	 * @throws IOException
	 */
	@Test
	public void testHttpdConf_003() throws CorruptInstallationException, IOException {

		EasyTravelConfig config = EasyTravelConfig.read();
		try {

			//========================================
			// Set up configuration properties for this test.
	 		// config.apacheWebServerUsesGeneratedHttpdConfig=false
			// config.apacheWebServerHttpdConfig=<path to a custom Apache config file>
			//========================================

			config.apacheWebServerUsesGeneratedHttpdConfig = false;
			File externalHttpdConf = new File(TestEnvironment.TEST_DATA_PATH, "httpd_external.conf");
			config.apacheWebServerHttpdConfig=externalHttpdConf.getAbsolutePath();

			//========================================
			// Place a know copy of httpd.conf
			// in user home area, so that we can
			// recognize if it has been overwritten.
			//========================================

			FileUtils.copyFile(testHttpdConf, dest);

			//========================================
			// Create the class being tested
			//========================================
			new MockApacheHttpdProcedure(new DefaultProcedureMapping("apache"));

			//========================================
			// Test to see if httpd.conf has been
			// overwritten with the external file:
			// compare httpd.conf now in user home
			// with the external file:
			//
			// So test passes if compare returns: true
			//========================================

			assertTrue("We expect the file have the same contents.", FileUtils.contentEquals(externalHttpdConf, dest));
		} finally {
			EasyTravelConfig.resetSingleton();
		}

	}

	/**
	 * Test 004:
	 * Input conditions:
	 * 		- config.apacheWebServerUsesGeneratedHttpdConfig=false
	 * 		- config.apacheWebServerHttpdConfig=<a custom Apache config file>
	 *		- the custom file DOES NOT exist
	 *		- httpd.conf already exists in user home
	 * Expected result:
	 * 		httpd.conf in the user home area is not overwritten or re-generated
	 *
	 * @throws CorruptInstallationException
	 * @throws IOException
	 */
	@Test
	public void testHttpdConf_004() throws CorruptInstallationException, IOException {

		EasyTravelConfig config = EasyTravelConfig.read();
		try {

			//========================================
			// Set up configuration properties for this test.
	 		// config.apacheWebServerUsesGeneratedHttpdConfig=false
			// config.apacheWebServerHttpdConfig
			// 		=<path to a custom Apache config file that does not exist>
			//========================================

			config.apacheWebServerUsesGeneratedHttpdConfig = false;
			File externalHttpdConf = new File(TestEnvironment.TEST_DATA_PATH, "non_existent_httpd_external.conf");
			config.apacheWebServerHttpdConfig=externalHttpdConf.getAbsolutePath();

			//========================================
			// Place a know copy of httpd.conf
			// in user home area, so that we can
			// recognize if it has been overwritten.
			//========================================

			FileUtils.copyFile(testHttpdConf, dest);

			//========================================
			// Create the class being tested
			//========================================
			new MockApacheHttpdProcedure(new DefaultProcedureMapping("apache"));

			//========================================
			// Test to see if httpd.conf has
			// not been regenerated.
			// compare httpd.conf now in user home
			// with the sample file that we copied there:
			// If the file has been re-generated,
			// the compare will fail.
			//
			// So test passes if compare returns: true
			//========================================

			assertTrue("We expect the file have the same contents.", FileUtils.contentEquals(testHttpdConf, dest));
		} finally {
			EasyTravelConfig.resetSingleton();
		}

	}

	/**
	 * Test 005:
	 * Input conditions:
	 * 		- config.apacheWebServerUsesGeneratedHttpdConfig=false
	 * 		- config.apacheWebServerHttpdConfig=<a custom Apache config file>
	 *		- the custom file DOES NOT exist
	 *		- httpd.conf does not exists in user home
	 * Expected result:
	 * 		httpd.conf in the user home area is generated
	 *
	 * @throws CorruptInstallationException
	 * @throws IOException
	 */
	@Test
	public void testHttpdConf_005() throws CorruptInstallationException, IOException {
		EasyTravelConfig config = EasyTravelConfig.read();
		try {

			//========================================
			// Set the property:
	 		// config.apacheWebServerUsesGeneratedHttpdConfig=false
			// config.apacheWebServerHttpdConfig
			// 		=<path to a custom Apache config file that does not exist>
			//========================================

			config.apacheWebServerUsesGeneratedHttpdConfig = false;
			File externalHttpdConf = new File(TestEnvironment.TEST_DATA_PATH, "non_existent_httpd_external.conf");
			config.apacheWebServerHttpdConfig=externalHttpdConf.getAbsolutePath();

			//========================================
			// If an httpd.conf is found in user home
			// then delete it.
			//========================================

			//========================================
			// Create the class being tested
			//========================================
			new MockApacheHttpdProcedure(new DefaultProcedureMapping("apache"));

			//========================================
			// Test to see if httpd.conf has
			// been regenerated.
			//
			// Test passes if it has been re-generated.
			//========================================

			assertTrue(dest.exists());
		} finally {
			EasyTravelConfig.resetSingleton();
		}

	}

	/**
	 * Test 006:
	 * Input conditions:
	 * 		- config.apacheWebServerUsesGeneratedHttpdConfig=false
	 * 		- config.apacheWebServerHttpdConfig not set
	 *		- httpd.conf already exists in user home
	 * Expected result:
	 * 		httpd.conf in the user home area is not overwritten or re-generated
	 *
	 * @throws CorruptInstallationException
	 * @throws IOException
	 */
	@Test
	public void testHttpdConf_006() throws CorruptInstallationException, IOException {

		EasyTravelConfig config = EasyTravelConfig.read();
		try {

			//========================================
			// Set up configuration properties for this test.
	 		// config.apacheWebServerUsesGeneratedHttpdConfig=false
			// config.apacheWebServerHttpdConfig not set
			//========================================

			config.apacheWebServerUsesGeneratedHttpdConfig = false;
			config.apacheWebServerHttpdConfig="";

			//========================================
			// Place a know copy of httpd.conf
			// in user home area, so that we can
			// recognize if it has been overwritten.
			//========================================

			FileUtils.copyFile(testHttpdConf, dest);

			//========================================
			// Create the class being tested
			//========================================
			new MockApacheHttpdProcedure(new DefaultProcedureMapping("apache"));

			//========================================
			// Test to see if httpd.conf has
			// not been regenerated.
			// compare httpd.conf now in user home
			// with the sample file that we copied there:
			// If the file has been re-generated,
			// the compare will fail.
			//
			// So test passes if compare returns: true
			//========================================

			assertTrue("We expect the file have the same contents.", FileUtils.contentEquals(testHttpdConf, dest));

		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}

	/**
	 * Test 007:
	 * Input conditions:
	 * 		- config.apacheWebServerUsesGeneratedHttpdConfig=false
	 * 		- config.apacheWebServerHttpdConfig empty
	 *		- httpd.conf does not exists in user home
	 * Expected result:
	 * 		httpd.conf in the user home area is generated
	 *
	 * @throws CorruptInstallationException
	 * @throws IOException
	 */
	@Test
	public void testHttpdConf_007() throws CorruptInstallationException, IOException {
		EasyTravelConfig config = EasyTravelConfig.read();
		try {

			//========================================
			// Set the property:
	 		// config.apacheWebServerUsesGeneratedHttpdConfig=false
			// config.apacheWebServerHttpdConfig empty
			//========================================

			config.apacheWebServerUsesGeneratedHttpdConfig = false;
			config.apacheWebServerHttpdConfig="";

			//========================================
			// If an httpd.conf is found in user home
			// then delete it.
			//========================================


			//========================================
			// Create the class being tested
			//========================================
			new MockApacheHttpdProcedure(new DefaultProcedureMapping("apache"));

			//========================================
			// Test to see if httpd.conf has
			// been regenerated.
			//
			// Test passes if it has been re-generated.
			//========================================

			assertTrue(dest.exists());
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}


	private final class MockApacheHttpdProcedure extends ApacheHttpdProcedure {

		public MockApacheHttpdProcedure(ProcedureMapping mapping)
				throws CorruptInstallationException {
			super(mapping);
		}

		@Override
		protected Process createProcess(ProcedureMapping mapping)
				throws CorruptInstallationException {
			return new MyProcess(null);
		}
	}

	private final class MyProcess extends AbstractProcess {

		protected MyProcess(DtAgentConfig dtAgentConfig) {
			super(dtAgentConfig);
		}

		@Override
		public CommandLine createCommand() {
			return new CommandLine("somecommand");
		}

		public List<String> getAppArs() {
			return super.getApplicationArguments();
		}
	}
}
