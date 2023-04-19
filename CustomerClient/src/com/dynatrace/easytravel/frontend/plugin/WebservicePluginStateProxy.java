package com.dynatrace.easytravel.frontend.plugin;

import java.rmi.RemoteException;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.dynatrace.easytravel.business.client.ConfigurationServiceStub;
import com.dynatrace.easytravel.business.webservice.*;
import com.dynatrace.easytravel.components.ComponentManagerProxy;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.PluginStateProxy;
import com.dynatrace.easytravel.util.LocalUriProvider;
import com.dynatrace.easytravel.util.ServiceStubFactory;
import com.dynatrace.easytravel.util.ServiceStubProvider;

import ch.qos.logback.classic.Logger;

public class WebservicePluginStateProxy implements PluginStateProxy, ComponentManagerProxy  {

	private static final Logger log = LoggerFactory.make();

	private final static GetAllPluginsDocument allPluginsDocument = GetAllPluginsDocument.Factory.newInstance();
	static {
		allPluginsDocument.setGetAllPlugins(GetAllPluginsDocument.GetAllPlugins.Factory.newInstance());
	}
	private String[] invokeGetAllPlugins() throws RemoteException {
		ConfigurationServiceStub configurationService = ServiceStubProvider.getServiceStub(ConfigurationServiceStub.class);
		try {
			GetAllPluginsResponseDocument res = configurationService.getAllPlugins(allPluginsDocument);
			String[] allPlugins = res.getGetAllPluginsResponse().getReturnArray();
			ServiceStubProvider.returnServiceStub(configurationService);
			return allPlugins;
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(configurationService);
			throw e;
		}
	}

	private final static GetAllPluginNamesDocument allPluginNamesDocument = GetAllPluginNamesDocument.Factory.newInstance();
	static {
		allPluginNamesDocument.setGetAllPluginNames(GetAllPluginNamesDocument.GetAllPluginNames.Factory.newInstance());
	}
	private String[] invokeGetAllPluginNames() throws RemoteException {
		ConfigurationServiceStub configurationService = ServiceStubProvider.getServiceStub(ConfigurationServiceStub.class);
		try {
			GetAllPluginNamesResponseDocument res = configurationService.getAllPluginNames(allPluginNamesDocument);
			String[] allPluginNames = res.getGetAllPluginNamesResponse().getReturnArray();
			ServiceStubProvider.returnServiceStub(configurationService);
			return allPluginNames;
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(configurationService);
			throw e;
		}
	}

	private final static GetEnabledPluginsDocument enabledPluginsDocument = GetEnabledPluginsDocument.Factory.newInstance();
	static {
		enabledPluginsDocument.setGetEnabledPlugins(GetEnabledPluginsDocument.GetEnabledPlugins.Factory.newInstance());
	}
	private String[] invokeGetEnabledPlugins() throws RemoteException {
		ConfigurationServiceStub configurationService = ServiceStubProvider.getServiceStub(ConfigurationServiceStub.class);
		try {
			GetEnabledPluginsResponseDocument res = configurationService.getEnabledPlugins(enabledPluginsDocument);
			String[] enabledPlugins = res.getGetEnabledPluginsResponse().getReturnArray();
			ServiceStubProvider.returnServiceStub(configurationService);
			return enabledPlugins;
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(configurationService);
			throw e;
		}
	}

	private String[] invokeGetEnabledPluginsForHost(String host) throws RemoteException {
		GetEnabledPluginsForHostDocument doc = GetEnabledPluginsForHostDocument.Factory.newInstance();
		doc.setGetEnabledPluginsForHost(GetEnabledPluginsForHostDocument.GetEnabledPluginsForHost.Factory.newInstance());
		doc.getGetEnabledPluginsForHost().setHost(host);

		ConfigurationServiceStub configurationService = ServiceStubProvider.getServiceStub(ConfigurationServiceStub.class);
		try {
			GetEnabledPluginsForHostResponseDocument res = configurationService.getEnabledPluginsForHost(doc);
			String[] enabledPlugins = res.getGetEnabledPluginsForHostResponse().getReturnArray();
			ServiceStubProvider.returnServiceStub(configurationService);
			return enabledPlugins;
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(configurationService);
			throw e;
		}
	}

	private final static GetEnabledPluginNamesDocument enabledPluginNamesDocument = GetEnabledPluginNamesDocument.Factory.newInstance();
	static {
		enabledPluginNamesDocument.setGetEnabledPluginNames(GetEnabledPluginNamesDocument.GetEnabledPluginNames.Factory.newInstance());
	}
	private String[] invokeGetEnabledPluginNames() throws RemoteException {
		ConfigurationServiceStub configurationService = ServiceStubProvider.getServiceStub(ConfigurationServiceStub.class);
		try {
			GetEnabledPluginNamesResponseDocument res = configurationService.getEnabledPluginNames(enabledPluginNamesDocument);
			String[] enabledPluginNames = res.getGetEnabledPluginNamesResponse().getReturnArray();
			ServiceStubProvider.returnServiceStub(configurationService);
			return enabledPluginNames;
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(configurationService);
			throw e;
		}
	}

