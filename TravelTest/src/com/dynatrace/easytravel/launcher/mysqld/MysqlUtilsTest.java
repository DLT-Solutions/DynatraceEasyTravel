package com.dynatrace.easytravel.launcher.mysqld;

import static org.junit.Assert.assertNotNull;

import java.util.logging.Logger;

import org.junit.Test;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;

public class MysqlUtilsTest {

	private static final Logger LOGGER = Logger.getLogger(MysqlUtilsTest.class.getName());
	
    static {
        System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");
        LOGGER.warning("Using files from: " + System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION));
    }	

	
	@Test
	public void test() {
		assertNotNull(MysqlUtils.getExecutableDependingOnOs());
		assertNotNull(MysqlUtils.getMySqlbaseDir());
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(MysqlUtils.class);
	}	
}
