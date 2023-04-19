package com.dynatrace.easytravel.integration;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.junit.Test;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.launcher.engine.StopMode;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.procedures.BrowserProcedure;
import com.dynatrace.easytravel.launcher.process.AbstractProcess;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;


public class BrowserProcedureIntegrationTest {
	@Test
	public void test() throws Exception {
		BrowserProcedure proc = new BrowserProcedure(new DefaultProcedureMapping(Constants.Procedures.BROWSER_ID));
		proc.run();

		try {
			Thread.sleep(400);

			assertTrue(proc.isRunning());
			assertTrue(proc.isOperatingCheckSupported());
			assertTrue(proc.isOperating());
			assertTrue(proc.isRunning());
			assertEquals(StopMode.PARALLEL, proc.getStopMode());
			assertFalse(proc.hasLogfile());
			assertNull(proc.getLogfile());
			assertNull(proc.getTechnology());
			assertEquals(OperatingSystem.IS_WINDOWS, proc.isEnabled());

			// TODO: ensure that we re-set proxy settings!!
		} finally {
			proc.stop();

			BatchProcess resetReg = new BatchProcess("resetProxy", "reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v \"ProxyEnable\" /d 0 /t REG_DWORD /f");
			resetReg.start();
		}
	}

	static class BatchProcess extends AbstractProcess {

		private final String filename;
		private Exception exception;

		public BatchProcess(String name, String ... commands) {
			super(null);
			filename = createBatch(name, commands);
		}

		@Override
		public CommandLine createCommand() {
			return new CommandLine(filename);
		}

		private String createBatch(String name, String ... commands) {
			File script = new File(Directories.getConfigDir(), name + ".cmd");
			try {
				FileWriter fw = new FileWriter(script);
				for(String command : commands) {
					fw.write(command);
					fw.write(System.getProperty("line.separator"));
				}
				fw.close();
			} catch (IOException e) {
				exception = e;
			}
			return script.getAbsolutePath();
		}

		public Exception getException() {
			return exception;
		}

	}

}
