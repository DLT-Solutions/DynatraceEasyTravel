package com.dynatrace.easytravel.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.misc.RESTConstants;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


public class DtVersionDetector {
	private static final Logger LOGGER = LoggerFactory.make();

	private static final Pattern INSTALL_DIR_REGEX = Pattern.compile("dynaTrace\\s*([\\.0-9]+)");
	private static final Pattern XML_RESULT_REGEX = Pattern.compile("<result value=\"(.*)\"");

	// ..."buildVersion":"0.4.0.20130801-063958"...
	private static final Pattern APM_NG_VERSION_REGEX = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)-(\\d+)");
	
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.3205.0 Safari/537.36";

	private static final String EMPTY_VALUE = "NO_CONTENT_FOUND";
	private static final Cache<String, String> CACHED = CacheBuilder.newBuilder()
		    .concurrencyLevel(4)
		    .maximumSize(10)
		    // keep entries for 5 minutes, then check again
		    .expireAfterWrite(5, TimeUnit.MINUTES)
		    .build();

	private static InstallationType installationType = null;

	public static InstallationType getInstallationType() {
		synchronized (DtVersionDetector.class) {
			if (installationType == null) {
				determineDTVersion(null);
			}
			return installationType;
		}
	}

	public static boolean isClassic() {
		return getInstallationType() == InstallationType.Classic;
	}

	public static boolean isAPM() {
		return getInstallationType() == InstallationType.APM;
	}

	@TestOnly
	public static void enforceInstallationType(InstallationType aInstallationType) {
		synchronized (DtVersionDetector.class) {
			installationType = aInstallationType;
		}
	}

	/**
	 * Determine the dynaTrace version by trying to get it from the dtServerWebURL
	 * REST management interface.
	 *
	 * @param fallbackAgentPath  if provided, and the REST call did not succeed, that agent config path
	 * that will be used to derive the version as a fallback.
	 *
	 * @return
	 * @author philipp.grasboeck
	 */
	public static String determineDTVersion(String fallbackAgentPath) {
	    String version = determineAPMVersion();
	    if (version == null) {
	    	version = determineDTClassicVersion(fallbackAgentPath);
			synchronized (DtVersionDetector.class) {
				if (version != null) {
					installationType = InstallationType.Classic;
				} else {
					installationType = EasyTravelConfig.read().apmServerDefault;
				}
			}
	    }
	    return version;
	}

	private static String determineAPMVersion() {
	    final EasyTravelConfig CONFIG = EasyTravelConfig.read();
	    if ((CONFIG.apmServerHost == null) || CONFIG.apmServerHost.isEmpty()) {
	    	return null;
	    }
	    if ((CONFIG.apmServerWebPort == null) || CONFIG.apmServerWebPort.isEmpty()) {
	    	return null;
	    }

	    String sUrl = DynatraceUrlUtils.getDtVersionDetectorUrl();	    

		// cache the information to avoid reading this multiple times!
		String string = getCached(sUrl);
		if(string != null) {
			if(EMPTY_VALUE.equals(string)) {
				return null;
			}

			installationType = InstallationType.APM;
			return string;
		}

		String version = queryAPMVersion(sUrl);

		// try with http if https did not respond
		if(version == null) {
			version = queryAPMVersion("http://" + CONFIG.apmServerHost + ":" + CONFIG.apmServerWebPort + "/");
		}

		putCache(sUrl, version);
		return version;
	}

	private static String queryAPMVersion(String sUrl) {
		String version = null;
		try {
		    String data = new DtSSLHelper(10000, USER_AGENT).getData(sUrl + "?browser_ok");
	    	if (data != null) {
		    	Matcher matcher = APM_NG_VERSION_REGEX.matcher(data);
    			if (matcher.find()) {
    				version = matcher.group(1) + "." + matcher.group(2) + "." +
							matcher.group(3) + "." + matcher.group(4) + "-" + matcher.group(5);
    			} else if (data.contains("ruxit")) {
    				LOGGER.info("Applying workaround to detect ruxit for URL " + sUrl + " for data: " + StringUtils.abbreviate(data.replace("\n", "").replace("\r", ""), 100));
    			} else if (data.contains("dynaTrace")) {
    				// return without log-output as we will likely be able to detect the dynaTrace Version
    				return null;
    			} else {
    				LOGGER.info("Could not get ruxit version from data '" + StringUtils.abbreviate(data.replace("\n", "").replace("\r", ""), 100) + "' retrieved from URL '" + sUrl + "'");

    				// not detected => probably dynaTrace
    				return null;
    			}

	    		synchronized (DtVersionDetector.class) {
	    			installationType = InstallationType.APM;
	    		}
	    	}
		} catch (IOException e) {
			LOGGER.info("Could not get ruxit version from URL: " + sUrl + ", error=" + e.getMessage());
		}
		return version;
	}

	private static String determineDTClassicVersion(String fallbackAgentPath) {
	    final EasyTravelConfig CONFIG = EasyTravelConfig.read();
	    String sUrl = TextUtils.appendTrailingSlash(CONFIG.dtServerWebURL) + RESTConstants.MANAGEMENT_VERSION;
		// cache the information to avoid reading this multiple times!
		String string = getCached(sUrl);
		if(string != null) {
			return EMPTY_VALUE.equals(string) ? null : string;
		}

		String version = null;
		try {
		    URL url = new URL(sUrl);
		    if (url.getHost() == null || url.getHost().isEmpty()) {
		        LOGGER.warn("Could not get dynaTrace version via rest (" + sUrl + "), host is empty, check config settings!");
		    } else {
    		    String data = new DtSSLHelper(10000, USER_AGENT).getData(sUrl);
    		    if(data != null) {
    		    	Matcher matcher = XML_RESULT_REGEX.matcher(data);
	    			if (matcher.find()) {
	    				version = matcher.group(1);
	    			} else {
	    				LOGGER.info("Could not get dynaTrace version from data '" + data + "' retrieved from URL '" + sUrl + "'");
	    			}
    		    } else {
    				LOGGER.info("Received empty version data from URL '" + sUrl + "'");
    		    }
		    }
		} catch (MalformedURLException mue) {
		    LOGGER.warn("Malformed url, check config-settings: " + sUrl + ", error=" + mue.getMessage());
		} catch (IOException e) {
			LOGGER.info("Could not get dynaTrace version from URL: " + sUrl + ", error=" + e.getMessage());
		}

		if (version == null && fallbackAgentPath != null) {
			version = convertAgentPathToVersionString(fallbackAgentPath);
		}

		putCache(sUrl, version);
		return version;
	}

	private static void putCache(String sUrl, String version) {
		if(version == null) {
			CACHED.put(sUrl, EMPTY_VALUE);
		} else {
			CACHED.put(sUrl, version);
		}
	}

	private static String getCached(String sUrl) {
		return CACHED.getIfPresent(sUrl);
	}

	public static String convertAgentPathToVersionString(String agentPath) {
		String version = null;
		Matcher matcher = INSTALL_DIR_REGEX.matcher(agentPath);
		if (matcher.find()) {
			version = matcher.group(1);
		}
		return version;
	}

	public static String getHeaderImageName() {
		return DtVersionDetector.isAPM() ? BaseConstants.Images.HEADER_APM_EASY_TRAVEL : BaseConstants.Images.HEADER_EASY_TRAVEL;
	}

	public static String getServerLabel() {
		return DtVersionDetector.isAPM() ? BaseConstants.Labels.APM_SERVER : BaseConstants.Labels.CLASSIC_SERVER;
	}

	/**
	 * Determines if the version is greater or equal the given version.
	 *
	 * @param version The version to be tested. Format 6.2.x = 62.
	 * @return True if version is greater or equal the detected one, false otherwise.
	 */
	public static boolean isDetectedVersionGreaterOrEqual(int version) {
		return DtVersionDetector.isDetectedVersionGreaterOrEqual(DtVersionDetector.determineDTVersion(null), version);
	}

	/**
	 * Determines if the version is greater or equal the given version.
	 *
	 * @param versionString
	 * @param version The version to be tested. Format 6.2.x = 62.
	 * @return True if version is greater or equal the detected one, false otherwise.
	 */
	public static boolean isDetectedVersionGreaterOrEqual(String versionString, int version) {
		if (versionString != null) {
			String[] spl = versionString.split("\\.");
			if (spl.length < 2) {
				return false;
			}
			String releaseVersionString = spl[0] + "" + spl[1];
			int releaseVersion = Integer.parseInt(releaseVersionString);
			if (releaseVersion >= version) {
				return true;
			}
			return false;
		}
		return false;
	}

	/**
	 * for testing.
	 *
	 * @author cwat-dstadler
	 */
	public static void clearCache() {
		CACHED.invalidateAll();
	}
}
