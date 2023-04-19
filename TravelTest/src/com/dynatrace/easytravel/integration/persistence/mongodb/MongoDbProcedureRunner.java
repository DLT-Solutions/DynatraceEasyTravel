package com.dynatrace.easytravel.integration.persistence.mongodb;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.dynatrace.easytravel.integration.StartProcedure;
import com.dynatrace.easytravel.integration.persistence.ProcedureRunner;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.procedures.MongoDbProcedure;


/**
 * Base class for running a database based test. It will
 * ensure that the database is started.
 *
 * @author stefan.moschinski
 */
public class MongoDbProcedureRunner {
	//private static final Logger log = LoggerFactory.make();

	private static MongoDbProcedure proc;

	@BeforeClass
	public static void setUpClass() throws IOException, CorruptInstallationException, InterruptedException {
		ProcedureRunner.configure();

		proc = StartProcedure.newMongoDbProcedure();
		proc.run();
	}

	@AfterClass
	public static void tearDownClass() {
		proc.stop();
	}

}
