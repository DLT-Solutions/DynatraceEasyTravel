package com.dynatrace.easytravel.weblauncher;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.TestHelpers;

import ch.qos.logback.classic.Logger;


public class RunLauncherTomcatTest {
	private static final Logger log = LoggerFactory.make();

    static {
        System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");
        log.warn("Using files from: " + System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION));
    }

	@Test
	public void testCreate() throws Exception {
		RunLauncherTomcat run = new RunLauncherTomcat();
		assertNotNull(run);
	}

	@Test
	public void testMainInvalidOption() throws Exception {
		try {
			RunLauncherTomcat.main(new String[] {"-invalidoption"});
			fail("Should catch exception");
		} catch (Exception e) {
			// tries to start the scenario which does not exist
			TestHelpers.assertContains(e, "-invalidoption");
		}
	}

	@Test
	public void testMainHelp() throws Exception {
		// prints help and exits
		RunLauncherTomcat.main(new String[] {"-h"});
		RunLauncherTomcat.main(new String[] {"--help"});
	}

	@Ignore("Tries to start Tomcat, runs out of permgen in Eclipse?")
	@Test
	public void testMain() throws Exception {
		RunLauncherTomcat .main(new String[] {});
	}
}
