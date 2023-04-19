package com.dynatrace.easytravel.launcher.process;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.exec.DefaultExecuteResultHandler;

import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.Technology;


public class NativeWebserverProcess extends NativeProcess {


	public NativeWebserverProcess(File executable, DtAgentConfig dtAgentConfig, Technology technology)
			throws FileNotFoundException {
		super(executable, dtAgentConfig, technology);
	}

	@Override
	protected DefaultExecuteResultHandler getResultHandler(String commandString, FailureListener failureListener) {
	    return new FailureTolerantResultHandler(commandString, failureListener);
	}



}
