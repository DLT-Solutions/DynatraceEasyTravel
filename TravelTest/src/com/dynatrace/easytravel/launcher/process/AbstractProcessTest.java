package com.dynatrace.easytravel.launcher.process;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteWatchdog;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.integration.IntegrationTestBase;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.StopListener;
import com.dynatrace.easytravel.launcher.httpd.ApacheHttpdUtils;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.TestHelpers;

public class AbstractProcessTest {
	private static final Logger log = LoggerFactory.make();

    static {
		// set system properties for installation location
		System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");

		log.info("Using files from: " + System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION));
    }

	@Test
	public void test() throws IOException {
		AbstractProcess process = new AbstractProcess(new DtAgentConfig("name", null, null, null)) {

			@Override
			public CommandLine createCommand() {
				return null;
			}
		};

		assertFalse(process.isRunning());

		// cover some of the simple methods
		process.setTimeout(0);
		assertEquals(ExecuteWatchdog.INFINITE_TIMEOUT, process.getTimeout());
		process.setTimeout(1000);
		assertEquals(1000, process.getTimeout());
		process.setOut(null);
		process.setIn(null);
		process.setErr(null);
		try {
			process.start();
			fail("Should throw exception here");
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "cannot be null");
		}
		process.stop();
		assertFalse(process.isRunning());
		assertNotNull(process.getDtAgentConfig());

		assertNotNull(process.getDetails());
		process.setEnvironmentVariable("testkey", "val1");
		assertNotNull(process.getDetails());

		assertTrue(process.isExpectedExitValue(0));
		assertFalse(process.isExpectedExitValue(1));

		assertFalse(process.hasResult());
		try {
			process.getExitValue();
			fail("Should throw exception here");
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "not started yet");
		}

		assertNull(process.getPropertyFile());
		process.setPropertyFile();
		assertNotNull(process.getPropertyFile());

		assertNotNull(process.getResultHandler("asdfasd", null));
		assertNotNull(process.getWatchdog(0123, null, null));
	}

	@Test
	public void testStopListener() throws IOException {
		AbstractProcess process = new AbstractProcess(new DtAgentConfig("name", null, null, null)) {

			@Override
			public CommandLine createCommand() {
				return null;
			}
		};

		StopListener listener = new StopListener() {

			@Override
			public void notifyProcessStopped() {
			}

			@Override
			public void notifyProcessFailed() {
			}
		};

		process.addStopListener(listener);
		process.removeStopListener(listener);
		process.clearStopListeners();
	}

	@Test
	public void testApplicationArguments() throws IOException {
		AbstractProcess process = new AbstractProcess(new DtAgentConfig("name", null, null, null)) {

			@Override
			public CommandLine createCommand() {
				return null;
			}
		};

		process.addApplicationArgument("arg1");
		process.addApplicationArgumentPair("key1", "value2");
		assertEquals("[arg1, key1, value2]", process.getApplicationArguments().toString());
		process.clearApplicationArguments();
		assertEquals("[]", process.getApplicationArguments().toString());
	}

	@Test
	public void testWorkingDir() throws IOException {
		AbstractProcess process = new AbstractProcess(new DtAgentConfig("name", null, null, null)) {

			@Override
			public CommandLine createCommand() {
				return null;
			}
		};

		assertEquals("No working dir => install dir", Directories.getInstallDir(), process.getWorkingDir());

		process.setWorkingSubDir("somedir");
		assertEquals("Non-existing working dir => install dir", Directories.getInstallDir(), process.getWorkingDir());

		File file = File.createTempFile("abstractProcess", "test", Directories.getInstallDir());
		process.setWorkingSubDir(file.getAbsolutePath());
		assertEquals("File as working dir => install dir", Directories.getInstallDir(), process.getWorkingDir());
		assertTrue(file.delete());

		assertTrue(Directories.getInstallDir().exists());
		assertTrue(Directories.getInstallDir().isDirectory());
		file = File.createTempFile("abstractProcess", "test", Directories.getInstallDir());
		assertTrue(file.delete());
		assertTrue(file.mkdir());
		process.setWorkingSubDir(file.getName());
		assertEquals("Now the specified directory is returned", new File(Directories.getInstallDir(), file.getName()), process.getWorkingDir());
		assertTrue(file.delete());
	}

	/**
	 * Provide access to the commandline arguments of a process, this is used by other
	 * unit tests!
	 *
	 * @param process
	 * @return
	 * @author cwat-dstadler
	 */
	public static List<String> getApplicationArguments(AbstractProcess process) {
		return process.getApplicationArguments();
	}

	@Test
	public void testEnvironmentVariables() {
		AbstractProcess process = new AbstractProcess(new DtAgentConfig("name", null, null, null)) {

			@Override
			public CommandLine createCommand() {
				return null;
			}
		};

		assertNull(process.getEnvironment()); // 'null' means default environment-path
		process.setEnvironmentVariable("TESTENV", "testvalue");
		assertEquals("{TESTENV=testvalue}", process.getEnvironment().toString());
	}

	@Test
	public void testStart() {
		AbstractProcess process = new AbstractProcess(new DtAgentConfig("name", null, null, null)) {

			@Override
			public CommandLine createCommand() {
				return new CommandLine("asdflaksdwraQ").addArgument("arg234");
			}
		};

		assertNotNull(process.getDetails());
		process.setEnvironmentVariable("testkey", "val1");
		assertNotNull(process.getDetails());
		process.addApplicationArgument("arg12");
		process.addApplicationArgument("arg423");
		TestHelpers.assertContains(process.getDetails(), "testkey", "val1", "arg234");

		process.setTimeout(10);
		assertNotNull(process.getDetails());

		process.start();
		assertFalse("Process should not have a result yet", process.hasResult());
		assertTrue("Process should still be running", process.isRunning());
		process.stop();
	}

	@Test
	public void testStartSuccess() throws Exception {
		String executable = ApacheHttpdUtils.getExecutableDependingOnOs();

    	File executableFile = null;
    	final EasyTravelConfig CONFIG = EasyTravelConfig.read();
    	if(executable.equalsIgnoreCase(CONFIG.b2bFrontendServerIIS) || executable.equalsIgnoreCase(CONFIG.paymentBackendServerIIS)){ //if executable is a IIS WorkerProcess
    		executableFile = new File(Directories.getWinDir(), executable + OperatingSystem.getCurrentExecutableExtension());
    	}else{
    		if (executable.equalsIgnoreCase(CONFIG.mysqlServer)) {
    			executableFile = new File(Directories.getWorkingDir().getParent(), executable + OperatingSystem.getCurrentExecutableExtension());
    		} else {
    			executableFile = new File(Directories.getInstallDir(), executable + OperatingSystem.getCurrentExecutableExtension());
    		}
    	}

		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		DtAgentConfig dtAgentConfig = new DtAgentConfig(null,
				EASYTRAVEL_CONFIG.apacheWebServerAgent,
				null,
				EASYTRAVEL_CONFIG.apacheWebServerEnvArgs);

		AbstractProcess process = new NativeWebserverProcess(executableFile, dtAgentConfig, Technology.WEBSERVER);

		File HttpdConf = new File(Directories.getConfigDir().getAbsolutePath()
				+ "/httpd.conf");
		process.addApplicationArgument("-f");
		process.addApplicationArgument(HttpdConf.getAbsolutePath());
		if (!OperatingSystem.isCurrent(OperatingSystem.WINDOWS)) {
			process.addApplicationArgument("-D");
			process.addApplicationArgument("NO_DETACH");
		}

		process.start();

		try {
			for(int i = 0;i < 10;i++) {
				//assertFalse(process.hasResult());
				//assertTrue(process.isRunning());

				Thread.sleep(1000);
			}
		} finally {
			// try to stop httpd gracefully
			IntegrationTestBase.cleanup();

			// if gracefull shutdown did not work, ensure that we try to stop the process via the Process as well
			process.stop();
		}
	}
}
