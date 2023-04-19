package com.dynatrace.easytravel.launcher.procedures;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.engine.StopMode;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;

public class MysqlContentCreationProcedureTest {
    private static final Logger LOGGER = LoggerFactory.make();

    static {
		// set system properties for installation location
		System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");
		LOGGER.info("Using files from: " + System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION));
    }

    @Ignore("Fails on Linux and leaves mysqld processes behind there, likely needs more of an integration test...")
	@Test
	public void testMysqlContentCreationProcedure() throws Exception {
		// first start derby db, it is needed as data is copied from it to mysql
        DbmsProcedure derby = new DbmsProcedure(new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID));

        derby.run();

        assertTrue(derby.isOperating());

        try {

	        // then start up MySqlProcedure
			MysqlProcedure mysql = new MysqlProcedure(new DefaultProcedureMapping(Constants.Procedures.INPROCESS_MYSQL_ID));

			assertTrue(mysql.isOperatingCheckSupported());
			assertFalse(mysql.isOperating());

			mysql.run();

			assertTrue("Should be operating now, but was reported as false", mysql.isOperating());

			try {
				runMyqlContentCreatorProcedure();

			} finally {
				mysql.stop();
			}
        } finally {
        	derby.stop();
        }
	}
    
    @Ignore ("Integration test; requires running mysql")
    @Test
    public void testContentCreatorOnly(){    	
    	runMyqlContentCreatorProcedure();
    }
    
    private void runMyqlContentCreatorProcedure() {
		MysqlContentCreationProcedure proc = new MysqlContentCreationProcedure(new DefaultProcedureMapping(Constants.Procedures.MYSQL_CONTENT_CREATOR_ID));

		assertTrue(proc.isStoppable());
		assertEquals(StopMode.PARALLEL, proc.getStopMode());
		assertFalse(proc.isRunning());
		try {
			proc.isOperating();
			fail("Not supported");
		} catch (UnsupportedOperationException e) {
			// expected
		}
		assertTrue(proc.isSynchronous());
		assertNotNull(proc.getDetails());
		assertFalse(proc.hasLogfile());
		assertNull(proc.getLogfile());
		assertFalse(proc.agentFound());
		assertNull(proc.getTechnology());
		proc.addStopListener(null);
		proc.removeStopListener(null);
		proc.clearStopListeners();

		proc.stop();

		proc.run();

		assertFalse(proc.isRunning());

		proc.stop();    	
    }
}
