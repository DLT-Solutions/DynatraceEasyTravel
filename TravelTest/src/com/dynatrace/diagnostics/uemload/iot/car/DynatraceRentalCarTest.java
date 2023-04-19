package com.dynatrace.diagnostics.uemload.iot.car;

import org.junit.Test;

import com.dynatrace.diagnostics.uemload.ExtendedDemoUser;
import com.dynatrace.diagnostics.uemload.iot.IotDevice;
import com.dynatrace.openkit.CrashReportingLevel;
import com.dynatrace.openkit.DataCollectionLevel;

/**
 *
 * @author Michal.Bakula
 *
 */
public class DynatraceRentalCarTest {
	private static final RentalCarParams car = new RentalCarParams() {
		{
			setCountry("Poland");
			setIp("127.0.0.1");
			setManufacturer("easyTravel");
			setModel("easyTravel Car");
			setOs("easyTravel");
			setAppVersion("1");
			setDeviceId(101L);
			setDataCollectionLevel(DataCollectionLevel.USER_BEHAVIOR);
			setCrashReportingLevel(CrashReportingLevel.OPT_IN_CRASHES);
		}
	};

	@Test(expected = EngineFailure.class)
	public void crashTest() throws Exception {
		IotDevice device = new DynatraceRentalCar(car, ExtendedDemoUser.MARIA_USER);
		device.crash();
	}

}
