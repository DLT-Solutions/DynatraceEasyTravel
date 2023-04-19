package com.dynatrace.easytravel.launcher.vagrant;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.launcher.engine.AbstractCommandWrapper;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;

public class VagrantWrapper extends AbstractCommandWrapper {
	
	private static final Logger LOGGER = LoggerFactory.make();

	private String provider;

	public VagrantWrapper(String executable, String workingDir, ProcedureMapping mapping) {
		super(executable, workingDir);
		provider = mapping.getSettingValue("procedure_config", "config.vagrantProvider");

	}

	@Override
	public String up() {
		if(StringUtils.isNotBlank(provider)){
			return runVagrantCommandWithParameter("up", "--provider=" + provider);
		} else {
			return runVagrantCommand("up");
		}	
	}

	@Override
	public String status() {
		return runVagrantCommand("status");
	}

	@Override
	public String halt() {
		return runVagrantCommand("halt");
	}
	
	public String sshConfig(){
		return runVagrantCommand("ssh-config");
	}
	
	public String destroy(){
		return runVagrantCommandWithParameter("destroy", "-f");
	}

	private String runVagrantCommand(String command) {
		this.setupBasicProcessBuilder(command);
		return runCommandWithLockCheck();
	}

	private String runVagrantCommandWithParameter(String command, String parameter) {
		this.setupBasicProcessBuilder(command);
		this.addParameterToProcessBuilderCommand(parameter);
		return runCommandWithLockCheck();
	}

	private String runCommandWithLockCheck() {
		String output = "";
		try {
			output = runCommand();
			if (output.contains("Vagrant lock")) {
				throw new IOException(output);
			}
		} catch (IOException e) {
			LOGGER.error("Vagrant procedure is in lock state, please kill all ruby processes.", e);
		} 
		return output;
	}

}
