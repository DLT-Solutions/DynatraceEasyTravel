package com.dynatrace.easytravel.launcher.engine;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.exec.*;
import org.junit.Test;
import org.junit.experimental.theories.ParametersSuppliedBy;

import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.process.AbstractProcess;
import com.dynatrace.easytravel.launcher.process.Process;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.utils.TestEnvironment;
import com.dynatrace.easytravel.utils.TestHelpers;

public class AbstractProcessProcedureTest {

	@Test
	public void testAbstractProcessProcedure() throws CorruptInstallationException {
		AbstractProcessProcedure proc = new MyProcedure(new DefaultProcedureMapping("somemapping"));
		assertNotNull(proc);
		assertNotNull(proc.getProcess());
		assertNull(proc.getPropertyFile());
		assertFalse(proc.agentFound());
	}

	@Test
	public void testRun() throws Exception {
		AbstractProcessProcedure proc = new MyProcedure(new DefaultProcedureMapping("somemapping"));
		assertEquals(Feedback.Neutral, proc.run());

		assertTrue("Immediately after starting, the proc is reported as running", proc.isRunning());

		Thread.sleep(1000);

		assertFalse("After a short while the process failed to start and thus is not running any more",
				proc.isRunning());
	}

	@Test
	public void testIsStoppable() throws CorruptInstallationException {
		AbstractProcessProcedure proc = new MyProcedure(new DefaultProcedureMapping("somemapping"));
		assertTrue(proc.isStoppable());
	}

	@Test
	public void testStop() throws CorruptInstallationException {
		AbstractProcessProcedure proc = new MyProcedure(new DefaultProcedureMapping("somemapping"));
		assertEquals(Feedback.Neutral, proc.stop());
	}

	@Test
	public void testIsRunning() throws CorruptInstallationException {
		AbstractProcessProcedure proc = new MyProcedure(new DefaultProcedureMapping("somemapping"));
		assertFalse(proc.isRunning());
	}

	@Test
	public void testAddApplicationArgument() throws CorruptInstallationException {
		AbstractProcessProcedure proc = new MyProcedure(new DefaultProcedureMapping("somemapping"));
		proc.addApplicationArgument("somearg");
	}

	@Test
	public void testClearApplicationArgument() throws CorruptInstallationException {
		AbstractProcessProcedure proc = new MyProcedure(new DefaultProcedureMapping("somemapping"));
		proc.addApplicationArgument("somearg");
		proc.addApplicationArgument("somearg2");
		proc.clearApplicationArguments();
	}

	@Test
	public void testStopListener() throws CorruptInstallationException {
		AbstractProcessProcedure proc = new MyProcedure(new DefaultProcedureMapping("somemapping"));

		// just cover, don't fully test for now
		proc.addStopListener(null);
		proc.removeStopListener(null);
		proc.clearStopListeners();
	}

	@Test
	public void testGetDetails() throws CorruptInstallationException {
		AbstractProcessProcedure proc = new MyProcedure(new DefaultProcedureMapping("somemapping"));
		Pattern pattern;

		pattern = getProcDetailsPattern("somecommand1");

		assertTrue(pattern.matcher(proc.getDetails()).find());
		assertTrue("detail is cached", pattern.matcher(proc.getDetails()).find());

		// this also calls createCommand(), thus "using up" somecommand2
		proc.run();

		pattern = getProcDetailsPattern("somecommand3");

		assertTrue("detail-cache is reset", pattern.matcher(proc.getDetails()).find());
		assertTrue("detail is cached", pattern.matcher(proc.getDetails()).find());

		// stop calls getDetails() once, so the counter has to increase one extra time
		proc.stop();

		pattern = getProcDetailsPattern("somecommand5");

		assertTrue("detail-cache is reset", pattern.matcher(proc.getDetails()).find());
		assertTrue("detail is cached", pattern.matcher(proc.getDetails()).find());
	}

	private Pattern getProcDetailsPattern(String command) {
		return Pattern.compile(
                		command+"\\s" +
                        ".*\\sDT_RELEASE_BUILD_VERSION=" +
                        ".*\\sDT_RELEASE_PRODUCT=" +
                        ".*\\sDT_RELEASE_STAGE=" +
                        ".*\\sDT_RELEASE_VERSION=.*"
        );
	}

	private Technology technology = null;
	private DtAgentConfig agentConfig = null;

