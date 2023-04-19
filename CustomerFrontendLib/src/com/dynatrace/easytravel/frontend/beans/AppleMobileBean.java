package com.dynatrace.easytravel.frontend.beans;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;

@ManagedBean
@RequestScoped
public class AppleMobileBean {

	private static final Logger log = LoggerFactory.make();

	public String getSslHostAddress() {
		EasyTravelConfig config = EasyTravelConfig.read();
		String host = config.apacheWebServerSslHost;

		if (host == null || host.trim().equals(BaseConstants.EMPTY_STRING)) {
			host = getLocalHostAddress();
		}

		int port = config.apacheWebServerSslPort;
		return host + ":" + port;
	}

	public String getUrlForPropertiesListFile() {
		EasyTravelConfig config = EasyTravelConfig.read();
		String baseUrl;

		// determine if ssl is enabled
		if (config.apacheWebServerSslPort > 0) {
			baseUrl = "https://" +  getSslHostAddress();
		} else {
			baseUrl = "https://" + getLocalHostAddress();
		}
		return baseUrl + "/apps/easyTravel.plist";
	}

	// get the public IP of CustomerFrontend machine.
	private static String getLocalHostAddress() {
		try {
		    InetAddress addr = InetAddress.getLocalHost();	// NOSONAR - we don't care too much about multi-home machines here
		    return addr.getHostAddress();
		} catch (UnknownHostException e) {
			log.warn("Cannot get localhost", e);
			return "localhost";
		}
	}
}
