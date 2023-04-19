package com.dynatrace.easytravel.database;

import java.util.Arrays;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.persistence.Database;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.spring.SpringUtils;

public class DatabaseAccessExceedPool extends AbstractGenericPlugin {

	private static Logger log = LoggerFactory.make();

	// this plugin can run in two different modes of operation
	private enum Mode {
		threaded, nonthreaded
	}

	// which mode to use here
	// this is usually overridden in the .ctx.xml
	private Mode mode = Mode.nonthreaded;

	// how many parallel threads to start in threaded mode
	// and how many entity managers to use in non-threaded mode
	// this is usually set in the .ctx.xml
	public int iterationCount = 8;

	public synchronized void setMode(String mode) {
		this.mode = Mode.valueOf(mode);
	}

	public void setCount(int count) {
		this.iterationCount = count;
	}


	@Override
	public Object doExecute(String location, Object... context) {
		TimeManager manager = TimeManager.initialize(this.getClass(), 5000, 0.9);
		long id = manager.start(); // starts time measuring

		if (!manager.isContended()) { // if too many requests are pending, the processing is skipped
			synchronized (this) { // Synchronized to avoid multiple calls interfeering with each other...
				log.info("DatabaseAccessExceedPool: Had extension point: " + location + ", context: " + Arrays.toString(context));

				final AccessDatabaseForLoading loader;
				if (Mode.threaded.equals(mode)) {
					loader = new AccessDatabaseThreaded();
				} else {
					loader = new AccessDatabaseDirectly();
				}

				Database database;
				// if we get passed an entity factory we use it (this is used for tests!)
				if (context.length > 0 && context[0] instanceof Database) {
					database = (Database) context[0];
				} else {
					// otherwise retrieve the entity manager via Spring
					database = SpringUtils.getBean("database", Database.class);
				}

				loader.execute(database, iterationCount);
			}
		}
		manager.stop(id);

		return null;
	}
}
