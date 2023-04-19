package com.dynatrace.easytravel.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.procedures.DbmsProcedure;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.model.GenericDataAccess;
import com.dynatrace.easytravel.persistence.Database;
import com.dynatrace.easytravel.persistence.SqlDatabase;
import com.dynatrace.easytravel.util.DtVersionDetector;

/**
 * Base class for running a database based test. It will
 * ensure that the database is started.
 *
 * @author dominik.stadler
 */
public class DatabaseBase {
	private static final Logger log = LoggerFactory.make();

	protected static EasyTravelConfig config;
	private static DbmsProcedure proc;
	private static boolean started;


	protected static Database database;

	@BeforeClass
	public static void setUpClass() throws IOException {
		DtVersionDetector.enforceInstallationType(InstallationType.Classic);

		config = EasyTravelConfig.read();
		proc = new DbmsProcedure(new DefaultProcedureMapping("databasetest"));
		started = false;

		LoggerFactory.initLogging();

		if (config.internalDatabaseEnabled) {
			assertTrue(proc.init());
			if (!proc.isRunning()) { // JLT-44297 don't start Derby twice
				log.info("Starting database procedure");

				started = true;
				assertEquals(Feedback.Success, proc.run());
			}
		}
		database = new SqlDatabase().initialize();

//		JpaAccessUtils.setProperties(config.databaseDriver, config.databaseUrl, config.databaseUser, config.databasePassword);
	}

	protected DataAccess createNewAccess() {
		return new GenericDataAccess(database.createNewBusinessController());
	}

	@AfterClass
	public static void tearDownClass() throws IOException {
		if (database != null) {
			database.closeConnection();
		}

		if(started) {
			log.info("Stopping database procedure");

			assertEquals(Feedback.Neutral, proc.stop());
		}
	}
}
