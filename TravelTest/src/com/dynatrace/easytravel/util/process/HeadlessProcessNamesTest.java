package com.dynatrace.easytravel.util.process;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;

public class HeadlessProcessNamesTest {
	EasyTravelConfig config;
	
	@Before
	public void setup() {
		config = EasyTravelConfig.read();
	}
	
	@After
	public void tearDown() {
		EasyTravelConfig.resetSingleton();
	}
	
	@Test
	public void getChromeNameTest() {
		config.chromeBinary = Paths.get("some", "path", "to", "chrome", "binary.exe").toString();
		assertEquals("binary.exe", HeadlessProcessNames.getChromeExeName());
		assertEquals("binary.exe", HeadlessProcessNames.getChromeLinuxName());
		config.chromeBinary = "";
		assertEquals("chrome.exe", HeadlessProcessNames.getChromeExeName());
		assertEquals("chrome", HeadlessProcessNames.getChromeLinuxName());
	}
	
	@Test
	public void getChromeDriverNameTest() {
		config.chromeDriverBinary = Paths.get("some", "path", "to", "chrome", "driver", "binary.exe").toString();
		assertEquals("binary.exe", HeadlessProcessNames.getChromeDriverExeName());
		assertEquals("binary.exe", HeadlessProcessNames.getChromeDriverLinuxName());
		config.chromeDriverBinary = "";
		assertEquals("chromedriver_windows32.exe", HeadlessProcessNames.getChromeDriverExeName());
		assertEquals("chromedriver_linux64", HeadlessProcessNames.getChromeDriverLinuxName());
	}
	
	@Test
	public void getConfigChromeDriverExeNameShortTest() {
		config.chromeDriverBinary = Paths.get("some", "path", "to", "chrome", "very_long_chromedriver_windows32.exe").toString();
		assertEquals("very_long_chromedriver_wi", HeadlessProcessNames.getConfigChromeDriverExeNameShort());
		config.chromeDriverBinary = ""; 
		assertEquals("chromedriver_windows32.ex", HeadlessProcessNames.getConfigChromeDriverExeNameShort());
	}
	
	@Test
	public void getConfigChromeExeNameShortTest() {
		config.chromeBinary = Paths.get("some", "path", "to", "chrome", "very_long_chrome_name_windows32.exe").toString();
		assertEquals("very_long_chrome_name_win", HeadlessProcessNames.getConfigChromeExeNameShort());
		config.chromeBinary = ""; 
		assertEquals("chrome.exe", HeadlessProcessNames.getConfigChromeExeNameShort());
	}
		
	@Test
	public void getChromePathTest() {
		String path = Paths.get("some", "path", "to", "chrome", "chromebin_exe").toString();
		config.chromeBinary = path;
		assertEquals(path, HeadlessProcessNames.getChromePathWindows());
		assertEquals(path, HeadlessProcessNames.getChromePathLinux());
		config.chromeBinary = "";
		String defaultPathWin = Paths.get(Directories.getInstallDir().getAbsolutePath(), "chrome", "chrome.exe").toString(); 
		assertEquals(defaultPathWin, HeadlessProcessNames.getChromePathWindows());
		String defaultPathLinux = Paths.get(Directories.getInstallDir().getAbsolutePath(), "chrome", "chrome").toString();
		assertEquals(defaultPathLinux, HeadlessProcessNames.getChromePathLinux());
	}	
	
	@Test
	public void getChromeDriverPathTest() {
		String path = Paths.get("some", "path", "to", "chrome", "driver", "chromedrvier_exe").toString();
		config.chromeDriverBinary = path.toString();
		assertEquals(path, HeadlessProcessNames.getChromeDriverPathWindows());
		assertEquals(path, HeadlessProcessNames.getChromeDriverPathLinux());
		config.chromeDriverBinary = "";
		String defaultPathWin = Paths.get(Directories.getInstallDir().getAbsolutePath(), "chrome", "driver", "chromedriver_windows32.exe").toString();
		assertEquals(defaultPathWin, HeadlessProcessNames.getChromeDriverPathWindows());
		String defaultPathLinux = Paths.get(Directories.getInstallDir().getAbsolutePath(), "chrome", "driver", "chromedriver_linux64").toString();
		assertEquals(defaultPathLinux, HeadlessProcessNames.getChromeDriverPathLinux());
	}	
}
