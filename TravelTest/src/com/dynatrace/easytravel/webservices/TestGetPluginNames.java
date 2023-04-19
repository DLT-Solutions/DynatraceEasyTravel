package com.dynatrace.easytravel.webservices;

import java.rmi.RemoteException;
import java.util.Arrays;

import com.dynatrace.easytravel.business.client.ConfigurationServiceStub;
import com.dynatrace.easytravel.business.webservice.GetAllPluginNamesDocument;
import com.dynatrace.easytravel.business.webservice.GetAllPluginNamesResponseDocument;
import com.dynatrace.easytravel.business.webservice.GetAllPluginsDocument;
import com.dynatrace.easytravel.business.webservice.GetAllPluginsResponseDocument;
import com.dynatrace.easytravel.business.webservice.GetEnabledPluginNamesDocument;
import com.dynatrace.easytravel.business.webservice.GetEnabledPluginNamesResponseDocument;
import com.dynatrace.easytravel.business.webservice.GetEnabledPluginsDocument;
import com.dynatrace.easytravel.business.webservice.GetEnabledPluginsResponseDocument;
import com.dynatrace.easytravel.spring.Plugin;
import com.dynatrace.easytravel.spring.PluginInfoList;
import com.dynatrace.easytravel.util.ServiceStubProvider;

public class TestGetPluginNames {

	public static void main(String args[]) throws RemoteException {
		ConfigurationServiceStub configurationService = ServiceStubProvider.getServiceStub(ConfigurationServiceStub.class);
		try {
			System.out.println("===All plugins===");
			dumpPlugins(invokeGetAllPlugins(configurationService));
			System.out.println("===Enabled plugins===");
			dumpPlugins(invokeGetEnabledPlugins(configurationService));
			System.out.println("===All plugins names===");
			dumpPlugins(invokeGetAllPluginNames(configurationService));
			System.out.println("===Enabled plugins names===");
			dumpPlugins(invokeGetEnabledPluginNames(configurationService));
			ServiceStubProvider.returnServiceStub(configurationService);
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(configurationService);
			throw e;
		}
    }

	private static void dumpPlugins(String[] pluginData) {
		System.out.println("Array content: " + Arrays.toString(pluginData));
		PluginInfoList list = new PluginInfoList(pluginData);
		System.out.println("Plugin list: " + list);
		for (Plugin plugin : list) {
			System.out.println("Plugin entry: " + plugin);
		}
	}

	private static String[] invokeGetEnabledPlugins(ConfigurationServiceStub configurationService) throws RemoteException {
		GetEnabledPluginsDocument document = GetEnabledPluginsDocument.Factory.newInstance();
		document.setGetEnabledPlugins(GetEnabledPluginsDocument.GetEnabledPlugins.Factory.newInstance());
		GetEnabledPluginsResponseDocument res = configurationService.getEnabledPlugins(document);
		return res.getGetEnabledPluginsResponse().getReturnArray();
	}

	private static String[] invokeGetEnabledPluginNames(ConfigurationServiceStub configurationService) throws RemoteException {
		GetEnabledPluginNamesDocument document = GetEnabledPluginNamesDocument.Factory.newInstance();
		document.setGetEnabledPluginNames(GetEnabledPluginNamesDocument.GetEnabledPluginNames.Factory.newInstance());
		GetEnabledPluginNamesResponseDocument res = configurationService.getEnabledPluginNames(document);
		return res.getGetEnabledPluginNamesResponse().getReturnArray();
	}

	private static String[] invokeGetAllPlugins(ConfigurationServiceStub configurationService) throws RemoteException {
		GetAllPluginsDocument document = GetAllPluginsDocument.Factory.newInstance();
		document.setGetAllPlugins(GetAllPluginsDocument.GetAllPlugins.Factory.newInstance());
		GetAllPluginsResponseDocument res = configurationService.getAllPlugins(document);
		return res.getGetAllPluginsResponse().getReturnArray();
	}

	private static String[] invokeGetAllPluginNames(ConfigurationServiceStub configurationService) throws RemoteException {
		GetAllPluginNamesDocument document = GetAllPluginNamesDocument.Factory.newInstance();
		document.setGetAllPluginNames(GetAllPluginNamesDocument.GetAllPluginNames.Factory.newInstance());
		GetAllPluginNamesResponseDocument res = configurationService.getAllPluginNames(document);
		return res.getGetAllPluginNamesResponse().getReturnArray();
	}
}
