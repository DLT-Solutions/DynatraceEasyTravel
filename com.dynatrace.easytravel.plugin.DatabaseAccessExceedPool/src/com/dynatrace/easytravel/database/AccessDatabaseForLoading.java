package com.dynatrace.easytravel.database;

import com.dynatrace.easytravel.persistence.Database;

/**
 * Interface for the two different modes that we support
 * in this plugin.
 *
 * @author dominik.stadler
 */
public interface AccessDatabaseForLoading {

	void execute(Database database, int iterationCount);
}
