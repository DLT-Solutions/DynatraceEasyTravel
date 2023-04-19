package com.dynatrace.easytravel.launcher.procedures;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;

/**
 * @author rafal.psciuk
 *
 */
public class ThirdPartyContentServerProcedureTest {

	private static final Logger LOGGER = LoggerFactory.make();
	
    static {
		// set system properties for installation location
		System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");

		LOGGER.info("Using files from: " + System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION));
    }
    
	@Test
	public void testSetEnvArgs() throws CorruptInstallationException {
		EasyTravelConfig config = EasyTravelConfig.read();		
		try {
			config.thirdpartyEnvArgs = new String[] {"RUXIT_CLUSTER_ID=thirdParty", "NODE_ID=thirdPartyNode"};
			ThirdPartyContentServerProcedure proc = new ThirdPartyContentServerProcedure(new DefaultProcedureMapping(Constants.Procedures.THIRDPARTY_SERVER_ID));
			Map<String, String> envArgs = proc.getProcess().getEnvironment();
			assertEquals("thirdParty", envArgs.get("RUXIT_CLUSTER_ID"));
			assertEquals("thirdPartyNode", envArgs.get("NODE_ID"));
		} finally {
			EasyTravelConfig.resetSingleton();
		} 
	}
}

