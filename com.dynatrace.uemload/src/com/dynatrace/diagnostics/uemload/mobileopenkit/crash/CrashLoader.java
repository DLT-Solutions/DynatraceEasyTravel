package com.dynatrace.diagnostics.uemload.mobileopenkit.crash;

import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileDevice;
import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileOS;
import com.dynatrace.easytravel.util.ResourceFileReader;

import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrashLoader {
	private static final Logger LOGGER = Logger.getLogger(CrashLoader.class.getName());
	private static final EnumMap<MobileOS, List<Crash>> CRASH_MAP = new EnumMap<>(MobileOS.class);

	/** To match a line, e.g. "Version:         2.0.24 (2.0.024)" in order to replace the version name and build */
	private static final String VERSION_LINE_PATTERN = "^" + // start of line
			"(\\bVersion\\b:\\s*)" + // starting with "Version:" followed by 0..n spaces
			"([^\n]*)"; // any char except newline
	private static final Pattern APPLE_CRASH_VERSION_PATTERN = Pattern.compile(VERSION_LINE_PATTERN);

	static {
		CRASH_MAP.put(MobileOS.ANDROID, Arrays.asList(
				new Crash(6, "java.lang.IndexOutOfBoundsException", "easyTravel_Android.txt", -1, MobileOS.ANDROID.prefix),
				new Crash(3, "java.lang.RuntimeException", "easyTravel_Android2.txt", -1, MobileOS.ANDROID.prefix),
				new Crash("java.lang.NullPointerException", "easyTravel_Android3.txt", -1, MobileOS.ANDROID.prefix)
		));
		CRASH_MAP.put(MobileOS.IOS, Arrays.asList(
				new Crash(6, "NSRangeException", "easyTravel_iOS.crash", 6, MobileOS.IOS.prefix),
				new Crash(3, "NSDemoException", "easyTravel_iOS2.crash", 5, MobileOS.IOS.prefix),
				new Crash("NSRangeException", "easyTravel_iOS3.crash", 6, MobileOS.IOS.prefix)
		));
	}

	private CrashLoader() {}

	public static Crash loadCrashReport(MobileDevice device) {
		final int version = Character.getNumericValue(device.appVersion.charAt(0));
		for (Crash crash : CRASH_MAP.get(device.os)) {
			if(version > crash.minOsVersion) {
				if(crash.getStackTrace() == null) {
					String crashReport = loadReport(crash.file);
					if(device.isIOS()) {
						final String replacement = "Version:         " + device.appVersion + " (" + device.appVersionBuild + ")";
						Matcher m = APPLE_CRASH_VERSION_PATTERN.matcher(crashReport);
						crashReport = m.replaceFirst(replacement);
					}
					crash.setStackTrace(crashReport);
				}
				return crash;
			}
		}
		LOGGER.warning(() -> String.format("No crash found for os %s version %s", device.os, device.appVersion));
		return null;
	}

	private static String loadReport(String filename) {
		try {
			InputStream stream = ResourceFileReader.getInputStream(filename);
			if (stream == null) {
				LOGGER.log(Level.SEVERE, () -> String.format("Could not find requested file: %s in resources.", filename));
				return "";
			}
			try {
				byte[] buffer = new byte[10000];
				StringBuilder builder = new StringBuilder();
				int length;
				while ((length = stream.read(buffer)) != -1) {
					builder.append(new String(buffer, 0, length));
				}
				return builder.toString();
			} finally {
				stream.close();
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to load crash report '" + filename + "' from classpath-folder 'resources'", e);
			return "";
		}
	}
}
