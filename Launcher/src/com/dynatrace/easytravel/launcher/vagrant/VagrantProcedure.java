package com.dynatrace.easytravel.launcher.vagrant;

import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.AbstractCommandProcedure;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;

public class VagrantProcedure extends AbstractCommandProcedure {
	
	private static final Logger LOGGER = LoggerFactory.make();
	
	private VagrantWrapper vWrapper;
	
	private boolean isUrlCheckAvailable = false;
	
	private int port;
	
	private String context;
	
	public String getContext(){
		return context;
	}
	
	private String protocol;
	
	public String getProtocol(){
		return protocol;
	}

	public boolean isUrlCheckAvailable(){
		return isUrlCheckAvailable;
	}
	
	private VagrantBoxType vagrantBoxType;
	
	public VagrantBoxType getVagrantBoxType() {
		return vagrantBoxType;
	}
	
	private boolean doDestroyOnHalt = false;

	final String vagrantProcedureNameProperty = "config.vagrantProcedureName";
	final String vagrantBoxHostProperty = "config.vagrantBoxHost";
	final String vagrantBoxPortProperty = "config.vagrantBoxPort";
	final String vagrantBoxProtocol = "config.vagrantBoxProtocol";
	final String vagrantBoxContextProperty = "config.vagrantBoxContext";
	final String vagrantBoxTypeProperty = "config.vagrantBoxType";
	final String vagrantDestroyOnHaltProperty = "config.destroyOnHalt";
	

	public VagrantProcedure(ProcedureMapping mapping) throws CorruptInstallationException {
		super(mapping);
		
		vWrapper = new VagrantWrapper(getExecutable(), getWorkingDir(), mapping);
		
		this.updateProcedureName(mapping,vagrantProcedureNameProperty);
		this.doDestroyOnHalt = doDestroyOnHalt();
		
		vagrantBoxType = getVagrantBoxType(mapping);
		
		LOGGER.info("Preparing Vagrant procedure.");

	}
	
	private boolean doDestroyOnHalt(){
		String doDestroyOnHaltProperty = getMapping().getSettingValue("procedure_config", vagrantDestroyOnHaltProperty);
		if(StringUtils.isNotBlank(doDestroyOnHaltProperty)){
			return true;
		} else {
			return false;
		}
	}
	
	private void retrieveVagrantBoxAddress(){
		String host = getMapping().getSettingValue("procedure_config", vagrantBoxHostProperty);
		
		if(StringUtils.isBlank(host)){
			host = parseIpFromOutput(excludeFromOutput(vWrapper.sshConfig()));
		}		
	
		this.setURI(host+":"+Integer.toString(port));
	}
	
	private String parseIpFromOutput(String output){
		String outputLines[] = output.split("\\r?\\n");
		
		for(String line : outputLines){
			if(StringUtils.containsIgnoreCase(line,"hostname")){
				String[] splitedLine = line.split("\\s+");
				int lastIndex = splitedLine.length-1;
				
				return splitedLine[lastIndex]; 
			}
		}
		return StringUtils.EMPTY;
	}
	
	protected boolean isUrlCheckAvailable(ProcedureMapping mapping){
		String portProperty = mapping.getSettingValue("procedure_config", vagrantBoxPortProperty);
		String contextProperty = mapping.getSettingValue("procedure_config", vagrantBoxContextProperty);
		
		if(StringUtils.isNotBlank(portProperty)){
			initializeVariablesForUrlChecks(portProperty, contextProperty);
			return true;
		} else {
			return false;
		}
		
	}
	
	private void initializeVariablesForUrlChecks(String portProperty, String contextProperty){	
		if(StringUtils.isNotBlank(contextProperty)){
			this.context = contextProperty;
		} else {
			this.context = EasyTravelConfig.read().pluginAgentContext;
		}	
		
		this.port = Integer.parseInt(portProperty);
		
		this.protocol = getMapping().getSettingValue("procedure_config", vagrantBoxProtocol);
		if(StringUtils.isBlank(this.protocol)){
			this.protocol = EasyTravelConfig.read().vagrantBoxProtocol;
		}
		this.protocol = this.protocol + "://";

	}
	
