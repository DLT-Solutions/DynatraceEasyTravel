package com.dynatrace.easytravel.launcher.procedures;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.integration.IntegrationTestBase;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureSetting;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.utils.TestEnvironment;

public class AntProcedureTest {
    private static final Logger LOGGER = LoggerFactory.make();

    static {
		// set system properties for installation location
		System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");

		LOGGER.info("Using files from: " + System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION));
    }

	@Test
	public void test() throws InterruptedException {
		// prepare Coverage settings
		EasyTravelConfig.read().antJavaopts = new String[] {"-Xmx256m", "-Xms64m",
			IntegrationTestBase.getCoverageSetting("AntFork")
		};

		ProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.ANT_ID);

		String distTest = new File(TestEnvironment.ROOT_PATH, "Distribution/dist/").getAbsolutePath() + "/" + BaseConstants.SubDirectories.TEST;
		String distJUnitTest = Directories.getExistingTestsDir() + "/" + BaseConstants.SubDirectories.JUNIT;

		mapping.addSetting(new DefaultProcedureSetting(AntProcedure.TITLE, MessageConstants.MODULE_JUNIT_TESTS));
		mapping.addSetting(new DefaultProcedureSetting(AntProcedure.FILE, distTest + "/runtest.xml"));
		mapping.addSetting(new DefaultProcedureSetting(AntProcedure.TARGET, "runAllWithSession"));
		mapping.addSetting(new DefaultProcedureSetting(AntProcedure.RECURRENCE, "1"));
		mapping.addSetting(new DefaultProcedureSetting(AntProcedure.INSTRUMENTATION, AntProcedure.SETTING_VALUE_ON));
		mapping.addSetting(new DefaultProcedureSetting(AntProcedure.FORK, Boolean.TRUE.toString()));

		mapping.addSetting(new DefaultProcedureSetting(AntProcedure.PROPERTY, AntProcedure.TEST_REPORT_DIR, distJUnitTest));
		mapping.addSetting(new DefaultProcedureSetting(AntProcedure.PROPERTY, AntProcedure.PROPERTY_BUILD_NUMBER_FILE, distJUnitTest + "/build.number"));

		AntProcedure ant = new AntProcedure(mapping);
		assertEquals(Feedback.Neutral, ant.run());

		assertTrue("Ant should still be running now", ant.isRunning());

		assertTrue(ant.isOperating());

		Thread.sleep(5000);

		assertEquals(Feedback.Success, ant.stop());

		assertFalse(ant.agentFound());
		ant.setContinuously(true);
		assertTrue(ant.hasLogfile());
		assertNotNull(ant.getLogfile());
		assertTrue(ant.isOperatingCheckSupported());

		DtVersionDetector.enforceInstallationType(InstallationType.Classic);
		assertTrue("No instrumentation is supported based on technology", ant.isInstrumentationSupported());

		DtVersionDetector.enforceInstallationType(InstallationType.APM);
		assertFalse("No instrumentation support for APM when agent config is null", ant.isInstrumentationSupported());

		DtVersionDetector.enforceInstallationType(null);
	}
}
