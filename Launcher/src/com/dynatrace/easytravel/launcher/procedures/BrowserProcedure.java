package com.dynatrace.easytravel.launcher.procedures;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.*;
import com.dynatrace.easytravel.launcher.process.AbstractProcess;
import com.dynatrace.easytravel.launcher.process.Process;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;

/**
 * Procedure handling a web browser.
 *
 * @author clemens.fuchs
 *
 */
public class BrowserProcedure extends AbstractProcessProcedure {

	/**
	 * Process class for creating and running windows batches.
	 *
	 * @author clemens.fuchs
	 *
	 */
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
			FileWriter fw = null;
			try {
				try {
					fw = new FileWriter(script);
					for(String command : commands) {
						fw.write(command);
						fw.write(System.getProperty("line.separator"));
					}
				} finally {
					if(fw != null)
						fw.close();
				}
			} catch (IOException e) {
				exception = e;
			}
			return script.getAbsolutePath();
		}

		public Exception getException() {
			return exception;
		}

	}

	/**
	 * Process class for launching IE and manipulating the proxy setting before.
	 *
	 * @author clemens.fuchs
	 *
	 */
	static class IEProcess extends AbstractProcess {

		private Exception exception;
		private BatchProcess setReg;
		private BatchProcess resetReg;

		protected IEProcess() {
			super(null);
			setReg = new BatchProcess("setProxy", "reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v \"ProxyEnable\" /d 1 /t REG_DWORD /f",
					"reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v \"ProxyServer\" /d \"localhost:" + EasyTravelConfig.read().apacheWebServerProxyPort + "\" /t REG_SZ /f");
			setReg.addStopListener(new StopListener() {

				@Override
				public void notifyProcessStopped() {
					IEProcess.super.start(null);
				}

				@Override
				public void notifyProcessFailed() {
				}
			});
			resetReg = new BatchProcess("resetProxy", "reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v \"ProxyEnable\" /d 0 /t REG_DWORD /f");
			addStopListener(new StopListener() {

				@Override
				public void notifyProcessStopped() {
					resetReg.start();
				}

				@Override
				public void notifyProcessFailed() {
					resetReg.start();
				}
			});
		}

		@Override
		public CommandLine createCommand() {
			return new CommandLine(getExec());
		}

		@Override
		public Feedback start(final Runnable stopRunnable) {
			setReg.start();
			return Feedback.Neutral;
		}

		public Exception getException() {
			if(setReg.getException() != null) return setReg.getException();
			if(resetReg.getException() != null) return resetReg.getException();
			return exception;
		}

		private static File getExec() {
			String path =  EasyTravelConfig.read().browserPath;
			if(path == null || path.length() == 0)
				path = System.getenv("ProgramFiles") + "\\Internet Explorer\\iexplore.exe";

			return new File(path);
		}

		public static boolean exists() {
			return getExec().exists();
		}
	}


	public BrowserProcedure(ProcedureMapping mapping)
			throws CorruptInstallationException {
		super(mapping);
	}

	@Override
	public boolean isOperatingCheckSupported() {
		return true;
	}

	@Override
	public boolean isOperating() {
		return isRunning();
	}

    @Override
    public StopMode getStopMode() {
    	return StopMode.PARALLEL;
    }

	@Override
	public boolean hasLogfile() {
		return false;
	}

	@Override
	public String getLogfile() {
		return null;
	}

	@Override
	public Technology getTechnology() {
		return null;
	}

	@Override
	protected Process createProcess(ProcedureMapping mapping)
			throws CorruptInstallationException {
		IEProcess ie = new IEProcess();
		if(ie.getException() != null) {
			throw new CorruptInstallationException("Failed creating IE launcher!", ie.getException());
		}
		return ie;
	}

	@Override
	protected DtAgentConfig getAgentConfig() {
		return null;
	}

	@Override
	public boolean isEnabled() {
		return OperatingSystem.pickUp() == OperatingSystem.WINDOWS && super.isEnabled();
	}
}
