package com.dynatrace.easytravel.spring.pluginagent;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * @author Rafal.Psciuk
 *
 */
public class PluginAgentThread implements Runnable {

	private static final Logger log = Logger.getLogger(PluginAgentThread.class.getName());
	private final String targetURL;
	
	public PluginAgentThread(String pluginAgentHostUrl) {
		this.targetURL = pluginAgentHostUrl;
	}
	
	@Override
	public void run() {
		sendTransmission();

	}
	
	private void sendTransmission() {
	
		try {

			Client client = Client.create();

			WebResource webResource = client.resource(targetURL);

			ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

			if (response.getStatus() != 200) {
				throw new FailedGetException("Failed : HTTP error code : " + response.getStatus());
			}

			String output = response.getEntity(String.class);

			log.fine("Output from Server <" + output + ">\n");

		} 
		catch (Exception e) {

			log.log(Level.WARNING, "Error when writing to plugin. " + targetURL + "\n", e);

		}
	}

	public class FailedGetException extends RuntimeException
	{
		private static final long serialVersionUID = 4461128517145133063L;

		private FailedGetException(String msg) {
			super(msg);
		};
	}
}
