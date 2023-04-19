package com.dynatrace.easytravel.util;

import java.io.IOException;
import java.net.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.google.common.base.Strings;
import com.google.common.net.InetAddresses;

/**
 * Central class for getting local URIs
 *
 * @author philipp.grasboeck
 */
public class LocalUriProvider {

	private static final Logger log = Logger.getLogger(LocalUriProvider.class.getName());

	private static final String URI_TEMPLATE = "http://{0}:{1,number,#}{2}";
	private static final String URI_TEMPLATE_WITHOUT_PORT = "http://{0}{1}";

	// synchronized cache for holding translated FQDN names of hosts
	private static Map<String, String> hostTable = new Hashtable<String, String>();

	static final String LOCALHOST = "localhost";
	static final String HOST127 = "127.0.0.1";

	/**
	 * Get address of the loopback adapter.
	 *
	 * @return the loopback adapter or <code>null</code> if an error occur
	 * @author martin.wurzinger
	 */
	public static InetAddress getLoopbackAdapter() {
		try {
			return InetAddress.getByName(null);
		} catch (UnknownHostException e) {
			return null;
		}
	}

	public static String getLocalhost() {
		InetAddress loopbackAdapter = getLoopbackAdapter();
		if (loopbackAdapter == null) {
			return BaseConstants.LOCALHOST;
		}
		return loopbackAdapter.getHostName();
	}

	/**
	 * Get URI the customer frontend is operating on.
	 *
	 * @param port
	 * @param contextRoot
	 * @return The resulting URI, this always has a trailing slash.
	 *
	 * @author martin.wurzinger
	 */
	public static String getLocalUri(int port, String contextRoot) {
		return getUri(getLocalhost(), port, contextRoot);
	}

	public static String getLocalUriDNS(int port, String contextRoot) {
		return getUriDNS(getLocalhost(), port, contextRoot);
	}

	/**
	 * Get the FQDN for a given host string if it is local host or an IP address.
	 * @param host
	 *
	 * In the worst case, if we fail, we return host as given in input the parameter.
	 * If passed host is not "localhost" or "127.0.0.1", we have to use the host string
	 * to get the FQDN based on it, though it is not always guaranteed to work.
	 *
	 */
	public static String getFQDN(String host) {

		log.fine("getFQDN(): passed host = <" + host + ">");
		
		// If this is NOT ruxit
		// we do nothing i.e. return what we got.
		if (!DtVersionDetector.isAPM()) {
			log.fine("getFQDN(): FQDN substitution not applicable for classic mode: returning.");
			return host;
		}

		// If this functionality has been disabled in the configuration,
		// we do nothing i.e. return what we got.
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		if (EASYTRAVEL_CONFIG.disableFQDN) {
			log.fine("getFQDN(): FQDN substitution disabled: returning.");
			return host;
		}

/*
 * This was just an example of executing a ping process to get at the FQDN in the response.
 * It might still be useful to leave it here in case we need it.
 *
        // String ip = "127.0.0.1";
        String ip = "localhost";
        String pingResult = "";

        String pingCmd = "ping " + ip;
        try {
            Runtime r = Runtime.getRuntime();
            Process p = r.exec(pingCmd);

            BufferedReader in = new BufferedReader(new
            InputStreamReader(p.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
                pingResult += inputLine;
            }
            in.close();

        } catch (IOException e) {
            System.out.println(e);
        }
 */

		if (StringUtils.isEmpty(host)) {
			log.warning("getFQDN(): Unable to resolve FQDN: null host.");
			return host;
		}

		// TODO: Investigate if InetAddress will work for IPv6.
		// If not, we might have to use e.g. Guava or our own method.
		if (!host.equals(LOCALHOST) && !host.equals(HOST127) && !InetAddresses.isInetAddress(host)) {
			log.fine("getFQDN(): Assuming no need to resolve host <" + host + "> to FQDN.");
			return host;
		}

		//========================================
		// Check in our cache if we have already got this host in our table.
		// If so, no need to call the DNS.
		//========================================

		String mappedHost = hostTable.get(host);
		if (mappedHost != null) {
			log.fine("getFQDN(): Got FQDN from prviously mapped hosts as <" + mappedHost + ">");
			return mappedHost;
		}

		//========================================
		// At this point we know we want to convert host to an FQDN
		// (or at least to its first component).
		// For non-local hosts, we take our chances and call
		// getCanonicalHostName() based on the host string, but for local host
		// we know that getCanonicalHostName() will not work satisfactorily
		// on strings such as "localhost" or "127.0.0.1", as it will
		// simply return "127.0.0.1". Therefore we call getLocalHost()
		// first, to give us the first component of the FQDN.
		//========================================

		boolean isLocalHost = host.equals(LOCALHOST) || host.equals(HOST127);
		try {
			InetAddress addr = isLocalHost? InetAddress.getLocalHost() : InetAddress.getByName(host);
			String fqdn =  addr.getCanonicalHostName();

			// This is probably never going to happen (because
			// we would get an exception first), but test for it just in case
			if (StringUtils.isEmpty(fqdn)) {
				log.warning("getFQDN(): Got empty cannonical host name for host <" + host + ">");
				// worst case - we set ourselves up to return what we were given
				fqdn = host;
			} else {
				log.fine("Got canonical hostname as <" + fqdn + "> for host <" + host + ">");
			}
			
			// If we have still failed to get the FQDN, our last-ditch attempt at rescuing
			// the situation is to simply pass on the host name, if available, as this
			// should be enough for ruxit.
			if (fqdn.equals(LOCALHOST) || fqdn.equals(HOST127)) {
				
		        String hostname = addr.getHostName();
		        if (StringUtils.isEmpty(hostname)) {
		        	log.warning("getFQDN(): failed to get host name.");
		        } else if (hostname.equals(LOCALHOST) || hostname.equals(HOST127)) {
		        	log.warning("getFQDN(): host name is still <" + hostname + ">.");
		        } else {
		        	// success
		        	log.fine("getFQDN(): will use hostname obtained from getLocalHost().getHostName() <" + hostname + ">");
		        	fqdn = hostname;
		        }
			}

			//========================================
			// Place successful conversions in a cache so we do not
			// need to involve the DNS in the future for this host name.
			//========================================

			hostTable.put(host, fqdn);
			return fqdn;

		} catch (UnknownHostException e2) {
			log.warning("getFQDN(): Failed to get host name for host + <" + host + ">");
			// worst case we return what we were given
			return host;
		}
	}

