package com.dynatrace.easytravel.launcher.engine;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.ProcessExecutor;

import ch.qos.logback.classic.Logger;

/**
 * Class that can be used for basic abstraction for executing command line 
 *
 * @author kasper.kulikowski
 */

public abstract class AbstractCommandWrapper {
	
	private static final Logger LOGGER = LoggerFactory.make();
	private String executable;
	private String workingDir;
	private ProcessBuilder processBuilder;
	
	abstract public String up();
	
	abstract public String status();
	
	abstract public String halt();
	
	public AbstractCommandWrapper(String executable, String workingDir) {
		this.executable = executable;
		this.workingDir = workingDir;
	}
	
	/**
	 * Adding parameter to existing ProcessBuilder command
	 * 
	 * @param parameter
	 * @author kasper.kulikowski
	 */
	protected void addParameterToProcessBuilderCommand(String parameter){
		if(processBuilder != null){
			processBuilder.command().add(parameter);
		} else {
			LOGGER.error("Process Builder must not be null to add parameter.");
		}	
	}
	
	/**
	 * Setup of basic ProcessBuilder configuration based on parameters from constructor
	 * 
	 * @param command
	 * @author kasper.kulikowski
	 */
	
	protected void setupBasicProcessBuilder(String command){
		processBuilder = new ProcessBuilder(executable, command);
		processBuilder.directory(new File(workingDir));
		processBuilder.redirectErrorStream(true);
		processBuilder.environment().remove("CLASSPATH");
	}
	
	/**
	 * Run command that was previously setup in ProcessBuilder
	 * 
	 * @return output
	 * @author kasper.kulikowski
	 */
	
	public String runCommand() {
		String output = "";
		ProcessExecutor processExecutor;
		int commandTimeout = EasyTravelConfig.read().processRunningCheckInterval * 10;
		try {
			processExecutor = new ProcessExecutor(processBuilder);
			output = processExecutor.getInputAsString(commandTimeout, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			LOGGER.error("There were problems executing command", e);
		}
		return output;
	}
	
	protected ProcessBuilder getProcessBuilder(){
		return processBuilder;
	}
}
