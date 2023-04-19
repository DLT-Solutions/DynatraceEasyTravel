package com.dynatrace.easytravel.launcher.engine;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

import com.dynatrace.easytravel.TestUtil;
import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.httpd.ApacheHttpdUtils;
import com.dynatrace.easytravel.launcher.httpd.DummyAgentDLL;
import com.dynatrace.easytravel.launcher.httpd.HttpdConfSetup;
import com.dynatrace.easytravel.launcher.mysqld.MysqlUtils;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.utils.TestEnvironment;
import com.dynatrace.easytravel.utils.TestHelpers;


public class ApacheHttpdPhpProcedureTest {

	private static final Logger LOGGER = Logger.getLogger(ApacheHttpdPhpProcedure.class.getName());
	
	static {
		TestUtil.setInstallDirCorrection();
	}

	private static final String EASYTRAVEL_CONFIG_PATH = Directories.getConfigDir().getAbsolutePath();
	private static final File PHP_INI = new File(EASYTRAVEL_CONFIG_PATH, "php.ini");
	private static final File HTTPD_CONF = new File(EASYTRAVEL_CONFIG_PATH, "/httpd.conf");
	private static final String PHP_FILE_PATH = Directories.getPhpDir().getAbsolutePath();
	
	@Test
	public void testAdjustPHPIniFile() throws Exception {
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		PHP_INI.delete();

		if(SystemUtils.IS_OS_WINDOWS)
			ApacheHttpdPhpProcedure.adjustPHPIniFileWindows();
		else
			ApacheHttpdPhpProcedure.adjustPHPIniFile();

		String result = FileUtils.readFileToString(PHP_INI);

		
		String phpPahtForOs = ApacheHttpdUtils.getPhpInstallDirForUsedOs();

		String mysqlDriver = SystemUtils.IS_OS_WINDOWS ? "mysql" : "mysqli";
		List<String> expectedLines = new ArrayList<>();
		expectedLines.add("extension_dir= \"" + PHP_FILE_PATH + File.separator + phpPahtForOs + File.separator + "ext\"");
		expectedLines.add(mysqlDriver + ".default_socket= \"" + MysqlUtils.MySQL_SOCKET + "\"");

		// dtagent
		DtAgentConfig config = new DtAgentConfig(null,EASYTRAVEL_CONFIG.phpAgent, null, EASYTRAVEL_CONFIG.phpEnvArgs);

		String agentPath;
		try {
			agentPath = config.getAgentPath(Technology.WEBPHPSERVER);
			if (agentPath != null) {
				expectedLines.add("\nextension= \"" + agentPath + "\"");
			}
		} catch (ConfigurationException e) {
			LOGGER.log(Level.WARNING, "PHP agent not configured correctly", e);
		}
		LOGGER.info("CWPL-MPANKOWS--result: \n");
		LOGGER.info(result);
		LOGGER.info("CWPL-MPANKOWS--expectedString: \n");
		LOGGER.info(expectedLines.toString());

		expectedLines.forEach(line -> TestHelpers.assertContains(result.replace("\r", ""), line.replace("\r", "")));
	}
	

	@Test
	public void adjustPHPIniFileWindows() throws Exception {
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		PHP_INI.delete();

		ApacheHttpdPhpProcedure.adjustPHPIniFileWindows();

		String result = FileUtils.readFileToString(PHP_INI);

		String apacheDirForOs = ApacheHttpdUtils.getApacheInstallDirForUsedOs();
		
		StringBuilder expectedString = new StringBuilder("extension_dir= \"" + PHP_FILE_PATH + File.separator + apacheDirForOs +  File.separator + "ext\""
				+"\n\nupload_tmp_dir= \"" + PHP_FILE_PATH + File.separator + apacheDirForOs +  File.separator + "tmp\"" 
				+"\n\nsession.save_path= \"" + PHP_FILE_PATH + File.separator + apacheDirForOs + File.separator + "tmp\"" 
				+"\n\nxdebug.profiler_output_dir= \"" + PHP_FILE_PATH + File.separator + apacheDirForOs + File.separator + "tmp\"" 
				+ "\nmysql.default_socket= \"" + MysqlUtils.MySQL_SOCKET + "\"");


		// dtagent
		DtAgentConfig config = new DtAgentConfig(null,EASYTRAVEL_CONFIG.phpAgent, null, EASYTRAVEL_CONFIG.phpEnvArgs);

		String agentPath;
		try {
			agentPath = config.getAgentPath(Technology.WEBPHPSERVERWIN);
			if (agentPath != null) {
				expectedString.append("\nextension= \"" + agentPath + "\"");
			}
		} catch (ConfigurationException e) {
			LOGGER.log(Level.WARNING, "PHP agent not configured correctly", e);
		}
		LOGGER.info("CWPL-MPANKOWS--result: \n");
		LOGGER.info(result);
		LOGGER.info("CWPL-MPANKOWS--expectedString: \n");
		LOGGER.info(expectedString.toString());
		TestHelpers.assertContains(result.replace("\r", ""), expectedString.toString().replace("\r", ""));

	}
	
