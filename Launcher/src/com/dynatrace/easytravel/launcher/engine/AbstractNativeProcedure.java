package com.dynatrace.easytravel.launcher.engine;

import java.io.File;
import java.io.FileNotFoundException;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.process.NativeProcess;
import com.dynatrace.easytravel.launcher.process.NativeWebserverProcess;
import com.dynatrace.easytravel.launcher.process.Process;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;


public abstract class AbstractNativeProcedure extends AbstractProcessProcedure {
	
	private static final Logger LOGGER = LoggerFactory.make();

    /**
     * @throws CorruptInstallationException if the system is in wrong state and unable to start
     *         process
     * @author martin.wurzinger
     */
    protected AbstractNativeProcedure(ProcedureMapping mapping) throws CorruptInstallationException {
        super(mapping);
    }

	protected abstract String getExecutable(ProcedureMapping mapping);

	protected abstract String getWorkingDir();

    @Override
    protected Process createProcess(ProcedureMapping mapping) throws CorruptInstallationException {
    	return createProcess(getExecutable(mapping), getAgentConfig(), getWorkingDir(), getTechnology());
    }

    /**
     * @throws CorruptInstallationException if the system is in wrong state and unable to start
     *         process
     * @author martin.wurzinger
     */
    protected static Process createProcess(String executable, DtAgentConfig dtAgentConfig, String workingDir, Technology technology)
            throws CorruptInstallationException {
		LOGGER.debug("Creating process with working dir <" + workingDir + "> and executable <" + executable + ">");
    	
    	File executableFile = null;
    	
    	final EasyTravelConfig CONFIG = EasyTravelConfig.read();
    	String vagrantBinaryLocation = CONFIG.vagrantBinaryLocation;
    	if(executable.equalsIgnoreCase(CONFIG.b2bFrontendServerIIS) || executable.equalsIgnoreCase(CONFIG.paymentBackendServerIIS)){ //if executable is a IIS WorkerProcess
    		executableFile = new File(Directories.getWinDir(), createOsSpecificExecutable(executable));
    	}else if(executable.equalsIgnoreCase(vagrantBinaryLocation)){
    		executableFile = new File(vagrantBinaryLocation);
    	}else{
    		if (executable.equalsIgnoreCase(CONFIG.mysqlServer)) {
    			executableFile = new File(Directories.getWorkingDir().getParent(), createOsSpecificExecutable(executable));
    		} else {
    			executableFile = new File(Directories.getInstallDir(), createOsSpecificExecutable(executable));
    		}
    	}

        try {
        	NativeProcess process;
        	if(Technology.WEBSERVER.equals(technology) || Technology.WEBPHPSERVER.equals(technology) || Technology.NGINX.equals(technology)) {
        		process = new NativeWebserverProcess(executableFile, dtAgentConfig, technology);
        	} else {
        		process = new NativeProcess(executableFile, dtAgentConfig, technology);
        	}
            if (workingDir != null) {
                process.setWorkingSubDir(workingDir);
            }
            
            return process;
        } catch (FileNotFoundException fnfe) {
            throw new CorruptInstallationException(TextUtils.merge("Corrupt installation. The following file could not be found: {0}", executableFile.getAbsolutePath()), fnfe);
        }
    }

    protected static String createOsSpecificExecutable(String executableNameStub) {
    	if (OperatingSystem.pickUp() == OperatingSystem.WINDOWS && executableNameStub.endsWith(".cmd")) {
    		return executableNameStub;
    	}
        return executableNameStub + OperatingSystem.getCurrentExecutableExtension();
    }
}
