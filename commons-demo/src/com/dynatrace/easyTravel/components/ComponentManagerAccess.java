package com.dynatrace.easytravel.components;

import java.net.URL;

import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import ch.qos.logback.classic.Logger;

public class ComponentManagerAccess extends AbstractComponentAccessManager {
	private static final Logger log = LoggerFactory.make();
	
	public ComponentManagerAccess() {
		super(EasyTravelConfig.read().webServiceBaseDir+"ConfigurationService");	
	}

	@Override
	public void setComponent(String ip, String[] params) {
		String url = "";
		try {
			StringBuilder urlBuf = new StringBuilder(createURL("setComponent").toExternalForm());
			urlBuf.append("?ip=").append(ip);
			for (String param : params) {
				urlBuf.append("&params=").append(param);
			}
			
			url = urlBuf.toString();

			Client client = Client.create();

			WebResource webResource = client.resource(url);

			ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

			String output = response.getEntity(String.class);

			log.debug("Output from Server <" + output + ">\n");

		} catch (Exception e) {
			log.error( "Error setting component. " + url + "\n", e);
		}

	}

	@Override
	public void removeComponent(String ip) {
		URL url = createURL("removeComponent?ip="+ip);
		if(UrlUtils.checkConnect(url.toString()).isOK()){
			readString(url);
		} else {
			log.error(url.toString()+" is not available. Please check log files.");
		}
			
	}

	@Override
	public String[] getComponentsIPList(String type) {
		String readString = readString(createURL("getComponentsIPList?type=" + type));
		String[] addresses = StringUtils.substringsBetween(readString, "<ns:return>", "</ns:return>");
		
		return addresses;
	}

}