	@Test
	public void  adjustHttpdConfFile() throws IOException {
		FileUtils.copyDirectory(new File(TestEnvironment.ABS_TEST_DATA_PATH, "../../ThirdPartyLibraries/Apache/ApacheHTTP/plain_conf"),
				new File(EASYTRAVEL_CONFIG_PATH));
		
		File backup = null;			
		
		//backup original file
		if (HTTPD_CONF.exists()) {
			backup = new File(EASYTRAVEL_CONFIG_PATH,"/httpd.conf.backup");
			FileUtils.copyFile(HTTPD_CONF, backup);
		}
		
		EasyTravelConfig config = EasyTravelConfig.read();
		try {
			
			config.apacheWebServerUsesGeneratedHttpdConfig = true;
			
			generateHTTPDConfFile();
			assertTrue("No PHP settings in the file", hasPHPSettings());
			
			//PHP settings should not be added if file is not generated 
			config.apacheWebServerUsesGeneratedHttpdConfig = false;
			generateHTTPDConfFile();
			assertFalse("httpd.conf file has PHP settings", hasPHPSettings());						
		} finally {
			if (backup != null) {
				FileUtils.copyFile(backup, HTTPD_CONF);				
			}
			EasyTravelConfig.resetSingleton();
		}
	}
	
	@Test
	public void generateRuxitAgentProperties() throws IOException {
		InstallationType savInstType = DtVersionDetector.getInstallationType();
		EasyTravelConfig config = EasyTravelConfig.read();
		DummyAgentDLL dummyAgentDLL = new DummyAgentDLL();

		try {
			//first test some situtaion where we don't expect RuxitAgentConfig
			DtVersionDetector.enforceInstallationType(InstallationType.Classic);
			config.phpAgent = "auto";
			assertNotContainsRuxitAgentConfig();

			DtVersionDetector.enforceInstallationType(InstallationType.APM);
			config.phpAgent = "auto";
			assertNotContainsRuxitAgentConfig();

			dummyAgentDLL.createDummyAgentDLL();
			DtVersionDetector.enforceInstallationType(InstallationType.Classic);
			config.phpAgent=dummyAgentDLL.getMyTestPath();			
			assertNotContainsRuxitAgentConfig();

			//now RuxitAgentConfig should be added
			DtVersionDetector.enforceInstallationType(InstallationType.APM);
			config.phpAgent=dummyAgentDLL.getMyTestPath();
			config.apmServerWebURL="http://somehost.clients.dynatrace.org:8020";
			config.apmTenant="ruxitTenant";
			config.apmTenantToken="ruxitToken";

			PHP_INI.delete();
			assertFalse(PHP_INI.exists());

			ApacheHttpdPhpProcedure.adjustPHPIniFile();
			assertTrue(PHP_INI.exists());
			
			String result = FileUtils.readFileToString(PHP_INI);
			TestHelpers.assertContains(result, "phpagent.server=\"http://somehost.clients.dynatrace.org:8020\"");
			TestHelpers.assertContains(result, "phpagent.tenant=\"ruxitTenant\"");
			TestHelpers.assertContains(result, "phpagent.tenanttoken=\"ruxitToken\"");
		} finally {
			PHP_INI.delete();
			EasyTravelConfig.resetSingleton();
			DtVersionDetector.enforceInstallationType(savInstType);
			dummyAgentDLL.destroyDummyAgentDLL();
		}
	}
	
	private void generateHTTPDConfFile() throws IOException {
		if (HTTPD_CONF.exists()) {
			FileUtils.forceDelete(HTTPD_CONF);
		}
		HttpdConfSetup.write(createDtAgentConfig(), true);
		ApacheHttpdPhpProcedure.adjustHTTPDConfFile();
	}
	
	private boolean hasPHPSettings() throws FileNotFoundException {
		Scanner scanner = null;
		try {		
			scanner = new Scanner(HTTPD_CONF);
			String res = scanner.findWithinHorizon("#PHP Settings", 0);
			return res != null;
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
	}
	
	private static DtAgentConfig createDtAgentConfig() {
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		return new DtAgentConfig(null,
				EASYTRAVEL_CONFIG.apacheWebServerAgent,
				null,
				EASYTRAVEL_CONFIG.apacheWebServerEnvArgs);
	}

		
	private void assertNotContainsRuxitAgentConfig() throws IOException{
		PHP_INI.delete();
		assertFalse(PHP_INI.exists());

		ApacheHttpdPhpProcedure.adjustPHPIniFile();
		assertTrue(PHP_INI.exists());
		
		String result = FileUtils.readFileToString(PHP_INI);
		TestHelpers.assertNotContains(result, "phpagent.server");
		TestHelpers.assertNotContains(result, "phpagent.tenant");
		TestHelpers.assertNotContains(result, "phpagent.tenanttoken");
		PHP_INI.delete();
	}
}
