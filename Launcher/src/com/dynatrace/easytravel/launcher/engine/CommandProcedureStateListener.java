package com.dynatrace.easytravel.launcher.engine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.components.ComponentManagerAccess;
import com.dynatrace.easytravel.components.ComponentManagerProxy;
import com.dynatrace.easytravel.components.WatchedProcedureInfoHolder;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.frontend.plugin.WebservicePluginStateProxy;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.procedures.RemoteProcedure;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.launcher.vagrant.VagrantBoxType;
import com.dynatrace.easytravel.launcher.vagrant.VagrantProcedure;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.PluginNotificationTemplate;
import com.dynatrace.easytravel.spring.PluginStateProxy;
import com.dynatrace.easytravel.spring.RemotePluginService;
import com.dynatrace.easytravel.util.ResourceFileReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

import ch.qos.logback.classic.Logger;

public class CommandProcedureStateListener implements ProcedureStateListener {
	private static final Logger log = LoggerFactory.make();
	private static ComponentManagerProxy componentsManagerProxy;
	private static PluginStateProxy pluginStateProxy;
	private static List<WatchedProcedureInfoHolder> watchedProcedures;
	boolean doRestoreComponents = false;
	private final String procedureConfig = "procedure_config";
	private String templateConfiguration;
	private EasyTravelConfig config = EasyTravelConfig.read();

	public static CommandProcedureStateListener getInstance() {

		componentsManagerProxy = new ComponentManagerAccess();

		EasyTravelConfig config = EasyTravelConfig.read();
		String pluginServiceHost = config.pluginServiceHost;
		boolean isPluginServiceNotBlank = StringUtils.isNotBlank(pluginServiceHost);
		if (isPluginServiceNotBlank) {
			pluginStateProxy = new RemotePluginService(pluginServiceHost, config.pluginServicePort);
		}
		else {
			pluginStateProxy = new WebservicePluginStateProxy();
		}

		watchedProcedures = new CopyOnWriteArrayList<WatchedProcedureInfoHolder>();
		return INSTANCE;
	}

	private static final CommandProcedureStateListener INSTANCE = new CommandProcedureStateListener();

	@Override
	public void notifyProcedureStateChanged(StatefulProcedure subject, State oldState, State newState) {

		String procedureId = subject.getMapping().getId();

		if (Constants.Procedures.VAGRANT_ID.equals(procedureId)) {
			Procedure proc = subject.getDelegate();

			if (proc instanceof VagrantProcedure) {
				VagrantProcedure vagrantProc = (VagrantProcedure) subject.getDelegate();
				String vagrantBoxURI = vagrantProc.getURI();

				if (newState.equals(State.OPERATING) && vagrantProc.isUrlCheckAvailable()) {
					WatchedProcedureInfoHolder infoHolder = null;
					log.info("CommandController adding: " + procedureId + " " + vagrantProc.getURI());
					if(vagrantProc.getVagrantBoxType().equals(VagrantBoxType.CUSTOMER_FRONTEND)){
						infoHolder = new WatchedProcedureInfoHolder(vagrantBoxURI, new String[] { "vagrant", "frontend",  vagrantProc.getProtocol(), vagrantProc.getContext()});
						componentsManagerProxy.setComponent(infoHolder.getURI(), infoHolder.getParams());
					} else {
						infoHolder = new WatchedProcedureInfoHolder(vagrantBoxURI, new String[] { "vagrant", "backend", vagrantProc.getProtocol(), vagrantProc.getContext()});
						componentsManagerProxy.setComponent(infoHolder.getURI(), infoHolder.getParams());
					}

					watchedProcedures.add(infoHolder);

				} else if (!oldState.equals(State.STOPPED) && newState.equals(State.STOPPED)) {

					log.info("CommandController deleting: " + procedureId + " " + vagrantProc.getURI());
					removeFromWatchedProcedures(vagrantBoxURI);
					componentsManagerProxy.removeComponent(vagrantBoxURI);

				}
			} else {
				RemoteProcedure vagrantProc = (RemoteProcedure) subject.getDelegate();
				ProcedureMapping mapping = vagrantProc.getMapping();


				if (newState.equals(State.OPERATING)) {


					String context = retrieveVagrantContextFromMapping(mapping);
					String protocol = retrieveVagrantProtocolFromMapping(mapping);
					String type = retrieveVagrantTypeFromMapping(mapping);
					String vagrantBoxURI = vagrantProc.getComponentUri();

					WatchedProcedureInfoHolder infoHolder = null;

					log.info("Procedure Uri will be: "+vagrantProc.getComponentUri());
					if(type.equals("frontend")){
						infoHolder = new WatchedProcedureInfoHolder(vagrantBoxURI, new String[] { "vagrant", "frontend", protocol, context});
						componentsManagerProxy.setComponent( infoHolder.getURI(), infoHolder.getParams());
					} else {
						infoHolder = new WatchedProcedureInfoHolder(vagrantBoxURI, new String[] { "vagrant", "backend", protocol, context});
						componentsManagerProxy.setComponent(infoHolder.getURI(), infoHolder.getParams() );
					}

					watchedProcedures.add(infoHolder);

				} else if (!oldState.equals(State.STOPPED) && newState.equals(State.STOPPED)) {
					String vagrantBoxURI = vagrantProc.getComponentUri();
					removeFromWatchedProcedures(vagrantBoxURI);
					componentsManagerProxy.removeComponent(vagrantBoxURI);
				}

				log.info("Vagrant is running as Remote Procedure.");
			}
		} else if(Constants.Procedures.PLUGIN_SERVICE.equals(procedureId)) {
			String pluginServiceHost = config.pluginServiceHost;
			boolean isPluginServiceComponentHolder = StringUtils.isNotBlank(pluginServiceHost);
			if(isPluginServiceComponentHolder){
				resetComponents(oldState, newState);
				sendTemplateConfiguration(newState);
			}
		} else if(Constants.Procedures.BUSINESS_BACKEND_ID.equals(procedureId)) {
			String pluginServiceHost = config.pluginServiceHost;
			boolean isBusinessBackendComponentHolder = StringUtils.isBlank(pluginServiceHost);
			if(isBusinessBackendComponentHolder){
				resetComponents(oldState, newState);
				sendTemplateConfiguration(newState);
			}
		}
	}