	/**
	 * Get URI the customer frontend is operating on.
	 *
	 * @param port
	 * @param contextRoot
	 * @return The resulting URI, this never has a trailing slash.
	 *
	 * @author anita.engleder
	 */
	public static String getLocalUriWithoutTrailingSlash(int port, String contextRoot) {
		String uri = getUri(getLocalhost(), port, contextRoot);
		if(uri.endsWith("/")){
			return uri.substring(0, uri.length()-1);
		}else{
			return uri;
		}

	}

	/**
	 * Get URI the customer front-end is operating on.
	 *
	 * @param host
	 * @param port
	 * @param contextRoot
	 * @return The resulting URI, this always has a trailing slash.
	 *
	 * @author martin.wurzinger
	 */
	public static String getUri(String host, int port, String contextRoot) {

		if(port == 80) {
			return getUri(URI_TEMPLATE_WITHOUT_PORT, host, contextRoot);
		}
		return getUri(URI_TEMPLATE, host, port, contextRoot);
	}

	private static String getUri(String pattern, Object... arguments) {
		Object[] trimmedArgs = new Object[arguments.length];
		for (int i = 0; i < trimmedArgs.length; i++) {
			if (arguments[i] instanceof String) {
				trimmedArgs[i] = ((String) arguments[i]).trim();
			} else {
				trimmedArgs[i] = arguments[i];
			}
		}

		return TextUtils.appendTrailingSlash(TextUtils.merge(pattern, trimmedArgs));
	}

    /**
     * Determine public URL's for the frontent services
     * @param urlType
     * @param useDNS
     * @return
     * @author cwpl-rorzecho
     */
    public static String getURL(BaseConstants.UrlType urlType, boolean useDNS) {
        final EasyTravelConfig config = EasyTravelConfig.read();
        switch (urlType) {
            case APACHE_JAVA_FRONTEND:
                return isApacheFrontendPublicUrl() ? config.apacheFrontendPublicUrl : getApacheFrontendPublicUrl(useDNS);
            case NGINX_JAVA_FRONTEND:
                return isNginxFrontendPublicUrl() ? config.nginxFrontendPublicUrl : getNginxFrontendPublicUrl(useDNS);
            case APACHE_B2B_FRONTEND:
                return isApacheB2BFrontendPublicUrl() ? config.apacheB2BFrontendPublicUrl : getApacheB2BFrontendPublicUrl(useDNS);
            case NGINX_B2B_FRONTEND:
                return isNginxB2BFrontendPublicUrl() ? config.nginxB2BFrontendPublicUrl : getNginxB2BFrontendPublicUrl(useDNS);
            case APACHE_ANGULAR_FRONTEND:
                return isApacheAngularFrontendPublicUrl() ? config.angularFrontendPublicUrl : getApacheAngularFrontendPublicUrl(useDNS);
            case NGINX_ANGULAR_FRONTEND:
                return isNginxAngularFrontendPublicUrl() ? config.nginxAngularFrontendPublicUrl : getNginxAngularFrontendPublicUrl(useDNS);                
            default:
                throw new IllegalArgumentException(TextUtils.merge("The UrlType {0} is not valid", urlType));
        }
    }

