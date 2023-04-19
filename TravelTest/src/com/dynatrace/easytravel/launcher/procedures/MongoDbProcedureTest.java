package com.dynatrace.easytravel.launcher.procedures;

import static org.junit.Assert.*;

import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;

public class MongoDbProcedureTest {
    private static final Logger LOGGER = LoggerFactory.make();

    static {
		// set system properties for installation location
		System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");

		LOGGER.info("Using files from: " + System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION));
    }

	@Test
	public void testMongoDbProcedure() throws Exception {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.MONGO_DB_ID);
		MongoDbProcedure proc = new MongoDbProcedure(mapping);

		assertTrue(proc.isOperatingCheckSupported());
		// TODO: this always returns true!
		assertTrue(proc.isOperating());
		assertTrue(proc.hasLogfile());
		assertNotNull(proc.getLogfile());
		assertEquals(Technology.MONGODB, proc.getTechnology());
		assertNotNull(proc.getExecutable(mapping));
		assertNull(proc.getWorkingDir());
		assertNull(proc.getAgentConfig());
		assertFalse(proc.isInstrumentationSupported());

		proc.stop();

		// TODO: this always returns true!
		assertTrue(proc.isOperating());

		proc.run();

		assertTrue(proc.isOperating());

		proc.stop();

		// TODO: this always returns true!
		assertTrue(proc.isOperating());
	}
}
