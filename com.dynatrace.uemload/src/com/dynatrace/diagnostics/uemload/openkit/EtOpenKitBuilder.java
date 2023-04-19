package com.dynatrace.diagnostics.uemload.openkit;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.dynatrace.diagnostics.uemload.iot.NullOpenKitImpl;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.openkit.DynatraceOpenKitBuilder;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.protocol.ssl.SSLBlindTrustManager;


public class EtOpenKitBuilder extends DynatraceOpenKitBuilder {

	private static final Logger logger = Logger.getLogger(EtOpenKitBuilder.class.getName());
	private final String endpointURL;
	private String instanceName = "EtOpenKitBuilder";

	public EtOpenKitBuilder(String endpointURL, String applicationID, long deviceID) {
		super(endpointURL, applicationID, deviceID);
		this.endpointURL = endpointURL;
	}

	@Override
	public OpenKit build() {
		try {
			if (!validate()) {
				return new NullOpenKitImpl();
			}
		} catch (IllegalArgumentException e) {
			logger.log(Level.SEVERE, "Cannot initialize OpenKit for " + instanceName + ":", e);
			return new NullOpenKitImpl();
		}

		setSSLTrustManager();
		return super.build();
	}

	private boolean validate() {
		try {
			if (StringUtils.isEmpty(endpointURL)) {
				logger.log(Level.INFO, "The endpointURL passed to " + instanceName + " openkit builder is empty.");
				return false;
			}

			new URL(endpointURL).getHost();
			return true;
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Cannot parse endpointURL: " + endpointURL, e);
		}
	}

	private void setSSLTrustManager() {
		if( EasyTravelConfig.read().openKitTrustAllCertificates) {
			withTrustManager(new SSLBlindTrustManager());
		}
	}

	public EtOpenKitBuilder withInstanceName(String instanceName) {
        if (instanceName != null) {
        	this.instanceName = instanceName;
        }
        return this;
    }
}
