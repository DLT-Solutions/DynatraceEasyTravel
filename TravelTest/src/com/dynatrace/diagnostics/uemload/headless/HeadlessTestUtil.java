package com.dynatrace.diagnostics.uemload.headless;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

import org.apache.commons.lang3.SystemUtils;

import com.dynatrace.easytravel.TestUtil;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.util.process.HeadlessProcessKillerFactory;
import com.dynatrace.easytravel.util.process.HeadlessProcessNames;

public class HeadlessTestUtil {
	public static void setupChromeAndLogDirectories() {
        // set up path to chrome driver - this is usually done in DriverEntryFactory

		String chromePath = getChromeDir();
        // we need to replace the ChromeDriver when running from the TravelTest project
        if(SystemUtils.IS_OS_LINUX) {
            System.setProperty("webdriver.chrome.driver", chromePath + "/driver/chromedriver_linux64");
            HeadlessProcessNames.PATH_TO_CHROMIUM_LINUX = chromePath + "/chrome" ;
            System.setProperty("logback.configurationFile", Directories.getResourcesDir() + "/logback.xml");
        } else {
            System.setProperty("webdriver.chrome.driver", chromePath + "\\driver\\chromedriver_windows32.exe");
            HeadlessProcessNames.PATH_TO_CHROMIUM_WINDOWS = chromePath + "\\chrome.exe";
            System.setProperty("logback.configurationFile", Directories.getResourcesDir() + "\\logback.xml");
        }
	}

	private static String getChromeDir() {
		File chromeDir = Directories.getChromeDir();		
		if (chromeDir.exists() && chromeDir.isDirectory()) {
			return chromeDir.toString();
		} else {
			String testChromeDir = SystemUtils.IS_OS_LINUX ? "chrome-lin64" : "chrome-win32";
			return new File(Directories.getInstallDir(), testChromeDir).toString();
		}
	}
	
    public static void stopPoolAndCheckLeftDrivers(boolean isMobile) throws InterruptedException {
    	DriverEntryPool pool = isMobile ? MobileDriverEntryPoolSingleton.getInstance().getPool() : DriverEntryPoolSingleton.getInstance().getPool();

    	waitWhilePoolShutdownInProgress(pool);

    	pool.stopAll();

    	assertThat(HeadlessProcessKillerFactory.getChromeAndDriverProcesses(), is(empty()));
    }

    private static  void waitWhilePoolShutdownInProgress(DriverEntryPool pool) throws InterruptedException {
    	Supplier<Boolean> s = () -> pool.isShutDownInProgress();
    	TestUtil.waitWhileTrue(s, 20);
    }

}
