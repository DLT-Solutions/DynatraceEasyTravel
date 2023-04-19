package com.dynatrace.easytravel.util;

import java.util.UUID;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.google.common.base.Strings;

/**
 * @author Rafal.Psciuk
 *
 */
public final class DynatraceUrlUtils {
	
	private static final Logger LOG = LoggerFactory.make();
	
	private DynatraceUrlUtils() {		
	}
	
	public static String getDynatraceUrl() {
		checkInstallationType();
		
		EasyTravelConfig config = EasyTravelConfig.read();
		
		if (Strings.isNullOrEmpty(config.apmTenant)) {
			return getServerWebUrl(config);
		} else if (isMangedTenant(config.apmTenant)) {
			return TextUtils.merge("{0}/e/{1}", getServerWebUrl(config), config.apmTenant);
		} else {		
			return getServerWebUrl(config);
		} 				
	}
	
	public static String getDynatraceBeaconUrl() {
		checkInstallationType();
		
		EasyTravelConfig config = EasyTravelConfig.read();
		
		if (Strings.isNullOrEmpty(config.apmTenant)) {
			throw new IllegalArgumentException("config.apmTenant is empty");
		}
		
		if (isMangedTenant(config.apmTenant) || isLocalTenant(config.apmTenant)) {
			return TextUtils.merge("{0}/mbeacon/{1}", getServerUrl(config), config.apmTenant);
		} else {
			return TextUtils.merge("{0}/mbeacon", getServerUrl(config));
		}
		
	}
	
	public static String getDynatraceAMPBeaconUrl() {
		checkInstallationType();
		
		EasyTravelConfig config = EasyTravelConfig.read();
		
		if (Strings.isNullOrEmpty(config.ampBfTenant) || Strings.isNullOrEmpty(config.ampBfEnvironment)) {
			throw new IllegalArgumentException("config.apmBfTenant is empty");
		}
		
		if (isManagedOrLocalTenant(config.ampBfTenant)) {
			return TextUtils.merge("{0}://{1}:{2}/ampbf/{3}", config.ampBfProtocol, config.ampBfEnvironment, config.ampBfPort, config.ampBfTenant);
		} else {
			return TextUtils.merge("{0}://{1}.{2}:{3}/ampbf", config.ampBfProtocol, config.ampBfTenant, config.ampBfEnvironment, config.ampBfPort);
		}
	}
	
	public static String getDtVersionDetectorUrl() {
		EasyTravelConfig config = EasyTravelConfig.read();
		return getServerWebUrl(config);
	}
		
	private static void checkInstallationType() {
		if (!DtVersionDetector.isAPM()) {
			throw new IllegalStateException("We are in AppMon mode. This class provides only Dynatrace URLs");
		}
	}
	
	private static String getServerWebUrl(EasyTravelConfig config) {
		return getServerUrl(config, true);
	}
	
	private static String getServerUrl(EasyTravelConfig config) {
		return getServerUrl(config, false);
	}
	
	private static String getServerUrl(EasyTravelConfig config, boolean useWebPort) {
		
		String port = (useWebPort ? config.apmServerWebPort : config.apmServerPort);
		
		if (Strings.isNullOrEmpty(config.apmTenant) || isManagedOrLocalTenant(config.apmTenant)) {
			return TextUtils.merge("{0}://{1}:{2}", config.apmServerProtocol, config.apmServerHost, port);
		} else { //saas
			return TextUtils.merge("{0}://{1}.{2}:{3}", config.apmServerProtocol, config.apmTenant, config.apmServerHost, port); 
		}
	}

	public static boolean isManagedOrLocalTenant(String tenant) {
		return isMangedTenant(tenant) || isLocalTenant(tenant);
	}
	
	private static boolean isMangedTenant(String tenant) {
		return isUUID(tenant) ;
	}
	
	private static boolean isLocalTenant(String tenant) {
		return tenant.length() < 8;
	}
			
	private static boolean isUUID(String apmTenant) {
		try {
			UUID.fromString(apmTenant);
			return true;
		} catch (IllegalArgumentException e) { // NOSONAR - exception is expected here
			LOG.debug(apmTenant + " is not an UUID");
			return false;
		}
	}				
}
