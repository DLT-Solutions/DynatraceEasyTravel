package com.dynatrace.easytravel.launcher.engine;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class that is destined to run procedure for executing command line (Vagrant, Docker etc.)
 *
 * @author kasper.kulikowski
 */

public abstract class AbstractCommandProcedure extends AbstractProcedure {

	protected AbstractCommandProcedure(ProcedureMapping mapping) throws IllegalArgumentException {
		super(mapping);
		detailsBuilder = new StringBuilder();
	}
	private static final Logger LOGGER = LoggerFactory.make();
	
	public static int WATCHER_THREAD_INTERVALL_MS = 10000; // made public for test purposes
	
	private final List<StopListener> stopListeners = new ArrayList<StopListener>();
	
	private Thread watcherThread;
	
	private String uri;
	
	private boolean shouldWatcherThreadStop;
	
	/**
	 * Set shouldWatcherThreadStop to true if watcher is no longer needed
	 * 
	 * @param shouldWatcherThreadStop
	 * @author kasper.kulikowski
	 */
	public void setShouldWatcherThreadStop(boolean shouldWatcherThreadStop) {
		this.shouldWatcherThreadStop = shouldWatcherThreadStop;
	}

	protected StringBuilder detailsBuilder;

	abstract protected String getExecutable();
	
	abstract protected String getWorkingDir();
	
	abstract protected boolean isUrlCheckAvailable(ProcedureMapping mapping);
	
	/**
	 * Starts watcher that can determine if procedure should be still in Operating state
	 * 
	 * @param name
	 * @author kasper.kulikowski
	 */
	protected void runWatcherThread(String name){
		watcherThread = new CommandProcedureWatcherThread(name);
		watcherThread.start();
	}
	
	/**
	 * Updates procedure name based on directory or on property if specified
	 * 
	 * @param mapping
	 * @param namePropertyConfig
	 * @author kasper.kulikowski
	 */
	
	protected void updateProcedureName(ProcedureMapping mapping, String namePropertyConfig) {
		String procedureName = mapping.getSettingValue("procedure_config", namePropertyConfig);
		if (StringUtils.isBlank(procedureName) && !StringUtils.isBlank(this.getWorkingDir())) {
			procedureName = this.parseProcedureNameFromDirName();
		}
		this.name = name + " (" + procedureName + ")";
	}
	
	private String parseProcedureNameFromDirName(){
		String procedureName = this.getWorkingDir();
		String[] procedureNameParts = procedureName.split("/");
		procedureNameParts = procedureNameParts[procedureNameParts.length - 1].split("\\\\");
		procedureName = procedureNameParts[procedureNameParts.length - 1];
		return procedureName;
	}
	
	@Override
	public String getURI() {
		return uri;
	}

	public void setURI(String uri) {
		this.uri = uri;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getDetails() {
		return detailsBuilder.toString();
	}
	
	/**
	 * Append string to procedure details. Visible after starting procedure and clicking getDetails().
	 * 
	 * @param textToAppend
	 * @author kasper.kulikowski
	 */
	protected void addToDetails(String textToAppend){		
		detailsBuilder.append(textToAppend).append("\n");
	}

	@Override
	public int getTimeout(){
		return ((EasyTravelConfig.read().syncProcessTimeoutMs)*5);
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean isStoppable() {
		return getStopMode().isStoppable();
	}

	/** {@inheritDoc} */
	@Override
	public StopMode getStopMode() {
		return StopMode.PARALLEL;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean isRunning() {
		return isOperating();
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean isOperatingCheckSupported() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasLogfile() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public String getLogfile() {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean agentFound() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public void addStopListener(StopListener stopListener) {
		stopListeners.add(stopListener);
	}

	/** {@inheritDoc} */
	@Override
	public void removeStopListener(StopListener stopListener) {
		stopListeners.remove(stopListener);
	}

	/** {@inheritDoc} */
	@Override
	public void clearStopListeners() {
		stopListeners.clear();
	}
	
	/**
	 * Checks if procedure is still operating or should it be terminated
	 * 
	 * @author kasper.kulikowski
	 */
	
	public void checkIfProcedureStillOperating(){
		boolean isProcedureOperating = isOperating();
		
		if(!isProcedureOperating){
			LOGGER.error("Procedure exited, please check log files");
			stop();
			notifyStopListeners();
		}
	}
	
	private void notifyStopListeners() {
		for(StopListener listener : stopListeners) {
			listener.notifyProcessStopped();
		}
	}
	
	private class CommandProcedureWatcherThread extends Thread {
		public CommandProcedureWatcherThread(String name) {
			super(name);

			setDaemon(true);
		}

		@Override
		public void run() {
			
			while(!shouldWatcherThreadStop) {
				try {
					Thread.sleep(WATCHER_THREAD_INTERVALL_MS);
					checkIfProcedureStillOperating();
								
				} catch (InterruptedException e) {
					LOGGER.warn("CommandProcedureWatcherThread was interrupted", e);
				}
				
			}
		}
	}
}