	private String[] invokeGetEnabledPluginNamesForHost(String host) throws RemoteException {
		GetEnabledPluginNamesForHostDocument doc = GetEnabledPluginNamesForHostDocument.Factory.newInstance();
		doc.setGetEnabledPluginNamesForHost(GetEnabledPluginNamesForHostDocument.GetEnabledPluginNamesForHost.Factory.newInstance());
		doc.getGetEnabledPluginNamesForHost().setHost(host);

		ConfigurationServiceStub configurationService = ServiceStubProvider.getServiceStub(ConfigurationServiceStub.class);
		try {
			GetEnabledPluginNamesForHostResponseDocument res = configurationService.getEnabledPluginNamesForHost(doc);
			String[] enabledPluginNames = res.getGetEnabledPluginNamesForHostResponse().getReturnArray();
			ServiceStubProvider.returnServiceStub(configurationService);
			return enabledPluginNames;
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(configurationService);
			throw e;
		}
	}

	private void invokeRegisterPlugins(String[] pluginData) throws RemoteException
	{
		log.info("Registering " + pluginData.length + " plugins on the remote plugin server located at " + LocalUriProvider.getBackendWebServiceUri(ServiceStubFactory.getServiceName(ConfigurationServiceStub.class)));
		RegisterPluginsDocument doc = RegisterPluginsDocument.Factory.newInstance();
		doc.setRegisterPlugins(RegisterPluginsDocument.RegisterPlugins.Factory.newInstance());
		doc.getRegisterPlugins().setPluginDataArray(pluginData);
		ConfigurationServiceStub configurationService = ServiceStubProvider.getServiceStub(ConfigurationServiceStub.class);
		try {
			configurationService.registerPlugins(doc);
			ServiceStubProvider.returnServiceStub(configurationService);
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(configurationService);
			throw e;
		}
	}

	private void invokeSetPluginEnabled(String name, boolean enabled) throws RemoteException {
		SetPluginEnabledDocument doc = SetPluginEnabledDocument.Factory.newInstance();
		doc.setSetPluginEnabled(SetPluginEnabledDocument.SetPluginEnabled.Factory.newInstance());
		doc.getSetPluginEnabled().setName(name);
		doc.getSetPluginEnabled().setEnabled(enabled);

		ConfigurationServiceStub configurationService = ServiceStubProvider.getServiceStub(ConfigurationServiceStub.class);
		try {
			configurationService.setPluginEnabled(doc);
			ServiceStubProvider.returnServiceStub(configurationService);
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(configurationService);
			throw e;
		}
	}

	private void invokeSetPluginHosts(String name, String[] hosts) throws RemoteException {
		SetPluginHostsDocument doc = SetPluginHostsDocument.Factory.newInstance();
		doc.setSetPluginHosts(SetPluginHostsDocument.SetPluginHosts.Factory.newInstance());
		doc.getSetPluginHosts().setName(name);
		doc.getSetPluginHosts().setHostsArray(hosts);

		ConfigurationServiceStub configurationService = ServiceStubProvider.getServiceStub(ConfigurationServiceStub.class);
		try {
			configurationService.setPluginHosts(doc);
			ServiceStubProvider.returnServiceStub(configurationService);
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(configurationService);
			throw e;
		}
	}

	private void invokeSetComponent(String ip, String[] params) throws RemoteException{
		SetComponentDocument doc = SetComponentDocument.Factory.newInstance();
		doc.setSetComponent(SetComponentDocument.SetComponent.Factory.newInstance());
		doc.getSetComponent().setIp(ip);
		doc.getSetComponent().setParamsArray(params);

		ConfigurationServiceStub configurationService = ServiceStubProvider.getServiceStub(ConfigurationServiceStub.class);

		try {
			configurationService.setComponent(doc);
			ServiceStubProvider.returnServiceStub(configurationService);
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(configurationService);
			throw e;
		}
	}

	private void invokeRemoveComponent(String ip) throws RemoteException{
		RemoveComponentDocument doc = RemoveComponentDocument.Factory.newInstance();
		doc.getRemoveComponent().setIp(ip);
		ConfigurationServiceStub configurationService = ServiceStubProvider.getServiceStub(ConfigurationServiceStub.class);

		try {
			configurationService.removeComponent(doc);
			ServiceStubProvider.returnServiceStub(configurationService);
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(configurationService);
			throw e;
		}
	}

	private String[] invokeGetComponentsList(String type) throws RemoteException{
		GetComponentsIPListDocument doc = GetComponentsIPListDocument.Factory.newInstance();
		doc.setGetComponentsIPList(GetComponentsIPListDocument.GetComponentsIPList.Factory.newInstance());
		doc.getGetComponentsIPList().setType(type);

		ConfigurationServiceStub configurationService = ServiceStubProvider.getServiceStub(ConfigurationServiceStub.class);
		try {
			GetComponentsIPListResponseDocument res = configurationService.getComponentsIPList(doc);
			String[] componentsIPList = res.getGetComponentsIPListResponse().getReturnArray();
			ServiceStubProvider.returnServiceStub(configurationService);
			return componentsIPList;
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(configurationService);
			throw e;
		}
	}