	private class MyProcedure extends AbstractProcessProcedure {

		public MyProcedure(ProcedureMapping mapping) throws CorruptInstallationException {
			super(mapping);
		}

		@Override
		protected Process createProcess(ProcedureMapping mapping) throws CorruptInstallationException {
			return new MyProcess(new DtAgentConfig("name", "path", new String[] {}, new String[] {}));
		}

		@Override
		public boolean isOperatingCheckSupported() {
			return false;
		}

		@Override
		public boolean isOperating() {
			return false;
		}

		@Override
		public String getLogfile() {
			return null;
		}

		@Override
		public boolean hasLogfile() {
			return false;
		}

		@Override
		public Technology getTechnology() {
			return technology;
		}

		@Override
		protected DtAgentConfig getAgentConfig() {
			return agentConfig;
		}
	}

	private final class MyProcess extends AbstractProcess {
		int i = 0;

		protected MyProcess(DtAgentConfig dtAgentConfig) {
			super(dtAgentConfig);
		}

		@Override
		public CommandLine createCommand() {
			i++;

			return new CommandLine("somecommand" + i);
		}
	}

	/**
	 * Reproduces https://issues.apache.org/jira/browse/EXEC-71
	 *
	 * @throws Exception
	 * @author cwat-dstadler
	 */
	@Test
	public void testCommonsExecHang() throws Exception {
		CommandLine command = new CommandLine("someunexistingcommand");
		DefaultExecutor executor = new DefaultExecutor();

		ExecuteWatchdog watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
		executor.setWatchdog(watchdog);

		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler() {
			@Override
			public void onProcessFailed(ExecuteException e) {
				System.out.println("Process did not stop gracefully, had exception '" + e.getMessage()
						+ "' while executing process");
				e.printStackTrace();
				super.onProcessFailed(e);
			}
		};

		executor.execute(command, null, resultHandler);

		// this hangs!!
		watchdog.isWatching();

		watchdog.destroyProcess();
	}

	@Test
	public void testAbstractProcessProcedureWithTenant() throws CorruptInstallationException {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping("somemapping");
		mapping.setAPMTenantUUID("tenant123");
		AbstractProcessProcedure proc = new MyProcedure(mapping);
		assertNotNull(proc);
		assertNotNull(proc.getProcess());
		assertNull(proc.getPropertyFile());
		assertFalse(proc.agentFound());
		TestHelpers.assertContains(((MyProcess) proc.getProcess()).getDetails(), "Environment:", "DT_TENANT=tenant123");
	}

	@Test
	public void testIsInstrumentationSupported() throws CorruptInstallationException, IOException {
		DtVersionDetector.enforceInstallationType(InstallationType.Classic);

		AbstractProcessProcedure proc = new MyProcedure(new DefaultProcedureMapping("somemapping"));
		assertFalse("No instrumentation when technology is null", proc.isInstrumentationSupported());
		technology = Technology.JAVA;
		assertTrue("No instrumentation is supported based on technology", proc.isInstrumentationSupported());

		DtVersionDetector.enforceInstallationType(InstallationType.APM);
		assertFalse("No instrumentation support for APM when agent config is null", proc.isInstrumentationSupported());
		agentConfig = new DtAgentConfig("name", BaseConstants.AUTO, null, null);
		assertFalse("No instrumentation support for APM when agent config is AUTO", proc.isInstrumentationSupported());
		agentConfig = new DtAgentConfig("name", BaseConstants.NONE, null, null);
		assertFalse("No instrumentation support for APM when agent config is NONE", proc.isInstrumentationSupported());
		agentConfig = new DtAgentConfig("name", "somepath", null, null);
		assertTrue("Instrumentation support for APM when agent config has a path", proc.isInstrumentationSupported());
		agentConfig = new DtAgentConfig("name", "somepath", null, null);
		assertTrue("Instrumentation support for APM when agent config has a path", proc.isInstrumentationSupported());

		TestEnvironment.createOrClearRuntimeData();
		File file = File.createTempFile("agent", "test", new File(TestEnvironment.ABS_RUNTIME_DATA_PATH));
		agentConfig = new DtAgentConfig("name", file.getAbsolutePath(), null, null);
		assertTrue("Instrumentation support for APM when agent config has a path", proc.isInstrumentationSupported());
		assertTrue(file.delete());

		DtVersionDetector.enforceInstallationType(null);
	}
}