    /**
     * Check if apacheFrontendPublicUrl is specified in config file
     * @return
     */
    public static boolean isApacheFrontendPublicUrl() {
        final EasyTravelConfig config = EasyTravelConfig.read();
        return !Strings.isNullOrEmpty(config.apacheFrontendPublicUrl);
    }

    /**
     * Check if nginxFrontendPublicUrl is specified in config file
     * @return
     */
    public static boolean isNginxFrontendPublicUrl() {
        final EasyTravelConfig config = EasyTravelConfig.read();
        return !Strings.isNullOrEmpty(config.nginxFrontendPublicUrl);
    }
    
    public static boolean isNginxAngularFrontendPublicUrl() {
        final EasyTravelConfig config = EasyTravelConfig.read();
        return !Strings.isNullOrEmpty(config.nginxAngularFrontendPublicUrl);    	
    }

    /**
     * Check if apacheB2BFrontendPublicUrl is specified in config file
     * @return
     */
    public static boolean isApacheB2BFrontendPublicUrl() {
        final EasyTravelConfig config = EasyTravelConfig.read();
        return !Strings.isNullOrEmpty(config.apacheB2BFrontendPublicUrl);
    }

    /**
     * Check if nginxB2BFrontendPublicUrl is specified in config file
     * @return
     */
    public static boolean isNginxB2BFrontendPublicUrl() {
        final EasyTravelConfig config = EasyTravelConfig.read();
        return !Strings.isNullOrEmpty(config.nginxB2BFrontendPublicUrl);
    }
    
    public static boolean isApacheAngularFrontendPublicUrl() {
        final EasyTravelConfig config = EasyTravelConfig.read();
        return !Strings.isNullOrEmpty(config.angularFrontendPublicUrl);
    }
    

    /**
     * @return apacheFrontendPublicUrl
     */
    public static String getApacheFrontendPublicUrl() {
        return getURL(BaseConstants.UrlType.APACHE_JAVA_FRONTEND, false);
    }

    /**
     * @return nginxFrontendPublicUrl
     */
    public static String getNginxFrontendPublicUrl() {
        return getURL(BaseConstants.UrlType.NGINX_JAVA_FRONTEND, false);
    }
    
    public static String getNginxAngularFrontendPublicUrl() {
    	return getURL(BaseConstants.UrlType.NGINX_ANGULAR_FRONTEND, false);
    }

    /**
     * nginxB2BFrontendPublicUrl
     * @return
     */
    public static String getNginxB2BFrontendPublicUrl() {
        return getURL(BaseConstants.UrlType.NGINX_B2B_FRONTEND, false);
    }

    /**
     * apacheB2BFrontendPublicUrl
     * @return
     */
    public static String getApacheB2BFrontendPublicUrl() {
        return getURL(BaseConstants.UrlType.APACHE_B2B_FRONTEND, false);
    }

    /**
     * @param useDNS
     * @return
     */
    public static String getApacheFrontendPublicUrl(boolean useDNS) {
        final EasyTravelConfig config = EasyTravelConfig.read();
        if (useDNS) {
            return getUriDNS(config.apacheWebServerHost, config.apacheWebServerPort, config.frontendContextRoot);
        }
        return getUri(config.apacheWebServerHost, config.apacheWebServerPort, config.frontendContextRoot);
    }

    /**
     * @param useDNS
     * @return nginxFrontendPublicUrl with FQDM
     */
    public static String getNginxFrontendPublicUrl(boolean useDNS) {
        final EasyTravelConfig config = EasyTravelConfig.read();
        if (useDNS) {
            return getUriDNS(config.nginxWebServerHost, config.nginxWebServerPort, config.frontendContextRoot);
        }
        return getUri(config.nginxWebServerHost, config.nginxWebServerPort, config.frontendContextRoot);
    }
    
    public static String getNginxAngularFrontendPublicUrl(boolean useDNS) {
        final EasyTravelConfig config = EasyTravelConfig.read();
        if (useDNS) {
            return getUriDNS(config.nginxWebServerHost, config.nginxWebServerAngularPort, config.angularFrontendContextRoot);
        }
        return getUri(config.nginxWebServerHost, config.nginxWebServerAngularPort, config.angularFrontendContextRoot);
    }

