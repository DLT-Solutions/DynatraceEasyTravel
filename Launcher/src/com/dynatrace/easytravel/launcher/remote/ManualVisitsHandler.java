package com.dynatrace.easytravel.launcher.remote;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.LauncherUI;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * @author Rafal.Psciuk
 *
 */
@Path("/" + Constants.REST.MANUAL_VISITS)
public class ManualVisitsHandler {
	private static final Logger LOGGER = LoggerFactory.make();

	@GET
	@Path("/{enabled}")
	@Produces("text/plain")
	public String setManualVisitsCreation(@PathParam("enabled") boolean enabled) {
		LOGGER.trace(Constants.REST.MANUAL_VISITS + " service called with parameter " + enabled);
		for (LauncherUI launcher : Launcher.getLauncherUIList()) {
			launcher.setManualVistisCreation(enabled);
		}
		return "Manual visits creation set to " + enabled;
	}
}