	private VagrantBoxType getVagrantBoxType(ProcedureMapping mapping){
		String vagrantBoxType = mapping.getSettingValue("procedure_config", vagrantBoxTypeProperty);
		
		if(StringUtils.isNotBlank(vagrantBoxType)){
			return vagrantBoxType.equals("frontend") ? VagrantBoxType.CUSTOMER_FRONTEND : VagrantBoxType.BUSINESS_BACKEND;
		} else {
			return VagrantBoxType.BUSINESS_BACKEND;
		}	
	}
	
	@Override
	public Feedback run() {
		this.isUrlCheckAvailable = isUrlCheckAvailable(getMapping());
		
		clearDetails();
		
		setShouldWatcherThreadStop(false);
		
		if (this.isOperating()) {
			LOGGER.warn("Vagrant box is already running.");
		} else {			
			runVagrant();			
		}
		
		runWatcherThread(vagrantProcedureNameProperty);
		
		return Feedback.Neutral;
	}
	
	public void runVagrant(){
		addToDetails(excludeFromOutput(vWrapper.up()));
		addToDetails(excludeFromOutput(vWrapper.sshConfig()));
	}

	private void clearDetails(){
		detailsBuilder.setLength(0);
		addToDetails("Vagrant Box Type: "+vagrantBoxType.toString());
		addToDetails("Vagrant working directory: "+getWorkingDir());	
	}
	
	private String excludeFromOutput(String output){		
		return output.substring(output.lastIndexOf('}')+1, output.length());
	}
	
	@Override
	public boolean isOperating() {	
		
		if(isUrlCheckAvailable){
			this.retrieveVagrantBoxAddress();		
			if(!UrlUtils.checkConnect(this.getProtocol()+this.getURI()).isOK()){
				return false;
			}
		}
		return checkVagrantBoxStatus();
	}
	
	private boolean checkVagrantBoxStatus(){
		String out = vWrapper.status();
		if (out.contains("running")) {
			LOGGER.info("Vagrant procedure is running.");
			return true;
		} else {
			return false;
		}	
	}
	
	@Override
	public boolean isInstrumentationSupported(){
		return false;
	}
	
	@Override
	public Technology getTechnology() {
		return Technology.VAGRANT;
	}

	@Override
	public String getExecutable() {
		return EasyTravelConfig.read().vagrantBinaryLocation;
	}
	
	@Override
	public String getWorkingDir(){
		return getMapping().getSettingValue("procedure_config", "config.vagrantWorkingDir");
	}

	@Override
	public Feedback stop() {
		LOGGER.debug("Stopping procedures. Stopping Vagrant.");
		this.isUrlCheckAvailable = false;
		String output = "";
		int maxRetries = 30;
		int counter = 0;
		this.setShouldWatcherThreadStop(true);
		try {
			do {
				if (timeoutReached(maxRetries, counter)) {
					throw new TimeoutException("Timeout reached.");
				}
				if(doDestroyOnHalt){
					output = vWrapper.destroy();
				} else{
					output = vWrapper.halt();
				}
									
				LOGGER.info("Waiting for procedure to halt. "+output);
				Thread.sleep(EasyTravelConfig.read().processOperatingCheckIntervalMs);
				counter++;

			} while (output.contains("already executing"));

			counter = 0;

			do {
				if (timeoutReached(maxRetries, counter)) {
					throw new TimeoutException("Timeout reached.");
				}
				output = vWrapper.status();
				LOGGER.info("Waiting for procedure to halt. "+output);
				Thread.sleep(EasyTravelConfig.read().processOperatingCheckIntervalMs);
				counter++;
			} while (!output.contains("poweroff") && !output.contains("not created") && !output.contains("stopped."));
			
			LOGGER.debug("Stopping procedures. Vagrant stopped.");
			return Feedback.Success;
		} catch (Exception e) {
			LOGGER.error("There were problems shutting down procedure", e);
		}
		
		LOGGER.debug("Stopping procedures. Vagrant stop failed");
		return Feedback.Failure;
	}

	private boolean timeoutReached(int maxRetries, int counter) {
		return (counter >= maxRetries);
	}

	public void setVagrantWrapper(VagrantWrapper vWrapper){
		this.vWrapper = vWrapper;
	}
	
	@Override
	public boolean isRunning() {
		return isOperating();
	}
}
