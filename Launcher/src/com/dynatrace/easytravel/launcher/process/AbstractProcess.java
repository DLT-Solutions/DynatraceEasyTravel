package com.dynatrace.easytravel.launcher.process;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.StopListener;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.base.Preconditions;


public abstract class AbstractProcess implements Process {

    private static final Logger LOGGER = Logger.getLogger(AbstractProcess.class.getName());

    private final List<String> applicationArguments = new ArrayList<String>();
    private final DefaultRunningSubject runningSubject = new DefaultRunningSubject();
    private final DtAgentConfig dtAgentConfig;
    private long timeout = ExecuteWatchdog.INFINITE_TIMEOUT;
    private String workingSubDir = null;
    private Watchdog watchdog = null;
    private DefaultExecuteResultHandler resultHandler = null;
    private OutputStream out = System.out;
    private OutputStream err = System.err;
    private InputStream in = null;
    private File propertyFile = null;
	private Map<String, String> environmentOverrides; // avoid empty (size==0), non-null maps!

    protected AbstractProcess(DtAgentConfig dtAgentConfig) {
        this.dtAgentConfig = dtAgentConfig;
    }

    /**
     * @param timeout the timeout of the process in milliseconds or 0 in order to set no timeout
     * @author martin.wurzinger
     */
    public void setTimeout(long timeout) {
        if (timeout > 0) {
            this.timeout = timeout;
        } else {
            this.timeout = ExecuteWatchdog.INFINITE_TIMEOUT;
        }
    }

    @Override
    public void setOut(OutputStream out) {
        this.out = out;
    }

    @Override
    public void setErr(OutputStream err) {
        this.err = err;
    }

    @Override
    public void setIn(InputStream in) {
        this.in = in;
    }

    @Override
    public Feedback start() {
        return start(null);
    }

    @Override
    public Feedback start(Runnable stopRunnable) {
        CommandLine command = createCommand();
        if(command == null) {
        	throw new IllegalStateException("Created command cannot be null");
        }

        DefaultExecutor executor = new DefaultExecutor();

        File workingDir = getWorkingDir();
        Map<String, String> environment = getEnvironment();
    	LOGGER.info("Run command(" + workingDir + "): " + command.toString() + " with environment: " + environment);

		executor.setWorkingDirectory(workingDir);

        // define a watchdog to stop the process if it does not return in the specified time-limit
        this.watchdog = getWatchdog(timeout, runningSubject.getStopListeners(), stopRunnable);
        executor.setWatchdog(this.watchdog);

        resultHandler = getResultHandler(command.toString(), watchdog);
        executor.setStreamHandler(new PumpStreamHandler(out, err,  in));

        try {
            executor.execute(command, environment, resultHandler);
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, TextUtils.merge("Unable to run command ''{0}''", command.toString()), ioe);
            return Feedback.Failure;
        }

