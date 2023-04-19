package com.dynatrace.easytravel.config;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.SystemProperties;
import com.dynatrace.easytravel.util.ConfigurationProvider;
import com.dynatrace.easytravel.util.TextUtils;
import com.dynatrace.easytravel.utils.TestHelpers;
import com.dynatrace.easytravel.utils.ThreadTestHelper;


/**
 *
 * @author dominik.stadler
 * @author stefan.moschinski
 */
public class EasyTravelConfigTest {

	private static final Logger LOGGER = Logger.getLogger(EasyTravelConfigTest.class.getName());

	private static final int NUMBER_OF_THREADS = 10;

	private static final int NUMBER_OF_TESTS = 100000;

	private static File TEST_DIR = null;

	@BeforeClass
	public static void setUpClass() {
		if (System.getProperty(BaseConstants.SystemProperties.HOME_DIR_CORRECTION) != null) {
			LOGGER.info("Found home dir correction: " + System.getProperty(BaseConstants.SystemProperties.HOME_DIR_CORRECTION));
		} else {
			LOGGER.info("Setting home dir correction to 'easyTravelUnitTest'");
			System.setProperty(BaseConstants.SystemProperties.HOME_DIR_CORRECTION, "easyTravelUnitTest");
		}

		String tempDir = System.getProperty("java.io.tmpdir");
		TEST_DIR = new File(tempDir, EasyTravelConfigTest.class.getCanonicalName());
	}

	@Before
	public void setUp() {
		// make sure the override property file is removed again and re-read config
		File file = EasyTravelConfig.getEasyTravelLocalPropertiesFile();
		LOGGER.info("File during setup: " + file);
		file.delete();
		//delete private properties
		file = EasyTravelConfig.getEasyTravelPrivatePropertiesFile();
		LOGGER.info("File during setup: " + file);
		file.delete();

		// ensure that config and tempdir are existing before running tests
		assertTrue(Directories.getConfigDir().exists() || Directories.getConfigDir().mkdirs());
		assertTrue(Directories.getTempDir().exists() || Directories.getTempDir().mkdirs());
		EasyTravelConfig.resetSingleton();
	}

	@AfterClass
	public static void clearUp() {
		if (TEST_DIR != null) {
			TEST_DIR.delete();
		}

		// make sure the override property file is removed again
		File file = EasyTravelConfig.getEasyTravelLocalPropertiesFile();
		LOGGER.info("Deleting " + file);
		file.delete();
		//delete private properties
		file = EasyTravelConfig.getEasyTravelPrivatePropertiesFile();
		LOGGER.info("File during setup: " + file);
		file.delete();
	}

