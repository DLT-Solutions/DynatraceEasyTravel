package com.dynatrace.easytravel.launcher.procedures.utils;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.logging.Logger;

import org.junit.Test;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.launcher.httpd.ApacheHttpdUtils;

/**
 * @author Rafal.Psciuk
 *
 */
public class ExecutableUtilsTest {

	private static final Logger LOGGER = Logger.getLogger( ExecutableUtilsTest.class.getName() );
	
	static {
        System.setProperty( BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist" );
        LOGGER.warning("Using files from: " + System.getProperty( BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION) );
    }
	
	@Test
	public void test() { 
		String osPrefix = OperatingSystem.IS_WINDOWS ? "Windows" : "Linux";
		String bitness = null;
		if( fileExists(ApacheHttpdUtils.INSTALL_APACHE_PATH, osPrefix + "_x64")) {
			bitness = "_x64";
		} else {
			bitness = "_x86"; 
		}
		String result = ExecutableUtils.getInstallDirDependingOs( ApacheHttpdUtils.INSTALL_APACHE_PATH );
		assertEquals( osPrefix + bitness, result );
	}
		
	private boolean fileExists( String baseDir, String name ) {
		return new File( baseDir, name ).exists();
	}
}