/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: EasyTravelHostManager.java
 * @date: 25.01.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload.scenarios;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.util.LocalUriProvider;


/**
 * Class to manage available hosts for easyTravel scenarios.
 * @author peter.lang
 */
public class EasyTravelHostManager {

	private final CopyOnWriteArrayList<String> customerFrontendHosts;
	private final CopyOnWriteArrayList<String> b2bFrontendHosts;
	private final CopyOnWriteArrayList<String> backendHosts;
	private final CopyOnWriteArrayList<String> angularFrontendHosts;
	private final CopyOnWriteArrayList<String> onlineBoutiqueHosts;

	public EasyTravelHostManager() {
		customerFrontendHosts = new CopyOnWriteArrayList<String>();
		b2bFrontendHosts = new CopyOnWriteArrayList<String>();
		backendHosts = new CopyOnWriteArrayList<String>();
		angularFrontendHosts = new CopyOnWriteArrayList<String>();
		onlineBoutiqueHosts = new CopyOnWriteArrayList<String>();
	}

	public void setCustomerFrontendHost(String customerFrontendHost) {
	    customerFrontendHosts.clear();
	    customerFrontendHosts.addIfAbsent(customerFrontendHost);
	}

	public void addCustomerFrontendHost(String customerFrontendHost) {
	    customerFrontendHosts.addIfAbsent(customerFrontendHost);
	}

	public boolean removeCustomerFrontendHost(String customerFrontendHost) {
	    return customerFrontendHosts.remove(customerFrontendHost);
    }

	public void setB2BFrontendHost(String b2bFrontendHost) {
        b2bFrontendHosts.clear();
        b2bFrontendHosts.addIfAbsent(b2bFrontendHost);
    }

	public void addB2BFrontendHost(String b2bFrontendHost) {
        b2bFrontendHosts.addIfAbsent(b2bFrontendHost);
    }

    public boolean removeB2BFrontendHost(String b2bFrontendHost) {
        return b2bFrontendHosts.remove(b2bFrontendHost);
    }

    public void setAngularFrontendHost(String angularFrontendHost) {
	    angularFrontendHosts.clear();
	    angularFrontendHosts.addIfAbsent(angularFrontendHost);
	}

	public void addAngularFrontendHost(String angularFrontendHost) {
		angularFrontendHosts.addIfAbsent(angularFrontendHost);
	}

	public boolean removeAngularFrontendHost(String angularFrontendHost) {
	    return angularFrontendHosts.remove(angularFrontendHost);
    }

	public void addOnlineBoutiqueHost(String onlineBoutiqueHost) {
		onlineBoutiqueHosts.addIfAbsent(onlineBoutiqueHost);
	}
	
	public boolean removeOnlineBoutiqueHost(String onlineBoutiqueHost) {
	    return onlineBoutiqueHosts.remove(onlineBoutiqueHost);
    }
	
    public boolean hasCustomerFrontendHost() {
    	return !customerFrontendHosts.isEmpty();
    }

    public boolean hasB2BFrontendHost() {
    	return !b2bFrontendHosts.isEmpty();
    }

    public boolean hasAngularFrontendHost() {
    	return !angularFrontendHosts.isEmpty();
    }
    
    public boolean hasOnlineBoutiqueHost() {
    	return !onlineBoutiqueHosts.isEmpty();
    }


    public List<String> getCustomerFrontendHosts() {
        return Collections.unmodifiableList(customerFrontendHosts);
    }

    public List<String> getB2bFrontendHosts() {
        return Collections.unmodifiableList(b2bFrontendHosts);
    }

    public List<String> getAngularFrontendHosts() {
    	return Collections.unmodifiableList(angularFrontendHosts);
    }
    
    public List<String> getOnlineBoutiqueHosts() {
    	return Collections.unmodifiableList(onlineBoutiqueHosts);
    }

    public int getCustomerFrontendHostCount() {
    	return customerFrontendHosts.size();
    }

    public int getB2bFrontendHostCount() {
    	return b2bFrontendHosts.size();
    }

    public int getAngularFrontendHostCount() {
    	return angularFrontendHosts.size();
    }

    /**
     * Helper method to check if BaseLoad Simulator generates traffic to the Apache Webserver
     * @param customerFrontendHost
     * @return
     */
    public static boolean isApacheWebserver(String customerFrontendHost) {
        return customerFrontendHost.equalsIgnoreCase(LocalUriProvider.getURL(BaseConstants.UrlType.APACHE_JAVA_FRONTEND, true));
    }

	public void addBackendHost(String host) {
		backendHosts.add(host);
	}

	public void removeBackendHost(String host) {
		backendHosts.remove(host);
	}

	public boolean hasBackendHost() {
		return ! backendHosts.isEmpty();
	}

	public String getBackendHost() {
		// TODO this is awkward!!
		return hasBackendHost() ? backendHosts.get(0) : null;
	}

	public String getAllHostsAsString() {
		if (customerFrontendHosts.isEmpty() && b2bFrontendHosts.isEmpty() && backendHosts.isEmpty() && angularFrontendHosts.isEmpty()) {
			return StringUtils.EMPTY;
		}

		return String.format("%s%s%s%s",
			customerFrontendHosts.isEmpty() ? "" : String.format("customerFrontendHosts: %s ", String.join(",", customerFrontendHosts)),
			b2bFrontendHosts.isEmpty() ? "" : String.format("b2bFrontendHosts: %s ", String.join(",", b2bFrontendHosts)),
			backendHosts.isEmpty() ? "" : String.format("backendHosts: %s ", String.join(",", backendHosts)),
			angularFrontendHosts.isEmpty() ? "" : String.format("angularFrontendHosts: %s ", String.join(",", angularFrontendHosts)));
	}
}
