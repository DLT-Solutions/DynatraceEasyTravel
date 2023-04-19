package com.dynatrace.diagnostics.uemload.mobile;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.google.common.base.Strings;

public class MobileDeviceTest {
	
	EasyTravelConfig config; 
	@Before
	public void setup() {
		DtVersionDetector.enforceInstallationType(InstallationType.APM);
		config = EasyTravelConfig.read();
		
		config.apmServerHost = "live.dynatrace.com";
		config.apmServerPort = "443";		
	}
	
	@After
	public void tearDonw() {
		DtVersionDetector.enforceInstallationType(null);
		EasyTravelConfig.resetSingleton();
	}
	
	@Test
	public void testEmptyTenantRuxitBeaconUrl() {		
		config.apmTenant = "";
		MobileDevice.initRuxitBeaconUrl();
		String ruxitBeaconUrl = MobileDevice.getRuxitBeaconUrl();
		assertTrue("ruxitBeaconUrl is not empty: " + ruxitBeaconUrl, Strings.isNullOrEmpty(ruxitBeaconUrl));
	}
	
	@Test
	public void testSaaSTenant() {
		config.apmTenant = "jnc47888";
		MobileDevice.initRuxitBeaconUrl();
		String ruxitBeaconUrl = MobileDevice.getRuxitBeaconUrl();		
		assertEquals("SECRET",ruxitBeaconUrl);
	}
	
	@Test
	public void testClassicMode() {
		testSaaSTenant();
		DtVersionDetector.enforceInstallationType(InstallationType.Classic);
		MobileDevice.initRuxitBeaconUrl();
		String ruxitBeaconUrl = MobileDevice.getRuxitBeaconUrl();		
		assertTrue("ruxitBeaconUrl is not empty: " + ruxitBeaconUrl, Strings.isNullOrEmpty(ruxitBeaconUrl));
	}
	
	@Test
	public void testLocalTenant() {
		config.apmTenant = "1";
		MobileDevice.initRuxitBeaconUrl();
		String ruxitBeaconUrl = MobileDevice.getRuxitBeaconUrl();		
		assertEquals("SECRET",ruxitBeaconUrl);
	}
	
	@Test
	public void testManagedTenant() {
		config.apmTenant = "c6daec2a-d3ec-4502-84ae-ff0122ab6549";
		MobileDevice.initRuxitBeaconUrl();
		String ruxitBeaconUrl = MobileDevice.getRuxitBeaconUrl();		
		assertEquals("SECRET",ruxitBeaconUrl);
	}
}
