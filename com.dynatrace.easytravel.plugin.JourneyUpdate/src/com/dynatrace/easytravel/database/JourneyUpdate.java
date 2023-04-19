package com.dynatrace.easytravel.database;

import java.util.Arrays;
import java.util.Collection;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.model.GenericDataAccess;
import com.dynatrace.easytravel.persistence.Database;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.spring.SpringUtils;

public class JourneyUpdate extends AbstractGenericPlugin {
	private static Logger log = LoggerFactory.make();

	// this plugin can run in two different modes of operation
	protected enum Mode {
		fast, slow
	}

	// which mode to use here
	// this is usually overridden in the .ctx.xml
	private Mode mode = Mode.slow;

	public synchronized void setMode(String mode) {
		this.mode = Mode.valueOf(mode);
	}

	// Synchronized to avoid multiple calls interfeering with each other...
	@Override
	public synchronized Object doExecute(String location, Object... context) {
		log.info("JourneyUpdate: Had extension point: " + location
				+ ", context: " + Arrays.toString(context));

		// if we get passed an entity factory we use it (this is used for
		// tests!)
		Database database;
		if (context.length > 0 && context[0] instanceof Database) {
			database = (Database) context[0];
		} else {
			// otherwise retrieve the entity manager via Spring
			database = SpringUtils.getBean("database",
					Database.class);
		}

		UpdateThread updateThread = null;

		DataAccess access = new GenericDataAccess(database.createNewBusinessController());

		// load journeys which amounts should be updated
		Collection<Journey> journeys = access.getJourneys(150);

		// create and start update thread
		updateThread = new UpdateThread(access, mode, journeys);

		Thread thread = new Thread(updateThread, "UpdateThread");
		thread.setDaemon(true);
		thread.start();

		return thread;
	}
}
