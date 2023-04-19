package com.dynatrace.diagnostics.uemload.iot;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.openkit.Device;
import com.dynatrace.diagnostics.uemload.openkit.OpenKitParams;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.openkit.api.OpenKit;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author Michal.Bakula
 */
public abstract class IotDevice extends Device {
	private static final int NO_OF_VERSIONS = 5;
	private static final int SIZE_OF_ARRAY = (int) ((NO_OF_VERSIONS * 0.5 + 0.5) * NO_OF_VERSIONS);

	public IotDevice(OpenKit openKit, OpenKitParams params, ExtendedCommonUser user) {
		super(openKit, params, user);
	}

	public abstract void crash() throws IotCrashException;

	public abstract String getCrashName();

	public abstract String getCrashReason();

	public static String getRandomAppVersion() {
		return getVersions()[UemLoadUtils.randomInt(SIZE_OF_ARRAY)];
	}

	protected static String[] getVersions() {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		return getVersions(cal);
	}

	protected static String[] getVersions(Calendar cal) {
		String[] versions = new String[SIZE_OF_ARRAY];
		int index = 0;
		for (int i = 0; i < NO_OF_VERSIONS; i++) {
			Integer major = cal.get(Calendar.YEAR) - 2017;
			cal.add(Calendar.DAY_OF_YEAR, -7);
			Integer minor = cal.get(Calendar.WEEK_OF_YEAR);
			String version = String.format("%d.%d", major, minor);
			for (int j = i; j < NO_OF_VERSIONS; j++) {
				versions[index] = version;
				index++;
			}
		}
		return versions;
	}
}
