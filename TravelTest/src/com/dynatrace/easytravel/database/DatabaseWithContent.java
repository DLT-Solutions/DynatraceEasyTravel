package com.dynatrace.easytravel.database;

import java.io.IOException;

import org.junit.BeforeClass;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.persistence.Database;
import com.dynatrace.easytravel.persistence.SqlDatabase;
import com.dynatrace.easytravel.spring.PluginConstants;

/**
 * Base test class for tests that require database and
 * useful content in the database.
 * 
 * @author dominik.stadler
 */
public class DatabaseWithContent extends DatabaseBase {

	private static final Logger log = LoggerFactory.make();
	@SuppressWarnings("hiding")
	protected static Database database;

	@BeforeClass
	public static void setUpClass() throws IOException {
		// start database if necessary
		DatabaseBase.setUpClass();

		database = new SqlDatabase();
		database.initialize(database.getBusinessController());

		// clean up before starting to create content to not have a huge database here
		cleanup(database);

		// populate database
		log.info("Start filling the database with content.");

		database.createContents(config, true);

		log.info("Done filling the database with content.");
	}

	/**
	 * Helper method to start database cleanup.
	 * 
	 * @author dominik.stadler
	 * @param sqlDatabase
	 */
	protected static void cleanup(Database sqlDatabase) {
		log.info("Starting database cleanup procedure");
		DatabaseCleanup cleanup = new DatabaseCleanup();
		cleanup.setExtensionPoint(new String[] { PluginConstants.BACKEND_JOURNEY_SEARCH });
		cleanup.setEnabled(true);

		cleanup.execute(PluginConstants.BACKEND_JOURNEY_SEARCH,
				sqlDatabase);
	}


}