	private void invokeSetPluginTemplateConfiguration(String configuration) throws RemoteException{
		ConfigurationServiceStub configurationService = ServiceStubProvider.getServiceStub(ConfigurationServiceStub.class);
		try {
			SetPluginTemplateConfigurationDocument doc = SetPluginTemplateConfigurationDocument.Factory.newInstance();
			doc.setSetPluginTemplateConfiguration(SetPluginTemplateConfigurationDocument.SetPluginTemplateConfiguration.Factory.newInstance());
			doc.getSetPluginTemplateConfiguration().setConfiguration(configuration);
			configurationService.setPluginTemplateConfiguration(doc);
			ServiceStubProvider.returnServiceStub(configurationService);
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(configurationService);
			throw e;
		}
	}

	@Override
	public void registerPlugins(String[] pluginData) {
		try {
			// TODO: stop if registering did fail because of missing Backend
			invokeRegisterPlugins(pluginData);
		} catch (RemoteException e) {
			log.error("Cannot invoke WebService for plugin register" + getLocationAndMessage(e));
		}
	}

	@Override
	public String[] getEnabledPlugins() {
		try {
			return invokeGetEnabledPlugins();
		} catch (RemoteException e) {
			log.error("Cannot invoke WebService for plugin get enabled" + getLocationAndMessage(e));
			return null;
		}
	}

	@Override
	public String[] getEnabledPluginsForHost(String host) {
		try {
			return invokeGetEnabledPluginsForHost(host);
		} catch (RemoteException e) {
			log.error("Cannot invoke WebService for plugin get enabled" + getLocationAndMessage(e));
			return null;
		}
	}

	@Override
	public String[] getAllPlugins() {
		try {
			return invokeGetAllPlugins();
		} catch (RemoteException e) {
			log.error("Cannot invoke WebService for plugin get all" + getLocationAndMessage(e));
			return null;
		}
	}

	@Override
	public String[] getAllPluginNames() {
		try {
			return invokeGetAllPluginNames();
		} catch (RemoteException e) {
			log.error("Cannot invoke WebService for plugin get all names" + getLocationAndMessage(e));
			return null;
		}
	}

	@Override
	public String[] getEnabledPluginNames() {
		try {
			return invokeGetEnabledPluginNames();
		} catch (RemoteException e) {
			log.error("Cannot invoke WebService for plugin get enabled names" + getLocationAndMessage(e));
			return null;
		}
	}

	@Override
	public String[] getEnabledPluginNamesForHost(String host) {
		try {
			return invokeGetEnabledPluginNamesForHost(host);
		} catch (RemoteException e) {
			log.error("Cannot invoke WebService for plugin get enabled names" + getLocationAndMessage(e));
			return null;
		}
	}

	@Override
	public void setPluginEnabled(String name, boolean enabled) {
		try {
			invokeSetPluginEnabled(name, enabled);
		} catch (RemoteException e) {
			log.error("Cannot invoke WebService for plugin enable" + getLocationAndMessage(e));
		}
	}

	@Override
	public void setPluginHosts(String name, String[] hosts) {
		try {
			invokeSetPluginHosts(name, hosts);
		} catch (RemoteException e) {
			log.error("Cannot invoke WebService for plugin enable" + getLocationAndMessage(e));
		}
	}

	private String getLocationAndMessage(RemoteException e) {
		//log.log(Level.INFO, "Exception", e);
		return " while connecting to " + LocalUriProvider.getBackendWebServiceUri(ServiceStubFactory.getServiceName(ConfigurationServiceStub.class)) + ": " + e + ": " + ExceptionUtils.getRootCauseMessage(e);
	}

	@Override
	public void setComponent(String ip, String[] params) {
		try {
			invokeSetComponent(ip, params);
		} catch (RemoteException e) {
			log.error("Cannot invoke WebService for setting component" + getLocationAndMessage(e));
		}

	}

	@Override
	public void removeComponent(String ip) {
		try {
			invokeRemoveComponent(ip);
		} catch (RemoteException e) {
			log.error("Cannot invoke WebService for removing component" + getLocationAndMessage(e));
		}

	}

	@Override
	public String[] getComponentsIPList(String type) {
		try {
			return invokeGetComponentsList(type);
		} catch (RemoteException e) {
			log.error("Cannot invoke WebService for getting components ip list" + getLocationAndMessage(e));
			return null;
		}
	}

	@Override
	public void setPluginTemplateConfiguration(String configuration) {
		try {
			invokeSetPluginTemplateConfiguration(configuration);
		} catch (RemoteException e) {
			log.error("Cannot invoke WebService for setting plugin template configuration with: " + configuration, e);
		}
	}
}
