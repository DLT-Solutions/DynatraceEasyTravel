package com.dynatrace.diagnostics.uemload.utils;

import com.dynatrace.diagnostics.uemload.iot.car.DynatraceRentalCar;
import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileDevice;
import com.dynatrace.easytravel.util.SpecialUserData;
import com.dynatrace.easytravel.util.process.HeadlessProcessKillerFactory;

public class ShutdownUtils {
	
	public static void shutdown() {
		saveSpecialUsersState();
		HeadlessProcessKillerFactory.stopChromeProcesses(true);
		shutdownOpenKit();
	}

	private static void saveSpecialUsersState() {
		SpecialUserData sud = SpecialUserData.getInstance();
		sud.generateConfigFile();
	}

	private static void shutdownOpenKit() {
        MobileDevice.shutdownOpenKit();
        DynatraceRentalCar.shutdownOpenKit();
	}
}