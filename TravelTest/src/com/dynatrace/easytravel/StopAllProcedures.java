package com.dynatrace.easytravel;

import java.io.IOException;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.integration.IntegrationTestBase;
import com.dynatrace.easytravel.logging.LoggerFactory;

/**
 * Small helper application which tries to stop any remaining easyTravel process based on the available easyTravel Configuration.
 *
 * @author dominik.stadler
 */
public class StopAllProcedures {
	private static final Logger log = LoggerFactory.make();

	public static void main(String[] args) throws IOException, InterruptedException {
		LoggerFactory.initLogging();
		log.info("Ensuring that no process is left over from previous tests");

		// just use the code from
		IntegrationTestBase.cleanup();

		try {
			IntegrationTestBase.checkAllPorts(EasyTravelConfig.read());
		} catch (AssertionError e) {
			log.warn("Had assertion failure while checking ports during shutdown of easyTravel ports and processes", e);
			System.exit(1);
		} catch (Exception e) {
			log.warn("Had exception while checking ports during shutdown of easyTravel ports and processes", e);
			System.exit(1);
		}

		log.info("All checks done successfully.");
	}
}
