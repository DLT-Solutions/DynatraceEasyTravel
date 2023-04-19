package com.dynatrace.easytravel.webservices;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import com.dynatrace.easytravel.business.client.ConfigurationServiceStub;
import com.dynatrace.easytravel.business.webservice.SetPluginEnabledDocument;
import com.dynatrace.easytravel.util.ServiceStubProvider;

public class TestEnablePlugin {

    private static Logger log = Logger.getLogger(TestEnablePlugin.class.getName());

	static Date parseDate(String text)
	{
		try {
			return new SimpleDateFormat("dd.MM.yyyy").parse(text);
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}


	public static void main(String args[]) throws RemoteException {
		ConfigurationServiceStub configurationService = ServiceStubProvider.getServiceStub(ConfigurationServiceStub.class);
		String name = (args.length > 0) ? args[0] : "TestPlugin";
		boolean enabled = "true".equals((args.length > 1) ? args[1] : "true");
		log.info("set plugin '" + name + "' enabled state to " + enabled);

		invokeSetPluginEnabled(configurationService, name, enabled);
    }

	private static void invokeSetPluginEnabled(ConfigurationServiceStub stub, String name, boolean enabled) throws RemoteException
	{
		SetPluginEnabledDocument doc = SetPluginEnabledDocument.Factory.newInstance();
		doc.setSetPluginEnabled(SetPluginEnabledDocument.SetPluginEnabled.Factory.newInstance());
		doc.getSetPluginEnabled().setName(name);
		doc.getSetPluginEnabled().setEnabled(enabled);

		stub.setPluginEnabled(doc);
	}
}
