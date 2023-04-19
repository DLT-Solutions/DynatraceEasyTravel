package com.dynatrace.easytravel.launcher.procedures;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.StopMode;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;


public class HbaseProcedureTest {
    private static final Logger LOGGER = LoggerFactory.make();

    static {
		// set system properties for installation location
		System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");

		LOGGER.info("Using files from: " + System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION));
    }

    @Ignore
	@Test
	public void testHbaseProcedure() throws Exception {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.HBASE_ID);
		HbaseProcedure proc = new HbaseProcedure(mapping);
		assertFalse(proc.isInstrumentationSupported());
		assertTrue(proc.isOperatingCheckSupported());

		// TODO: this currently reports TRUE before starting!
		assertTrue(proc.isOperating());

		proc.hasLogfile();
		assertNotNull(proc.getLogfile());
		assertEquals(Technology.HBASE, proc.getTechnology());
		assertNotNull(proc.getExecutable(mapping));
		assertNotNull(proc.getStopExecutable());
		assertEquals(StopMode.SEQUENTIAL, proc.getStopMode());
		assertNull(proc.getWorkingDir());
		assertNotNull(proc.getAgentConfig());

		proc.stop();

		proc.run();

		assertTrue(proc.isOperating());

		proc.stop();

		// TODO: this currently reports TRUE before starting!
		assertTrue(proc.isOperating());
	}
}
