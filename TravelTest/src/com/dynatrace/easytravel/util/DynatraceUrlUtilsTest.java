package com.dynatrace.easytravel.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;

/**
 * @author Rafal.Psciuk
 *
 */
public class DynatraceUrlUtilsTest {
	
	private static final String PROTOCOL = "https";
	private static final String APM_PORT = "443";
	private static final String APM_WEB_PORT = "8443";
	private static final String APM_HOST = "somehost";
	private static final String SAAS_TENANT = "jnc47888";
	private static final String LOCAL_TENANT = "1";
	private static final String MANAGED_ENV = "ee4b97fe-5357-438f-bc95-5cdad722e1b8";
	
	EasyTravelConfig config;	
 
	@Before
	public void setup() {
		DtVersionDetector.enforceInstallationType(InstallationType.APM);
		config = EasyTravelConfig.read();		
		
		config.apmServerHost = APM_HOST;
		config.apmServerPort = APM_PORT;
		config.apmServerWebPort = APM_WEB_PORT;
		config.apmServerProtocol = PROTOCOL;
		
		config.ampBfPort = APM_PORT;
		config.ampBfProtocol = PROTOCOL;
		config.ampBfEnvironment = APM_HOST;
	}
	
	@After
	public void tearDonw() {
		EasyTravelConfig.resetSingleton();
		
		// to get the authenticator set
		DtVersionDetector.clearCache();
		DtVersionDetector.enforceInstallationType(null);

	}
		
	@Test(expected=IllegalStateException.class)
	public void testGetUrlAppMon() {
		DtVersionDetector.enforceInstallationType(InstallationType.Classic);
		DynatraceUrlUtils.getDynatraceUrl();
	}
	
	@Test(expected=IllegalStateException.class)
	public void testGetBeaconUrlAppMon() {
		DtVersionDetector.enforceInstallationType(InstallationType.Classic);
		DynatraceUrlUtils.getDynatraceBeaconUrl();
	}
	
	@Test(expected=IllegalStateException.class)
	public void testGetAMPBeaconUrlAppMon() {
		DtVersionDetector.enforceInstallationType(InstallationType.Classic);
		DynatraceUrlUtils.getDynatraceAMPBeaconUrl();
	}

	public void testGetUrlEmptyTenant() {
		config.apmTenant = null;
		String expectedURL = TextUtils.merge("https://{0}:{1}", APM_HOST, APM_WEB_PORT, SAAS_TENANT);
		assertEquals(expectedURL, DynatraceUrlUtils.getDynatraceUrl());
	}
		
	@Test(expected=IllegalArgumentException.class)
	public void testGetBeaconUrlEmptyTenant() {
		config.apmTenant = null;
		DynatraceUrlUtils.getDynatraceBeaconUrl();
	}
	
	@Test 
	public void testGetSaaSUrl() {
		config.apmTenant = SAAS_TENANT;
		
		String expectedURL = TextUtils.merge("https://{2}.{0}:{1}", APM_HOST, APM_WEB_PORT, SAAS_TENANT);
		assertEquals(expectedURL, DynatraceUrlUtils.getDynatraceUrl());
	}
	
	@Test 
	public void testGetSaaSBeaconUrl() {
		config.apmTenant = SAAS_TENANT;
		String expectedURL = TextUtils.merge("https://{2}.{0}:{1}/mbeacon", APM_HOST, APM_PORT, SAAS_TENANT);
		assertEquals(expectedURL, DynatraceUrlUtils.getDynatraceBeaconUrl());
	}
	
	@Test 
	public void testGetSaaSAMPBeaconUrl() {
		config.ampBfTenant = SAAS_TENANT;
		String expectedURL = TextUtils.merge("https://{2}.{0}:{1}/ampbf", APM_HOST, APM_PORT, SAAS_TENANT);
		assertEquals(expectedURL, DynatraceUrlUtils.getDynatraceAMPBeaconUrl());
	}
	
	@Test 
	public void testGetManagedUrl() {
		config.apmTenant = MANAGED_ENV;
		String expectedURL = TextUtils.merge("https://{0}:{1}/e/{2}", APM_HOST, APM_WEB_PORT, MANAGED_ENV);
		assertEquals(expectedURL, DynatraceUrlUtils.getDynatraceUrl());
	}
	
	@Test 
	public void testGetManagedBeaconUrl() {
		config.apmTenant = MANAGED_ENV;
		String expectedURL = TextUtils.merge("https://{0}:{1}/mbeacon/{2}", APM_HOST, APM_PORT, MANAGED_ENV);
		assertEquals(expectedURL, DynatraceUrlUtils.getDynatraceBeaconUrl());
	}
	
	@Test 
	public void testGetManagedAMPBeaconUrl() {
		config.ampBfTenant = MANAGED_ENV;
		String expectedURL = TextUtils.merge("https://{0}:{1}/ampbf/{2}", APM_HOST, APM_PORT, MANAGED_ENV);
		assertEquals(expectedURL, DynatraceUrlUtils.getDynatraceAMPBeaconUrl());
	}
	
	@Test 
	public void testGetLocalUrl() {
		config.apmTenant = LOCAL_TENANT;
		String expectedURL = TextUtils.merge("https://{0}:{1}", APM_HOST, APM_WEB_PORT);
		assertEquals(expectedURL, DynatraceUrlUtils.getDynatraceUrl());
	}
	
	@Test 
	public void testGetLocalBeaconUrl() {
		config.apmTenant = LOCAL_TENANT;
		String expectedURL = TextUtils.merge("https://{0}:{1}/mbeacon/{2}", APM_HOST, APM_PORT, LOCAL_TENANT);
		assertEquals(expectedURL, DynatraceUrlUtils.getDynatraceBeaconUrl());
	}
	
	@Test
	public void testGetDtVersionDetectorUrlSaaS() {
		config.apmTenant = SAAS_TENANT;
		String expectedURL = TextUtils.merge("https://{2}.{0}:{1}", APM_HOST, APM_WEB_PORT, SAAS_TENANT);
		assertEquals(expectedURL, DynatraceUrlUtils.getDtVersionDetectorUrl());
	}
	
	@Test
	public void testGetDtVersionDetectorUrlLocal() {
		config.apmTenant = LOCAL_TENANT;
		String expectedURL = TextUtils.merge("https://{0}:{1}", APM_HOST, APM_WEB_PORT);
		assertEquals(expectedURL, DynatraceUrlUtils.getDtVersionDetectorUrl());
	}
	
	@Test
	public void testGetDtVersionDetectorUrlManaged() {
		config.apmTenant = MANAGED_ENV;
		String expectedURL = TextUtils.merge("https://{0}:{1}", APM_HOST, APM_WEB_PORT);
		assertEquals(expectedURL, DynatraceUrlUtils.getDtVersionDetectorUrl());
	}
	
	@Test
	public void testGetDtVersionDetectorUrlEmptyTenant() {
		config.apmTenant = "";
		String expectedURL = TextUtils.merge("https://{0}:{1}", APM_HOST, APM_WEB_PORT);
		assertEquals(expectedURL, DynatraceUrlUtils.getDtVersionDetectorUrl());
	}
	
}
