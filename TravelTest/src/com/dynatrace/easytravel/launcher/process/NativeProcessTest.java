package com.dynatrace.easytravel.launcher.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.TestUtil;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.utils.TestEnvironment;
import com.dynatrace.easytravel.utils.TestHelpers;

import ch.qos.logback.classic.Level;


public class NativeProcessTest {
	
	static {
		TestUtil.setInstallDirCorrection();
    }
	
	private static final EasyTravelConfig CONFIG = EasyTravelConfig.read();

	@Before
	public void setUp() throws IOException {
		TestEnvironment.createOrClearRuntimeData();
	}

	@After
	public void tearDown() throws IOException {
		TestEnvironment.clearRuntimeData();
	}

	@Test
	public void testConstruct() throws Exception {
		try {
			new NativeProcess(new File("notexists"), createDtAgentConfig(), Technology.ADK);
			fail("Should throw exception because notexisting file");
		} catch (FileNotFoundException e) {
			TestHelpers.assertContains(e, "notexists");
		}

		File file = File.createTempFile("NativeProcessTest", ".tst", new File(TestEnvironment.ABS_RUNTIME_DATA_PATH));
		NativeProcess nativeProcess = new NativeProcess(file, createDtAgentConfig(), Technology.ADK);
		assertNotNull(nativeProcess.getEnvironment());

		nativeProcess = new NativeProcess(file, createDtAgentConfig(), Technology.WEBSERVER);
		assertNotNull(nativeProcess.getEnvironment());

		nativeProcess = new NativeProcess(file, createDtAgentConfig(), Technology.WEBPHPSERVER);
		assertNotNull(nativeProcess.getEnvironment());

		nativeProcess = new NativeProcess(file, createDtAgentConfig(), Technology.DOTNET_20);
		assertNotNull(nativeProcess.getEnvironment());

		nativeProcess = new NativeProcess(file, null, Technology.MYSQL);
		assertNotNull(nativeProcess.getEnvironment());

		assertNotNull(nativeProcess.getResultHandler("", null));

		try {
			new NativeProcess(null, createDtAgentConfig(), Technology.ADK);
			fail("Expect exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "must not be null");
		}
	}

	@Test
	public void testWithDifferentLogLevel() throws Exception {
		final AtomicReference<Exception> exception = new AtomicReference<Exception>();

		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {

			@Override
			public void run() {
				try {
					testConstruct();
				} catch (Exception e) {
					exception.set(e);
				}

			}
		}, NativeProcess.class.getName(), Level.INFO);

		if(exception.get() != null) {
			throw exception.get();
		}

		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {

			@Override
			public void run() {
				try {
					testConstruct();
				} catch (Exception e) {
					exception.set(e);
				}

			}
		}, NativeProcess.class.getName(), Level.WARN);

		if(exception.get() != null) {
			throw exception.get();
		}

		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {

			@Override
			public void run() {
				try {
					testConstruct();
				} catch (Exception e) {
					exception.set(e);
				}

			}
		}, NativeProcess.class.getName(), Level.DEBUG);

		if(exception.get() != null) {
			throw exception.get();
		}
	}

    private static DtAgentConfig createDtAgentConfig() {
    	return new DtAgentConfig(CONFIG.creditCardAuthorizationSystemProfile, CONFIG.creditCardAuthorizationAgent, CONFIG.creditCardAuthorizationAgentOptions, CONFIG.creditCardAuthorizationEnvArgs);
    }

    @Test
    public void testCreateCommend() throws Exception {
		File file = File.createTempFile("NativeProcessTest", ".tst", new File(TestEnvironment.ABS_RUNTIME_DATA_PATH));

		NativeProcess process = new NativeProcess(file, createDtAgentConfig(), Technology.DOTNET_20);
		assertNotNull(process.createCommand());
		assertNotNull(process.getEnvironment());
		assertEquals(Technology.DOTNET_20, process.getTechnology());

		process = new NativeProcess(file, createDtAgentConfig(), Technology.ADK);
		assertNotNull(process.createCommand());
		assertNotNull(process.getEnvironment());
		assertEquals(Technology.ADK, process.getTechnology());

		process = new NativeProcess(file, createDtAgentConfig(), Technology.JAVA);
		assertNotNull(process.createCommand());
		try {
			assertNotNull(process.getEnvironment());
			fail("Expect exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "Unexpected Technology", "JAVA");
		}
		assertEquals(Technology.JAVA, process.getTechnology());

		process = new NativeProcess(file, createDtAgentConfig(), Technology.WEBSERVER);
		assertNotNull(process.createCommand());
		assertNotNull(process.getEnvironment());
		assertEquals(Technology.WEBSERVER, process.getTechnology());

		process = new NativeProcess(file, createDtAgentConfig(), Technology.WEBSERVER);
		process.addApplicationArgument("-startmeup");
		process.setEnvironmentVariable("somevar", "somevalue");
		assertTrue("Env: " + process.getEnvironment(),
				process.getEnvironment().containsKey("somevar"));

		assertNotNull(process.createCommand());
		assertNotNull(process.getEnvironment());
		assertEquals(Technology.WEBSERVER, process.getTechnology());

		process = new NativeProcess(file, createDtAgentConfig(), Technology.WEBPHPSERVER);
		assertNotNull(process.createCommand());
		assertNotNull(process.getEnvironment());
		assertEquals(Technology.WEBPHPSERVER, process.getTechnology());
    }
}
