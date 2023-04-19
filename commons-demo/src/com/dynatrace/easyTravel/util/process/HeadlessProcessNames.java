package com.dynatrace.easytravel.util.process;

import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.google.common.base.Strings;

public final class HeadlessProcessNames {	
	
	// location from where Chromium is installed. In the easyTravel distribution i.e. in C:\Program Files\dynaTrace\easyTravel (x64)
	// or in development it will be easyTravel\distribution\dist folder
	public static String PATH_TO_CHROMIUM_WINDOWS = getChromePathWindows();
	public static final String PATH_TO_CHROME_DRIVER_WINDOWS = getChromeDriverPathWindows();
	public static String PATH_TO_CHROMIUM_LINUX = getChromePathLinux();
	public static final String PATH_TO_CHROME_DRIVER_LINUX = getChromeDriverPathLinux();
	public static final String PATH_TO_CHROME_USER_DIR = Paths.get(Directories.getTempDir().getAbsolutePath(), "chrome").toString();
	
	public static final String CHROME_EXE = getChromeExeName();
	public static final String CHROME_DRIVER_EXE = getChromeDriverExeName();
	public static final String CHROME_EXE_SHORT = getConfigChromeExeNameShort();
	public static final String CHROME_DRIVER_EXE_SHORT = getConfigChromeDriverExeNameShort(); 
	public static final String CHROME_EXE_LINUX = getChromeLinuxName();
	public static final String CHROME_DRIVER_EXE_LINUX = getChromeDriverLinuxName();
	
	private static final String CHROME_EXE_DEFAULT 					= "chrome.exe";
	private static final String CHROME_DRIVER_EXE_DEFAULT 			= "chromedriver_windows32.exe";
	private static final String CHROME_EXE_LINUX_DEFAULT			= "chrome";
	private static final String CHROME_DRIVER_EXE_LINUX_DEFAULT		= "chromedriver_linux64";


	static String getChromeExeName() {
		String chromeBinary = FilenameUtils.getName(EasyTravelConfig.read().chromeBinary);
		return getConfigOrDetectedValue(chromeBinary, CHROME_EXE_DEFAULT);
	}
	
	static String getChromeDriverExeName() {
		String chromeDriverBinary = FilenameUtils.getName(EasyTravelConfig.read().chromeDriverBinary);
		return getConfigOrDetectedValue(chromeDriverBinary, CHROME_DRIVER_EXE_DEFAULT);
	}
	
	static String getConfigChromeDriverExeNameShort() {
		return StringUtils.left(getChromeDriverExeName(), 25);
	}
	
	static String getConfigChromeExeNameShort() {
		return StringUtils.left(getChromeExeName(), 25);
	}
	
	static String getChromeLinuxName() {
		String chromeBinary = FilenameUtils.getName(EasyTravelConfig.read().chromeBinary);
		return getConfigOrDetectedValue(chromeBinary, CHROME_EXE_LINUX_DEFAULT);
	}
	
	static String getChromeDriverLinuxName() {
		String chromeDriverBinary = FilenameUtils.getName(EasyTravelConfig.read().chromeDriverBinary);
		return getConfigOrDetectedValue(chromeDriverBinary, CHROME_DRIVER_EXE_LINUX_DEFAULT);
	}
	
	static String getChromePathWindows() {
		String chromeBinary = EasyTravelConfig.read().chromeBinary;
		String detectedValue = Paths.get( Directories.getChromeDir().getAbsolutePath(),  CHROME_EXE_DEFAULT ).toString();
		return getConfigOrDetectedValue(chromeBinary, detectedValue); 
	}
	
	static String getChromeDriverPathWindows() {
		String chromeDriverBinary = EasyTravelConfig.read().chromeDriverBinary;
		String detectedValue = Paths.get( Directories.getChromeDir().getAbsolutePath(), "driver", CHROME_DRIVER_EXE_DEFAULT ).toString();
		return getConfigOrDetectedValue(chromeDriverBinary, detectedValue);
	}
	
	static String getChromePathLinux() {
		String chromeBinary = EasyTravelConfig.read().chromeBinary;
		String detectedValue = Paths.get( Directories.getChromeDir().getAbsolutePath(), CHROME_EXE_LINUX_DEFAULT ).toString();
		return getConfigOrDetectedValue(chromeBinary, detectedValue); 
	}
	
	static String getChromeDriverPathLinux() {
		String chromeDriverBinary = EasyTravelConfig.read().chromeDriverBinary;
		String detectedValue = Paths.get( Directories.getChromeDir().getAbsolutePath(), "driver", CHROME_DRIVER_EXE_LINUX_DEFAULT ).toString();
		return getConfigOrDetectedValue(chromeDriverBinary, detectedValue); 
	}
	
	private static String getConfigOrDetectedValue(String configValue, String detectedValue) {
		if(Strings.isNullOrEmpty(configValue)) {
			return detectedValue;
		}
		return configValue;
	}
	
	private HeadlessProcessNames() {}	
}
