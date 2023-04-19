package com.dynatrace.diagnostics.uemload;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.util.TextUtils;

public abstract class URLUtil {

	public static final String UNKNOWN_HOST_ADDRESS = "{UNKNOWN_HOST_ADDRESS}";

	private static final Logger logger = Logger.getLogger(URLUtil.class.getName());

	public static String convertHostToIP(String hostName) {
		return getHostIP(hostName, false);
	}

	public static String getLocalHostIP() {
		return getHostIP(null, true);
	}

	private static String getHostIP(String hostName, boolean localhost) {
		try {
			InetAddress inetAddress = localhost ? InetAddress.getLocalHost() : InetAddress.getByName(hostName);	// NOSONAR - we don't care too much about multi-home machines here
			if (inetAddress != null) {
				return inetAddress.getHostAddress();
			}
		} catch (UnknownHostException e) {
			logger.log(Level.WARNING, "Could not find host " + hostName, e);
		}
		return UNKNOWN_HOST_ADDRESS;
	}

	public static String getHostFromURL(String url) {
		try {
			return new URL(url).getHost();
		} catch (MalformedURLException e) {
			logger.log(Level.WARNING, TextUtils.merge("Could not extract host from ''{0}''. The URL is malformed.", url), e);
		}
		return null;
	}

	public static String getPathFromURL(String url) {
		try {
			return new URL(url).getPath();
		} catch (MalformedURLException e) {
			logger.log(Level.WARNING, TextUtils.merge("Could not extract path from ''{0}''. The URL is malformed.", url), e);
		}
		return null;
	}

	/**
	 * guess mime type from url
	 *
	 * @param url
	 * @return mime type or null
	 */
	public static String guessMimeType(String url) {
		if (url == null) {
			return null;
		}

		if (url.endsWith(BaseConstants.EXTENSION_JS)) {
			return "javascript";
		} else if (url.endsWith(BaseConstants.EXTENSION_PNG)
				|| url.endsWith(BaseConstants.EXTENSION_JPG)
				|| url.endsWith(BaseConstants.EXTENSION_JPEG)) {
			return "image";
		}
		return null;
	}

}