    /**
     * @param useDNS
     * @return
     */
    public static String getApacheB2BFrontendPublicUrl(boolean useDNS) {
        final EasyTravelConfig config = EasyTravelConfig.read();
        if (useDNS) {
            return getUriDNS(config.apacheWebServerB2bHost, config.apacheWebServerB2bPort, "/");
        }
        return getUri(config.apacheWebServerB2bHost, config.apacheWebServerB2bPort, "/");
    }
    
    
    public static String getApacheAngularFrontendPublicUrl(boolean useDNS) {

        final EasyTravelConfig config = EasyTravelConfig.read();
        if (useDNS) {
            return getUriDNS(config.apacheWebServerHost, config.angularFrontendApachePort, config.angularFrontendContextRoot);
        }
        return getUri(config.apacheWebServerHost, config.angularFrontendApachePort, config.angularFrontendContextRoot);
    }

    /**
     * @param useDNS
     * @return
     */
    public static String getNginxB2BFrontendPublicUrl(boolean useDNS) {
        final EasyTravelConfig config = EasyTravelConfig.read();
        if (useDNS) {
            return getUriDNS(config.nginxWebServerB2bHost, config.nginxWebServerB2bPort, "/");
        }
        return getUri(config.nginxWebServerB2bHost, config.nginxWebServerB2bPort, "/");
    }

    /**
	 * Get URI the customer front-end is operating on.
	 * As getURI, but returns a fully qualified domain name.
	 *
	 * @param host
	 * @param port
	 * @param contextRoot
	 * @return The resulting URI, this always has a trailing slash.
	 *
	 * Translate new local host address to DNS-based.
	 */
	public static String getUriDNS(String host, int port, String contextRoot) {

		String newHost = getFQDN(host);
		return getUri(newHost, port, contextRoot);
	}

	/**
	 * Returns an URI which can be used to access the specified Java Backend Web Service.
	 *
	 * @param serviceName
	 * @return The resulting URI, this always has a trailing slash.
	 *
	 * @author dominik.stadler
	 */
	public static String getBackendWebServiceUri(String serviceName) {
		final EasyTravelConfig CONFIG = EasyTravelConfig.read();
		return TextUtils.appendTrailingSlash(CONFIG.webServiceBaseDir + serviceName);
	}

    /**
     * Create BusinessBackend URI for specified servide method and values
     *
     * @param serviceName
     * @param serviceMethod
     * @param value
     * @return The resulting URI with proper character encoding.
     */
    public static String getBackendWebServiceUri(String serviceName, String serviceMethod, String...values) {
        StringBuilder uriBuilder = new StringBuilder();
        uriBuilder.append(getBackendWebServiceUri(serviceName));
        uriBuilder.append(TextUtils.merge(serviceMethod, (Object[])values));
        return encodeURI(uriBuilder);
    }

	/**
	 * Returns an URI which can be used to access the specified third party Web Service
	 * defined by 'config.thirdPartyWebserviceUri', if the setting is not configured
	 * it returns {@link #getBackendWebServiceUri(String)}
	 *
	 * @param serviceName
	 *
	 * @return The resulting URI, this always has a trailing slash.
	 * @author stefan.moschinski
	 */
	public static String getThirdPartyWebServiceUri(String serviceName) {
		String thirdPartyWebserviceUrl = EasyTravelConfig.read().thirdPartyWebserviceUri;
		if (StringUtils.trimToNull(thirdPartyWebserviceUrl) == null) {
			if (log.isLoggable(Level.FINE)) log.fine("The 'config.thirdPartyWebserviceUri' is not set");
			return getBackendWebServiceUri(serviceName);
		}
		String thirdPartyWebserviceUrlWithSlash = TextUtils.appendTrailingSlash(thirdPartyWebserviceUrl);
		return TextUtils.appendTrailingSlash(thirdPartyWebserviceUrlWithSlash + serviceName);
	}

    private static String encodeURI(StringBuilder uriBuilder) {
        return encodeURI(uriBuilder.toString());
    }

    public static String encodeURI(String sUri) {
        return createURL(sUri).toString();
    }

    /**
     * Create URL with proper char encoding
     * @param sUrl
     * @return
     * @throws IOException
     */
    public static URL createURL(String sUrl)  {
        URL url = null;
        try {
            url = new URL(sUrl);
            return createURI(url).toURL();
        } catch (MalformedURLException e) {
            log.log(Level.SEVERE, TextUtils.merge("Cannot create URL for: {0}", sUrl), e);
        }
        return url;
    }

    /**
     * Create URI for proper encoding assurance
     * @param url
     * @return
     */
    // SECRET
    private static URI createURI(URL url)  {
        URI uri = null;
        try {
            uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), null);
        } catch (URISyntaxException e) {
            log.log(Level.SEVERE, TextUtils.merge("Cannot create URI for: {0}", url), e);
        }
        return uri;
    }
}