	@Test
	public void testCreateSingleton() {
		try {
			EasyTravelConfig.createSingleton(EasyTravelConfig.read().filePath);
			fail("Will throw exception as it is called a second time here");
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "a second time");
		}
	}

	@Test
	public void testJavaopts() {
		EasyTravelConfig config = EasyTravelConfig.read();
		assertNotNull(config.javaopts);
		assertTrue("Expected at least one java-option, but had: " + Arrays.toString(config.javaopts),
				config.javaopts.length >= 1);
		assertTrue("We expect to find -Xmx as first item in frontendJavaopts, but is not contained: " + config.frontendJavaopts,
				config.frontendJavaopts[0].contains("-Xmx"));
		assertTrue("We expect to find -Xmx as first item in angularFrontendJavaopts, but is not contained: " + config.angularFrontendJavaopts,
				config.angularFrontendJavaopts[0].contains("-Xmx"));
		assertTrue("We expect to find -Xmx as first item in backendJavaopts, but is not contained: " + config.backendJavaopts,
				config.backendJavaopts[0].contains("-Xmx"));
	}

	@Test
	public void testGlobalPoolStrategy() {
		EasyTravelConfig config = EasyTravelConfig.read();
		assertNotNull(config.serviceStubStrategy);
		assertTrue("Expecting globalPool serviceStubStrategy",
				config.serviceStubStrategy == EasyTravelConfig.ServiceStubStrategy.globalPool);
	}

	@Test
	public void testTrailingSlash() {
		EasyTravelConfig config = EasyTravelConfig.read();
		assertTrue("Should end with trailing slash: " + config.dtServerWebURL,
				config.dtServerWebURL.endsWith(BaseConstants.FSLASH));
		assertTrue("Should end with trailing slash: " + config.frontendContextRoot,
				config.frontendContextRoot.endsWith(BaseConstants.FSLASH));
		assertTrue("Should end with trailing slash: " + config.weblauncherContextRoot,
				config.weblauncherContextRoot.endsWith(BaseConstants.FSLASH));
		assertTrue("Should end with trailing slash: " + config.backendContextRoot,
				config.backendContextRoot.endsWith(BaseConstants.FSLASH));
		assertTrue("Should end with trailing slash: " + config.webServiceBaseDir,
				config.webServiceBaseDir.endsWith(BaseConstants.FSLASH));
		assertTrue("Should end with trailing slash: " + config.dotNetBackendWebServiceBaseDir,
				config.dotNetBackendWebServiceBaseDir.endsWith(BaseConstants.FSLASH));
	}

	@Test
	public void testGetAbsolutePropertiesFilePath() throws IOException {
		File dirWithSpaceChars = new File(TEST_DIR, "some directory");
		dirWithSpaceChars.mkdirs();

		File fileWithSpaceChars = new File(dirWithSpaceChars, "easyTravelConfig.properties");
		fileWithSpaceChars.createNewFile();

		String pathWithSpaceChars = fileWithSpaceChars.getAbsolutePath();
		LOGGER.info("test file with whitepsace character in path: " + pathWithSpaceChars);

		assertEquals(pathWithSpaceChars, EasyTravelConfig.getAbsolutePropertiesFilePath(pathWithSpaceChars,true));

		assertNotNull(EasyTravelConfig.getAbsolutePropertiesFilePath("easyTravelConfig",true));

		try {
			assertNotNull(EasyTravelConfig.getAbsolutePropertiesFilePath("NotExist",true));
			fail("Should not find resource");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "NotExist.properties");
		}
	}

	@Test
	public void testEmptyValueIsNull() {
		EasyTravelConfig config = EasyTravelConfig.read();
		assertEquals("Usually autostartGroup is not set, therefore we expect it to be '', but it was: '" + config.autostartGroup +
				"'",
				"", config.autostartGroup);
	}

	@Test
	public void testEnhance() {
		EasyTravelConfig config = EasyTravelConfig.read();
		assertEquals("localhost", config.dtServer);

		Properties prop = new Properties();
		prop.setProperty("config.dtServer", "myhost");

		config.enhance(prop);

		assertEquals("myhost", config.dtServer);

		// also test with some agent-pathes and ensure that these are adjusted correctly for the current platform
		String value = "C:/some\\path/with\\different/path\\separator";
		prop.setProperty("config.agent", value);

		config.enhance(prop);

		assertEquals("myhost", config.dtServer);
		assertEquals(value.replace("/", File.separator).replace("\\", File.separator), config.agent);

		// reset to not influence other tests
		EasyTravelConfig.resetSingleton();
	}

	@Test
	public void testStore() throws IOException {
		EasyTravelConfig config = EasyTravelConfig.read();
		assertEquals("localhost", config.dtServer);
		assertEquals(InstallationType.APM, config.apmServerDefault);

		Properties prop = new Properties();
		prop.setProperty("config.dtServer", "myhost");

		config.enhance(prop);
		try {
			// get a unique file name
			File file = File.createTempFile("easyTravelConfig", ".properties", Directories.getExistingTempDir());

			// ensure that properties which are "unlisted" in the .properties-file are still stored
			config.antEnvArgs = new String[] { "key=2134234" };
			config.shortHostDisplay = false;

			// store the properties to the file
			config.store(file);

			verifyProperties(file);

			String content = FileUtils.readFileToString(file);
			assertTrue(content.contains("2134234"));
			assertTrue(content.contains("config.antEnvArgs=key\\=2134234"));
			assertTrue(content.contains("config.shortHostDisplay=false"));
		} catch (IOException ioe) {
			throw new IOException(ioe.getMessage() + " " + Directories.getTempDir(), ioe);
		} finally {
			// reset to not influence other tests
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testStoreAPM() throws IOException {
		EasyTravelConfig config = EasyTravelConfig.read();

		config.apmServerDefault = InstallationType.APM;

		assertEquals(InstallationType.APM, config.apmServerDefault);

		try {
			// get a unique file name
			File file = File.createTempFile("easyTravelConfig", ".properties", Directories.getExistingTempDir());

			// store the properties to the file
			config.store(file);

			// verify that the changed enum was stored correctly
			String content = FileUtils.readFileToString(file);
			assertTrue(content.contains("config.apmServerDefault=APM"));
		} catch (IOException ioe) {
			throw new IOException(ioe.getMessage() + " " + Directories.getTempDir(), ioe);
		} finally {
			// reset to not influence other tests
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testStoreInTempDir() throws IOException {
		EasyTravelConfig config = EasyTravelConfig.read();
		assertEquals("localhost", config.dtServer);

		Properties prop = new Properties();
		prop.setProperty("config.dtServer", "myhost");

		config.enhance(prop);

		try {
			// store the properties to the file
			File file = config.storeInTempFile();

			verifyProperties(file);
		} finally {
			// reset to not influence other tests
			EasyTravelConfig.resetSingleton();
		}
	}

	public void verifyProperties(File file) throws IOException, FileNotFoundException {
		Properties newProp = new Properties();
		newProp.load(new FileInputStream(file));

		String configfilePath = EasyTravelConfig.getAbsolutePropertiesFilePath("easyTravelConfig",true);
		Properties origProp = ConfigurationProvider.readPropertyFile(configfilePath);

		// first check the one property that we changed
		assertEquals("File: " + file.getAbsolutePath(),
				"myhost", newProp.getProperty("config.dtServer"));

		for (String name : origProp.stringPropertyNames()) {
			// we changed one property, this will not be equal
			if (
					// the remoting-items are only used by .NET
					!name.equals("config.dtServer") &&
					!name.equals("remotingHost") &&
					!name.equals("remotingPort")

					// some that are changed locally for me
					/* && !name.equals("config.agent") && !name.equals("config.frontendPortRangeStart") &&
					!name.equals("config.frontendPortRangeEnd") &&
					!name.equals("config.paymentBackendEnvArgs") && !name.equals("config.b2bFrontendEnvArgs") &&
					!name.equals("config.apacheWebServerAgent") &&
					!name.equals("config.creditCardAuthorizationAgent") &&
					!name.equals("config.frontendAgent") &&
					!name.equals("config.backendAgent") &&
					!name.equals("config.antAgent")*/
				) {
				assertEquals("Property: " + name + ", " +
						"\nOrig: " + origProp.getProperty(name) + ", " +
						"\nNew: " + newProp.getProperty(name) + ", " +
						"\nFile: " + file.getAbsolutePath() + "\n",
						origProp.getProperty(name), newProp.getProperty(name));
			}
		}
	}

	@Test
	public void testRecursivePropertyUse() {
		EasyTravelConfig config = EasyTravelConfig.read();
		assertNotNull(config.databaseUrl);
		assertFalse(config.databaseUrl.contains("${config.internalDatabasePort}"));
	}

	@Test
	public void testSystemPropertyOverrideInValue() {
		EasyTravelConfig config = EasyTravelConfig.read();
		int dbPort = config.internalDatabasePort;
		String dbUrl = config.databaseUrl;
		assertNotNull(config.databaseUrl);
		assertFalse("Had: " + config.databaseUrl, config.databaseUrl.contains("${config.internalDatabasePort}"));
		assertTrue("Had: " + config.databaseUrl, config.databaseUrl.contains(":" + dbPort));

		int newDbPort = dbPort + 1111;
		System.setProperty("config.internalDatabasePort", "" + newDbPort);
		EasyTravelConfig.resetSingleton();
		config = EasyTravelConfig.read();
		String newDbUrl = config.databaseUrl;
		assertNotNull(config.databaseUrl);
		assertFalse(dbUrl.equals(newDbUrl));
		assertFalse("Had: " + config.databaseUrl, config.databaseUrl.contains("${config.internalDatabasePort}"));
		assertTrue("Had: " + config.databaseUrl, config.databaseUrl.contains(":" + newDbPort));

		LOGGER.info("Overridden databaseUrl: " + config.databaseUrl);
	}

	@Test
	public void testSystemPropertyOverride() {
		System.setProperty("config.dotNetBackendWebServiceBaseDir", "mywebservices/");
		EasyTravelConfig.resetSingleton();
		EasyTravelConfig config = EasyTravelConfig.read();
		assertNotNull(config.dotNetBackendWebServiceBaseDir);
		assertEquals("mywebservices/", config.dotNetBackendWebServiceBaseDir);
	}

	@Test
	public void testSystemPropertyOverrideWithRecursion() {
		System.setProperty("config.dotNetBackendWebServiceBaseDir",
				"https://${config.backendHost}:${config.paymentBackendPort}/mywebservices/");
		EasyTravelConfig.resetSingleton();
		EasyTravelConfig config = EasyTravelConfig.read();
		assertNotNull(config.dotNetBackendWebServiceBaseDir);
		assertTrue(config.dotNetBackendWebServiceBaseDir.startsWith("https://"));
		assertFalse("Had: " + config.databaseUrl, config.databaseUrl.contains("${config.backendHost}"));
		assertFalse("Had: " + config.databaseUrl, config.databaseUrl.contains("${config.paymentBackendPort}"));

		LOGGER.info("Overridden dotNetBackendWebServiceBaseDir: " + config.dotNetBackendWebServiceBaseDir);
	}

	@Test
	public void testSystemPropertyOverrideWithRecursionFromSystem() {
		System.setProperty("myproperty1", "mywebservices/");
		System.setProperty("config.dotNetBackendWebServiceBaseDir", "${myproperty1}");
		EasyTravelConfig.resetSingleton();
		EasyTravelConfig config = EasyTravelConfig.read();
		assertNotNull(config.dotNetBackendWebServiceBaseDir);
		assertEquals("mywebservices/", config.dotNetBackendWebServiceBaseDir);
	}

	@Test
	public void testAdaptPath() {
		System.setProperty("config.frontendAgent", "C:\\data");
		System.setProperty("config.agent", "C:/data/data");
		System.setProperty("config.antAgent", "C:///data");
		System.setProperty("config.creditCardAuthorizationAgent", "C:/\\data");
		System.setProperty("config.backendAgent", "\\\\dynatrace.local\\\\check");
		System.setProperty("config.apacheWebServerAgent", "//dynatrace.local//check");

		try {
			EasyTravelConfig.resetSingleton();
			EasyTravelConfig config = EasyTravelConfig.read();
			String separator = File.separator;
			String doubleSeparator = separator + separator;

			assertEquals("C:" + separator + "data", config.frontendAgent);
			assertEquals("C:" + separator + "data" + separator + "data", config.agent);
			assertEquals("C:" + separator + "data", config.antAgent);
			assertEquals("C:" + separator + "data", config.creditCardAuthorizationAgent);
			assertEquals(doubleSeparator + "dynatrace.local" + separator + "check", config.backendAgent);
			assertEquals(doubleSeparator + "dynatrace.local" + separator + "check", config.apacheWebServerAgent);
		} finally {
			System.clearProperty("config.frontendAgent");
			System.clearProperty("config.agent");
			System.clearProperty("config.antAgent");
			System.clearProperty("config.creditCardAuthorizationAgent");
			System.clearProperty("config.backendAgent");
			System.clearProperty("config.apacheWebServerAgent");
		}
	}

	@Test
	public void testRangeCheck() throws Exception {
		EasyTravelConfig config = EasyTravelConfig.read();

		File file = config.storeInTempFile();
		String contents = FileUtils.readFileToString(file);

		// replace
		contents = contents.replace(Integer.toString(config.frontendPortRangeEnd), "7090");

		FileUtils.writeStringToFile(file, contents);

		// should throw exception now
		try {
			EasyTravelConfig.create(file.getAbsolutePath());
			fail("Should throw exception about invalid range for properties: \n" + contents);
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "7090", Integer.toString(config.frontendPortRangeStart));
		}
	}

	@Test
	public void testMultipleJavaOptsJLT45198() throws Exception {
		EasyTravelConfig config = EasyTravelConfig.read();
		File file = config.storeInTempFile();

		String contents = FileUtils.readFileToString(file);

		// ensure that we find this:
		assertTrue("Config-file should contain javaoption, but did not, contents: " + contents,
				contents.contains("Javaopts=-Xmx"));

		contents = contents.replace("config.backendJavaopts=-Xmx",
				"config.backendJavaopts=-Xmx64m,-Xdebug,-Xrunjdwp:transport=dt_socket,,server=y,,suspend=y,,address=5555");

		FileUtils.writeStringToFile(file, contents);

		EasyTravelConfig newconfig = EasyTravelConfig.create(file.getAbsolutePath());

		assertNotNull(newconfig.backendJavaopts);
		assertEquals(
				"Expected to have 4 elements, but had: " + newconfig.backendJavaopts.length + ": " +
						Arrays.toString(newconfig.backendJavaopts),
				4, newconfig.backendJavaopts.length);

		LOGGER.info("Had: " + toString(newconfig.backendJavaopts, "\n"));
	}

	public static String toString(Object[] a, String delimiter) {
		if (a == null) {
			return "null";
		}
		int iMax = a.length - 1;
		if (iMax == -1) {
			return "[]";
		}

		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = 0;; i++) {
			b.append(String.valueOf(a[i]));
			if (i == iMax) {
				return b.append(']').toString();
			}
			b.append(delimiter);
		}
	}

	@Test
	public void testOverrideConfig() throws Exception {
		// make sure the override property file is removed again
		File file = new File(Directories.getConfigDir(), EasyTravelConfig.LOCAL_PROPERTIES_FILE);
		file.delete();
		EasyTravelConfig.resetSingleton();

		assertEquals("", EasyTravelConfig.read().autostart);
		FileUtils.writeStringToFile(file, "config.autostart=TestAutostart");

		EasyTravelConfig.resetSingleton();
		assertEquals("TestAutostart", EasyTravelConfig.read().autostart);

		assertTrue(file.delete());
	}

	@Test
	public void testOverrideConfigUnicode() throws Exception {
		assertEquals("", EasyTravelConfig.read().autostart);
		File file = new File(Directories.getConfigDir(), EasyTravelConfig.LOCAL_PROPERTIES_FILE);
		FileUtils.writeStringToFile(file, "config.autostart=TestAutostart\u1234");

		EasyTravelConfig.resetSingleton();
		assertTrue(EasyTravelConfig.read().autostart.startsWith("TestAutostart"));

		assertTrue(file.delete());
	}

	@Test
	public void testOverrideConfigInvalidUnicode() throws Exception {
		assertEquals("", EasyTravelConfig.read().autostart);
		File file = new File(Directories.getConfigDir(), EasyTravelConfig.LOCAL_PROPERTIES_FILE);
		FileUtils.writeStringToFile(file, "config.autostart=TestAutostart\\uabcg");

		EasyTravelConfig.resetSingleton();
		try {
			EasyTravelConfig.read();
			fail("Should trow exception because of illegal unicode with 'g'");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "Malformed \\uxxxx encoding");
		}

		assertTrue(file.delete());
	}

	@Test
	public void testConfigOverridesProperties() throws IOException {
		File file = new File(Directories.getConfigDir(), EasyTravelConfig.LOCAL_PROPERTIES_FILE);

		// move away any existing file
		File backup = null;
		if (file.exists()) {
			backup = new File(Directories.getConfigDir(), EasyTravelConfig.LOCAL_PROPERTIES_FILE + ".backup");
			FileUtils.copyFile(file, backup);
		}

		try {
			// first with values
			FileUtils.writeStringToFile(file,
					"config.frontendPublicUrl=123\n" +
							"config.b2bFrontendPublicUrl=234\n" +
							"config.apacheFrontendPublicUrl=345\n" +
							"config.apacheB2BFrontendPublicUrl=456\n" +
							"config.angularFrontendPublicUrl=567\n", true);
			EasyTravelConfig.resetSingleton();
			assertEquals("123", EasyTravelConfig.read().frontendPublicUrl);
			assertEquals("234", EasyTravelConfig.read().b2bFrontendPublicUrl);
			assertEquals("345", EasyTravelConfig.read().apacheFrontendPublicUrl);
			assertEquals("456", EasyTravelConfig.read().apacheB2BFrontendPublicUrl);
			assertEquals("567", EasyTravelConfig.read().angularFrontendPublicUrl);
			assertFalse(EasyTravelConfig.read().isLocalEnvironment());

			// then with emtpy values
			FileUtils.writeStringToFile(file,
					"config.frontendPublicUrl=\n" +
							"config.b2bFrontendPublicUrl=\n" +
							"config.apacheFrontendPublicUrl=\n" +
							"config.apacheB2BFrontendPublicUrl=\n" +
							"config.angularFrontendPublicUrl=\n", true);
			EasyTravelConfig.resetSingleton();
			assertEquals(null, EasyTravelConfig.read().frontendPublicUrl);
			assertEquals(null, EasyTravelConfig.read().b2bFrontendPublicUrl);
			assertEquals(null, EasyTravelConfig.read().apacheFrontendPublicUrl);
			assertEquals(null, EasyTravelConfig.read().apacheB2BFrontendPublicUrl);
			assertEquals(null, EasyTravelConfig.read().angularFrontendPublicUrl);
			assertTrue(EasyTravelConfig.read().isLocalEnvironment());
		} finally {
			if (backup != null) {
				FileUtils.copyFile(backup, file);
			}
		}
	}

	@Test
	public void testConfigOverridesPrivateProperties() throws IOException {
		File localFile = new File(Directories.getConfigDir(), EasyTravelConfig.LOCAL_PROPERTIES_FILE);
		File privateFile = new File(Directories.getConfigDir(), EasyTravelConfig.PRIVATE_PROPERTIES_FILE);

		try {
			FileUtils.writeStringToFile(localFile,
					"config.frontendPublicUrl=123\n" +
					"config.b2bFrontendPublicUrl=234\n" +
					"config.apacheFrontendPublicUrl=345\n" +
					"config.apacheB2BFrontendPublicUrl=456\n" +
					"config.angularFrontendPublicUrl=567\n", true);
			EasyTravelConfig.resetSingleton();
			assertEquals("123", EasyTravelConfig.read().frontendPublicUrl);
			assertEquals("234", EasyTravelConfig.read().b2bFrontendPublicUrl);
			assertEquals("345", EasyTravelConfig.read().apacheFrontendPublicUrl);
			assertEquals("456", EasyTravelConfig.read().apacheB2BFrontendPublicUrl);
			assertEquals("567", EasyTravelConfig.read().angularFrontendPublicUrl);
			assertFalse(EasyTravelConfig.read().isLocalEnvironment());

			FileUtils.writeStringToFile(privateFile,
					"config.b2bFrontendPublicUrl=777\n" +
					"config.apacheFrontendPublicUrl=\n" +
					"config.apacheB2BFrontendPublicUrl=888\n" +
					"config.angularFrontendPublicUrl=999\n", true);

			// disable loading private properties
			System.setProperty(SystemProperties.DONT_USE_PRIVATE_ET_PROPERTIES, "true");
			EasyTravelConfig.resetSingleton();
			assertEquals("123", EasyTravelConfig.read().frontendPublicUrl);
			assertEquals("234", EasyTravelConfig.read().b2bFrontendPublicUrl);
			assertEquals("345", EasyTravelConfig.read().apacheFrontendPublicUrl);
			assertEquals("456", EasyTravelConfig.read().apacheB2BFrontendPublicUrl);
			assertEquals("567", EasyTravelConfig.read().angularFrontendPublicUrl);
			assertFalse(EasyTravelConfig.read().isLocalEnvironment());

			//enable loading private properties
			System.clearProperty(SystemProperties.DONT_USE_PRIVATE_ET_PROPERTIES);
			EasyTravelConfig.resetSingleton();
			assertEquals("123", EasyTravelConfig.read().frontendPublicUrl);
			assertEquals("777", EasyTravelConfig.read().b2bFrontendPublicUrl);
			assertEquals(null, EasyTravelConfig.read().apacheFrontendPublicUrl);
			assertEquals("888", EasyTravelConfig.read().apacheB2BFrontendPublicUrl);
			assertEquals("999", EasyTravelConfig.read().angularFrontendPublicUrl);
			assertFalse(EasyTravelConfig.read().isLocalEnvironment());


		} finally {
			localFile.delete();
			privateFile.delete();
		}
	}

	@Test
	public void testWriteLocalConfig() throws Exception {
		File file = new File(Directories.getConfigDir(), EasyTravelConfig.LOCAL_PROPERTIES_FILE);

		// move away any existing file
		File backup = null;
		if (file.exists()) {
			backup = new File(Directories.getConfigDir(), EasyTravelConfig.LOCAL_PROPERTIES_FILE + ".backup");
			FileUtils.copyFile(file, backup);
		}

		try {
			assertTrue("Could not delete file before running the test",
					!file.exists() || file.delete());

			// write when file is not existing
			verifyWrite(file);

			assertTrue(file.exists());

			// write property when the file is already existing
			verifyWrite(file);

			verifyComments(file);

			assertTrue(file.delete());
		} finally {
			if (backup != null) {
				FileUtils.copyFile(backup, file);
			}
		}
	}

	public void verifyComments(File file) throws IOException, FileNotFoundException {
		// finally write a comment to the file and ensure that it is still contained afterwards
		List<String> lines = FileUtils.readLines(file);
		lines.add("#comment123");
		FileUtils.writeLines(file, lines);

		// write property when the file is already existing
		verifyWrite(file);

		lines = FileUtils.readLines(file);
		assertTrue("Should still have comment in the property file, but had: " + lines,
				lines.contains("#comment123"));
	}

	public void verifyWrite(File file) throws IOException, FileNotFoundException {
		String t = "" + System.currentTimeMillis();
		EasyTravelConfig.writeLocalSetting("testproperty", t);

		assertTrue(file.exists());

		final Properties props = new Properties();
		InputStream in = new FileInputStream(file);
		try {
			props.load(in);
		} finally {
			in.close();
		}

		assertEquals("Property is contained with the correct value now",
				t, props.getProperty("testproperty"));
	}

	@Test
	public void testReload() throws Exception {
		EasyTravelConfig config = EasyTravelConfig.read();

		File file = config.storeInTempFile();
		String contents = FileUtils.readFileToString(file);

		// replace
		contents = contents.replace(Integer.toString(config.frontendPortRangeEnd), "7090");

		FileUtils.writeStringToFile(file, contents);
		EasyTravelConfig.read().filePath = file.getAbsolutePath();

		// should throw exception now
		try {
			EasyTravelConfig.reload();
			fail("Should throw exception about invalid range for properties: \n" + contents);
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "7090", Integer.toString(config.frontendPortRangeStart));
		}

		contents = contents.replace("7090", "8589");
		FileUtils.writeStringToFile(file, contents);
		EasyTravelConfig.read().filePath = file.getAbsolutePath();

		// reload works now
		EasyTravelConfig.reload();

		assertEquals("Expected 8589 as port range end, but had: " + EasyTravelConfig.read().frontendPortRangeEnd,
				8589, EasyTravelConfig.read().frontendPortRangeEnd);
	}

	@Test
	public void testNotifyListenersWorksWhenFileChanges() throws Exception {
		final AtomicInteger changeNo = new AtomicInteger();

		EasyTravelConfig.addConfigChangeListener(new ConfigChangeListener() {

			@Override
			public void notifyConfigLoaded(EasyTravelConfig oldCfg, EasyTravelConfig newCfg) {
				if (newCfg.equals(oldCfg)) {
					return;
				}
				changeNo.incrementAndGet();
			}
		});

		EasyTravelConfig config = EasyTravelConfig.read();
		assertEquals(1, changeNo.intValue());

		EasyTravelConfig.reload();
		assertEquals(1, changeNo.intValue());

		File file = config.storeInTempFile();
		String contents = FileUtils.readFileToString(file);
		contents = contents.replace("8095", "5555");
		FileUtils.writeStringToFile(file, contents);
		EasyTravelConfig.read().filePath = file.getAbsolutePath();

		EasyTravelConfig.reload();
		assertEquals(2, changeNo.intValue());

		file = config.storeInTempFile();
		contents = FileUtils.readFileToString(file);
		contents = contents.replace("8095", "5555");
		FileUtils.writeStringToFile(file, contents);
		EasyTravelConfig.read().filePath = file.getAbsolutePath();

		EasyTravelConfig.reload();
		assertEquals(2, changeNo.intValue());
	}

	@Test
	public void testNotifyListenersWorksWhenScenarioConfigOverridesProperty() throws Exception {
		final AtomicInteger changedNo = new AtomicInteger();

		EasyTravelConfig.addConfigChangeListener(new ConfigChangeListener() {

			@Override
			public void notifyConfigLoaded(EasyTravelConfig oldCfg, EasyTravelConfig newCfg) {
				if (newCfg.equals(oldCfg)) {
					return;
				}
				changedNo.incrementAndGet();
			}
		});

		assertEquals(0, changedNo.intValue());

		EasyTravelConfig.applyCustomSettings(Collections.singletonMap("config.frontendJavaopts", "-Xmx256m"));
		assertEquals(1, changedNo.intValue());
		assertEquals("-Xmx256m", EasyTravelConfig.read().frontendJavaopts[0]);

		EasyTravelConfig.applyCustomSettings(Collections.singletonMap("config.frontendJavaopts", "-Xmx256m"));
		assertEquals(1, changedNo.intValue());
		assertEquals("-Xmx256m", EasyTravelConfig.read().frontendJavaopts[0]);

		EasyTravelConfig.applyCustomSettings(Collections.singletonMap("config.frontendJavaopts", "-Xmx512m"));
		assertEquals(2, changedNo.intValue());
		assertEquals("-Xmx512m", EasyTravelConfig.read().frontendJavaopts[0]);
	}

	@Test
	public void testAccessThreaded() throws Throwable {
		ThreadTestHelper helper =
				new ThreadTestHelper(NUMBER_OF_THREADS, NUMBER_OF_TESTS);

		helper.executeTest(new ThreadTestHelper.TestRunnable() {

			@Override
			public void doEnd(int threadnum) throws Exception {
			}

			@Override
			public void run(int threadnum, int iter) throws Exception {
				// one thread resets it, the others read continuously
				if (threadnum == 1) {
					EasyTravelConfig.resetSingleton();
				} else if (threadnum == 2 && iter % 19 == 0) {
					EasyTravelConfig.reload();
				} else {
					assertNotNull(EasyTravelConfig.read());
				}

				if (iter % (NUMBER_OF_TESTS/10) == 0) {
					LOGGER.info("Thread: " + threadnum + ", iter: " + iter);
				}
			}
		});
	}

	@Test
	public void testRefreshAfter10Seconds() throws InterruptedException {
		EasyTravelConfig config = EasyTravelConfig.read();
		// try fo r
		boolean changed = false;
		for(int i = 0;i < 11 && !changed;i++) {
			Thread.sleep(1000);

			// do a id-compare on purpose here!
			if(config != EasyTravelConfig.read()) {
				changed = true;
			}
		}

		// we disabled the re-reading as it causes trouble, see JLT-41104
		assertFalse("Expected the EasyTravelConfig instance to be re-read at least once in 10 seconds, but still had the same now: " + config + " and " + EasyTravelConfig.read(),
				changed);
	}

	@Test
	public void testCustomSettingsAreKeptAlthoughNewConfigRead() {
		Map<String, String> customSettings = new HashMap<String, String>();
		customSettings.put("config.backendPort", "99");
		customSettings.put("config.backendJavaopts",  "-Xmx8m");
		EasyTravelConfig.applyCustomSettings(customSettings);
		assertEquals(99, EasyTravelConfig.read().backendPort);

		String[] backendJavaopts = EasyTravelConfig.read().backendJavaopts;
		assertEquals(1, backendJavaopts.length);
		assertEquals("-Xmx8m", backendJavaopts[0]);

		// reset the custom settings
		EasyTravelConfig.applyCustomSettings(Collections.emptyMap());
		assertFalse(Integer.valueOf(99).equals(EasyTravelConfig.read().backendPort));
		assertFalse("-Xmx8m".equals(EasyTravelConfig.read().backendJavaopts[0]));
	}

	@Test
	public void testCustomSettingsCanReadDefaultSyntax() {
		EasyTravelConfig.applyCustomSettings(Collections.singletonMap("config.backendJavaopts",  "-Xmx64m,-Xdebug,-Xrunjdwp:transport=dt_socket,,server=y,,suspend=y,,address=5555"));

		String[] backendJavaopts = EasyTravelConfig.read().backendJavaopts;

		assertEquals(
				"Expected to have 3 elements, but had: " + backendJavaopts.length + ": " +
						Arrays.toString(backendJavaopts),
				3, backendJavaopts.length);
		assertEquals("-Xmx64m", backendJavaopts[0]);
		assertEquals("-Xdebug", backendJavaopts[1]);
		assertEquals("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5555", backendJavaopts[2]);
	}

	@Test
	public void testHasDifferentLoadSettingsThan() {
		EasyTravelConfig config = EasyTravelConfig.read();
		assertTrue(config.hasDifferentLoadSettingsThan(null));

		EasyTravelConfig unchangedCfg = EasyTravelConfig.read(Collections.emptyMap());
		assertFalse(config.hasDifferentLoadSettingsThan(unchangedCfg));

		EasyTravelConfig newCfg = EasyTravelConfig.read(Collections.singletonMap("config.baseLoadDefault", 1));
		assertTrue(config.hasDifferentLoadSettingsThan(newCfg));

		newCfg = EasyTravelConfig.read(Collections.singletonMap("config.baseLoadB2BRatio", 0.1));
		assertTrue(config.hasDifferentLoadSettingsThan(newCfg));

		newCfg = EasyTravelConfig.read(Collections.singletonMap("config.baseLoadCustomerRatio", 0.0));
		assertTrue(config.hasDifferentLoadSettingsThan(newCfg));

		newCfg = EasyTravelConfig.read(Collections.singletonMap("config.baseLoadMobileNativeRatio", 0.0));
		assertTrue(config.hasDifferentLoadSettingsThan(newCfg));

		newCfg = EasyTravelConfig.read(Collections.singletonMap("config.baseLoadMobileBrowserRatio", 0.0));
		assertTrue(config.hasDifferentLoadSettingsThan(newCfg));

		newCfg = EasyTravelConfig.read(Collections.singletonMap("config.baseLoadMobileBrowserRatio", 0.15));
		assertFalse(config.hasDifferentLoadSettingsThan(newCfg));

		newCfg = EasyTravelConfig.read(Collections.singletonMap("config.baseLoadHotDealServiceRatio", 0.1));
		assertTrue(config.hasDifferentLoadSettingsThan(newCfg));

		newCfg = EasyTravelConfig.read(Collections.singletonMap("config.customerLoadScenario", "EasyTravelFixed"));
		assertFalse(config.hasDifferentLoadSettingsThan(newCfg));
	}

	@Test
	public void testHasDifferentCustomerLoad() {
		EasyTravelConfig config = EasyTravelConfig.read();
		assertTrue(config.hasDifferentCustomerLoad(null));

		EasyTravelConfig newCfg1 = EasyTravelConfig.read(Collections.singletonMap("config.customerLoadScenario", "EasyTravelFixed"));
		assertTrue(config.hasDifferentCustomerLoad(newCfg1));

		EasyTravelConfig newCfg2 = EasyTravelConfig.read(Collections.singletonMap("config.customerLoadScenario", "EasyTravel"));
		assertFalse(config.hasDifferentCustomerLoad(newCfg2));
	}

	@Test
	public void testEnhancementsLost() throws InterruptedException {
		assertEquals("localhost", EasyTravelConfig.read().backendHost);

		// first enhance the Config with some values
		Properties props = new Properties();
		props.setProperty("config.backendHost", "host123");
		EasyTravelConfig.read().enhance(props);
		assertEquals("host123",
				EasyTravelConfig.read().backendHost);

		// sleep at least 10 seconds to make EasyTravelConfig re-read the config
		Thread.sleep(10010);

		assertEquals("Should still be the 'enhanced' property",
				"host123", EasyTravelConfig.read().backendHost);
	}

	@Test
	public void testAgentAutoAdaption() throws Exception {
		EasyTravelConfig config = EasyTravelConfig.read();

		File file = config.storeInTempFile();
		String contents = FileUtils.readFileToString(file);

		// replace
		contents = contents.replace("config.agent=auto", "config.agent=customagent");

		// all other agents should be at "auto" for this test
		TestHelpers.assertContains(contents, "config.agent=customagent");
		TestHelpers.assertContains(contents, "config.frontendAgent=auto");
		TestHelpers.assertContains(contents, "config.backendAgent=auto");
		TestHelpers.assertContains(contents, "config.creditCardAuthorizationAgent=auto");
		TestHelpers.assertContains(contents, "config.antAgent=auto");
		TestHelpers.assertContains(contents, "config.apacheWebServerAgent=auto");
		TestHelpers.assertContains(contents, "config.phpAgent=auto");

		FileUtils.writeStringToFile(file, contents);

		// all should be at "customagent" now
		config = EasyTravelConfig.create(file.getAbsolutePath());

		assertEquals("customagent", config.agent);

		assertEquals("customagent", config.frontendAgent);
		assertEquals("customagent", config.backendAgent);
		assertEquals("customagent", config.creditCardAuthorizationAgent);
		assertEquals("customagent", config.antAgent);

		// JLT-76536: don't adapt these
		assertEquals("auto", config.apacheWebServerAgent);
		assertEquals("auto", config.phpAgent);

		EasyTravelConfig.resetSingleton();
	}

	@Test
	public void testCustomerTrafficScenario() {
		EasyTravelConfig.resetSingleton();
		EasyTravelConfig config = EasyTravelConfig.read();
		assertEquals("easyTravel", CustomerTrafficScenarioEnum.EasyTravel, config.getCustomerTrafficScenario());

		//test overwrite
		try {
			System.setProperty("config.customerLoadScenario", "EasyTravelFixed");
			EasyTravelConfig.resetSingleton();
			config = EasyTravelConfig.read();
			assertEquals("EasyTravelFixed", CustomerTrafficScenarioEnum.EasyTravelFixed, config.getCustomerTrafficScenario());

			System.setProperty("config.customerLoadScenario", "someStrangeValue");
			EasyTravelConfig.resetSingleton();
			config = EasyTravelConfig.read();
			assertEquals("EasyTravel", CustomerTrafficScenarioEnum.EasyTravel, config.getCustomerTrafficScenario());
		} finally {
			System.setProperty("config.customerLoadScenario", "EasyTravel"); //reset property value
		}

		EasyTravelConfig.resetSingleton();
		config = EasyTravelConfig.read();
		assertEquals("easyTravel", CustomerTrafficScenarioEnum.EasyTravel, config.getCustomerTrafficScenario());
	}
	
	/**
	 * Tests if easyTravelTrainingConfig.properties contains all properties with correct values
	 * from easyTravelConfig.properties. 
	 * Properties that are not in easyTravelTrainingConfig.properties 
	 * or have different values on purpose, can be add to
	 * missingProperies map to avoid test from failing.
	 * @throws IOException 
	 * 
	 * @author Michal.Bakula
	 */
	@Test
	public void testEasyTravelTraining() throws IOException {

		String config = "easyTravelConfig.properties";
		String training = "easyTravelTrainingConfig.properties";

		// Add properties that shouldn't be or should have different value in
		// easyTravelTrainingConfig file here
		Map<Object, Object> missingProperties = new HashMap<>();
		missingProperties.put("config.backendSystemProfile", "BusinessBackend_easyTravel");
		missingProperties.put("config.antAgent", "auto");
		missingProperties.put("config.creditCardAuthorizationEnvArgs", "DT_WAIT=5,RUXIT_WAIT=5");
		missingProperties.put("config.creditCardAuthorizationSystemProfile", "CreditCardAuthorization_easyTravel");
		missingProperties.put("config.apacheWebServerAgent", "auto");
		missingProperties.put("config.cassandraAgent", "auto");
		missingProperties.put("config.creditCardAuthorizationAgent", "auto");
		missingProperties.put("config.agent", "auto");
		missingProperties.put("config.backendAgent", "auto");
		missingProperties.put("config.b2bFrontendEnvArgs", "DT_WAIT=5,RUXIT_WAIT=5,COR_ENABLE_PROFILING=0x1");
		missingProperties.put("config.apacheWebServerUsesGeneratedHttpdConfig", "true");
		missingProperties.put("config.frontendSystemProfile", "CustomerFrontend_easyTravel#{_port}");
		missingProperties.put("config.b2bFrontendSystemProfile", "dotNetFrontend_easyTravel");
		missingProperties.put("config.nginxWebServerAgent", "auto");
		missingProperties.put("config.paymentBackendEnvArgs", "DT_WAIT=5,RUXIT_WAIT=5,COR_ENABLE_PROFILING=0x1");
		missingProperties.put("config.paymentBackendSystemProfile", "dotNetBackend_easyTravel");
		missingProperties.put("config.phpAgent", "auto");
		missingProperties.put("config.frontendAgent", "auto");

		Set<Map.Entry<Object, Object>> missing = missingProperties.entrySet();

		Set<Map.Entry<Object, Object>> confProp;
		Set<Map.Entry<Object, Object>> confTrainProp;

		Properties eT_Conf = new Properties();
		Properties eT_TraiConf = new Properties();
		InputStream input = null;

		input = EasyTravelConfigTest.class.getClassLoader().getResourceAsStream(config);
		assertTrue(TextUtils.merge("Could not read {0}", config), input != null);

		eT_Conf.load(input);
		confProp = eT_Conf.entrySet();

		input = null;
		input = EasyTravelConfigTest.class.getClassLoader().getResourceAsStream(training);
		assertTrue(TextUtils.merge("Could not read {0}", training), input != null);

		eT_TraiConf.load(input);
		confTrainProp = eT_TraiConf.entrySet();

		Set<Map.Entry<Object, Object>> newSet = new HashSet<Map.Entry<Object, Object>>(confTrainProp);
		newSet.addAll(missing);

		for (Map.Entry<Object, Object> e : confProp) {
			assertTrue(TextUtils.merge(
					"{0} property from {1} is missing or have different value comparing to {2}.\n"
							+ "Add/change property in {3} or (if it's intentional) add to missingProperties map in testEasyTravelTraining() method.",
					e.getKey(), config, training, training), newSet.contains(e));
		}
	}
}