	private void removeFromWatchedProcedures(String procedureURI){
		Iterator<WatchedProcedureInfoHolder> iter = watchedProcedures.iterator();
		List<WatchedProcedureInfoHolder> tmpList = new ArrayList<WatchedProcedureInfoHolder>();

		while(iter.hasNext()){
			WatchedProcedureInfoHolder e = iter.next();
			if(e.getURI().contains(procedureURI)){
				tmpList.add(e);
			}
		}

		watchedProcedures.removeAll(tmpList);
	}

	private String retrieveVagrantContextFromMapping(ProcedureMapping mapping){
		final String vagrantBoxContextProperty = "config.vagrantBoxContext";

		String context = mapping.getSettingValue(procedureConfig, vagrantBoxContextProperty);

		if(StringUtils.isBlank(context)){
			context = config.pluginAgentContext;
		}
		return context;
	}
	private String retrieveVagrantProtocolFromMapping(ProcedureMapping mapping){
		final String vagrantBoxProtocol = "config.vagrantBoxProtocol";

		String protocol = mapping.getSettingValue(procedureConfig, vagrantBoxProtocol);

		if(StringUtils.isBlank(protocol)){
			protocol = config.vagrantBoxProtocol;
		}

		protocol = protocol + "://";
		return protocol;
	}
	private String retrieveVagrantTypeFromMapping(ProcedureMapping mapping){
		final String vagrantBoxTypeProperty = "config.vagrantBoxType";

		String type = mapping.getSettingValue(procedureConfig, vagrantBoxTypeProperty);
		if(StringUtils.isBlank(type)){
			type = "frontend";
		}

		return type;
	}

	private void resetComponents(State oldState, State newState){
		if (!oldState.equals(State.STOPPED) && newState.equals(State.STOPPED) && !watchedProcedures.isEmpty()) {
			doRestoreComponents = true;
			log.warn("There will be a need to restore components");
		} else if (newState.equals(State.OPERATING) && doRestoreComponents) {

			for(WatchedProcedureInfoHolder entry : watchedProcedures){
				log.warn("Restoring: "+entry.getURI());
				componentsManagerProxy.setComponent( entry.getURI(), entry.getParams());
			}
			doRestoreComponents = false;
		}
	}

	private void sendTemplateConfiguration(State newState) {
		if (newState.equals(State.OPERATING)) {
			if (Strings.isNullOrEmpty(templateConfiguration)) {
				log.info("sendTemplateConfiguration - loading template from file");
				readTemplateConfigurationFromFile();
			}

			log.info("sendTemplateConfiguration - config: " + templateConfiguration);
			pluginStateProxy.setPluginTemplateConfiguration(templateConfiguration);
		}
	}

	private void readTemplateConfigurationFromFile() {
		try {
			PluginNotificationTemplate[] templates;
			ObjectMapper mapper = new ObjectMapper();
			File file = new File(Directories.getConfigDir(), ResourceFileReader.PLUGINNOTIFICATIONCONFIG);

			if (file.exists()) {
				templates = mapper.readValue(file, PluginNotificationTemplate[].class);
				templateConfiguration = mapper.writeValueAsString(templates);
			}
		} catch (IOException e) {
			log.warn("Problem parsing json configuration", e);
		}
	}
}
