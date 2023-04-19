package com.dynatrace.easytravel.launcher.procedures.utils;

import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;

public class VMWareUtilsTest {

	@Test
	@Ignore("Integration test")
	/*
	 * You need to set following proerties (example for demo1)
	 * config.vCenterUser=Vmware_admin_2@vsphere.local
	 * config.vCenterPassword=<fill this>
	 * config.vCenterHost=192.168.238.142
	 * config.fromHost=192.168.118.69
	 * config.toHost=192.168.118.71
	 * config.resPool=cpu-2
	 * config.vmName=cpu-3-2
	 */

	public void testVmotion() throws Exception {
		EasyTravelConfig config = EasyTravelConfig.read();
		VMwareUtils.callvMotionTask(config.vCenterHost, config.vCenterUser, config.vCenterPassword, config.vmName, config.resPool, config.toHost, config.fromHost);
		VMwareUtils.callvMotionTask(config.vCenterHost, config.vCenterUser, config.vCenterPassword, config.vmName, config.resPool, config.fromHost, config.toHost);		
	}
}
