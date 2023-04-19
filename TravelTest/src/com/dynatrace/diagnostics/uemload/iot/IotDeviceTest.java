package com.dynatrace.diagnostics.uemload.iot;

import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.ExtendedDemoUser;
import com.dynatrace.diagnostics.uemload.iot.car.DynatraceRentalCar;
import com.dynatrace.diagnostics.uemload.iot.car.RentalCarParams;
import com.dynatrace.openkit.CrashReportingLevel;
import com.dynatrace.openkit.DataCollectionLevel;

/**
 *
 * @author Michal.Bakula
 *
 */
public class IotDeviceTest {
	private static final RentalCarParams car = new RentalCarParams() {
		{
			setCountry("Poland");
			setIp("127.0.0.1");
			setManufacturer("easyTravel");
			setModel("easyTravel Car");
			setOs("easyTravel");
			setAppVersion("1");
			setDeviceId(102L);
			setDataCollectionLevel(DataCollectionLevel.USER_BEHAVIOR);
			setCrashReportingLevel(CrashReportingLevel.OPT_IN_CRASHES);
		}
	};

	@Test
	public void iotVersionTest() {
		DynatraceRentalCar device = new DynatraceRentalCar(car, ExtendedDemoUser.MARIA_USER);

		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.set(2018, 2, 20);

		String[] versions = IotDevice.getVersions(cal);
		Assert.assertTrue(Arrays.stream(versions).filter(s -> s.equals("1.11")).collect(Collectors.toList()).size() == 5);
		Assert.assertTrue(Arrays.stream(versions).filter(s -> s.equals("1.10")).collect(Collectors.toList()).size() == 4);
		Assert.assertTrue(Arrays.stream(versions).filter(s -> s.equals("1.9")).collect(Collectors.toList()).size() == 3);
		Assert.assertTrue(Arrays.stream(versions).filter(s -> s.equals("1.8")).collect(Collectors.toList()).size() == 2);
		Assert.assertTrue(Arrays.stream(versions).filter(s -> s.equals("1.7")).collect(Collectors.toList()).size() == 1);

		cal.set(2019, 0, 14);
		versions = IotDevice.getVersions(cal);
		Assert.assertTrue(Arrays.stream(versions).filter(s -> s.equals("2.2")).collect(Collectors.toList()).size() == 5);
		Assert.assertTrue(Arrays.stream(versions).filter(s -> s.equals("2.1")).collect(Collectors.toList()).size() == 4);
		Assert.assertTrue(Arrays.stream(versions).filter(s -> s.equals("1.52")).collect(Collectors.toList()).size() == 3);
		Assert.assertTrue(Arrays.stream(versions).filter(s -> s.equals("1.51")).collect(Collectors.toList()).size() == 2);
		Assert.assertTrue(Arrays.stream(versions).filter(s -> s.equals("1.50")).collect(Collectors.toList()).size() == 1);

		cal.set(2020, 8, 31);
		versions = IotDevice.getVersions(cal);
		Assert.assertTrue(Arrays.stream(versions).filter(s -> s.equals("3.39")).collect(Collectors.toList()).size() == 5);
		Assert.assertTrue(Arrays.stream(versions).filter(s -> s.equals("3.38")).collect(Collectors.toList()).size() == 4);
		Assert.assertTrue(Arrays.stream(versions).filter(s -> s.equals("3.37")).collect(Collectors.toList()).size() == 3);
		Assert.assertTrue(Arrays.stream(versions).filter(s -> s.equals("3.36")).collect(Collectors.toList()).size() == 2);
		Assert.assertTrue(Arrays.stream(versions).filter(s -> s.equals("3.35")).collect(Collectors.toList()).size() == 1);
	}

}
