/**
 *
 */
package com.dynatrace.easytravel.webservices;

import java.rmi.RemoteException;

import com.dynatrace.easytravel.business.client.ConfigurationServiceStub;
import com.dynatrace.easytravel.business.webservice.SetPluginTemplateConfigurationDocument;
import com.dynatrace.easytravel.util.ServiceStubProvider;

/**
 * @author tomasz.wieremjewicz
 * @date 29 lis 2017
 *
 */
public class TestPluginTemplateConfigurationService {
	public static void main(String args[]) throws RemoteException {
		ConfigurationServiceStub configurationService = ServiceStubProvider.getServiceStub(ConfigurationServiceStub.class);
		try {
			System.out.println("===Template config test - SET===");
			invokeSetPluginTemplateConfiguration(configurationService);

			ServiceStubProvider.returnServiceStub(configurationService);
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(configurationService);
			throw e;
		}
    }

	private static void invokeSetPluginTemplateConfiguration(ConfigurationServiceStub configurationService) throws RemoteException {
		SetPluginTemplateConfigurationDocument doc = SetPluginTemplateConfigurationDocument.Factory.newInstance();
		doc.setSetPluginTemplateConfiguration(SetPluginTemplateConfigurationDocument.SetPluginTemplateConfiguration.Factory.newInstance());
		doc.getSetPluginTemplateConfiguration().setConfiguration("{\"test\":\"test\"}");
		configurationService.setPluginTemplateConfiguration(doc);
	}
}