        return Feedback.Neutral;
    }

	public abstract CommandLine createCommand();

	@Override
    public Map<String, String> getEnvironment() {
        return environmentOverrides;
    }

    @Override
    public Feedback stop() {
        if (watchdog == null || !watchdog.isWatching()) {
            LOGGER.log(Level.WARNING, "Trying to stop an unmonitored process");
            return Feedback.Neutral;
        }

        watchdog.destroyProcess();

        try {
        	final EasyTravelConfig config = EasyTravelConfig.read();
			resultHandler.waitFor(config.shutdownTimeoutMs);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, TextUtils.merge("Waiting for stopping the following process timed out: ''{0}''", createCommand().toString()), e);
        }

        try {
            if (isExpectedExitValue(watchdog.getExitValue())) {
                // process terminated with expected exit value
                return Feedback.Success;
            } else {
                // process terminated with unexpected exit value++++++
                return Feedback.Neutral;
            }
        } catch (IllegalStateException ise) {
            // means: process could not been terminated
            return Feedback.Failure;
        }
    }

    @Override
    public boolean isRunning() {
        return watchdog != null && !resultHandler.hasResult() && !hasExited();
    }

    // TODO@(stefan.moschinski): REFACTOR! --> why has the result handler sometimes no result (i.e., why does it not recognize the termination of the java processes)
    private boolean hasExited() {
    	try {
    		if (watchdog.getProcess() != null && Integer.valueOf(watchdog.getExitValue()) != null) {
    			return true;
    		}
    	} catch (IllegalStateException ex) {
    		if(LOGGER.isLoggable(Level.FINE)) {
    			LOGGER.fine("Process '" + watchdog.getProcess() + "' has not exited yet");
    		}
    	}
    	return false;
	}

	/** {@inheritDoc} */
    @Override
	public Process addApplicationArgument(String appArgument) {
        this.applicationArguments.add(appArgument);
		return this;
    }

	@Override
	public Process addApplicationArgumentPair(String appArgument, Object value) {
		Preconditions.checkNotNull(value);
		this.applicationArguments.add(appArgument);
		this.applicationArguments.add(value.toString());
		return this;
	}

    protected List<String> getApplicationArguments() {
        return Collections.unmodifiableList(applicationArguments);
    }

    @Override
    public void clearApplicationArguments() {
        applicationArguments.clear();
    }

	@Override
	public void setEnvironmentVariable(String key, String value) {
		if (environmentOverrides == null)
			environmentOverrides = new HashMap<String, String>(30);
		environmentOverrides.put(key, value);
	}

    public void setWorkingSubDir(String workingSubDir) {
        this.workingSubDir = workingSubDir;
    }

    protected File getWorkingDir() {
        if (workingSubDir == null) {
            return Directories.getInstallDir();
        }

        File workingDir = new File(Directories.getInstallDir(), workingSubDir);
        if (workingDir.exists() && workingDir.isDirectory()) {
            return workingDir;
        } else {
            LOGGER.warning(TextUtils.merge("The specified working directory is invalid: {0}", workingDir));
            return Directories.getInstallDir();
        }
    }

    protected long getTimeout() {
        return timeout;
    }

    @Override
    public DtAgentConfig getDtAgentConfig() {
        return dtAgentConfig;
    }

    protected boolean isExpectedExitValue(int exitValue) {
        return exitValue == 0;
    }

    @Override
    public void addStopListener(StopListener stopListener) {
        runningSubject.addStopListener(stopListener);
    }

    @Override
    public void removeStopListener(StopListener stopListener) {
        runningSubject.removeStopListener(stopListener);
    }

    @Override
    public void clearStopListeners() {
        runningSubject.clearStopListeners();
    }

    @Override
	public String getDetails() {
    	StringBuilder builder = new StringBuilder();

    	CommandLine command = createCommand();
    	if(command != null) {
			builder.append(command.getExecutable());
			for(String arg : command.getArguments()) {
				builder.append(" ").append(arg);
			}
    	}

		Map<String, String> environment = getEnvironment();
		if(environment != null && environment.size() > 0) {
			builder.append("\nEnvironment: ");
			List<String> sortedKeys = new ArrayList<String>(environment.keySet());
			Collections.sort(sortedKeys);
			for(String key : sortedKeys) {
				builder.append("\n").append(key).append("=").append(environment.get(key));
			}
		}

    	return builder.toString();
	}

    /**
     * Check if the process has been stopped and if a exit value is available.
     * @return <code>true</code> if a process exit value is available or <code>false</code> otherwise
     * @author martin.wurzinger
     */
    public boolean hasResult() {
        if (resultHandler == null) {
            return false;
        } else {
            return resultHandler.hasResult();
        }
    }

    /**
     * Get the process exit value.
     * @return the exit value of the process
     * @throws IllegalStateException if the process has not exited yet
     * @author martin.wurzinger
     */
    public int getExitValue() throws IllegalStateException {
        if (resultHandler == null) {
            throw new IllegalStateException("The process was not started yet");
        }
        //this call throws an illegal state exception as well
        return watchdog.getExitValue();
    }


	@Override
	public void setPropertyFile() {
        addApplicationArgument(BaseConstants.MINUS + Constants.CmdArguments.PROPERTY_FILE);
        propertyFile = EasyTravelConfig.read().storeInTempFile();
        if(propertyFile == null) {
        	// fallback to normal file if we cannot write the tempfile
        	addApplicationArgument(EasyTravelConfig.read().filePath);
        } else {
            addApplicationArgument(propertyFile.getAbsolutePath());
        }
	}

	@Override
	public File getPropertyFile() {
		return propertyFile;
	}

	protected DefaultExecuteResultHandler getResultHandler(String commandString, FailureListener failureListener ) {
		return new AttentiveExecuteResultHandler(commandString, failureListener);
	}

    protected Watchdog getWatchdog(long timeout, List<StopListener> stopListeners, Runnable stopRunnable) {
		return new Watchdog(timeout, stopListeners, stopRunnable);
	}
}
